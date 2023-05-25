package com.medtroniclabs.opensource.ui.medicalreview.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.data.model.APIResponse
import com.medtroniclabs.opensource.data.model.PatientHistoryRequest
import com.medtroniclabs.opensource.data.model.PatientPrescriptionHistoryResponse
import com.medtroniclabs.opensource.data.model.PatientPrescriptionModel
import com.medtroniclabs.opensource.data.model.PrescriptionModel
import com.medtroniclabs.opensource.data.model.ResponseDataModel
import com.medtroniclabs.opensource.data.model.UnitMetricEntity
import com.medtroniclabs.opensource.data.model.UpdateMedicationModel
import com.medtroniclabs.opensource.db.tables.FrequencyEntity
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.medicalreview.repo.MedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class PrescriptionCopyViewModel @Inject constructor(
    private val medicalReviewRepo: MedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
    ) :
    ViewModel() {

    val prescriptionListLiveDate = MutableLiveData<Resource<ArrayList<PrescriptionModel>>>()
    val disContinuedPrescriptionListLiveDate =
        MutableLiveData<Resource<ArrayList<PrescriptionModel>>>()
    var patient_track_id: Long? = null
    var patient_visit_id: Long? = null
    var tenant_id: Long? = null
    var prescriptionUIModel: ArrayList<PrescriptionModel>? = null
    val removePrescriptionLiveDate = MutableLiveData<Resource<ResponseDataModel>>()
    var savePrescriptionList: ArrayList<UpdateMedicationModel>? = null
    val updatePrescriptionLiveDate = MutableLiveData<Resource<ResponseDataModel>>()
    val frequencyList = MutableLiveData<List<FrequencyEntity>>()
    val medicationHistoryLiveData = MutableLiveData<Resource<PatientPrescriptionHistoryResponse>>()
    val reloadInstruction = MutableLiveData<Boolean>()
    val unitList = MutableLiveData<List<UnitMetricEntity>>()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    fun getPrescriptionList(
        isDiscontinuedMedicationList: Boolean
    ) {
        prescriptionListLiveDate.postLoading()
        viewModelScope.launch(dispatcherIO) {
            try {
                val response = medicalReviewRepo.getPrescriptionList(
                    PatientPrescriptionModel(
                        patientTrackId = patient_track_id,
                        tenantId = tenant_id,
                        isDeleted = isDiscontinuedMedicationList
                    )
                )
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        prescriptionListLiveDate.postSuccess(res.entityList ?: ArrayList())
                    else
                        prescriptionListLiveDate.postError()
                } else {
                    prescriptionListLiveDate.postError()
                }
            } catch (e: Exception) {
                prescriptionListLiveDate.postError()
            }
        }
    }

    fun getDiscountinuedPrescriptionList(
        isDiscontinuedMedicationList: Boolean
    ) {
        disContinuedPrescriptionListLiveDate.postLoading()
        viewModelScope.launch(dispatcherIO) {
            try {
                val response = medicalReviewRepo.getPrescriptionList(
                    PatientPrescriptionModel(
                        patientTrackId = patient_track_id,
                        tenantId = tenant_id,
                        isDeleted = isDiscontinuedMedicationList
                    )
                )
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        disContinuedPrescriptionListLiveDate.postSuccess(
                            res.entityList ?: ArrayList()
                        )
                    else
                        disContinuedPrescriptionListLiveDate.postError()
                } else {
                    disContinuedPrescriptionListLiveDate.postError()
                }
            } catch (e: Exception) {
                disContinuedPrescriptionListLiveDate.postError()
            }
        }
    }

    fun removePrescription(context: Context,prescriptionId: Long, discontinuedReason: String?) {
        if (connectivityManager.isNetworkAvailable()) {
            removePrescriptionLiveDate.postLoading()
            viewModelScope.launch(dispatcherIO) {
                try {
                    val response = medicalReviewRepo.removePrescription(
                        PatientPrescriptionModel(
                            id = prescriptionId,
                            patientVisitId = patient_visit_id,
                            tenantId = tenant_id,
                            discontinuedReason = discontinuedReason,
                            patientTrackId = patient_track_id
                        )
                    )
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            removePrescriptionLiveDate.postSuccess(res.entity)
                        else
                            removePrescriptionLiveDate.postError()
                    } else {
                        removePrescriptionLiveDate.postError()
                    }
                } catch (e: Exception) {
                    removePrescriptionLiveDate.postError()
                }
            }
        } else {
            removePrescriptionLiveDate.postError(context.getString(R.string.no_internet_error))
        }
    }


    fun createOrUpdatePrescription(
        context: Context,
        signatureBitmap: Bitmap,
        filePath: File,
        request: PatientPrescriptionModel
    ) {
        if (connectivityManager.isNetworkAvailable()) {
            updatePrescriptionLiveDate.postLoading()
            viewModelScope.launch(dispatcherIO) {
                try {
                    filePath.mkdirs()
                    val signature = "${request.patientVisitId}${DefinedParams.SIGN_SUFFIX}.jpeg"
                    val file = File(filePath, signature)
                    val isDeleted = if (file.exists()) {
                        file.delete()
                    } else true
                    if (isDeleted) {
                        val out = FileOutputStream(file)
                        signatureBitmap.compress(Bitmap.CompressFormat.JPEG, 20, out)
                        out.flush()
                        out.close()
                        val builder = MultipartBody.Builder()
                        builder.setType(MultipartBody.FORM)
                        builder.addFormDataPart(
                            "signatureFile",
                            file.name,
                            file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                        )
                        val dataRequest = Gson().toJson(request)
                        builder.addFormDataPart("prescriptionRequest", dataRequest)
                        val requestBody = builder.build()
                        val response: Response<APIResponse<ResponseDataModel>> =
                            medicalReviewRepo.updatePrescription(requestBody)
                        if (response.isSuccessful) {
                            val res = response.body()
                            if (res?.status == true)
                                updatePrescriptionLiveDate.postSuccess()
                            else
                                updatePrescriptionLiveDate.postError()
                        } else {
                            updatePrescriptionLiveDate.postError()
                        }
                    }
                } catch (e: Exception) {
                    updatePrescriptionLiveDate.postError()
                }
            }
        } else {
            updatePrescriptionLiveDate.postError(context.getString(R.string.no_internet_error))
        }
    }

    fun getFrequencyList() {
        viewModelScope.launch(dispatcherIO) {
            frequencyList.postValue(medicalReviewRepo.getFrequency())
        }
    }

    fun getMedicationHistory(prescriptionId: Long?) {
        medicationHistoryLiveData.postLoading()
        viewModelScope.launch(dispatcherIO) {
            try {
                val response = medicalReviewRepo.getPatientPrescriptionHistoryList(
                    PatientHistoryRequest(
                        isLatestRequired = false,
                        patientVisitId = null,
                        patientTrackId = patient_track_id ?: -1,
                        prescriptionId = prescriptionId,
                        tenantId = tenant_id ?: -1
                    )
                )
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        medicationHistoryLiveData.postSuccess(res.entity)
                    else
                        medicationHistoryLiveData.postError()
                } else {
                    medicationHistoryLiveData.postError()
                }
            } catch (e: Exception) {
                medicationHistoryLiveData.postError()
            }
        }
    }

    fun getDosageUnitList() {
        viewModelScope.launch(dispatcherIO) {
            try {
                unitList.postValue(medicalReviewRepo.getUnitList(DefinedParams.PRESCRIPTION))
            } catch (_: Exception) {
                //Exception - Catch block
            }
        }
    }
}