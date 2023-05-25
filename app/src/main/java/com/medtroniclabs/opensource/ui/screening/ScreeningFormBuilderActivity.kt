package com.medtroniclabs.opensource.ui.screening

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.medtroniclabs.opensource.BuildConfig
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.opensource.custom.GeneralInfoDialog
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.custom.SecuredPreference.getUnitMeasurementType
import com.medtroniclabs.opensource.data.screening.SiteDetails
import com.medtroniclabs.opensource.databinding.ActivityScreeningFormBuilderBinding
import com.medtroniclabs.opensource.formgeneration.FormGenerator
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.calculateAverageBloodPressure
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.calculateBMI
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.calculateBloodGlucose
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.calculateCVDRiskFactor
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.calculatePHQScore
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.checkAssessmentCondition
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.getMeasurementTypeValues
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.processHeightMap
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.processLastMealTime
import com.medtroniclabs.opensource.formgeneration.listener.FormEventListener
import com.medtroniclabs.opensource.formgeneration.model.FormLayout
import com.medtroniclabs.opensource.location.ForegroundOnlyLocationService
import com.medtroniclabs.opensource.location.LocationConstant
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.FormResultGenerator
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.screening.viewmodel.ScreeningFormBuilderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScreeningFormBuilderActivity : BaseActivity(), FormEventListener {

    private lateinit var binding: ActivityScreeningFormBuilderBinding

    private val viewModel: ScreeningFormBuilderViewModel by viewModels()

    private lateinit var formGenerator: FormGenerator

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let { resultIntent ->
                    formGenerator.onSpinnerActivityResult(resultIntent)
                }
            }
        }

    // Provides location updates for while-in-use feature.
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null


    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver

    // Monitors connection to the while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            viewModel.foregroundOnlyLocationServiceBound = true
            checkPermissionAndRequestLocationUpdate()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            viewModel.foregroundOnlyLocationServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreeningFormBuilderBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.screening),
            homeAndBackVisibility = Pair(true, null),
            callbackHome = {
                onHomeIconClicked()
            }
        )
        initializeFormGenerator()
        initializeBroadcastReceiver()
        checkPermissionAndRequestLocationUpdate()
    }

    private fun initializeFormGenerator() {
        formGenerator =
            FormGenerator(this, binding.llForm, resultLauncher, this, binding.scrollView,
                translate = SecuredPreference.getIsTranslationEnabled())
        viewModel.fetchWorkFlow(UIConstants.screeningUniqueID,DefinedParams.Workflow)
        viewModel.getRiskEntityList()
        attachObservers()
    }

    private var screeningJSON: List<FormLayout>? = null

    private fun attachObservers() {
        viewModel.formResponseListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                       screeningJSON = formGenerator.combineAccountWorkflows(it,UIConstants.screeningUniqueID)
                    }
                }
            }
        }

        viewModel.screeningSaveResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    val intent = Intent(this, ScreeningSummaryActivity::class.java)
                    intent.putExtra(DefinedParams.ScreeningEntityId, viewModel.screeningEntityRowId)
                    startActivity(intent)
                }
            }
        }
        viewModel.mentalHealthQuestions.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    formGenerator.loadMentalHealthQuestions(resourceState.data)
                }
            }
        }

    }

    override fun onFormSubmit(
        resultHashMap: HashMap<String, Any>,
        serverData: List<FormLayout?>?
    ) {

        val unitGenericType = getUnitMeasurementType()

        showLoading()
        foregroundOnlyLocationService?.getUserLocation()?.let { location ->
            resultHashMap[DefinedParams.Latitude] = location.latitude.toString()
            resultHashMap[DefinedParams.Longitude] = location.longitude.toString()
        } ?: kotlin.run {
            resultHashMap[DefinedParams.Latitude] = 0.0.toString()
            resultHashMap[DefinedParams.Longitude] = 0.0.toString()
        }
        foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
        resultHashMap[DefinedParams.Screening_Date_Time] = DateUtils.getCurrentDateTimeInUserTimeZone(DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
        processValuesAndProceed(resultHashMap, unitGenericType)
        hideLoading()
    }

    private fun processValuesAndProceed(
        resultHashMap: HashMap<String, Any>,
        unitGenericType: String
    ) {
        val map = HashMap<String, Any>()
        map.putAll(resultHashMap)
        processLastMealTime(map)
        calculateBloodGlucose(map, formGenerator)
        calculateBMI(map, unitGenericType)
        processHeightMap(map,unitGenericType)
        calculateAverageBloodPressure(this, map)
        formGenerator.setPhQ4Score(calculatePHQScore(map))
        calculateFurtherAssessment(map, getMeasurementTypeValues(map))
        calculateCVDRiskFactor(map, viewModel.list, formGenerator.getSystolicAverage())
        SecuredPreference.putScreeningEntry(map)
        SecuredPreference.getChosenSiteEntity()?.apply {
            viewModel.siteDetail = SiteDetails(
                category,
                categoryType,
                siteName,
                siteId,
                categoryDisplayType = categoryDisplayType
            )
        }
        var isReferred = false
        if (map.containsKey(DefinedParams.ReferAssessment)) {
            val referralString = map[DefinedParams.ReferAssessment] as Boolean
            referralString.let {
                isReferred = it
            }
        }
        SecuredPreference.getDeviceID()?.let {
            map[DefinedParams.Device_Info_Id] = it
        }


        map[DefinedParams.UnitMeasurement] = getUnitMeasurementType()


        val result = screeningJSON?.let {
            FormResultGenerator().groupValues(
                context = this,
                serverData = it,
                map
            )
        }
        result?.let { res ->
            res.first?.let {
                viewModel.getSavedSiteDetail()?.let { siteDetail ->
                    viewModel.savePatientScreeningInformation(
                        it,
                        siteDetail,
                        isReferred = isReferred
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun initializeBroadcastReceiver() {
        foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    /**
     * Receiver for location broadcasts from [ForegroundOnlyLocationService].
     */
    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            /**
             * this method not used
             */
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            foregroundOnlyBroadcastReceiver,
            IntentFilter(
                ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST
            )
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            foregroundOnlyBroadcastReceiver
        )
        super.onPause()
    }

    override fun onStop() {
        if (viewModel.foregroundOnlyLocationServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            viewModel.foregroundOnlyLocationServiceBound = false
        }
        super.onStop()
    }

    private fun checkPermissionAndRequestLocationUpdate() {
        if (foregroundPermissionApproved()) {
            foregroundOnlyLocationService?.subscribeToLocationUpdates()
        } else {
            requestForegroundPermissions()
        }
    }

    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()
        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Snackbar.make(
                binding.root,
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@ScreeningFormBuilderActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LocationConstant.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this@ScreeningFormBuilderActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LocationConstant.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LocationConstant.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() -> {
                    //Invoked if the results are empty
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                else -> {
                    Snackbar.make(
                        binding.root,
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
        super.onDestroy()
    }

    override fun onBPInstructionClicked(
        title: String,
        informationList: ArrayList<String>,
        description: String?
    ) {
        GeneralInfoDialog.newInstance(
            title,
            description,
            informationList
        ).show(supportFragmentManager, GeneralInfoDialog.TAG)
    }

    override fun onRenderingComplete() {
        /**
         * this method is not used
         */
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        if (localDataCache is String) {
            when (localDataCache) {
                DefinedParams.PHQ4, DefinedParams.PHQ9 -> {
                    viewModel.fetchMentalHealthQuestions(id, localDataCache)
                }
                DefinedParams.Fetch_MH_Questions -> {
                    formGenerator.fetchMHQuestions(viewModel.mentalHealthQuestions.value?.data)
                }
            }
        }
    }

    override fun onFormError() {
        /**
         * this method is not used
         */
    }

    override fun onJsonMismatchError() {
        showAlertWith(message = getString(R.string.check_form_attribute), isNegativeButtonNeed = false, positiveButtonName = getString(R.string.ok)) {

        }
    }

    private fun calculateFurtherAssessment(map: HashMap<String, Any>, unitGenericType: String) {
        if (checkAssessmentCondition(
                formGenerator.getSystolicAverage(),
                formGenerator.getDiastolicAverage(),
                formGenerator.getPhQ4Score(),
                formGenerator.getFbsBloodGlucose(),
                formGenerator.getRbsBloodGlucose(),
                unitGenericType
            )
        ) {
            map[DefinedParams.ReferAssessment] = DefinedParams.PositiveValue
        } else {
            map[DefinedParams.ReferAssessment] = DefinedParams.NegativeValue
        }
    }
}