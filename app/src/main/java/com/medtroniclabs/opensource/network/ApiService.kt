package com.medtroniclabs.opensource.network

import com.google.gson.JsonObject
import com.medtroniclabs.opensource.data.assesssment.AssessmentPatientResponse
import com.medtroniclabs.opensource.data.assesssment.BPLogModel
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.data.ui.PatientBasicRequest
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("/auth-service/${NetworkConstants.LOGIN}")
    suspend fun doLogin(@Body request: RequestBody): Response<LoginResponse>

    @GET("/spice-service/static-data/details")
    suspend fun getMetaData(@Query("cultureId") cultureId: Long): Response<APIResponse<MetaDataResponse>>

    @GET("/spice-service/static-data/medical-review")
    suspend fun getMedicalReviewStaticData(@Query("cultureId") cultureId: Long): Response<APIResponse<MedicalReviewStaticResponse>>

    @POST("/spice-service/patient/search")
    suspend fun searchPatientById(@Body request: PatientsDataModel): APIResponse<ArrayList<PatientListRespModel>>

    @POST("/spice-service/patient/advance-search/country")
    suspend fun advancedSearchCountry(@Body request: PatientsDataModel): APIResponse<ArrayList<PatientListRespModel>>

    @POST("/spice-service/patient/advance-search/site")
    suspend fun advancedSearchSite(@Body request: PatientsDataModel): APIResponse<ArrayList<PatientListRespModel>>

    @POST("/spice-service/patient/list")
    suspend fun patientList(@Body request: PatientsDataModel): APIResponse<ArrayList<PatientListRespModel>>

    @POST("/spice-service/patientvisit/create")
    suspend fun createPatientVisit(@Body request: MedicalReviewBaseRequest): Response<APIResponse<PatientVisit>>

    @POST("/spice-service/${NetworkConstants.DEVICE_DETAILS}")
    suspend fun saveDeviceDetails(
        @Header("TenantId") tenantId: Long,
        @Body deviceInfo: DeviceInfo
    ): Response<APIResponse<DeviceInfo>>

    @POST("/spice-service/patient/details")
    suspend fun getPatientDetails(@Body request: PatientDetailsModel): Response<APIResponse<PatientDetailsModel>>

    @POST("/spice-service/screeninglog/details")
    suspend fun getScreeningDetails(@Body request: ScreeningDetail): Response<APIResponse<HashMap<String, Any>>>

    @POST("/auth-service/logout")
    suspend fun userLogout(): Response<ResponseBody>

    @POST("/spice-service/medical-review/create")
    suspend fun createContinuousMedicalReview(@Body request: MedicalReviewEditModel): Response<APIResponse<InitialEncounterResponse>>

    @POST("/spice-service/medical-review/summary")
    suspend fun getPatientMedicalReviewSummary(@Body request: MedicalReviewBaseRequest): Response<APIResponse<SummaryResponse>>

    @POST("/spice-service/patient/enrollment")
    suspend fun createPatient(@Body request: JsonObject): Response<APIResponse<PatientCreateResponse>>

    @GET("/user-service/user/validate")
    suspend fun validateSession(): Response<ResponseBody>

    @POST("/spice-service/medical-review/count")
    suspend fun getBadgeCount(@Body request: BadgeModel): Response<APIResponse<BadgeResponseModel>>

    @PUT("/spice-service/patient-treatment-plan/update")
    suspend fun updateTreatmentPlan(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/patient-treatment-plan/details")
    suspend fun treatmentPlanDetails(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/screeninglog/create")
    suspend fun createScreeningLog(@Body createRequest: JsonObject): Response<ScreeningPatientResponse>

    @POST("/spice-service/screeninglog/old-data")
    suspend fun createPatientScreening(@Body createRequest: JsonObject): Response<ScreeningPatientResponse>

    @POST("/spice-service/bplog/list")
    suspend fun getPatientBPLogList(@Body request: AssessmentListRequest): Response<APIResponse<BPLogListResponse>>

    @POST("/spice-service/bplog/create")
    suspend fun createPatientBPLog(@Body createRequest: JsonObject): Response<APIResponse<BPLogModel>>

    @POST("/spice-service/glucoselog/list")
    suspend fun getPatientBloodGlucoseList(@Body request: AssessmentListRequest): Response<APIResponse<BloodGlucoseListResponse>>

    @POST("/spice-service/glucoselog/create")
    suspend fun createBloodGlucoseBPLog(@Body request: JsonObject): Response<APIResponse<BPLogModel>>

    @POST("/spice-service/assessment/bplog-create")
    suspend fun createBpLog(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/assessment/glucoselog-create")
    suspend fun createGlucoseLog(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @PUT("/spice-service/patient/update")
    suspend fun updatePatient(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/assessment/create")
    suspend fun createPatientAssessment(@Body createRequest: JsonObject): Response<APIResponse<AssessmentPatientResponse>>

    @POST("/spice-service/patient/pregnancy-details/create")
    suspend fun createPregnancy(@Body request: PregnancyCreateRequest): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/patient/pregnancy-details/update")
    suspend fun updatePregnancy(@Body request: PregnancyCreateRequest): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/patient/pregnancy-details/info")
    suspend fun getPatientPregnancyDetails(@Body request: PatientPregnancyModel): Response<APIResponse<PregnancyCreateRequest>>

    @POST("/spice-service/patient/basic-details")
    suspend fun getPatientBasicDetailEdit(@Body request: PatientBasicRequest): Response<APIResponse<HashMap<String, Any>>>

    @POST("/admin-service/medication/search")
    suspend fun searchMedication(@Body request: MedicationSearchReqModel): Response<APIResponse<ArrayList<PrescriptionModel>>>

    @POST("/spice-service/prescription/update")
    suspend fun updatePrescription(@Body body: RequestBody): Response<APIResponse<ResponseDataModel>>

    @POST("/spice-service/prescription/list")
    suspend fun getPrescriptionList(@Body request: PatientPrescriptionModel): Response<APIResponse<ArrayList<PrescriptionModel>>>

    @PUT("/spice-service/prescription/remove")
    suspend fun removePrescription(@Body request: PatientPrescriptionModel): Response<APIResponse<ResponseDataModel>>

    @POST("/spice-service/prescription-history/list")
    suspend fun getPatientPrescriptionHistoryList(@Body request: PatientHistoryRequest): Response<APIResponse<PatientPrescriptionHistoryResponse>>

    @POST("/spice-service/fill-prescription/update")
    suspend fun fillPrescriptionUpdate(@Body request: FillPrescriptionUpdateRequest): Response<APIResponse<FillPrescriptionResponse>>

    @POST("/spice-service/fill-prescription/list")
    suspend fun getPatientFillPrescriptionList(@Body request: FillPrescriptionRequest): Response<APIResponse<ArrayList<FillPrescriptionListResponse>>>

    @POST("/spice-service/prescription/refill-history")
    suspend fun getPrescriptionRefillHistory(@Body request: PatientPrescriptionModel): Response<APIResponse<ArrayList<PrescriptionRefillHistoryResponse>>>

    @PATCH("/spice-service/patient/confirm-diagnosis/update")
    suspend fun confirmDiagnosis(@Body request: ConfirmDiagnosesRequest): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/medical-review/list")
    suspend fun getPatientMedicalReviewHistoryList(@Body request: MedicalReviewBaseRequest): Response<APIResponse<PatientMedicalReviewHistoryResponse>>

    @POST("/spice-service/mentalhealth/create")
    suspend fun createMentalHealth(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/mentalhealth/update")
    suspend fun updateMentalHealth(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/mentalhealth/details")
    suspend fun getMentalHealthDetails(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @GET("/user-service/user/${NetworkConstants.FORGOT_PASSWORD}/{username}")
    suspend fun resetPassword(@Path("username") userName: String): Response<APIResponse<Boolean>>

    @POST("/spice-service/patient-labtest/search")
    suspend fun searchLabTest(@Body request: SearchModel): Response<APIResponse<ArrayList<LabTestSearchResponse>>>

    @GET("/spice-service/patient-labtest/result/list/{labTestId}")
    suspend fun getLabTestResult(@Path("labTestId") labTestId: Long): Response<APIResponse<ArrayList<HashMap<String, Any>>>>

    @POST("/spice-service/patient-labtest/list")
    suspend fun getPatientLabTestHistory(@Body request: PatientHistoryRequest): Response<APIResponse<PatientLabTestHistoryResponse>>

    @POST("/spice-service/patient-labtest/list")
    suspend fun getPatientLabTests(@Body request: HashMap<String, Any?>): Response<APIResponse<LabTestListResponse>>

    @POST("/spice-service/patient-labtest/create")
    suspend fun referLabTest(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/patient-labtest/result/create")
    suspend fun createLabTestResult(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/patient-labtest/result/details")
    suspend fun getLabTestResultDetails(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/patient-labtest/remove")
    suspend fun removeLabTest(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/patient-labtest/review")
    suspend fun reviewLabTestResult(@Body request: HashMap<String, Any>): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/patient-lifestyle/create")
    suspend fun createPatientLifestyle(@Body request: LifeStyleManagement): Response<APIResponse<HashMap<String, Any>>>

    @PUT("/spice-service/patient-lifestyle/update")
    suspend fun updatePatientLifestyle(@Body request: PatientLifestyleModel): Response<APIResponse<LifeStyleManagement>>

    @POST("/spice-service/patient-lifestyle/list")
    suspend fun getPatientLifestyleList(@Body request: PatientLifestyleModel): Response<APIResponse<ArrayList<LifeStyleManagement>>>

    @POST("/spice-service/patient-lifestyle/remove")
    suspend fun removePatientLifestyle(@Body request: PatientLifestyleModel): Response<APIResponse<Boolean>>

    @PUT("/spice-service/patient-lifestyle/update-view-status")
    suspend fun clearBadgeNotification(@Body request: PatientLifestyleModel): Response<APIResponse<ResponseDataModel>>

    @POST("/spice-service/patient/lifestyle/details")
    suspend fun getPatientLifeStyleDetails(@Body requestPatient: PatientLifeStyleRequest): Response<APIResponse<ArrayList<PatientLifeStyle>>>


    @POST("/admin-service/site/country/site-list")
    suspend fun searchSite(@Body request: RegionSiteModel): Response<APIResponse<ArrayList<RegionSiteResponse>>>

    @POST("/user-service/user/role-user-list")
    suspend fun searchRoleUser(@Body request: SiteRoleModel): Response<APIResponse<ArrayList<SiteRoleResponse>>>

    @POST("/spice-service/patient-transfer/create")
    suspend fun createPatientTransfer(@Body request: TransferCreateRequest): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/patient-transfer/notification-count")
    suspend fun patientTransferNotificationCount(@Body request: PatientTransferNotificationCountRequest):Response<APIResponse<PatientTransferNotificationCountResponse>>

    @POST("/spice-service/patient/remove")
    suspend fun patientRemove(@Body request: PatientRemoveRequest):Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/patient-transfer/validate")
    suspend fun validatePatientTransfer(@Body request: FillPrescriptionRequest): Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/patient-transfer/list")
    suspend fun getPatientTransferList(@Body request: PatientTransferNotificationCountRequest):Response<APIResponse<PatientTransferListResponse>>

    @POST("/spice-service/patient-transfer/update")
    suspend fun patientTransferUpdate(@Body request: PatientTransferUpdateRequest):Response<APIResponse<PatientTransferUpdateResponse>>

    @POST("/user-service/user/update-culture")
    suspend fun cultureLocaleUpdate(@Body request: CultureLocaleModel) : Response<APIResponse<HashMap<String, Any>>>

    @POST("/spice-service/static-data/app-version")
    suspend fun versionCheck(): Response<APIResponse<Boolean>>
}
