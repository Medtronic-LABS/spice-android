package com.medtroniclabs.opensource.uploadservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.ContactsContract
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.data.model.ScreeningPatientResponse
import com.medtroniclabs.opensource.db.tables.ScreeningEntity
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.ui.boarding.repo.OnBoardingRepository
import com.medtroniclabs.opensource.ui.screening.repo.ScreeningRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class UploadForegroundService : Service() {

    @Inject
    lateinit var screeningRepository: ScreeningRepository

    @Inject
    lateinit var onBoardingRepo: OnBoardingRepository

    private val totalSkipCount = 2
    private val initialCounter = 1

    private lateinit var notificationManager: NotificationManager

    private var isServiceRunning = false

    private var screeningUploadList: List<ScreeningEntity>? = null

    private val notificationId = 1

    private val notificationChannelId = "ncd"

    private val job = SupervisorJob()

    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {

        const val ACTION_START =
            "${ContactsContract.Directory.PACKAGE_NAME}.action.START_UPLOAD_SERVICE"
        const val ACTION_STOP =
            "${ContactsContract.Directory.PACKAGE_NAME}.action.STOP_UPLOAD_SERVICE"

        fun startService(context: Context, message: String) {
            val startIntent = Intent(context, UploadForegroundService::class.java).apply {
                action = message
            }
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopUploadService(context: Context) {
            val stopIntent = Intent(context, UploadForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        startDataUpload()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.apply {
            action?.let {
                if (isServiceRunning) {
                    if (it == ACTION_STOP) {
                        stopService()
                    }
                } else {
                    if (it == ACTION_START) {
                        isServiceRunning = true
                        createNotificationChannel()
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {

        val titleText = getString(R.string.app_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notificationChannelId,
                titleText,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(getString(R.string.data_sync_started))
            .setBigContentTitle(titleText)
        val notificationCompatBuilder =
            NotificationCompat.Builder(
                applicationContext,
                notificationChannelId
            )
        val notification: Notification = notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(getString(R.string.data_sync_started))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        startForeground(notificationId, notification)
    }

    private fun startDataUpload() {

        scope.launch {
            screeningUploadList = screeningRepository.getAllScreeningRecords(false)

            val screeningEntityAsync = async {
                uploadAPIServiceCall(0, initialCounter)
            }
            if (screeningEntityAsync.await().isSuccess) {
                //delete all synced screening entities
                screeningRepository.deleteUploadedScreeningRecords(DateUtils.getTodayDateInMilliseconds())
            }
            stopService()
        }
    }


    private suspend fun uploadAPIServiceCall(index: Int, skipCounter: Int): Result<Boolean> {

        screeningUploadList ?: return Result.success(value = false)

        screeningUploadList?.apply {
            if (index < size) {
                get(index).apply {
                    try {
                        val requestJson = parseRequest(generalDetails, screeningDetails)
                        if (requestJson != null) {
                            val request = StringConverter.getJsonObject(Gson().toJson(requestJson))
                            val response = if(requestJson.containsKey("bio_metrics"))
                                onBoardingRepo.createPatientScreening(request)
                            else
                                onBoardingRepo.createScreeningLog(request)

                            handleScreeningResponse(
                                response,
                                id,
                                index,
                                skipCounter
                            )
                        } else uploadAPIServiceCall(index + 1, initialCounter)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return Result.success(false)
                    }
                }
            } else {
                return Result.success(true)
            }
        }

        return Result.success(false)
    }

    private suspend fun handleScreeningResponse(
        response: Response<ScreeningPatientResponse>,
        id: Long,
        index: Int,
        skipCounter: Int
    ) {
        if (response.isSuccessful) {
            val responseBody = response.body()
            responseBody?.let {
                screeningRepository.updateScreeningRecordById(
                    id,
                    true
                )
                uploadAPIServiceCall(index + 1, initialCounter)
            }
        } else
            uploadAPIServiceCall(
                if (skipCounter < totalSkipCount) index else index + 1,
                if (skipCounter < totalSkipCount) skipCounter + 1 else initialCounter
            )
    }

    private fun parseRequest(
        generalDetails: String,
        screeningDetails: String
    ): HashMap<String, Any>? {
        val generalData: Map<String, Any>? =
            StringConverter.convertStringToMap(generalDetails)
        (StringConverter.convertStringToMap(screeningDetails))?.let { map ->
            HashMap(map).let {
                setType(it, generalData)

                if (!it.containsKey(DefinedParams.Unit_Measurement)) {
                    it[DefinedParams.Unit_Measurement] = DefinedParams.Unit_Measurement_Metric_Type
                } else if (!it.containsKey(DefinedParams.UnitMeasurement)) {
                    it[DefinedParams.UnitMeasurement] = DefinedParams.Unit_Measurement_Metric_Type
                }

                if (it.containsKey(DefinedParams.glucose_log) && it[DefinedParams.glucose_log] is Map<*, *>) {
                    (it[DefinedParams.glucose_log] as Map<*, *>?)?.let { map ->
                        updateGlucoseLog(it, map)
                    }
                } else if (it.containsKey(DefinedParams.GlucoseLog) && it[DefinedParams.GlucoseLog] is Map<*, *>) {
                    (it[DefinedParams.GlucoseLog] as Map<*, *>?)?.let { map ->
                        updateGlucoseLog(it, map)
                    }
                }

                return it
            }
        }

        return null
    }

    private fun setType(it: HashMap<String, Any>, generalData: Map<String, Any>?) {
        generalData?.let { gData ->
            if (gData.containsKey(DefinedParams.Site_Id))
                it[DefinedParams.SiteId] = gData[DefinedParams.Site_Id] ?: -1
            if (gData.containsKey(DefinedParams.Category))
                it[DefinedParams.Category] = gData[DefinedParams.Category] as String
            if (gData.containsKey(DefinedParams.CategoryType))
                it[DefinedParams.Type] = gData[DefinedParams.CategoryType] as String
            else
                it[DefinedParams.Type] = ""
        }
    }

    private fun updateGlucoseLog(hashMap: HashMap<String, Any>, map: Map<*, *>) {
        var isChanged = false
        val subMap = HashMap(map)
        if (!subMap.containsKey(DefinedParams.BloodGlucoseID) && subMap.containsKey(
                DefinedParams.lastMealTime
            )
        ) {
            isChanged = true
            subMap.remove(key = DefinedParams.lastMealTime)
        }
        if (subMap.containsKey(DefinedParams.BloodGlucoseID) &&
            !subMap.containsKey(DefinedParams.BloodGlucoseID + DefinedParams.unitMeasurement_KEY)
        ) {
            isChanged = true
            subMap[DefinedParams.BloodGlucoseID + DefinedParams.unitMeasurement_KEY] =
                DefinedParams.mmoll
        }
        if (isChanged) {
            hashMap[DefinedParams.GlucoseLog] = subMap
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        if (job.isActive)
            job.cancel()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun stopService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        else
            stopForeground(true)
        stopSelf()
    }
}