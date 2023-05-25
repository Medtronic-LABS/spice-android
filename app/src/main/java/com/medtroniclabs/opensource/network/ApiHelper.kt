package com.medtroniclabs.opensource.network

import com.google.gson.JsonObject
import com.medtroniclabs.opensource.data.assesssment.AssessmentPatientResponse
import com.medtroniclabs.opensource.data.assesssment.BPLogModel
import com.medtroniclabs.opensource.data.model.APIResponse
import com.medtroniclabs.opensource.data.model.AssessmentListRequest
import com.medtroniclabs.opensource.data.model.BPLogListResponse
import com.medtroniclabs.opensource.data.model.BadgeModel
import com.medtroniclabs.opensource.data.model.BadgeResponseModel
import com.medtroniclabs.opensource.data.model.BloodGlucoseListResponse
import com.medtroniclabs.opensource.data.model.ConfirmDiagnosesRequest
import com.medtroniclabs.opensource.data.model.CultureLocaleModel
import com.medtroniclabs.opensource.data.model.DeviceInfo
import com.medtroniclabs.opensource.data.model.FillPrescriptionListResponse
import com.medtroniclabs.opensource.data.model.FillPrescriptionRequest
import com.medtroniclabs.opensource.data.model.FillPrescriptionResponse
import com.medtroniclabs.opensource.data.model.FillPrescriptionUpdateRequest
import com.medtroniclabs.opensource.data.model.InitialEncounterResponse
import com.medtroniclabs.opensource.data.model.LabTestListResponse
import com.medtroniclabs.opensource.data.model.LabTestSearchResponse
import com.medtroniclabs.opensource.data.model.LifeStyleManagement
import com.medtroniclabs.opensource.data.model.LoginResponse
import com.medtroniclabs.opensource.data.model.MedicalReviewBaseRequest
import com.medtroniclabs.opensource.data.model.MedicalReviewEditModel
import com.medtroniclabs.opensource.data.model.MedicalReviewStaticResponse
import com.medtroniclabs.opensource.data.model.MedicationSearchReqModel
import com.medtroniclabs.opensource.data.model.MetaDataResponse
import com.medtroniclabs.opensource.data.model.PatientCreateResponse
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.data.model.PatientHistoryRequest
import com.medtroniclabs.opensource.data.model.PatientLabTestHistoryResponse
import com.medtroniclabs.opensource.data.model.PatientLifeStyle
import com.medtroniclabs.opensource.data.model.PatientLifeStyleRequest
import com.medtroniclabs.opensource.data.model.PatientLifestyleModel
import com.medtroniclabs.opensource.data.model.PatientListRespModel
import com.medtroniclabs.opensource.data.model.PatientMedicalReviewHistoryResponse
import com.medtroniclabs.opensource.data.model.PatientPregnancyModel
import com.medtroniclabs.opensource.data.model.PatientPrescriptionHistoryResponse
import com.medtroniclabs.opensource.data.model.PatientPrescriptionModel
import com.medtroniclabs.opensource.data.model.PatientRemoveRequest
import com.medtroniclabs.opensource.data.model.PatientTransferListResponse
import com.medtroniclabs.opensource.data.model.PatientTransferNotificationCountRequest
import com.medtroniclabs.opensource.data.model.PatientTransferNotificationCountResponse
import com.medtroniclabs.opensource.data.model.PatientTransferUpdateRequest
import com.medtroniclabs.opensource.data.model.PatientTransferUpdateResponse
import com.medtroniclabs.opensource.data.model.PatientVisit
import com.medtroniclabs.opensource.data.model.PatientsDataModel
import com.medtroniclabs.opensource.data.model.PregnancyCreateRequest
import com.medtroniclabs.opensource.data.model.PrescriptionModel
import com.medtroniclabs.opensource.data.model.PrescriptionRefillHistoryResponse
import com.medtroniclabs.opensource.data.model.RegionSiteModel
import com.medtroniclabs.opensource.data.model.RegionSiteResponse
import com.medtroniclabs.opensource.data.model.ResponseDataModel
import com.medtroniclabs.opensource.data.model.ScreeningDetail
import com.medtroniclabs.opensource.data.model.ScreeningPatientResponse
import com.medtroniclabs.opensource.data.model.SearchModel
import com.medtroniclabs.opensource.data.model.SiteRoleModel
import com.medtroniclabs.opensource.data.model.SiteRoleResponse
import com.medtroniclabs.opensource.data.model.SummaryResponse
import com.medtroniclabs.opensource.data.model.TransferCreateRequest
import com.medtroniclabs.opensource.data.ui.PatientBasicRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body

interface ApiHelper {
    suspend fun doLogin(loginRequest: MultipartBody): Response<LoginResponse>
    suspend fun resetPassword(userName: String): Response<APIResponse<Boolean>>
    suspend fun getMetaData(cultureId: Long): Response<APIResponse<MetaDataResponse>>
    suspend fun saveDeviceDetails(
        tenantId: Long,
        deviceInfo: DeviceInfo
    ): Response<APIResponse<DeviceInfo>>

    suspend fun createPatient(request: JsonObject): Response<APIResponse<PatientCreateResponse>>
    suspend fun userLogout(): Response<ResponseBody>
    suspend fun createPatientAssessment(createRequest: JsonObject): Response<APIResponse<AssessmentPatientResponse>>
    suspend fun getPatientDetails(request: PatientDetailsModel): Response<APIResponse<PatientDetailsModel>>
    suspend fun getMedicalReviewStaticData(cultureId: Long): Response<APIResponse<MedicalReviewStaticResponse>>
    suspend fun getPatientBPLogList(request: AssessmentListRequest): Response<APIResponse<BPLogListResponse>>
    suspend fun createPatientBPLog(createRequest: JsonObject): Response<APIResponse<BPLogModel>>
    suspend fun createContinuousMedicalReview(request: MedicalReviewEditModel): Response<APIResponse<InitialEncounterResponse>>
    suspend fun getPatientBloodGlucoseList(request: AssessmentListRequest): Response<APIResponse<BloodGlucoseListResponse>>
    suspend fun createBloodGlucoseBPLog(createRequest: JsonObject): Response<APIResponse<BPLogModel>>
    suspend fun createMentalHealth(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun searchLabTest(request: SearchModel): Response<APIResponse<ArrayList<LabTestSearchResponse>>>
    suspend fun getLabTestResult(labTestId: Long): Response<APIResponse<ArrayList<HashMap<String, Any>>>>
    suspend fun getPatientLabTestHistory(request: PatientHistoryRequest): Response<APIResponse<PatientLabTestHistoryResponse>>
    suspend fun createPregnancy(request: PregnancyCreateRequest): Response<APIResponse<HashMap<String, Any>>>
    suspend fun updatePregnancy(request: PregnancyCreateRequest): Response<APIResponse<HashMap<String, Any>>>
    suspend fun getPatientPregnancyDetails(request: PatientPregnancyModel): Response<APIResponse<PregnancyCreateRequest>>
    suspend fun getPatientMedicalReviewHistoryList(request: MedicalReviewBaseRequest): Response<APIResponse<PatientMedicalReviewHistoryResponse>>
    suspend fun getPatientPrescriptionHistoryList(request: PatientHistoryRequest): Response<APIResponse<PatientPrescriptionHistoryResponse>>
    suspend fun confirmDiagnosis(request: ConfirmDiagnosesRequest): Response<APIResponse<HashMap<String, Any>>>
    suspend fun getPatientLifeStyleDetails(requestPatient: PatientLifeStyleRequest): Response<APIResponse<ArrayList<PatientLifeStyle>>>
    suspend fun createPatientVisit(request: MedicalReviewBaseRequest): Response<APIResponse<PatientVisit>>
    suspend fun getPatientMedicalReviewSummary(request: MedicalReviewBaseRequest): Response<APIResponse<SummaryResponse>>
    suspend fun getMentalHealthDetails(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun updateMentalHealth(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun getPatientFillPrescriptionList(request: FillPrescriptionRequest): Response<APIResponse<ArrayList<FillPrescriptionListResponse>>>
    suspend fun fillPrescriptionUpdate(request: FillPrescriptionUpdateRequest): Response<APIResponse<FillPrescriptionResponse>>
    suspend fun searchMedication(request: MedicationSearchReqModel): Response<APIResponse<ArrayList<PrescriptionModel>>>
    suspend fun updatePrescription(body: RequestBody): Response<APIResponse<ResponseDataModel>>
    suspend fun getPrescriptionList(request: PatientPrescriptionModel): Response<APIResponse<ArrayList<PrescriptionModel>>>
    suspend fun removePrescription(request: PatientPrescriptionModel): Response<APIResponse<ResponseDataModel>>
    suspend fun updateTreatmentPlan(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun treatmentPlanDetails(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun searchPatientById(request: PatientsDataModel): APIResponse<ArrayList<PatientListRespModel>>
    suspend fun advancedSearchCountry(request: PatientsDataModel): APIResponse<ArrayList<PatientListRespModel>>
    suspend fun advancedSearchSite(request: PatientsDataModel): APIResponse<ArrayList<PatientListRespModel>>
    suspend fun patientsList(request: PatientsDataModel): APIResponse<ArrayList<PatientListRespModel>>
    suspend fun getPatientLabTests(request: HashMap<String, Any?>): Response<APIResponse<LabTestListResponse>>
    suspend fun referLabTest(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun createLabTestResult(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun getLabTestResultDetails(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun getBadgeCount(request: BadgeModel): Response<APIResponse<BadgeResponseModel>>
    suspend fun validateSession(): Response<ResponseBody>
    suspend fun removeLabTest(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun reviewLabTestResult(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun getScreeningDetails(request: ScreeningDetail): Response<APIResponse<HashMap<String, Any>>>
    suspend fun createPatientLifestyle(request: LifeStyleManagement): Response<APIResponse<HashMap<String, Any>>>
    suspend fun updatePatientLifestyle(request: PatientLifestyleModel): Response<APIResponse<LifeStyleManagement>>
    suspend fun getPatientLifestyleList(request: PatientLifestyleModel): Response<APIResponse<ArrayList<LifeStyleManagement>>>
    suspend fun removePatientLifestyle(request: PatientLifestyleModel): Response<APIResponse<Boolean>>
    suspend fun getPrescriptionRefillHistory(request: PatientPrescriptionModel): Response<APIResponse<ArrayList<PrescriptionRefillHistoryResponse>>>
    suspend fun clearBadgeNotification(request: PatientLifestyleModel): Response<APIResponse<ResponseDataModel>>
    suspend fun getPatientBasicDetail(request: PatientBasicRequest): Response<APIResponse<HashMap<String, Any>>>
    suspend fun updatePatient(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun createBpLog(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun createGlucoseLog(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>
    suspend fun createScreeningLog(createRequest: JsonObject): Response<ScreeningPatientResponse>
    suspend fun createPatientScreening(createRequest: JsonObject): Response<ScreeningPatientResponse>
    suspend fun searchSite(request: RegionSiteModel): Response<APIResponse<ArrayList<RegionSiteResponse>>>
    suspend fun searchRoleUser(request: SiteRoleModel): Response<APIResponse<ArrayList<SiteRoleResponse>>>
    suspend fun createPatientTransfer(request: TransferCreateRequest): Response<APIResponse<HashMap<String, Any>>>
    suspend fun patientRemove(request: PatientRemoveRequest): Response<APIResponse<HashMap<String, Any>>>
    suspend fun validatePatientTransfer(request: FillPrescriptionRequest): Response<APIResponse<HashMap<String, Any>>>
    suspend fun patientTransferNotificationCount(request: PatientTransferNotificationCountRequest): Response<APIResponse<PatientTransferNotificationCountResponse>>
    suspend fun getPatientTransferList(request: PatientTransferNotificationCountRequest): Response<APIResponse<PatientTransferListResponse>>
    suspend fun patientTransferUpdate(request: PatientTransferUpdateRequest): Response<APIResponse<PatientTransferUpdateResponse>>
    suspend fun cultureLocaleUpdate(request: CultureLocaleModel): Response<APIResponse<HashMap<String, Any>>>
    suspend fun versionCheck(): Response<APIResponse<Boolean>>
}