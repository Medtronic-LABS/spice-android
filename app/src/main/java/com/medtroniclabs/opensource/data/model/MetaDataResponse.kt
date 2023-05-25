package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName
import com.medtroniclabs.opensource.data.account.workflow.AccountWorkflow
import com.medtroniclabs.opensource.db.tables.*

data class MetaDataResponse(
    val sites: ArrayList<SiteResponse>,
    val screening: FormResponse,
    val enrollment: FormResponse,
    val assessment: FormResponse,
    val operatingSites: ArrayList<SiteResponse>,
    val menus: ArrayList<ActivityObject>,
    @SerializedName("cvdRiskAlgorithms")
    val cvdRiskAlgorithm: RiskFactorResponse,
    val defaultSite: SiteResponse,
    var countries: ArrayList<CountryEntity>,
    var counties: ArrayList<CountyEntity>,
    @SerializedName("subcounties")
    var subCounties: ArrayList<SubCountyEntity>,
    var mentalHealth: ArrayList<MentalHealthModel>,
    var programs: ArrayList<ProgramEntity>?,
    var symptoms: ArrayList<SymptomEntity>,
    var medicalCompliances: ArrayList<MedicalComplianceEntity>,
    val diagnosis: ArrayList<DiagnosisEntity>,
    val clinicalWorkflow: WorkflowModel,
    var dosageFrequency: ArrayList<FrequencyEntity>?=null,
    @SerializedName("reasons")
    var reason: ArrayList<ShortageReasonEntity>,
    var units: ArrayList<UnitMetricEntity>,
    @SerializedName("operatingUnitId")
    var operatingUnit: Long,
    @SerializedName("accountId")
    var account: Long,
    val nutritionLifestyle: ArrayList<NutritionLifeStyle>? = null,
    @SerializedName("customizedWorkflow")
    val customizedWorkflows: ArrayList<AccountWorkflow>? = null,
    val cultures: ArrayList<CulturesEntity>? = null
)