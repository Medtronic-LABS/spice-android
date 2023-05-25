package com.medtroniclabs.opensource.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.opensource.data.model.UnitMetricEntity
import com.medtroniclabs.opensource.db.tables.*

@Dao
interface MetaDataDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormEntries(formEntity: FormEntity)

    @Query("SELECT * FROM FormEntity where formType=:formType AND userId=:userId")
    suspend fun getFormBasedOnType(formType: String, userId: Long): FormEntity

    @Query("SELECT * FROM FormEntity where formType=:formTypeOne OR formType=:formTypeTwo AND userId=:userId")
    suspend fun getFormBasedOnType(
        formTypeOne: String,
        formTypeTwo: String,
        userId: Long
    ): List<FormEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsentFormHTMLRaw(consentEntity: ConsentEntity)

    @Query("SELECT * FROM ConsentEntity where formType=:formType AND userId=:userId")
    suspend fun getConsentHTMLRawString(formType: String, userId: Long): ConsentEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiteDetails(siteEntity: List<SiteEntity>)

    @Query("SELECT * FROM SiteEntity Where userSite=:userSite AND userId=:userId")
    suspend fun getSiteEntityList(userSite: Boolean, userId: Long): List<SiteEntity>

    @Query("SELECT * FROM SiteEntity Where userId=:userId")
    suspend fun getAccountSiteList(userId: Long): List<SiteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenus(menuEntity: List<MenuEntity>)

    @Query("DELETE FROM MenuEntity")
    suspend fun deleteAllMenu()

    @Query("SELECT * FROM MenuEntity Where role=:role AND userId=:userId")
    suspend fun getMenuListByRole(role: String, userId: Long): List<MenuEntity>

    @Query("DELETE FROM SiteEntity")
    suspend fun deleteSiteList()

    @Query("DELETE FROM ConsentEntity")
    suspend fun deleteConsentEntry()

    @Query("DELETE FROM FormEntity")
    suspend fun deleteFormEntity()

    @Query("DELETE FROM RiskFactorEntity")
    suspend fun deleteRiskFactor()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountry(countyEntity: List<CountryEntity>)

    @Query("SELECT * FROM Country")
    suspend fun getCountryList(): List<CountryEntity>

    @Query("DELETE FROM Country")
    suspend fun deleteCountryList()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounty(countyEntity: List<CountyEntity>)

    @Query("SELECT * FROM County")
    suspend fun getCountyList(): List<CountyEntity>


    @Query("DELETE FROM County")
    suspend fun deleteCountyList()

    @Query("SELECT * FROM County where country =:selectedParent")
    suspend fun getCountyList(selectedParent: Long): List<CountyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCounty(subCountyEntity: List<SubCountyEntity>)

    @Query("SELECT * FROM SubCounty")
    suspend fun getSubCountyList(): List<SubCountyEntity>

    @Query("SELECT * FROM SubCounty where county=:selectedParent")
    suspend fun getSubCountyList(selectedParent: Long): List<SubCountyEntity>

    @Query("DELETE FROM SubCounty")
    suspend fun deleteSubCountyList()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptoms(symptomEntity: List<SymptomEntity>)

    @Query("DELETE FROM SymptomEntity")
    suspend fun deleteSymptomList()

    @Query("SELECT * FROM SymptomEntity ORDER BY display_order")
    suspend fun getSymptomList(): List<SymptomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalCompliance(list: List<MedicalComplianceEntity>)

    @Query("DELETE FROM MedicalComplianceEntity")
    suspend fun deleteMedicalComplianceList()

    @Query("SELECT * FROM MedicalComplianceEntity where parent_compliance_id IS NULL OR parent_compliance_id = '' ORDER BY display_order")
    suspend fun getMedicalComplianceList(): List<MedicalComplianceEntity>

    @Query("SELECT * FROM MedicalComplianceEntity where parent_compliance_id =:parentId ORDER BY display_order")
    suspend fun getMedicalComplianceList(parentId: Long): List<MedicalComplianceEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMHList(list: List<MentalHealthEntity>)

    @Query("SELECT * FROM MentalHealthEntity where formType=:type")
    suspend fun getMHQuestionsByType(type: String): MentalHealthEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(programEntity: List<ProgramEntity>)

    @Query("SELECT * FROM ProgramEntity where sites GLOB '*[^a-zA-Z0-9]'||:site||'[^a-zA-Z0-9]*'")
    suspend fun getProgramList(site: String): List<ProgramEntity>

    @Query("SELECT * FROM ProgramEntity where _id = :selectedParent and sites GLOB '*[^a-zA-Z0-9]'||:site||'[^a-zA-Z0-9]*'")
    suspend fun getProgramList(selectedParent: Long, site: String): List<ProgramEntity>

    @Query("DELETE FROM ProgramEntity")
    suspend fun deleteProgramList()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveComorbidity(list: ArrayList<ComorbidityEntity>)

    @Query("DELETE FROM comorbidity")
    suspend fun deleteComorbidity()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCurrentMedication(list: ArrayList<CurrentMedicationEntity>)

    @Query("DELETE FROM current_medication")
    suspend fun deleteCurrentMedication()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveComplication(list: ArrayList<ComplicationEntity>)

    @Query("DELETE FROM complication")
    suspend fun deleteComplication()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePhysicalExamination(list: ArrayList<PhysicalExaminationEntity>)

    @Query("DELETE FROM physical_examination")
    suspend fun deletePhysicalExamination()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLifeStyle(list: ArrayList<LifestyleEntity>)

    @Query("DELETE FROM lifestyle")
    suspend fun deleteLifeStyle()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveComplaints(list: ArrayList<ComplaintsEntity>)

    @Query("DELETE FROM complaints")
    suspend fun deleteComplaints()

    @Query("SELECT * FROM comorbidity ORDER BY display_order ASC")
    suspend fun getComorbidity(): List<ComorbidityEntity>

    @Query("SELECT * FROM complication ORDER BY display_order ASC")
    suspend fun getComplication(): List<ComplicationEntity>

    @Query("SELECT * FROM lifestyle ORDER BY display_order ASC")
    suspend fun getLifeStyle(): List<LifestyleEntity>

    @Query("SELECT * FROM NutritionLifeStyle ORDER BY display_order ASC")
    suspend fun getLifeStyleList(): List<NutritionLifeStyle>

    @Query("SELECT * FROM current_medication ORDER BY display_order ASC")
    suspend fun getCurrentMedicationList(): List<CurrentMedicationEntity>

    @Query("SELECT * FROM physical_examination ORDER BY display_order ASC")
    suspend fun getPhysicalExaminationList(): List<PhysicalExaminationEntity>

    @Query("SELECT * FROM complaints ORDER BY display_order ASC")
    suspend fun getChiefComplaints(): List<ComplaintsEntity>

    @Query("SELECT * FROM current_medication where type =:type or type = 'Other' ORDER BY display_order ASC")
    suspend fun getCurrentMedicationList(type: String): List<CurrentMedicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDiagnosis(list: ArrayList<DiagnosisEntity>)

    @Query("DELETE FROM diagnosis")
    suspend fun deleteDiagnosis()

    @Query("SELECT * FROM diagnosis where (UPPER(gender) IN (:gender) OR UPPER(diagnosis) = 'OTHER') AND (UPPER(type) NOT IN (:type)) ORDER BY display_order ASC")
    suspend fun getDiagnosis(
        gender: ArrayList<String>,
        type: ArrayList<String>
    ): List<DiagnosisEntity>

    @Query("DELETE FROM MentalHealthEntity")
    suspend fun deleteMHList()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFrequency(frequencyEntity: List<FrequencyEntity>)

    @Query("SELECT * FROM frequency ORDER BY display_order")
    suspend fun getFrequency(): List<FrequencyEntity>

    @Query("DELETE FROM frequency")
    suspend fun deleteFrequency()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortageEntries(shortageReasonEntityList: ArrayList<ShortageReasonEntity>)

    @Query("SELECT * FROM shortage_reason where type=:type")
    suspend fun getShortageEntries(type: String): List<ShortageReasonEntity>

    @Query("DELETE FROM shortage_reason")
    suspend fun deleteShortageReason()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitList(list: ArrayList<UnitMetricEntity>)

    @Query("SELECT * FROM unit_metric_entity where type=:type")
    suspend fun getUnitList(type: String): List<UnitMetricEntity>

    @Query("DELETE FROM unit_metric_entity")
    suspend fun deleteUnitMetric()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTreatmentPlan(model: TreatmentPlanEntity)

    @Query("SELECT * FROM TreatmentPlanEntity")
    suspend fun getTreatmentPlanData(): List<TreatmentPlanEntity>

    @Query("DELETE FROM TreatmentPlanEntity")
    suspend fun deleteTreatmentPlan()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNutritionLifeStyle(list: ArrayList<NutritionLifeStyle>)

    @Query("SELECT * FROM diagnosis")
    suspend fun getDiagnosisList(): List<DiagnosisEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCultures(culturesEntity: List<CulturesEntity>)

    @Query("SELECT * FROM Cultures")
    suspend fun getCulturesList(): List<CulturesEntity>

    @Query("DELETE FROM Cultures")
    suspend fun deleteCulturesList()
}