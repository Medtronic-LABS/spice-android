package com.medtroniclabs.opensource.ui.boarding.repo

import com.google.gson.JsonObject
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.db.local.RoomHelper
import com.medtroniclabs.opensource.db.tables.*
import com.medtroniclabs.opensource.network.ApiHelper
import okhttp3.MultipartBody
import javax.inject.Inject

class OnBoardingRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {
    suspend fun insertLanguage(
        languageEntity: LanguageEntity,
        callback: (onComplete: Boolean) -> Unit
    ) {
        roomHelper.insertLanguage(languageEntity)
        callback.invoke(true)
    }

    suspend fun deleteLanguage() = roomHelper.deleteAllLanguage()

    suspend fun doLogin(loginRequest: MultipartBody) = apiHelper.doLogin(loginRequest)

    suspend fun resetPassword(userName: String) =
        apiHelper.resetPassword(userName)

    suspend fun insertRiskFactor(riskFactorEntity: RiskFactorEntity) =
        roomHelper.insertRiskFactor(riskFactorEntity)

    suspend fun getMetaDataResponse(cultureId: Long) = apiHelper.getMetaData(cultureId)
    suspend fun saveDeviceDetails(tenantId: Long, deviceInfo: DeviceInfo) = apiHelper.saveDeviceDetails(tenantId, deviceInfo)

    suspend fun saveFormEntity(formEntity: FormEntity) = roomHelper.saveFormEntry(formEntity)

    suspend fun saveConsentEntity(consentEntity: ConsentEntity) =
        roomHelper.saveConsentEntry(consentEntity)

    suspend fun saveSiteEntities(list: List<SiteEntity>) = roomHelper.saveSiteList(list)

    suspend fun getSiteList(userSite: Boolean, userId: Long) =
        roomHelper.getSiteEntity(userSite, userId)

    suspend fun storeSelectedSiteEntity(siteEntity: SiteEntity) =
        roomHelper.saveSelectedSiteEntity(siteEntity)

    suspend fun getSelectedSiteEntity() =
        roomHelper.getSelectedSiteEntity()

    suspend fun deleteSiteCache() = roomHelper.deleteAllSiteCache()


    suspend fun deleteAllMenu() = roomHelper.deleteAllMenu()
    suspend fun saveMenuList(menuEntity: ArrayList<MenuEntity>) = roomHelper.saveMenus(menuEntity)
    suspend fun getMenuListByRole(role: String, userId: Long) =
        roomHelper.getMenuListByRole(role, userId)

    suspend fun createPatient(request: JsonObject) = apiHelper.createPatient(request)

    suspend fun deleteConsentEntity() = roomHelper.deleteConsentEntry()

    suspend fun deleteFormEntity() = roomHelper.deleteFormEntity()

    suspend fun deleteRiskFactor() = roomHelper.deleteRiskFactor()

    suspend fun userLogout() = apiHelper.userLogout()

    suspend fun saveCountryList(countryListEntity: List<CountryEntity>) =
        roomHelper.saveCountryList(countryListEntity)

    suspend fun saveCountyList(countyEntity: List<CountyEntity>) =
        roomHelper.saveCountyList(countyEntity)

    suspend fun saveSubCountyList(subCountyEntity: List<SubCountyEntity>) =
        roomHelper.saveSubCountyList(subCountyEntity)

    suspend fun getCountryList(): Any {
        return roomHelper.getCountryList()
    }

    suspend fun getCountyList(selectedParent: Long?): Any {
        return if (selectedParent == null) {
            roomHelper.getCountyList()
        } else {
            roomHelper.getCountyList(selectedParent)
        }
    }

    suspend fun getSubCountyList(selectedParent: Long?): Any {
        return if (selectedParent == null) {
            roomHelper.getSubCountyList()
        } else {
            roomHelper.getSubCountyList(selectedParent)
        }
    }

    suspend fun deleteCountryList() {
        roomHelper.deleteCountryList()
    }

    suspend fun deleteCountyList() {
        roomHelper.deleteCountyList()
    }

    suspend fun deleteSubCountyList() {
        roomHelper.deleteSubCountyList()
    }

    suspend fun deleteSymptoms() {
        roomHelper.deleteSymptoms()
    }

    suspend fun saveSymptoms(list: List<SymptomEntity>) {
        roomHelper.saveSymptomList(list)
    }

    suspend fun saveMedicalCompliance(list: List<MedicalComplianceEntity>) {
        roomHelper.saveMedicalCompliance(list)
    }

    suspend fun deleteMedicalCompliance() {
        roomHelper.deleteMedicalCompliance()
    }

    suspend fun getMedicationParentComplianceList(): List<MedicalComplianceEntity> {
        return roomHelper.getMedicalParentComplianceList()
    }

    suspend fun getSymptomList(): List<SymptomEntity> {
        return roomHelper.getSymptomList()
    }

    suspend fun getMedicationChildComplianceList(parentId: Long): List<MedicalComplianceEntity> {
        return roomHelper.getMedicalChildComplianceList(parentId)
    }

    suspend fun insertMHList(list: List<MentalHealthEntity>) = roomHelper.insertMHList(list)
    suspend fun getMHQuestionsByType(type: String): MentalHealthEntity =
        roomHelper.getMHQuestionsByType(type)

    suspend fun saveProgramList(programEntity: List<ProgramEntity>) =
        roomHelper.saveProgramList(programEntity)

    suspend fun getProgramList(selectedParent: Long?): Any {
        val site = SecuredPreference.getSelectedSiteEntity()?.id ?: -1
        return if (selectedParent == null) {
            roomHelper.getProgramList(site.toString())
        } else {
            roomHelper.getProgramList(selectedParent, site.toString())
        }
    }

    suspend fun deleteProgramList() {
        roomHelper.deleteProgramList()
    }

    suspend fun getMedicalReviewStaticData(cultureId: Long) =
        apiHelper.getMedicalReviewStaticData(cultureId)

    suspend fun saveComorbidity(list: ArrayList<ComorbidityEntity>) =
        roomHelper.saveComorbidity(list)

    suspend fun deleteComorbidity() = roomHelper.deleteComorbidity()

    suspend fun saveCurrentMedication(list: ArrayList<CurrentMedicationEntity>) =
        roomHelper.saveCurrentMedication(list)

    suspend fun deleteCurrentMedication() = roomHelper.deleteCurrentMedication()

    suspend fun saveComplication(list: ArrayList<ComplicationEntity>) =
        roomHelper.saveComplication(list)

    suspend fun deleteComplication() = roomHelper.deleteComplication()

    suspend fun savePhysicalExamination(list: ArrayList<PhysicalExaminationEntity>) =
        roomHelper.savePhysicalExamination(list)

    suspend fun deletePhysicalExamination() = roomHelper.deletePhysicalExamination()

    suspend fun saveLifeStyle(list: ArrayList<LifestyleEntity>) =
        roomHelper.saveLifeStyle(list)

    suspend fun deleteLifeStyle() = roomHelper.deleteLifeStyle()

    suspend fun deleteTreatmentPlanData() = roomHelper.deleteTreatmentPlan()

    suspend fun saveCompliants(list: ArrayList<ComplaintsEntity>) =
        roomHelper.saveCompliants(list = list)

    suspend fun deleteCompliants() = roomHelper.deleteCompliants()

    suspend fun getUnSyncedDataCount() = roomHelper.getUnSyncedDataCount()

    suspend fun saveDiagnosis(list: ArrayList<DiagnosisEntity>) =
        roomHelper.saveDiagnosis(list)

    suspend fun deleteDiagnosis() = roomHelper.deleteDiagnosis()

    suspend fun deleteMHList() = roomHelper.deleteMHList()

    suspend fun saveFrequency(list: ArrayList<FrequencyEntity>) =
        roomHelper.saveFrequency(list)

    suspend fun deleteFrequency() = roomHelper.deleteFrequency()

    suspend fun saveShortageReason(list: ArrayList<ShortageReasonEntity>) =
        roomHelper.saveShortageReason(list)

    suspend fun deleteShortageReason() = roomHelper.deleteShortageReason()

    suspend fun saveUnitList(list: ArrayList<UnitMetricEntity>) = roomHelper.saveUnitMetric(list)

    suspend fun getUnitList(type: String) = roomHelper.getUnitList(type)

    suspend fun deleteUnitList() = roomHelper.deleteUnitList()

    suspend fun saveTreatmentPlan(treatmentPlanEntity: TreatmentPlanEntity) = roomHelper.saveTreatmentPlan(treatmentPlanEntity)

    suspend fun validateSession() = apiHelper.validateSession()

    suspend fun saveNutritionLifeStyle(list: ArrayList<NutritionLifeStyle>) = roomHelper.saveNutritionLifeStyle(list)

    suspend fun createScreeningLog(createPatientRequest: JsonObject) =
        apiHelper.createScreeningLog(createPatientRequest)

    suspend fun createPatientScreening(createPatientRequest: JsonObject) =
        apiHelper.createPatientScreening(createPatientRequest)

    suspend fun patientTransferNotificationCount(request: PatientTransferNotificationCountRequest) = apiHelper.patientTransferNotificationCount(request)

    suspend fun patientTransferListResponse(request: PatientTransferNotificationCountRequest) = apiHelper.getPatientTransferList(request)

    suspend fun patientTransferUpdate(request:PatientTransferUpdateRequest) = apiHelper.patientTransferUpdate(request)

    suspend fun getCulturesList(): List<CulturesEntity> {
        return roomHelper.getCulturesList()
    }

    suspend fun saveCulturesList(culturesEntity: List<CulturesEntity>) =
        roomHelper.saveCulturesList(culturesEntity)

    suspend fun deleteCulturesList() {
        roomHelper.deleteCulturesList()
    }

    suspend fun cultureLocaleUpdate(localeRequest: CultureLocaleModel) = apiHelper.cultureLocaleUpdate(localeRequest)

    suspend fun versionCheck() = apiHelper.versionCheck()
}