package com.medtroniclabs.opensource.ui.medicalreview.repo

import com.google.gson.JsonObject
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.db.local.RoomHelper
import com.medtroniclabs.opensource.db.tables.MentalHealthEntity
import com.medtroniclabs.opensource.network.ApiHelper
import okhttp3.RequestBody
import javax.inject.Inject

class MedicalReviewRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    suspend fun getPatientDetails(request: PatientDetailsModel) =
        apiHelper.getPatientDetails(request)

    suspend fun getPatientBPLogList(request: AssessmentListRequest) =
        apiHelper.getPatientBPLogList(request)

    suspend fun createPatientBPLog(createRequest: JsonObject) =
        apiHelper.createPatientBPLog(createRequest)

    suspend fun getPatientBloodGlucoseList(request: AssessmentListRequest) =
        apiHelper.getPatientBloodGlucoseList(request)

    suspend fun createBloodGlucoseBPLog(createRequest: JsonObject) =
        apiHelper.createBloodGlucoseBPLog(createRequest)

    suspend fun getMHQuestionsByType(type: String): MentalHealthEntity =
        roomHelper.getMHQuestionsByType(type)

    suspend fun createMentalHealth(request: HashMap<String, Any>) =
        apiHelper.createMentalHealth(request)

    suspend fun searchLabTest(request: SearchModel) = apiHelper.searchLabTest(request)
    suspend fun getLabTestResult(labTestId: Long) = apiHelper.getLabTestResult(labTestId)

    suspend fun getPatientLabTestHistory(request: PatientHistoryRequest) =
        apiHelper.getPatientLabTestHistory(request)

    suspend fun searchMedication(request: MedicationSearchReqModel) =
        apiHelper.searchMedication(request)

    suspend fun createPregnancy(request: PregnancyCreateRequest) =
        apiHelper.createPregnancy(request)

    suspend fun updatePregnancy(request: PregnancyCreateRequest) =
        apiHelper.updatePregnancy(request)

    suspend fun getPatientPregnancyDetails(request: PatientPregnancyModel) =
        apiHelper.getPatientPregnancyDetails(request)

    suspend fun getPatientMedicalReviewHistoryList(request: MedicalReviewBaseRequest) =
        apiHelper.getPatientMedicalReviewHistoryList(request)

    suspend fun getPatientPrescriptionHistoryList(request: PatientHistoryRequest) =
        apiHelper.getPatientPrescriptionHistoryList(request)

    suspend fun confirmDiagnosis(request: ConfirmDiagnosesRequest) =
        apiHelper.confirmDiagnosis(request)

    suspend fun getPatientLifeStyleDetails(requestPatient: PatientLifeStyleRequest) =
        apiHelper.getPatientLifeStyleDetails(requestPatient)

    suspend fun getMentalHealthDetails(request: HashMap<String, Any>) =
        apiHelper.getMentalHealthDetails(request)

    suspend fun updateMentalHealth(request: HashMap<String, Any>) =
        apiHelper.updateMentalHealth(request)

    suspend fun getPatientMedicalReviewSummary(request: MedicalReviewBaseRequest) =
        apiHelper.getPatientMedicalReviewSummary(request)

    suspend fun updatePrescription(body: RequestBody) =
        apiHelper.updatePrescription(body)

    suspend fun getPrescriptionList(request: PatientPrescriptionModel) =
        apiHelper.getPrescriptionList(request)

    suspend fun removePrescription(request: PatientPrescriptionModel) =
        apiHelper.removePrescription(request)

    suspend fun getPatientFillPrescriptionList(request: FillPrescriptionRequest) =
        apiHelper.getPatientFillPrescriptionList(request)

    suspend fun fillPrescriptionUpdate(request: FillPrescriptionUpdateRequest) =
        apiHelper.fillPrescriptionUpdate(request)

    suspend fun getFrequency() = roomHelper.getFrequencyList()

    suspend fun getShortageReasonList(type: String) = roomHelper.getShortageReason(type)

    suspend fun getUnitList(type: String) = roomHelper.getUnitList(type)

    suspend fun getPatientLabTests(request: HashMap<String, Any?>) = apiHelper.getPatientLabTests(request)

    suspend fun referLabTest(request: HashMap<String, Any>) = apiHelper.referLabTest(request)

    suspend fun createLabTestResult(request: HashMap<String, Any>) = apiHelper.createLabTestResult(request)

    suspend fun getLabTestResultDetails(request: HashMap<String, Any>) = apiHelper.getLabTestResultDetails(request)

    suspend fun getComorbidity() = roomHelper.getComorbidity()

    suspend fun getComplication() = roomHelper.getComplication()

    suspend fun getLifeStyle() = roomHelper.getLifeStyle()

    suspend fun getCurrentMedicationList() = roomHelper.getCurrentMedicationList()

    suspend fun getPhysicalExaminationList() = roomHelper.getPhysicalExaminationList()

    suspend fun getChiefComplaints() = roomHelper.getChiefComplaints()

    suspend fun createContinuousMedicalReview(request: MedicalReviewEditModel) =
        apiHelper.createContinuousMedicalReview(request)

    suspend fun getCurrentMedicationList(type: String) = roomHelper.getCurrentMedicationList(type)

    suspend fun getDiagnosis(gender: ArrayList<String>, type: ArrayList<String>) = roomHelper.getDiagnosis(gender, type)

    suspend fun getDiagnosisList() = roomHelper.getDiagnosisList()

    suspend fun createPatientVisit(request: MedicalReviewBaseRequest) =
        apiHelper.createPatientVisit(request)

    suspend fun updateTreatmentPlan(request: HashMap<String, Any>) =
        apiHelper.updateTreatmentPlan(request)

    suspend fun treatmentPlanDetails(request: HashMap<String, Any>) =
        apiHelper.treatmentPlanDetails(request)

    suspend fun getTreatmentPlanData() = roomHelper.getTreatmentPlanData()

    suspend fun deleteTreatmentPlan() = roomHelper.deleteTreatmentPlan()

    suspend fun getBadgeCount(request: BadgeModel) = apiHelper.getBadgeCount(request)

    suspend fun removeLabTest(request: HashMap<String, Any>) = apiHelper.removeLabTest(request)

    suspend fun reviewLabTestResult(request: HashMap<String, Any>) =
        apiHelper.reviewLabTestResult(request)

    suspend fun getScreeningDetails(request: ScreeningDetail) =
        apiHelper.getScreeningDetails(request)

    suspend fun getLifestyleList() = roomHelper.getLifestyleList()

    suspend fun createPatientLifestyle(request: LifeStyleManagement) =
        apiHelper.createPatientLifestyle(request)

    suspend fun updatePatientLifestyle(request: PatientLifestyleModel) = apiHelper.updatePatientLifestyle(request)

    suspend fun getPatientLifestyleList(request: PatientLifestyleModel) = apiHelper.getPatientLifestyleList(request)

    suspend fun removePatientLifestyle(request: PatientLifestyleModel) =
        apiHelper.removePatientLifestyle(request)

    suspend fun getPrescriptionRefillHistory(request: PatientPrescriptionModel) =
        apiHelper.getPrescriptionRefillHistory(request)

    suspend fun clearBadgeNotification(request: PatientLifestyleModel) = apiHelper.clearBadgeNotification(request)

    suspend fun createBpLog(request: HashMap<String, Any>) = apiHelper.createBpLog(request)

    suspend fun createGlucoseLog(request: HashMap<String, Any>) = apiHelper.createGlucoseLog(request)

    suspend fun searchSite(request: RegionSiteModel) = apiHelper.searchSite(request)

    suspend fun searchRoleUser(request: SiteRoleModel) = apiHelper.searchRoleUser(request)

    suspend fun createPatientTransfer(request: TransferCreateRequest) = apiHelper.createPatientTransfer(request)

    suspend fun patientRemove(request: PatientRemoveRequest) = apiHelper.patientRemove(request)

    suspend fun validatePatientTransfer(request: FillPrescriptionRequest) = apiHelper.validatePatientTransfer(request)
}