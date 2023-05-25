package com.medtroniclabs.opensource.network

import com.google.gson.JsonObject
import com.medtroniclabs.opensource.data.assesssment.AssessmentPatientResponse
import com.medtroniclabs.opensource.data.assesssment.BPLogModel
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.data.ui.PatientBasicRequest
import com.medtroniclabs.opensource.data.model.APIResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl
@Inject
constructor(private val apiService: ApiService) : ApiHelper {
    override suspend fun doLogin(loginRequest: MultipartBody): Response<LoginResponse> {
        return apiService.doLogin(loginRequest)
    }

    override suspend fun resetPassword(userName: String): Response<APIResponse<Boolean>> {
        return apiService.resetPassword(userName)
    }

    override suspend fun createPatient(request: JsonObject): Response<APIResponse<PatientCreateResponse>> {
        return apiService.createPatient(request)
    }

    override suspend fun userLogout(): Response<ResponseBody> {
        return apiService.userLogout()
    }

    override suspend fun createPatientAssessment(createRequest: JsonObject): Response<APIResponse<AssessmentPatientResponse>>  =
        apiService.createPatientAssessment(createRequest)


    override suspend fun getMetaData(cultureId: Long): Response<APIResponse<MetaDataResponse>> {
        return apiService.getMetaData(cultureId)
    }

    override suspend fun saveDeviceDetails(tenantId: Long, deviceInfo: DeviceInfo): Response<APIResponse<DeviceInfo>> {
        return apiService.saveDeviceDetails(tenantId, deviceInfo)
    }

    override suspend fun getPatientDetails(request: PatientDetailsModel): Response<APIResponse<PatientDetailsModel>> =
        apiService.getPatientDetails(request)

    override suspend fun getMedicalReviewStaticData(cultureId:Long): Response<APIResponse<MedicalReviewStaticResponse>> {
        return apiService.getMedicalReviewStaticData(cultureId)
    }

    override suspend fun createContinuousMedicalReview(request: MedicalReviewEditModel): Response<APIResponse<InitialEncounterResponse>> {
        return apiService.createContinuousMedicalReview(request)
    }

    override suspend fun getPatientBPLogList(request: AssessmentListRequest,): Response<APIResponse<BPLogListResponse>> =
        apiService.getPatientBPLogList(request)

    override suspend fun createPatientBPLog(createRequest: JsonObject): Response<APIResponse<BPLogModel>> {
        return apiService.createPatientBPLog(createRequest)
    }

    override suspend fun getPatientBloodGlucoseList(request: AssessmentListRequest): Response<APIResponse<BloodGlucoseListResponse>> {
        return apiService.getPatientBloodGlucoseList(request)
    }

    override suspend fun createBloodGlucoseBPLog(createRequest: JsonObject): Response<APIResponse<BPLogModel>> {
        return apiService.createBloodGlucoseBPLog(createRequest)
    }

    override suspend fun createMentalHealth(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createMentalHealth(request)
    }

    override suspend fun searchLabTest(request: SearchModel): Response<APIResponse<ArrayList<LabTestSearchResponse>>> {
        return apiService.searchLabTest(request)
    }

    override suspend fun getLabTestResult(labTestId: Long): Response<APIResponse<ArrayList<HashMap<String, Any>>>> {
        return apiService.getLabTestResult(labTestId)
    }

    override suspend fun getPatientLabTestHistory(request: PatientHistoryRequest): Response<APIResponse<PatientLabTestHistoryResponse>> {
        return apiService.getPatientLabTestHistory(request)
    }

    override suspend fun searchMedication(request: MedicationSearchReqModel): Response<APIResponse<ArrayList<PrescriptionModel>>> {
        return apiService.searchMedication(request)
    }

    override suspend fun createPregnancy(request: PregnancyCreateRequest): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createPregnancy(request)

    override suspend fun updatePregnancy(request: PregnancyCreateRequest): Response<APIResponse<HashMap<String, Any>>> =
         apiService.updatePregnancy(request)

    override suspend fun getPatientPregnancyDetails(request: PatientPregnancyModel): Response<APIResponse<PregnancyCreateRequest>> =
        apiService.getPatientPregnancyDetails(request)

    override suspend fun getPatientMedicalReviewHistoryList(request: MedicalReviewBaseRequest): Response<APIResponse<PatientMedicalReviewHistoryResponse>> {
        return apiService.getPatientMedicalReviewHistoryList(request)
    }

    override suspend fun getPatientPrescriptionHistoryList(request: PatientHistoryRequest): Response<APIResponse<PatientPrescriptionHistoryResponse>> {
        return apiService.getPatientPrescriptionHistoryList(request)
    }

    override suspend fun confirmDiagnosis(request: ConfirmDiagnosesRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.confirmDiagnosis(request)
    }

    override suspend fun getPatientLifeStyleDetails(requestPatient: PatientLifeStyleRequest): Response<APIResponse<ArrayList<PatientLifeStyle>>> {
        return apiService.getPatientLifeStyleDetails(requestPatient)
    }


    override suspend fun createPatientVisit(request: MedicalReviewBaseRequest): Response<APIResponse<PatientVisit>> {
        return apiService.createPatientVisit(request)
    }

    override suspend fun getPatientMedicalReviewSummary(request: MedicalReviewBaseRequest): Response<APIResponse<SummaryResponse>> {
        return apiService.getPatientMedicalReviewSummary(request)
    }

    override suspend fun getMentalHealthDetails(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.getMentalHealthDetails(request)
    }

    override suspend fun updateMentalHealth(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.updateMentalHealth(request)
    }

    override suspend fun updatePrescription(body: RequestBody): Response<APIResponse<ResponseDataModel>> {
        return apiService.updatePrescription(body)
    }

    override suspend fun getPrescriptionList(request: PatientPrescriptionModel): Response<APIResponse<ArrayList<PrescriptionModel>>> {
        return apiService.getPrescriptionList(request)
    }

    override suspend fun removePrescription(request: PatientPrescriptionModel): Response<APIResponse<ResponseDataModel>> {
        return apiService.removePrescription(request)
    }

    override suspend fun getPatientFillPrescriptionList(request: FillPrescriptionRequest): Response<APIResponse<ArrayList<FillPrescriptionListResponse>>> {
        return apiService.getPatientFillPrescriptionList(request)
    }

    override suspend fun fillPrescriptionUpdate(request: FillPrescriptionUpdateRequest): Response<APIResponse<FillPrescriptionResponse>> {
        return apiService.fillPrescriptionUpdate(request)
    }

    override suspend fun updateTreatmentPlan(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.updateTreatmentPlan(request)
    }

    override suspend fun treatmentPlanDetails(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.treatmentPlanDetails(request)
    }

    override suspend fun searchPatientById(request: PatientsDataModel): APIResponse<ArrayList<PatientListRespModel>> {
        return apiService.searchPatientById(request)
    }

    override suspend fun advancedSearchCountry(request: PatientsDataModel): APIResponse<ArrayList<PatientListRespModel>> {
        return apiService.advancedSearchCountry(request)
    }

    override suspend fun advancedSearchSite(request: PatientsDataModel): APIResponse<ArrayList<PatientListRespModel>> {
        return apiService.advancedSearchSite(request)
    }

    override suspend fun patientsList(request: PatientsDataModel): APIResponse<ArrayList<PatientListRespModel>> {
        return apiService.patientList(request)
    }

    override suspend fun getPatientLabTests(request: HashMap<String, Any?>): Response<APIResponse<LabTestListResponse>> {
        return apiService.getPatientLabTests(request)
    }

    override suspend fun referLabTest(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.referLabTest(request)
    }

    override suspend fun createLabTestResult(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createLabTestResult(request)
    }
    override suspend fun getLabTestResultDetails(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.getLabTestResultDetails(request)
    }

    override suspend fun getBadgeCount(request: BadgeModel): Response<APIResponse<BadgeResponseModel>> {
        return apiService.getBadgeCount(request)
    }

    override suspend fun removeLabTest(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.removeLabTest(request)
    }

    override suspend fun reviewLabTestResult(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.reviewLabTestResult(request)
    }

    override suspend fun createPatientLifestyle(request: LifeStyleManagement): Response<APIResponse<HashMap<String, Any>>> =
        apiService.createPatientLifestyle(request)


    override suspend fun removePatientLifestyle(request: PatientLifestyleModel): Response<APIResponse<Boolean>> {
        return apiService.removePatientLifestyle(request)
    }

    override suspend fun getScreeningDetails(request: ScreeningDetail): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.getScreeningDetails(request)
    }

    override suspend fun validateSession(): Response<ResponseBody> {
        return apiService.validateSession()
    }

    override suspend fun updatePatientLifestyle(request: PatientLifestyleModel): Response<APIResponse<LifeStyleManagement>> {
        return apiService.updatePatientLifestyle(request)
    }

    override suspend fun getPatientLifestyleList(request: PatientLifestyleModel): Response<APIResponse<ArrayList<LifeStyleManagement>>> {
        return apiService.getPatientLifestyleList(request)
    }

    override suspend fun getPrescriptionRefillHistory(request: PatientPrescriptionModel): Response<APIResponse<ArrayList<PrescriptionRefillHistoryResponse>>> {
        return apiService.getPrescriptionRefillHistory(request)
    }

    override suspend fun clearBadgeNotification(request: PatientLifestyleModel): Response<APIResponse<ResponseDataModel>> {
        return apiService.clearBadgeNotification(request)
    }

    override suspend fun getPatientBasicDetail(request: PatientBasicRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.getPatientBasicDetailEdit(request)
    }

    override suspend fun updatePatient(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> =
        apiService.updatePatient(request)


    override suspend fun createBpLog(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createBpLog(request)
    }

    override suspend fun createGlucoseLog(request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createGlucoseLog(request)
    }

    override suspend fun createScreeningLog(createRequest: JsonObject): Response<ScreeningPatientResponse> {
        return apiService.createScreeningLog(createRequest)
    }

    override suspend fun createPatientScreening(createRequest: JsonObject): Response<ScreeningPatientResponse> {
        return apiService.createPatientScreening(createRequest)
    }

    override suspend fun searchSite(request: RegionSiteModel): Response<APIResponse<ArrayList<RegionSiteResponse>>> {
        return apiService.searchSite(request)
    }

    override suspend fun searchRoleUser(request: SiteRoleModel): Response<APIResponse<ArrayList<SiteRoleResponse>>> {
        return apiService.searchRoleUser(request)
    }

    override suspend fun createPatientTransfer(request: TransferCreateRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.createPatientTransfer(request)
    }

    override suspend fun patientTransferNotificationCount(request: PatientTransferNotificationCountRequest): Response<APIResponse<PatientTransferNotificationCountResponse>> {
        return apiService.patientTransferNotificationCount(request)
    }

    override suspend fun getPatientTransferList(request: PatientTransferNotificationCountRequest): Response<APIResponse<PatientTransferListResponse>> {
        return apiService.getPatientTransferList(request)
    }

    override suspend fun patientTransferUpdate(request: PatientTransferUpdateRequest): Response<APIResponse<PatientTransferUpdateResponse>> {
        return apiService.patientTransferUpdate(request)
    }


    override suspend fun patientRemove(request: PatientRemoveRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.patientRemove(request)
    }

    override suspend fun validatePatientTransfer(request: FillPrescriptionRequest): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.validatePatientTransfer(request)
    }

    override suspend fun cultureLocaleUpdate(request: CultureLocaleModel): Response<APIResponse<HashMap<String, Any>>> {
        return apiService.cultureLocaleUpdate(request)
    }

    override suspend fun versionCheck(): Response<APIResponse<Boolean>> {
        return apiService.versionCheck()
    }

}