package com.medtroniclabs.opensource.formgeneration.definedproperties

import com.medtroniclabs.opensource.data.ui.BPModel

object DefinedParams {

    const val PositiveValue: Boolean = true
    const val NegativeValue:Boolean = false

    // visibility related values
    const val VISIBLE = "visible"
    const val INVISIBLE = "invisible"
    const val GONE = "gone"
    const val VISIBILITY = "visibility"
    const val isEnabled = "isEnabled"

    // radio group option field
    const val NAME = "name"
    const val SSP16 = 16
    const val SSP14 = 14

    const val ACTION_FORM_SUBMISSION = "formSubmission"

    // age view type related
    const val DateOfBirth = "dateOfBirth"

    // BP view type related
    const val Systolic = "systolic"
    const val Diastolic = "diastolic"
    const val Pulse = "pulse"

    const val ID = "id"

    const val Workflow = "workflow"

    // Risk Calculation factors
    const val Height = "height"
    const val Weight = "weight"
    const val Gender = "gender"
    const val Age = "age"
    const val Yes = "yes"
    const val No = "no"
    const val Hour = "hour"
    const val Minute = "minute"
    const val AM_PM = "am/pm"

    fun getEmptyBPReading(size: Int): ArrayList<BPModel> {
        val list = ArrayList<BPModel>()
        for (i in 1..size) {
            list.add(BPModel())
        }
        return list
    }

    const val Category = "category"
    const val CategoryType = "category_type"
    const val Type = "type"
    const val TenantId = "tenantId"

    const val isMandatory = "isMandatory"
    const val Mandatory = "mandatory"
    const val patient_pregnancy_id = "patientPregnancyId"
    const val id = "id"
    const val diagnosis = "diagnosis"
    const val is_on_treatment = "isOnTreatment"
    const val gravida = "gravida"
    const val parity = "parity"
    const val last_menstrual_period_date = "lastMenstrualPeriodDate"
    const val estimated_delivery_date = "estimatedDeliveryDate"
    const val pregnancy_fetuses_number = "pregnancyFetusesNumber"
    const val diagnosis_time = "diagnosisTime"

    // MentalHealthFields
    const val NotAtAll = "Not At All"
    const val SeveralDays = "Several Days"
    const val MoreThanHalfADay = "More than half the days"
    const val NearlyEveryDay = "Nearly Every Day"
    const val ScreeningEntityId = "ScreeningEntityId"
    const val ScoreNotAtAll = 0
    const val ScoreSeveralDays = 1
    const val ScoreMoreThanHalfADay = 2
    const val ScoreNearlyEveryDay = 3

    const val UpperLimitSystolic = 140
    const val UpperLimitDiastolic = 90
    const val MaxHourLimit = 12
    const val MaxMinutesLimit = 59
    const val DialogWidth = 720f

    const val BloodGlucoseID = "glucose"
    const val lastMealTime = "lastMealTime"
    const val BMI = "bmi"
    const val FBS = "FBS"
    const val RBS = "RBS"
    const val RBS_FBS = "RBS & FBS"
    const val rbs = "rbs"
    const val fbs = "fbs"
    const val PHQ4_Score = "phq4Score"
    const val PHQ4_Risk_Level = "phq4RiskLevel"
    const val PHQ9_Score = "phq9Score"
    const val PHQ9_Risk_Level = "phq9RiskLevel"
    const val GAD7_Score = "gad7Score"
    const val GAD7_Risk_Level = "gad7RiskLevel"
    const val PHQ9_Result = "phq9_result"
    const val AM = "AM"
    const val PM = "PM"
    const val ReferAssessment = "isReferAssessment"
    const val Hemoglobin = "hba1c"

    const val Latitude = "latitude"
    const val Longitude = "longitude"
    const val DBKEY = "%42#94$5K"
    const val Site_Id = "site_id"
    const val SiteId = "siteId"
    const val First_Name = "firstName"
    const val Last_Name = "lastName"
    const val Middle_Name = "middleName"
    const val Phone_Number = "phoneNumber"
    const val is_regular_smoker = "isRegularSmoker"
    const val Question_Id = "questionId"
    const val Answer_Id = "answerId"
    const val PHQ4_Result = "phq4_result"
    const val Glucose_Type = "glucoseType"
    const val Glucose_Value = "glucoseValue"
    const val CVD_Risk_Score = "cvdRiskScore"
    const val CVD_Risk_Level = "cvdRiskLevel"
    const val CVD_Risk_Score_Display = "cvdRiskScoreDisplay"
    const val Avg_Blood_pressure = "avgBloodPressure"
    const val Last_Meal_Date = "last_meal_date"
    const val Today = "Today"
    const val Yesterday = "Yesterday"
    const val National_Id = "nationalId"
    const val Patient_Id = "patientId"
    const val DoorToDoor = "Door to Door"
    const val outpatient = "Outpatient"
    const val inpatient = "Inpatient"
    const val Other = "other"
    const val OPDTriage = "OPD"
    const val Camp = "camp"
    const val Pharmacy = "pharmacy"
    val Normal: String = "Normal"
    val Mild: String = "Mild"
    val Moderate = "Moderate"
    val Severe = "Severe"
    const val Device_Info_Id = "deviceInfoId"
    const val Avg_Systolic = "avgSystolic"
    const val Avg_Diastolic = "avgDiastolic"
    const val Avg_Pulse = "avg_pulse"
    const val BPLog_Details = "bpLogDetails"
    const val Glucose_Date_Time = "glucoseDateTime"
    const val Is_Regular_Smoker = "isRegularSmoker"
    const val PHQ4_Mental_Health = "phq4MentalHealth"
    const val PHQ9_Mental_Health = "phq9MentalHealth"
    const val GAD7_Mental_Health = "gad7MentalHealth"
    const val Health_Text = "health_text"
    const val PHQ4 = "PHQ4"
    const val PHQ9 = "PHQ9"
    const val GAD7 = "GAD7"
    const val DOB_Details = "dob_details"
    const val Temperature = "temperature"
    const val Bio_Metrics = "bioMetrics"
    const val Patient_Visit_Id = "patientVisitId"
    const val btnSubmit = "btnSubmit"

    const val BPAverageMinimumValue = 50.0
    const val BPAverageMaximumValue = 300.0
    const val PulseMinimumValue = 50.0
    const val PulseMaximumValue = 300.0
    const val FBSMaximumValue = 6.1
    const val RBSMaximumValue = 7.8
    const val FBSMaximumMGDlValue = 110
    const val RBSMaximumMGDlValue = 140

    const val country = "country"
    const val subCounty = "subCounty"
    const val county = "county"
    const val program = "programId"
    const val Provisional_Diagnosis = "provisionalDiagnosis"
    const val HTN_Diagnosis = "HTN"
    const val DM_Diagnosis = "DM"


    const val compliance_id: String = "complianceId"
    const val is_child_exists: String = "isChildExist"
    const val other_compliance: String = "otherCompliance"
    const val compliances = "compliances"
    const val Compliance_Type_Diabetes = "Diabetes"
    const val Compliance_Type_Hypertension = "Hypertension"
    const val Compliance_Type_Other = "Other"
    const val symptoms = "symptoms"
    const val symptom_id = "symptomId"
    const val other_symptom = "otherSymptom"
    const val type = "type"
    const val Fetch_MH_Questions = "Fetch_MH_Questions"
    const val Result_Name = "resultName"
    const val Result_Value = "resultValue"
    const val Unit = "unit"
    const val Display_Order = "displayOrder"
    const val Lab_Test = "labTest"
    const val Lab_Result_Range = "labTestResultRanges"
    const val Is_Empty_Ranges = "isEmptyRanges"
    const val Minimum_Value = "minimumValue"
    const val Maximum_Value = "maximumValue"

    // CVD Risks
    const val very_low_risk_limit = 5
    const val low_risk_limit = 10
    const val medium_risk_limit = 20
    const val medium_high_risk_limit = 30

    const val LIFE_STYLE_SMOKE = "SMOKE"
    const val LIFE_STYLE_ALCOHOL = "ALCOHOL"
    const val LIFE_STYLE_NUT = "NUT"

    const val N_A = "N/A"
    const val NEW = "New Patient"
    const val KNOWN = "Known Patient"

    const val Female = "Female"
    const val Male = "Male"

    // Add new reading
    const val bp_log = "bpLog"
    const val GlucoseLog = "glucoseLog"
    const val glucose_log = "glucose_log"

    const val MaximumAge = 130
    const val MinimumAge = 1

    const val SIGN_SUFFIX = "_signature"
    const val SIGN_DIR = "sign"

    const val Tablet = "Tablet"
    const val Liquid_Oral = "Liquid"
    const val Injection_Injectable_Solution = "Injection"
    const val Capsule = "Capsule"

    const val span_count_1 = 1
    const val span_count_2 = 2
    const val span_count_3 = 3
    const val Initial = "initial"

    const val TYPE_REFILL = "REFILL"
    const val DESCRIPTION = "description"
    const val PRESCRIPTION = "PRESCRIPTION"
    const val Is_Provisional = "isProvisional"

    const val Origin = "Origin"
    const val ENROLLED = "ENROLLED"
    const val Patient_LabTest_Results = "patientLabTestResults"
    const val Patient_LabTest_Id = "patientLabTestId"
    const val Tested_On = "testedOn"
    const val Comment = "comment"
    const val Display_Name = "displayName"
    const val Result_Status = "resultStatus"
    const val Is_Abnormal = "isAbnormal"
    const val Result_Positive = "POSITIVE"
    const val Result_Negative = "NEGATIVE"
    const val LABTEST = "LABTEST"
    const val RoleName = "roleName"
    const val is_latest_required = "isLatestRequired"
    const val message = "message"
    const val Is_Reviewed = "isReviewed"
    const val BP_Check_Freq = "bpCheckFrequency"
    const val Medical_Review_Freq = "medicalReviewFrequency"
    const val BG_Check_Freq = "bgCheckFrequency"

    const val mentalHealthScore = "score"

    const val Red_Risk_Low = "Low"
    const val Red_Risk_Moderate = "Moderate"
    const val Red_Risk_High = "High"

    const val PatientTrackId = "patientTrackId"
    const val Screening_Id = "screening_id"
    const val PageLimit = 15
    const val is_confirm_diagnosis = "isConfirmDiagnosis"

    const val Is_Result_Value_Valid = "isResultValueValid"
    const val Is_Unit_Valid = "isUnitValid"
    const val UnitMeasurement = "unitMeasurement"
    const val Unit_Measurement = "unit_measurement"

    const val Unit_Measurement_Metric_Type = "metric"
    const val Unit_Measurement_Imperial_Type = "imperial"

    const val Build_Type_Production = "production"
    const val Build_Type_BD_Production = "bangladeshProd"
    const val bp_log_id = "bpLogId"
    const val GlucoseId = "glucoseId"
    const val GlucoseLogId = "glucoseLogId"

    const val Feet = "feet"
    const val Inches = "inches"
    const val unitMeasurement_KEY = "Unit"
    const val mmoll = "mmol/L"
    const val mgdl = "mg/dL"
    const val glucose_unit = "glucoseUnit"

    const val operating_unit = "operating_unit"
    const val account = "account"

    const val TagPrivacyPolicy = "privacy"
    const val TagHomeScreen = "home_screen"

    const val Diabetes = "Diabetes"
    const val Hypertension = "Hypertension"
    const val Pre_Hypertension = "Pre-Hypertension"
    const val Customized_Workflows = "customizedWorkflows"
    const val Screening_Date_Time = "screeningDateTime"

    const val fromMedicalReview = "fromMedicalReview"
    const val HTNPatientType = "htnPatientType"
    const val DiabetesPatientType = "diabetesPatientType"
    const val NewPatient = "New Patient"
    const val KnownPatient = "Known Patient"
    const val isHTNDiagnosis = "isHtnDiagnosis"
    const val isDiabetesDiagnosis = "isDiabetesDiagnosis"
    const val diabetesYearOfDiagnosis = "diabetesYearOfDiagnosis"
    const val htnYearOfDiagnosis = "htnYearOfDiagnosis"
    const val diagnosisDiabetes = "diagnosisDiabetes"
    const val diagnosisHypertension = "diagnosisHypertension"
    const val htnDiagnosis = "htnDiagnosis"
    const val diabetesDiagControlledType = "diabetesDiagControlledType"
    const val diabetesDiagnosis = "diabetesDiagnosis"
    const val PreDiabetic = "Pre-Diabetes"
    const val typeOne = "Type 1"
    const val typeTwo = "Type 2"
    const val Message = "message"
    const val dmtOne = "Diabetes Mellitus Type 1"
    const val dmtTwo = "Diabetes Mellitus Type 2"

    const val BPTakenOn = "bpTakenOn"
    const val BGTakenOn = "bgTakenOn"
    const val BloodGlucoseUnit = "glucoseUnit"
    const val HBA1CUnit = "hba1cUnit"
    const val GestationalDiabetes = "Gestational Diabetes(GDM)"
    const val Authorization = "Authorization"
    const val Username = "username"
    const val Password = "password"
    const val Entity = "entity"
    const val Questions = "questions"
    const val Answer = "answer"
    const val ModelAnswers = "modelAnswers"
    const val TransferPatient = "transfer_patient"
    const val TYPE_TRANSFER = "TRANSFER"
    const val TYPE_DELETE = "PATIENT_DELETE"
    const val cultureValue = "cultureValue"

    const val TranslationEnabled = false
    const val InsuranceStatus = "insuranceStatus"
    const val InsuranceType = "insuranceType"
    const val OtherInsurance = "otherInsurance"

    // To Add the language name
    const val EN_Locale = "English"
    const val BN_Locale = "Bengali"
    const val Swahili_Locale = "Swahili"

    // To Add the language code
    const val EN = "en"
    const val BN = "bn"
    const val SW = "sw"
}