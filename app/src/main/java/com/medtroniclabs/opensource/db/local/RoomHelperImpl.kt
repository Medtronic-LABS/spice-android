package com.medtroniclabs.opensource.db.local

import com.google.gson.Gson
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.UnitMetricEntity
import com.medtroniclabs.opensource.data.screening.SiteDetails
import com.medtroniclabs.opensource.db.dao.LanguageDAO
import com.medtroniclabs.opensource.db.dao.MetaDataDAO
import com.medtroniclabs.opensource.db.dao.RiskFactorDAO
import com.medtroniclabs.opensource.db.dao.ScreeningDAO
import com.medtroniclabs.opensource.db.tables.*
import javax.inject.Inject

class RoomHelperImpl @Inject constructor(
    private val languageDAO: LanguageDAO,
    private val screeningDAO: ScreeningDAO,
    private val metaDataDAO: MetaDataDAO,
    private val riskFactorDAO: RiskFactorDAO
) : RoomHelper {

    override suspend fun insertLanguage(languageEntity: LanguageEntity) =
        languageDAO.insertLanguage(languageEntity)

    override suspend fun deleteAllLanguage() = languageDAO.deleteAllLanguage()

    override suspend fun savePatientScreeningInformation(screeningEntity: ScreeningEntity): Long =
        screeningDAO.insertScreening(screeningEntity)

    override suspend fun insertRiskFactor(riskFactorEntity: RiskFactorEntity) {
        riskFactorDAO.insertRiskFactor(riskFactorEntity)
    }

    override suspend fun getRiskFactorEntity(): List<RiskFactorEntity> {
        return riskFactorDAO.getAllRiskFactorEntity()
    }

    override suspend fun saveFormEntry(formEntity: FormEntity) {
        metaDataDAO.insertFormEntries(formEntity)
    }

    override suspend fun saveConsentEntry(consentEntity: ConsentEntity) {
        metaDataDAO.insertConsentFormHTMLRaw(consentEntity)
    }

    override suspend fun getFormEntity(formType: String, userId: Long): FormEntity {
        return metaDataDAO.getFormBasedOnType(formType, userId)
    }

    override suspend fun getFormEntity(
        formTypeOne: String,
        formTypeTwo: String,
        userId: Long
    ): List<FormEntity> {
        return metaDataDAO.getFormBasedOnType(formTypeOne,formTypeTwo,userId)
    }

    override suspend fun getConsentEntity(formType: String, userId: Long): ConsentEntity {
        return metaDataDAO.getConsentHTMLRawString(formType, userId)
    }

    override suspend fun getSiteEntity(userSite: Boolean, userId: Long): List<SiteEntity> {
        return metaDataDAO.getSiteEntityList(userSite, userId)
    }

    override suspend fun saveSiteList(siteEntity: List<SiteEntity>) {
        return metaDataDAO.insertSiteDetails(siteEntity)
    }

    override suspend fun saveSelectedSiteEntity(siteEntity: SiteEntity) =
        SecuredPreference.putSelectedSiteEntity(siteEntity)

    override suspend fun getSelectedSiteEntity(): SiteEntity? {
        return SecuredPreference.getSelectedSiteEntity()
    }

    override suspend fun saveChosenSiteDetail(siteDetail: SiteDetails) {
        val siteDetailsString = Gson().toJson(siteDetail)
        SecuredPreference.putString(
            SecuredPreference.EnvironmentKey.SITE_DETAIL.name,
            siteDetailsString
        )
    }

    override suspend fun getScreenedPatientCount(
        startDate: Long,
        endDate: Long,
        userId: Long
    ): Long {
        return screeningDAO.getScreenedPatientCount(startDate, endDate, userId)
    }

    override suspend fun getScreenedPatientReferredCount(
        startDate: Long,
        endDate: Long,
        userId: Long,
        isReferred: Boolean
    ): Long {
        return screeningDAO.getScreenedPatientReferredCount(startDate, endDate, userId, isReferred)
    }

    override suspend fun getAccountSiteList(userId: Long): List<SiteEntity> {
        return metaDataDAO.getAccountSiteList(userId)
    }

    override suspend fun getAllScreeningRecords(uploadStatus: Boolean): List<ScreeningEntity> {
        return screeningDAO.getAllScreeningRecords(uploadStatus)
    }

    override suspend fun deleteUploadedScreeningRecords(todayDateTimeInMilliSeconds: Long) {
        return screeningDAO.deleteUploadedScreeningRecords(todayDateTimeInMilliSeconds)
    }

    override suspend fun updateScreeningRecordById(id: Long, uploadStatus: Boolean) {
        return screeningDAO.updateScreeningRecordById(id, uploadStatus)
    }

    override suspend fun updateGeneralDetailsById(id: Long, generalDetails: String) {
        return screeningDAO.updateGeneralDetailsById(id, generalDetails)
    }

    override suspend fun getScreeningRecordById(id: Long): ScreeningEntity {
        return screeningDAO.getScreeningRecordById(id)
    }

    override suspend fun deleteAllSiteCache() {
        return metaDataDAO.deleteSiteList()
    }

    override suspend fun saveMenus(menuEntity: ArrayList<MenuEntity>) {
        return metaDataDAO.insertMenus(menuEntity)
    }

    override suspend fun deleteAllMenu() {
        return metaDataDAO.deleteAllMenu()
    }

    override suspend fun getMenuListByRole(role: String, userId: Long): List<MenuEntity> {
        return metaDataDAO.getMenuListByRole(role, userId)
    }

    override suspend fun deleteConsentEntry() {
        return metaDataDAO.deleteConsentEntry()
    }

    override suspend fun deleteFormEntity() {
        return metaDataDAO.deleteFormEntity()
    }

    override suspend fun deleteRiskFactor() {
        return metaDataDAO.deleteRiskFactor()
    }

    override suspend fun saveCountryList(countryListEntity: List<CountryEntity>) {
        return metaDataDAO.insertCountry(countryListEntity)
    }

    override suspend fun saveCountyList(countyEntity: List<CountyEntity>) {
        return metaDataDAO.insertCounty(countyEntity)
    }

    override suspend fun saveSubCountyList(subCountyEntity: List<SubCountyEntity>) {
        return metaDataDAO.insertSubCounty(subCountyEntity)
    }

    override suspend fun getCountryList(): Any {
        return metaDataDAO.getCountryList()
    }


    override suspend fun getCountyList(): Any {
        return metaDataDAO.getCountyList()
    }

    override suspend fun getCountyList(selectedParent: Long): Any {
        return metaDataDAO.getCountyList(selectedParent)
    }

    override suspend fun getSubCountyList(): Any {
        return metaDataDAO.getSubCountyList()
    }

    override suspend fun getSubCountyList(selectedParent: Long): Any {
        return metaDataDAO.getSubCountyList(selectedParent)
    }

    override suspend fun deleteCountryList() {
        metaDataDAO.deleteCountryList()
    }

    override suspend fun deleteCountyList() {
        metaDataDAO.deleteCountyList()
    }

    override suspend fun deleteSubCountyList() {
        metaDataDAO.deleteSubCountyList()
    }

    override suspend fun saveSymptomList(symptoms: List<SymptomEntity>) {
        metaDataDAO.insertSymptoms(symptoms)
    }

    override suspend fun deleteSymptoms() {
        metaDataDAO.deleteSymptomList()
    }

    override suspend fun saveMedicalCompliance(list: List<MedicalComplianceEntity>) {
        metaDataDAO.insertMedicalCompliance(list)
    }

    override suspend fun deleteMedicalCompliance() {
        metaDataDAO.deleteMedicalComplianceList()
    }

    override suspend fun getMedicalParentComplianceList(): List<MedicalComplianceEntity> {
        return metaDataDAO.getMedicalComplianceList()
    }

    override suspend fun getMedicalChildComplianceList(parentId: Long): List<MedicalComplianceEntity> {
        return metaDataDAO.getMedicalComplianceList(parentId)
    }

    override suspend fun getSymptomList(): List<SymptomEntity> {
        return metaDataDAO.getSymptomList()
    }


    override suspend fun insertMHList(list: List<MentalHealthEntity>) =
        metaDataDAO.insertMHList(list)

    override suspend fun getMHQuestionsByType(type: String): MentalHealthEntity =
        metaDataDAO.getMHQuestionsByType(type)

    override suspend fun saveProgramList(programEntity: List<ProgramEntity>) {
        return metaDataDAO.insertProgram(programEntity)
    }

    override suspend fun getProgramList(site: String): Any {
        return metaDataDAO.getProgramList(site)
    }

    override suspend fun getProgramList(selectedParent: Long, site: String): Any {
        return metaDataDAO.getProgramList(selectedParent, site)
    }

    override suspend fun deleteProgramList() {
        metaDataDAO.deleteProgramList()
    }

    override suspend fun saveComorbidity(list: ArrayList<ComorbidityEntity>) {
        metaDataDAO.saveComorbidity(list)
    }

    override suspend fun deleteComorbidity() {
        metaDataDAO.deleteComorbidity()
    }

    override suspend fun saveCurrentMedication(list: ArrayList<CurrentMedicationEntity>) {
        metaDataDAO.saveCurrentMedication(list)
    }

    override suspend fun deleteCurrentMedication() {
        metaDataDAO.deleteCurrentMedication()
    }

    override suspend fun saveComplication(list: ArrayList<ComplicationEntity>) {
        metaDataDAO.saveComplication(list)
    }

    override suspend fun deleteComplication() {
        metaDataDAO.deleteComplication()
    }

    override suspend fun savePhysicalExamination(list: ArrayList<PhysicalExaminationEntity>) {
        metaDataDAO.savePhysicalExamination(list)
    }

    override suspend fun deletePhysicalExamination() {
        metaDataDAO.deletePhysicalExamination()
    }

    override suspend fun saveLifeStyle(list: ArrayList<LifestyleEntity>) {
        metaDataDAO.saveLifeStyle(list)
    }

    override suspend fun deleteLifeStyle() {
        metaDataDAO.deleteLifeStyle()
    }

    override suspend fun saveCompliants(list: ArrayList<ComplaintsEntity>) {
        metaDataDAO.saveComplaints(list)
    }

    override suspend fun deleteCompliants() {
        metaDataDAO.deleteComplaints()
    }

    override suspend fun getUnSyncedDataCount() = screeningDAO.getUnSyncedDataCount()
    override suspend fun getComorbidity(): List<ComorbidityEntity> = metaDataDAO.getComorbidity()
    override suspend fun getComplication(): List<ComplicationEntity> = metaDataDAO.getComplication()
    override suspend fun getLifeStyle(): List<LifestyleEntity> = metaDataDAO.getLifeStyle()
    override suspend fun getCurrentMedicationList(): List<CurrentMedicationEntity> =
        metaDataDAO.getCurrentMedicationList()

    override suspend fun getCurrentMedicationList(type: String): List<CurrentMedicationEntity> =
        metaDataDAO.getCurrentMedicationList(type)

    override suspend fun getPhysicalExaminationList(): List<PhysicalExaminationEntity> =
        metaDataDAO.getPhysicalExaminationList()

    override suspend fun getChiefComplaints(): List<ComplaintsEntity> =
        metaDataDAO.getChiefComplaints()

    override suspend fun saveDiagnosis(list: ArrayList<DiagnosisEntity>) =
        metaDataDAO.saveDiagnosis(list)

    override suspend fun deleteDiagnosis() = metaDataDAO.deleteDiagnosis()

    override suspend fun getDiagnosis(gender: ArrayList<String>, type: ArrayList<String>): List<DiagnosisEntity> = metaDataDAO.getDiagnosis(gender, type)
    override suspend fun deleteMHList() = metaDataDAO.deleteMHList()
    override suspend fun saveFrequency(frequencies: List<FrequencyEntity>) =
        metaDataDAO.insertFrequency(frequencies)

    override suspend fun getFrequencyList(): List<FrequencyEntity> = metaDataDAO.getFrequency()

    override suspend fun deleteFrequency() = metaDataDAO.deleteFrequency()

    override suspend fun saveShortageReason(shortageReasonEntityList: ArrayList<ShortageReasonEntity>) {
        metaDataDAO.insertShortageEntries(shortageReasonEntityList)
    }

    override suspend fun getShortageReason(type: String): List<ShortageReasonEntity> {
        return metaDataDAO.getShortageEntries(type)
    }

    override suspend fun deleteShortageReason() {
        return metaDataDAO.deleteShortageReason()
    }

    override suspend fun saveUnitMetric(list: ArrayList<UnitMetricEntity>) {
        metaDataDAO.insertUnitList(list)
    }

    override suspend fun getUnitList(type: String): List<UnitMetricEntity> {
        return metaDataDAO.getUnitList(type)
    }

    override suspend fun deleteUnitList() {
        /**
         * this method is not usd
         */
    }
    override suspend fun saveTreatmentPlan(model: TreatmentPlanEntity) {
        metaDataDAO.saveTreatmentPlan(model)
    }

    override suspend fun getTreatmentPlanData(): List<TreatmentPlanEntity> {
        return metaDataDAO.getTreatmentPlanData()
    }

    override suspend fun deleteTreatmentPlan() {
        metaDataDAO.deleteTreatmentPlan()
    }

    override suspend fun getLifestyleList(): List<NutritionLifeStyle> {
        return metaDataDAO.getLifeStyleList()
    }

    override suspend fun saveNutritionLifeStyle(list: ArrayList<NutritionLifeStyle>) {
        return metaDataDAO.saveNutritionLifeStyle(list = list)
    }

    override suspend fun getDiagnosisList(): List<DiagnosisEntity> {
        return metaDataDAO.getDiagnosisList()
    }

    override suspend fun saveCulturesList(culturesEntity: List<CulturesEntity>) {
        return metaDataDAO.insertCultures(culturesEntity)
    }

    override suspend fun getCulturesList(): List<CulturesEntity> {
        return metaDataDAO.getCulturesList()
    }

    override suspend fun deleteCulturesList() {
        metaDataDAO.deleteCulturesList()
    }
}