package com.medtroniclabs.opensource.formgeneration.formsupport

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.medtroniclabs.opensource.BuildConfig
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.data.model.RiskClassificationModel
import com.medtroniclabs.opensource.data.model.RiskFactorModel
import com.medtroniclabs.opensource.data.ui.BPModel
import com.medtroniclabs.opensource.db.tables.ComorbidityEntity
import com.medtroniclabs.opensource.db.tables.ComplaintsEntity
import com.medtroniclabs.opensource.db.tables.ComplicationEntity
import com.medtroniclabs.opensource.db.tables.CurrentMedicationEntity
import com.medtroniclabs.opensource.db.tables.DiagnosisEntity
import com.medtroniclabs.opensource.db.tables.NutritionLifeStyle
import com.medtroniclabs.opensource.db.tables.PhysicalExaminationEntity
import com.medtroniclabs.opensource.db.tables.ShortageReasonEntity
import com.medtroniclabs.opensource.formgeneration.FormGenerator
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.ui.RoleConstant
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt

object CommonUtils {

    var localeCode = DefinedParams.EN

    fun checkAssessmentCondition(
        systolicAverage: Int? = null,
        diastolicAverage: Int? = null,
        phQ4Score: Int? = null,
        fbs: Double,
        rbs: Double,
        unitGenericType: String
    ): Boolean {
        var status = false
        if ((systolicAverage ?: 0) > DefinedParams.UpperLimitSystolic || (diastolicAverage
                ?: 0) > DefinedParams.UpperLimitDiastolic
        ) {
            status = true
        } else if ((phQ4Score ?: 0) > 4) {
            status = true
        } else if (unitGenericType == DefinedParams.mgdl && (fbs > DefinedParams.FBSMaximumMGDlValue || rbs >= DefinedParams.RBSMaximumMGDlValue)) {
            status = true
        } else if (fbs > DefinedParams.FBSMaximumValue || rbs >= DefinedParams.RBSMaximumValue) {
            status = true
        }
        return status
    }


    fun calculateBMI(map: HashMap<String, Any>, unitGenericType: String) {
        val bmiValue: Double?
        if (map.containsKey(DefinedParams.Weight) &&
            map.containsKey(DefinedParams.Height)
        ) {
            val height: Double
            val weight = fetchValue(map, DefinedParams.Weight)
            if (map[DefinedParams.Height] is Map<*, *>) {
                val heightMap = map[DefinedParams.Height] as Map<*, *>
                val feet = heightMap[DefinedParams.Feet] as Double
                val inches = heightMap[DefinedParams.Inches] as Double
                height = (feet * 12) + inches
            } else {
                height = fetchValue(map, DefinedParams.Height)
            }
            bmiValue = getBMI(unitGenericType, height, weight)
            val formattedValue = String.format(Locale.US, "%.2f", bmiValue).toDouble()
            map[DefinedParams.BMI] = formattedValue
        }
    }

    private fun fetchValue(map: HashMap<String, Any>, params: String): Double {
        return if (map[params] is String)
            (map[params] as String).toDouble()
        else
            map[params] as Double
    }

    private fun getBMI(unitGenericType: String, height: Double, weight: Double): Double {
        return if (unitGenericType == DefinedParams.Unit_Measurement_Metric_Type)
            (((weight / height) / height) * 10000)
        else
            (weight / (height * height)) * 703
    }

    fun processLastMealTime(map: HashMap<String, Any>, isFromValidation: Boolean = false): Long? {
        if (map.containsKey(DefinedParams.lastMealTime) && map[DefinedParams.lastMealTime] is Map<*,*>) {
            val lastMealTimeMap = map[DefinedParams.lastMealTime] as Map<*, *>
            if (lastMealTimeMap.containsKey(DefinedParams.Hour) &&
                lastMealTimeMap.containsKey(DefinedParams.Minute) &&
                lastMealTimeMap.containsKey(DefinedParams.AM_PM)
            ) {
                getCalendar(lastMealTimeMap).let {
                    val mealTime = it.timeInMillis
                    if (isFromValidation)
                        return mealTime
                    else {
                        map.remove(DefinedParams.lastMealTime)
                        map[DefinedParams.lastMealTime] = mealTime
                    }
                }
            } else {
                if (!isFromValidation) {
                    map.remove(DefinedParams.lastMealTime)
                }
            }
        }
        return null
    }

    private fun getCalendar(lastMealTimeMap: Map<*, *>): Calendar {
        val calendar = Calendar.getInstance()
        val hour = lastMealTimeMap[DefinedParams.Hour] as String
        val minute = lastMealTimeMap[DefinedParams.Minute] as String
        val amPm = lastMealTimeMap[DefinedParams.AM_PM] as String

        calculateDay(calendar, lastMealTimeMap)
        val hourInt = hour.toDouble().roundToInt()
        when(amPm.uppercase()) {
            DefinedParams.AM -> calendar.set(Calendar.HOUR_OF_DAY, if(hourInt == 12) 0 else hourInt)
            else -> calendar.set(Calendar.HOUR_OF_DAY, if(hourInt == 12) hourInt else hourInt + 12)
        }
        calendar.set(Calendar.MINUTE, minute.toDouble().roundToInt())
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND))

        return calendar
    }

    private fun calculateDay(cal: Calendar, map: Map<*, *>) {
        if (map.containsKey(DefinedParams.Last_Meal_Date)) {
            val lastMealDate = map[DefinedParams.Last_Meal_Date] as String
            if (lastMealDate.equals(DefinedParams.Yesterday, ignoreCase = true))
                cal.add(Calendar.DAY_OF_MONTH, -1)
        }
    }

    fun calculateCVDRiskFactor(
        map: HashMap<String, Any>,
        list: ArrayList<RiskClassificationModel>,
        avgSystolic: Int?
    ) {
        if (map.containsKey(DefinedParams.BMI) && list.isNotEmpty()) {
            val riskFactor = calculateRiskFactor(
                map,
                list,
                (map[DefinedParams.BMI] as Double),
                avgSystolic
            )

            riskFactor?.let { riskFactorMap ->
                map[DefinedParams.CVD_Risk_Score] =
                    riskFactorMap[DefinedParams.CVD_Risk_Score] as Int
                map[DefinedParams.CVD_Risk_Level] =
                    riskFactorMap[DefinedParams.CVD_Risk_Level] as String
                map[DefinedParams.CVD_Risk_Score_Display] =
                    riskFactorMap[DefinedParams.CVD_Risk_Score_Display] as String
            }
        }
    }

    fun calculateBloodGlucose(map: HashMap<String, Any>, formGenerator: FormGenerator? = null) {
        if (map.containsKey(DefinedParams.BloodGlucoseID)) {
            val bloodGlucoseString = map[DefinedParams.BloodGlucoseID]
            var bloodGlucoseValue: Double? = null
            if (bloodGlucoseString is String) {
                bloodGlucoseValue = bloodGlucoseString.toDoubleOrNull()
            } else if (bloodGlucoseString is Double) {
                bloodGlucoseValue = bloodGlucoseString
            }
            if (bloodGlucoseValue != null) {
                map[DefinedParams.Glucose_Value] = bloodGlucoseValue
                if(map.containsKey(DefinedParams.lastMealTime) && map[DefinedParams.lastMealTime] is Number) {
                    if (checkFBS(map)) {
                        map[DefinedParams.Glucose_Type] = DefinedParams.fbs
                        formGenerator?.setFbsBloodGlucose(bloodGlucoseValue)
                    } else {
                        map[DefinedParams.Glucose_Type] = DefinedParams.rbs
                        formGenerator?.setRbsBloodGlucose(bloodGlucoseValue)
                    }
                }
                else{
                    if(map.containsKey(DefinedParams.Glucose_Type))
                    {
                        when((map[DefinedParams.Glucose_Type] as String).lowercase())
                        {
                            DefinedParams.rbs -> {
                                formGenerator?.setFbsBloodGlucose(bloodGlucoseValue)
                            }
                            DefinedParams.fbs -> {
                                formGenerator?.setRbsBloodGlucose(bloodGlucoseValue)
                            }
                        }

                    }
                }
                if(map[DefinedParams.lastMealTime] is Number)
                    map[DefinedParams.lastMealTime] = DateUtils.getDateString(map[DefinedParams.lastMealTime] as Long)
                map[DefinedParams.Glucose_Date_Time] = DateUtils.getTodayDateDDMMYYYY()
            }
        } else {
            if (map.containsKey(DefinedParams.lastMealTime) && map[DefinedParams.lastMealTime] is Long) {
                map[DefinedParams.lastMealTime] =
                    DateUtils.getDateString(map[DefinedParams.lastMealTime] as Long)
            }
        }
    }

    private fun checkFBS(map: HashMap<String, Any>): Boolean {
        if (map.containsKey(DefinedParams.lastMealTime) && map[DefinedParams.lastMealTime] is Number) {
            val lastMealCalendar = Calendar.getInstance()
            lastMealCalendar.timeInMillis = map[DefinedParams.lastMealTime] as Long
            val calendar = Calendar.getInstance()
            var different: Long = calendar.timeInMillis - lastMealCalendar.timeInMillis
            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val elapsedHours: Long = different / hoursInMilli
            different %= hoursInMilli
            if (elapsedHours >= 8) {
                return true
            }
        }
        return false
    }

    /*
    * With reference to FormSummaryReporter.createBPSummaryView
    * Added for Screening API - Average Blood pressure
    */
    fun calculateAverageBloodPressure(context: Context, resultMap: HashMap<String, Any>) {
        if (resultMap.containsKey(DefinedParams.BPLog_Details)) {
            val actualMapList = resultMap[DefinedParams.BPLog_Details]
            if (actualMapList is ArrayList<*>) {
                var systolic = 0.0
                var diastolic = 0.0
                var enteredBGCount = 0
                actualMapList.forEach { map ->
                    val sys = getSystolicValue(map)
                    val dia = getDiastolicValue(map)
                    if (sys > 0 && dia > 0) {
                        enteredBGCount++
                        systolic += sys
                        diastolic += dia
                    }
                }
                val finalSys = (systolic / enteredBGCount).roundToInt()
                val finalDia = (diastolic / enteredBGCount).roundToInt()
                resultMap[DefinedParams.Avg_Systolic] = finalSys
                resultMap[DefinedParams.Avg_Diastolic] = finalDia
                resultMap[DefinedParams.Avg_Blood_pressure] = ("$finalSys/$finalDia")
            }
        }
    }

    private fun getSystolicValue(map: Any?): Double {
        var returnValue = 0.0
        if (map is Map<*, *> && map.containsKey(DefinedParams.Systolic))
            returnValue = map[DefinedParams.Systolic] as Double
        else if (map is BPModel)
            map.systolic?.let {
                returnValue = it
            }
        return returnValue
    }

    private fun getDiastolicValue(map: Any?): Double {
        var returnValue = 0.0
        if (map is Map<*, *> && map.containsKey(DefinedParams.Diastolic))
            returnValue = map[DefinedParams.Diastolic] as Double
        else if (map is BPModel)
            map.diastolic?.let {
                returnValue = it
            }
        return returnValue
    }


    fun calculateRiskFactor(
        map: Map<String, Any>,
        list: ArrayList<RiskClassificationModel>,
        bmiValue: Double?,
        systolicAverage: Int?
    ): Map<String, Any>? {

        val age: Double? = if (map.containsKey(DefinedParams.Age)) {
            when {
                map.containsKey(DefinedParams.Age) -> {
                    (map[DefinedParams.Age] as Double)
                }
                else -> {
                    null
                }
            }
        } else {
            null
        }

        val gender: String? = if (map.containsKey(DefinedParams.Gender)) {
            val value = map[DefinedParams.Gender] as String
            if (value.equals(DefinedParams.Female, ignoreCase = true))
                value
            else
                DefinedParams.Male
        } else {
            null
        }

        val smoker: Boolean? = if (map.containsKey(DefinedParams.is_regular_smoker)) {
            val tobaccoUsage = map[DefinedParams.is_regular_smoker] as Boolean
            tobaccoUsage
        } else {
            null
        }

        val resultModel = list.filter {
            it.isSmoker == smoker && it.gender.equals(gender, true) && isAgeInLimit(
                age,
                it.age
            )
        }

        if (resultModel.isNotEmpty()) {
            return getRiskBasedOnParams(resultModel[0].riskFactors, bmiValue, systolicAverage)
        }
        return null
    }

    private fun getRiskBasedOnParams(
        riskFactors: ArrayList<RiskFactorModel>,
        bmiValue: Double?,
        systolicAverage: Int?
    ): Map<String, Any>? {
        if (bmiValue == null || systolicAverage == null)
            return null
        riskFactors.forEach { riskFactorModel ->
            if (checkBMIValue(riskFactorModel.bmi, bmiValue) && checkSystolicBPValue(
                    riskFactorModel.sbp,
                    systolicAverage
                )
            ) {
                val resultMap = HashMap<String, Any>()
                resultMap[DefinedParams.CVD_Risk_Score] = riskFactorModel.riskScore
                resultMap[DefinedParams.CVD_Risk_Score_Display] =
                    "${riskFactorModel.riskScore}% - ${riskFactorModel.riskLevel}"
                resultMap[DefinedParams.CVD_Risk_Level] = riskFactorModel.riskLevel
                return resultMap
            }
        }

        return null
    }

    private fun checkSystolicBPValue(sbp: String, systolicAverage: Int): Boolean {
        when {
            sbp.startsWith("<") -> {
                getCheckList(sbp, "<")?.let {
                    val maxvalue = it[1].trim().toInt()
                    return systolicAverage < maxvalue
                }
            }
            sbp.contains("-") -> {
                getCheckList(sbp, "-")?.let {
                    val minValue = it[0].trim().toInt()
                    val maxValue = it[1].trim().toInt()
                    return systolicAverage in minValue..maxValue
                }
            }
            sbp.contains(">=") -> {
                getCheckList(sbp, ">=")?.let {
                    val minValue = it[1].trim().toInt()
                    return systolicAverage >= minValue
                }
            }
        }
        return false
    }

    private fun checkBMIValue(bmi: String, bmiValue: Double): Boolean {
        when {
            bmi.contains(">=") -> {
                getCheckList(bmi, ">=")?.let {
                    val maxvalue = it[1].trim().toDouble()
                    return bmiValue >= maxvalue
                }
            }
            bmi.contains("<=") -> {
                getCheckList(bmi, "<=")?.let {
                    val maxvalue = it[1].trim().toDouble()
                    return bmiValue <= maxvalue
                }
            }
            bmi.startsWith("<") -> {
                getCheckList(bmi, "<")?.let {
                    val maxvalue = it[1].trim().toDouble()
                    return bmiValue < maxvalue
                }
            }
            bmi.contains("-") -> {
                getCheckList(bmi, "-")?.let {
                    val minValue = it[0].trim().toDouble()
                    val maxValue = it[1].trim().toDouble()
                    return bmiValue in minValue..maxValue
                }
            }
            bmi.contains(">") -> {
                getCheckList(bmi, ">")?.let {
                    val minValue = it[1].trim().toDouble()
                    return bmiValue > minValue
                }
            }
        }
        return false
    }

    private fun getCheckList(s: String, character: String): List<String>? {
        s.split(character).let {
            if (it.size > 1)
                return it
        }
        return null
    }

    private fun isAgeInLimit(age: Double?, limit: String): Boolean {
        var status = false
        val limitArray = limit.split("-")
        if (age != null && limitArray.size == 2) {
            val minValue = limitArray[0].toIntOrNull()
            val maxValue = limitArray[1].toIntOrNull()
            if (minValue != null && maxValue != null) {
                status = age >= minValue && age <= maxValue
            }
        }
        return status
    }

    fun calculatePHQScore(
        map: HashMap<String, Any>,
        type: String = DefinedParams.PHQ4
    ): Int {
        val key = mentalHealthKey(type)
        if (map.containsKey(key)) {
            val phqMap = ArrayList<HashMap<String, Any>>()
            var phqScore = 0
            val mentalHealthResultMap = map[key]
            if (mentalHealthResultMap is Map<*, *>) {
                mentalHealthResultMap.keys.forEach { mapKey ->
                    val optionsMap = HashMap<String, Any>()
                    val actualValue = mentalHealthResultMap[mapKey]
                    var optionScore: Int
                    if (actualValue is HashMap<*, *>) {
                        getScores(actualValue).let {
                            phqScore += it.first
                            optionScore = it.second
                        }
                        optionsMap[DefinedParams.Question_Id] =
                            actualValue[DefinedParams.Question_Id] as Long
                        optionsMap[DefinedParams.Answer_Id] =
                            actualValue[DefinedParams.Answer_Id] as Long
                        optionsMap[DefinedParams.Display_Order] =
                            actualValue[DefinedParams.Display_Order] as Long
                        optionsMap[DefinedParams.mentalHealthScore] = optionScore
                        phqMap.add(optionsMap)
                    }
                }
            }
            applyScores(type, map, phqMap, phqScore)
            return phqScore
        }
        return 0
    }

    private fun getScores(actualValue: HashMap<*, *>): Pair<Int, Int> {
        var phqScore = 0
        var optionScore = 0
        when (actualValue[DefinedParams.Answer] as String) {
            DefinedParams.NotAtAll -> {
                phqScore = DefinedParams.ScoreNotAtAll
                optionScore = DefinedParams.ScoreNotAtAll
            }
            DefinedParams.SeveralDays -> {
                phqScore = DefinedParams.ScoreSeveralDays
                optionScore = DefinedParams.ScoreSeveralDays
            }
            DefinedParams.MoreThanHalfADay -> {
                phqScore = DefinedParams.ScoreMoreThanHalfADay
                optionScore = DefinedParams.ScoreMoreThanHalfADay
            }
            DefinedParams.NearlyEveryDay -> {
                phqScore = DefinedParams.ScoreNearlyEveryDay
                optionScore = DefinedParams.ScoreNearlyEveryDay
            }
        }
        return Pair(phqScore, optionScore)
    }

    private fun applyScores(
        type: String,
        map: HashMap<String, Any>,
        phqMap: ArrayList<HashMap<String, Any>>,
        phqScore: Int
    ) {
        when (type) {
            DefinedParams.PHQ9 -> {
                map[DefinedParams.PHQ9_Score] = phqScore
                map[DefinedParams.PHQ9_Risk_Level] = getPhQ4RiskLevel(phqScore)
                map[DefinedParams.PHQ9_Mental_Health] = phqMap
                if (map.containsKey(DefinedParams.PHQ4_Mental_Health))
                    map.remove(DefinedParams.PHQ4_Mental_Health)
            }
            DefinedParams.GAD7 -> {
                map[DefinedParams.GAD7_Score] = phqScore
                map[DefinedParams.GAD7_Risk_Level] = getPhQ4RiskLevel(phqScore)
                map[DefinedParams.GAD7_Mental_Health] = phqMap
                if (map.containsKey(DefinedParams.PHQ4_Mental_Health))
                    map.remove(DefinedParams.PHQ4_Mental_Health)
            }
            DefinedParams.PHQ4 -> {
                map[DefinedParams.PHQ4_Score] = phqScore
                map[DefinedParams.PHQ4_Risk_Level] = getPhQ4RiskLevel(phqScore)
                map[DefinedParams.PHQ4_Mental_Health] = phqMap
            }
        }
    }

    private fun getPhQ4RiskLevel(phq4Score: Int): String {
        return when (phq4Score) {
            3, 4, 5 -> DefinedParams.Mild
            6, 7, 8 -> DefinedParams.Moderate
            0, 1, 2 -> DefinedParams.Normal
            else -> DefinedParams.Severe
        }
    }

    fun calculateProvisionalDiagnosis(
        map: HashMap<String, Any>,
        isConfirmDiagnosis: Boolean? = null,
        avgSystolic: Int? = null,
        avgDiastolic: Int? = null,
        fbsValue: Double = 0.0,
        rbsValue: Double = 0.0,
        unitType: String
    ) {
        if (isConfirmDiagnosis == false) {
            val diagnosisMap = ArrayList<String>()

            checkAvg(avgSystolic, avgDiastolic)?.let {
                diagnosisMap.add(it)
            }

            when (unitType) {
                DefinedParams.mgdl -> {
                    if (fbsValue > DefinedParams.FBSMaximumMGDlValue || rbsValue >= DefinedParams.RBSMaximumMGDlValue) {
                        diagnosisMap.add(DefinedParams.DM_Diagnosis)
                    }
                }
                else -> {
                    if (fbsValue > DefinedParams.FBSMaximumValue || rbsValue >= DefinedParams.RBSMaximumValue) {
                        diagnosisMap.add(DefinedParams.DM_Diagnosis)
                    }
                }
            }
            if (diagnosisMap.size > 0)
                map[DefinedParams.Provisional_Diagnosis] = diagnosisMap
        }
    }

    private fun checkAvg(avgSystolic: Int?, avgDiastolic: Int?): String? {
        if ((avgSystolic ?: 0) > DefinedParams.UpperLimitSystolic || (avgDiastolic
                ?: 0) > DefinedParams.UpperLimitDiastolic
        )
            return DefinedParams.HTN_Diagnosis
        return null
    }

    fun cvdRiskColorCode(score: Double, context: Context): Int {
        return when {
            score < DefinedParams.very_low_risk_limit -> context.getColor(R.color.very_low_risk_color)
            score < DefinedParams.low_risk_limit -> context.getColor(R.color.low_risk_color)
            score < DefinedParams.medium_risk_limit -> context.getColor(R.color.medium_risk_color)
            score < DefinedParams.medium_high_risk_limit -> context.getColor(R.color.medium_high_risk_color)
            else -> context.getColor(R.color.high_risk_color)
        }
    }

    fun getGenderConstant(gender: String?): String {
        if (gender == null)
            return ""
        return when (gender.lowercase()) {
            DefinedParams.Female.lowercase() -> "F"
            DefinedParams.Male.lowercase() -> "M"
            else -> ""
        }
    }

    fun checkIsTablet(context: Context): Boolean {
        val res = context.resources?.getBoolean(R.bool.isTablet)
        return res ?: false
    }

    fun getFilePath(id: String, context: Context): File {
        val cw = ContextWrapper(context)
        val directory = cw.getDir(id, Context.MODE_PRIVATE)
        return File(directory, DefinedParams.SIGN_DIR)
    }

    fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic")
                && Build.DEVICE.startsWith("generic")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator"))
    }

    fun mentalHealthKey(type: String): String {
        var key = DefinedParams.PHQ4_Mental_Health
        if (type == DefinedParams.PHQ9)
            key = DefinedParams.PHQ9_Mental_Health
        else if (type == DefinedParams.GAD7)
            key = DefinedParams.GAD7_Mental_Health
        return key
    }

    fun getMentalHealthScoreWithRisk(response: PatientDetailsModel, type: String): String {
        var details = "-"
        when (type) {
            DefinedParams.PHQ9 -> {
                response.phq9Score?.toString()?.let {
                    response.phq9RiskLevel?.let { risk ->
                        details = "$it, $risk"
                    } ?: kotlin.run {
                        details = "$it"
                    }
                }
            }
            DefinedParams.GAD7 -> {
                response.gad7Score?.let {
                    response.gad7RiskLevel?.let { risk ->
                        details = "$it, $risk"
                    } ?: kotlin.run {
                        details = "$it"
                    }
                }
            }
            else -> {
                response.phq4Score?.let {
                    response.phq4RiskLevel?.let { risk ->
                        details = "$it, $risk"
                    } ?: kotlin.run {
                        details = "$it"
                    }
                }
            }
        }
        return details
    }

    fun calculateGestationalAge(
        menstrualDate: Date?,
        deliveryDate: Date?
    ): String? {

        if (menstrualDate != null) {
            if (deliveryDate != null && deliveryDate.before(Date())) {
                return null
            }
            val timeDifference = abs(Calendar.getInstance().timeInMillis - menstrualDate.time)
            val weeks = (TimeUnit.DAYS.convert(timeDifference, TimeUnit.MILLISECONDS) / 7).toInt()
            return "$weeks ${if (weeks <= 1) "Week" else "Weeks"}"
        }
        return null
    }

    fun getSortHashmap(list: ArrayList<Pair<String, Any>>): LinkedHashMap<String, Any> {
        val map = LinkedHashMap<String, Any>()
        list.forEach { pair ->
            map.put(pair.first, pair.second)
        }
        return map
    }

    fun getMeasurementTypeValues(map: HashMap<String, Any>): String {
        val unitType = map[DefinedParams.BloodGlucoseID + DefinedParams.unitMeasurement_KEY]
        if (unitType is String) {
            return unitType
        }
        return DefinedParams.mmoll
    }

    fun getDecimalFormatted(value: Any?, pattern: String = "###.##"): String {
        var formattedValue = ""
        try {
            value?.let {
                val actualValue = if (it is String) it.toDoubleOrNull() ?: "" else it
                val df = DecimalFormat(pattern, DecimalFormatSymbols(Locale.ENGLISH))
                df.roundingMode = RoundingMode.FLOOR
                if (actualValue is String) {
                    if (actualValue.isNotBlank())
                        formattedValue = df.format(actualValue)
                } else
                    formattedValue = df.format(actualValue)
            }
        } catch (_: Exception) {
            //Exception - Catch block
        }
        return formattedValue
    }

    fun getDecimalFormattedNew(value: Any?, pattern: String = "###.##"): String {
        var formattedValue = ""
        try {
            value?.let {
                val actualValue = if (it is String) it.toDoubleOrNull() ?: "" else it
                val df = DecimalFormat(pattern, DecimalFormatSymbols(Locale.ENGLISH))
//                df.roundingMode = RoundingMode.FLOOR
                if (actualValue is String) {
                    if (actualValue.isNotBlank())
                        formattedValue = df.format(actualValue)
                } else
                    formattedValue = df.format(actualValue)
            }
        } catch (_: Exception) {
            //Exception - Catch block
        }
        return formattedValue
    }

    fun processHeightMap(map: HashMap<String, Any>, unitGenericType: String) {
        if (map[DefinedParams.Height] is Map<*, *> && unitGenericType == DefinedParams.Unit_Measurement_Imperial_Type) {
            val heightMap = map[DefinedParams.Height] as Map<*, *>
            val feet = heightMap[DefinedParams.Feet] as Double
            val inches = heightMap[DefinedParams.Inches] as Double
            map.remove(DefinedParams.Height)
            map[DefinedParams.Height] = (feet * 12) + inches
        }
    }

    fun getGlucoseUnit(glucoseUnit: String?, withoutBracket: Boolean): String {
        return if (glucoseUnit.isNullOrBlank()) {
            ""
        } else if (withoutBracket) {
            "$glucoseUnit"
        } else {
            "(${glucoseUnit})"
        }
    }

    fun updateAccountAndOperatingUnit(map: HashMap<String, Any>) {
        SecuredPreference.getLong(SecuredPreference.EnvironmentKey.ACCOUNT.name).let { account ->
            map[DefinedParams.account] = account
        }
        SecuredPreference.getString(SecuredPreference.EnvironmentKey.OPERATING_UNIT.name)
            ?.let { operatingUnit ->
                map[DefinedParams.operating_unit] = operatingUnit
            }
    }

    fun diagnosisEnableDisable(
        diagnosisMap: HashMap<String, MutableList<String>>,
        chipGroup: ChipGroup,
        selectedChip: String,
        keys: List<String>? = null
    ) {
        var groupKey: String? = null
        diagnosisMap.forEach { (key, value) ->
            val filteredValue = value.find { it == selectedChip }
            if (filteredValue != null) {
                groupKey = key
                return@forEach
            }
        }

        val diagnosisValues = groupKey?.let { diagnosisMap[it] }
        keys?.let {
            if (groupKey in keys && !(diagnosisValues.isNullOrEmpty())) {
                applyChip(diagnosisValues, chipGroup, selectedChip)
            }
        }
    }

    private fun applyChip(
        diagnosisValues: MutableList<String>,
        chipGroup: ChipGroup,
        selectedChip: String
    ) {
        for (j in 0 until chipGroup.childCount) {
            for (i in diagnosisValues.indices) {
                val chip = chipGroup.getChildAt(j)
                val tag = chip.tag
                val actualDiagnosisName: String? = getActualNameOfChip(tag)
                if (chip is Chip && diagnosisValues[i] == actualDiagnosisName && diagnosisValues[i] != selectedChip && chip.isChecked) {
                    chip.isChecked = false
                    break
                }
            }
        }
    }

    fun getActualNameOfChip(tag:Any?): String? {
        var actualDiagnosisName:String ?= null
        tag?.let {data ->
            when (data) {
                is ComorbidityEntity -> {
                    actualDiagnosisName = data.comorbidity
                }
                is ComplicationEntity -> {
                    actualDiagnosisName = data.complication
                }
                is CurrentMedicationEntity -> {
                    actualDiagnosisName = data.medicationName
                }
                is PhysicalExaminationEntity -> {
                    actualDiagnosisName = data.name
                }
                is ComplaintsEntity -> {
                    actualDiagnosisName = data.name
                }
                is NutritionLifeStyle -> {
                    actualDiagnosisName = data.name
                }
                is DiagnosisEntity -> {
                    actualDiagnosisName = data.diagnosis
                }
                is ShortageReasonEntity -> {
                    actualDiagnosisName = data.reason
                }
                is String -> {
                    actualDiagnosisName = data
                }
                else -> {
                    actualDiagnosisName = null
                }
            }
        }

        return actualDiagnosisName
    }

    fun calculateHtnDiabetesDiagnosis(map: HashMap<String, Any>) {

        if (map.containsKey(DefinedParams.HTNPatientType)){
            val htnPatientType = map[DefinedParams.HTNPatientType]
            if (htnPatientType is String) {
                map[DefinedParams.isHTNDiagnosis] = (htnPatientType == DefinedParams.KnownPatient || htnPatientType == DefinedParams.NewPatient)
            }
        }
        if (map.containsKey(DefinedParams.DiabetesPatientType)){
            val diabetesPatientType = map[DefinedParams.DiabetesPatientType]
            if (diabetesPatientType is String) {
                map[DefinedParams.isDiabetesDiagnosis] = (diabetesPatientType == DefinedParams.KnownPatient || diabetesPatientType == DefinedParams.NewPatient)
            }
        }
    }


    fun isNurse(): Boolean {
        return SecuredPreference.getSelectedSiteEntity()?.role == RoleConstant.NURSE
    }

    fun convertType(input: Any?): Long {
        return when (input) {
            is Int -> input.toLong()
            is Double -> input.toLong()
            is Long -> input
            else -> input.toString().toLong()
        }
    }

    fun getFeetFromHeight(heightValue: Double) = heightValue.roundToInt() / 12

    fun getInchesFromHeight(heightValue: Double) = heightValue.roundToInt() % 12

    fun validateFeetAndInches(feet: Double, inches: Double) = (feet <= 1 || feet > 10 || inches > 11)

    fun isGooglePlayServiceInstalled(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        return googleApiAvailability.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    fun formatConsent(consent: String): String {
        return consent.replace("\\\"", "\"").replace("contenteditable=\"true\"", "")
    }

    fun parseUserLocale(): String {
        val preference = SecuredPreference.getCultureName()
        return when {
            preference.contains(DefinedParams.EN_Locale, ignoreCase = true) ->
                DefinedParams.EN
            preference.contains(DefinedParams.BN_Locale, ignoreCase = true) ->
                DefinedParams.BN
            else -> DefinedParams.EN
        }
    }
    fun checkIfTranslationEnabled(name: String): Boolean {
        return name.contains(DefinedParams.BN_Locale, ignoreCase = true)
    }

    fun isProduction(): Boolean {
        return BuildConfig.FLAVOR == DefinedParams.Build_Type_Production || BuildConfig.FLAVOR == DefinedParams.Build_Type_BD_Production
    }

    fun capitalize(str: String): String {
        val words = str.lowercase().split(" ")
        val sb = StringBuilder()
        words.forEach {
            if (it != "") {
                sb.append(it[0].uppercase()).append(it.substring(1))
            }
            sb.append(" ")
        }
        return sb.toString().trim { it <= ' ' }
    }
}