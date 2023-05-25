package com.medtroniclabs.opensource.db.local

import com.medtroniclabs.opensource.data.model.UnitMetricEntity
import com.medtroniclabs.opensource.data.screening.SiteDetails
import com.medtroniclabs.opensource.db.tables.*

interface RoomHelper {

    suspend fun insertLanguage(languageEntity: LanguageEntity)
    suspend fun deleteAllLanguage()
    suspend fun savePatientScreeningInformation(screeningEntity: ScreeningEntity): Long
    suspend fun insertRiskFactor(riskFactorEntity: RiskFactorEntity)
    suspend fun getRiskFactorEntity(): List<RiskFactorEntity>
    suspend fun saveFormEntry(formEntity: FormEntity)
    suspend fun saveConsentEntry(consentEntity: ConsentEntity)
    suspend fun getFormEntity(formType: String, userId: Long): FormEntity
    suspend fun getConsentEntity(formType: String, userId: Long): ConsentEntity
    suspend fun getSiteEntity(userSite: Boolean, userId: Long): List<SiteEntity>
    suspend fun saveSiteList(siteEntity: List<SiteEntity>)
    suspend fun saveSelectedSiteEntity(siteEntity: SiteEntity)
    suspend fun getSelectedSiteEntity(): SiteEntity?
    suspend fun saveChosenSiteDetail(siteDetail: SiteDetails)
    suspend fun getScreenedPatientCount(startDate: Long, endDate: Long, userId: Long): Long
    suspend fun getScreenedPatientReferredCount(
        startDate: Long,
        endDate: Long,
        userId: Long,
        isReferred: Boolean
    ): Long

    suspend fun getAccountSiteList(userId: Long): List<SiteEntity>
    suspend fun getAllScreeningRecords(uploadStatus: Boolean): List<ScreeningEntity>?
    suspend fun deleteUploadedScreeningRecords(todayDateTimeInMilliSeconds: Long)
    suspend fun updateScreeningRecordById(id: Long, uploadStatus: Boolean)
    suspend fun updateGeneralDetailsById(id: Long, generalDetails: String)
    suspend fun getScreeningRecordById(id: Long): ScreeningEntity
    suspend fun deleteAllSiteCache()
    suspend fun saveMenus(menuEntity: ArrayList<MenuEntity>)
    suspend fun deleteAllMenu()
    suspend fun getMenuListByRole(role: String, userId: Long): List<MenuEntity>
    suspend fun deleteConsentEntry()
    suspend fun deleteFormEntity()
    suspend fun deleteRiskFactor()
    suspend fun saveCountryList(countryListEntity: List<CountryEntity>)
    suspend fun saveCountyList(countyEntity: List<CountyEntity>)
    suspend fun saveSubCountyList(subCountyEntity: List<SubCountyEntity>)
    suspend fun getCountryList(): Any
    suspend fun getCountyList(): Any
    suspend fun getCountyList(selectedParent: Long): Any
    suspend fun getSubCountyList(): Any
    suspend fun getSubCountyList(selectedParent: Long): Any
    suspend fun deleteCountryList()
    suspend fun deleteCountyList()
    suspend fun deleteSubCountyList()
    suspend fun saveSymptomList(symptoms: List<SymptomEntity>)
    suspend fun deleteSymptoms()
    suspend fun saveMedicalCompliance(list: List<MedicalComplianceEntity>)
    suspend fun deleteMedicalCompliance()
    suspend fun getMedicalParentComplianceList(): List<MedicalComplianceEntity>
    suspend fun getMedicalChildComplianceList(parentId: Long): List<MedicalComplianceEntity>
    suspend fun getSymptomList(): List<SymptomEntity>
    suspend fun insertMHList(list: List<MentalHealthEntity>)
    suspend fun getMHQuestionsByType(type: String): MentalHealthEntity
    suspend fun saveProgramList(programEntity: List<ProgramEntity>)
    suspend fun getProgramList(site: String): Any
    suspend fun getProgramList(selectedParent: Long, site: String): Any
    suspend fun deleteProgramList()
    suspend fun saveComorbidity(list: ArrayList<ComorbidityEntity>)
    suspend fun deleteComorbidity()
    suspend fun saveCurrentMedication(list: ArrayList<CurrentMedicationEntity>)
    suspend fun deleteCurrentMedication()
    suspend fun saveComplication(list: ArrayList<ComplicationEntity>)
    suspend fun deleteComplication()
    suspend fun savePhysicalExamination(list: ArrayList<PhysicalExaminationEntity>)
    suspend fun deletePhysicalExamination()
    suspend fun saveLifeStyle(list: ArrayList<LifestyleEntity>)
    suspend fun deleteLifeStyle()
    suspend fun saveCompliants(list: ArrayList<ComplaintsEntity>)
    suspend fun deleteCompliants()
    suspend fun getUnSyncedDataCount(): Long
    suspend fun getComorbidity(): List<ComorbidityEntity>
    suspend fun getComplication(): List<ComplicationEntity>
    suspend fun getLifeStyle(): List<LifestyleEntity>
    suspend fun getCurrentMedicationList(): List<CurrentMedicationEntity>
    suspend fun getPhysicalExaminationList(): List<PhysicalExaminationEntity>
    suspend fun getChiefComplaints(): List<ComplaintsEntity>
    suspend fun getCurrentMedicationList(type: String): List<CurrentMedicationEntity>
    suspend fun saveDiagnosis(list: ArrayList<DiagnosisEntity>)
    suspend fun deleteDiagnosis()
    suspend fun getDiagnosis(gender: ArrayList<String>, type: ArrayList<String>): List<DiagnosisEntity>
    suspend fun deleteMHList()
    suspend fun saveFrequency(frequencies: List<FrequencyEntity>)
    suspend fun getFrequencyList(): List<FrequencyEntity>
    suspend fun deleteFrequency()
    suspend fun saveShortageReason(shortageReasonEntityList: ArrayList<ShortageReasonEntity>)
    suspend fun getShortageReason(type: String): List<ShortageReasonEntity>
    suspend fun deleteShortageReason()
    suspend fun saveUnitMetric(list: ArrayList<UnitMetricEntity>)
    suspend fun getUnitList(type: String): List<UnitMetricEntity>
    suspend fun deleteUnitList()
    suspend fun saveTreatmentPlan(model: TreatmentPlanEntity)
    suspend fun getTreatmentPlanData(): List<TreatmentPlanEntity>
    suspend fun deleteTreatmentPlan()
    suspend fun getLifestyleList() : List<NutritionLifeStyle>
    suspend fun saveNutritionLifeStyle(list: ArrayList<NutritionLifeStyle>)
    suspend fun getFormEntity(formTypeOne: String,formTypeTwo:String, userId: Long): List<FormEntity>
    suspend fun getDiagnosisList():List<DiagnosisEntity>
    suspend fun saveCulturesList(culturesEntity: List<CulturesEntity>)
    suspend fun getCulturesList(): List<CulturesEntity>
    suspend fun deleteCulturesList()
}