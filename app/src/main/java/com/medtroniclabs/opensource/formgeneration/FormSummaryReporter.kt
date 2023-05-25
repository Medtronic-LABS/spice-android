package com.medtroniclabs.opensource.formgeneration

import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.databinding.CardLayoutBinding
import com.medtroniclabs.opensource.databinding.SummaryLayoutBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_FORM_AGE
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_FORM_BP
import com.medtroniclabs.opensource.formgeneration.model.FormLayout
import com.medtroniclabs.opensource.ui.FormResultGenerator
import kotlin.math.roundToInt

class FormSummaryReporter(
    var context: Context,
    private val parentLayout: LinearLayout,
) : ContextWrapper(context) {

    private val rootSuffix = "rootView"
    private val resultRootSuffix = "resultRootView"
    private val bioDataRootSuffix = "bioRootView"
    private var systolicAverageSummary: Int? = null
    private var diastolicAverageSummary: Int? = null
    private var phq4Score: Int? = null
    private var cvdRisk: String? = null
    private var fbsBloodGlucose: Double? = null
    private var rbsBloodGlucose: Double? = null

    fun populateSummary(serverData: List<FormLayout>, resultMap: Map<String, Any>,translate: Boolean = false) {
        parentLayout.removeAllViews()
        parentLayout.addView(initialCardLayout(getString(R.string.bio_data), bioDataRootSuffix))
        parentLayout.addView(initialCardLayout(getString(R.string.result), resultRootSuffix))
        serverData.forEach { formLayout ->
            formLayout.apply {
                isSummary?.let {
                    if(it)
                    {
                        when (viewType) {
                            VIEW_TYPE_FORM_AGE -> createAgeSummaryView(serverData, formLayout, resultMap,translate)
                            VIEW_TYPE_FORM_BP -> createBPSummaryView(serverData, formLayout, resultMap,translate)
                            else -> createSummaryView(formLayout, resultMap,translate)
                        }
                    }
                }
            }
        }
    }


    private fun getFamilyView(family: String?): LinearLayout? {
        family ?: return null
        return parentLayout.findViewWithTag(family)
    }

    private fun createBPSummaryView(
        serverData: List<FormLayout>,
        formLayout: FormLayout,
        resultMap: Map<String, Any>,
        translate: Boolean
    ) {
        formLayout.apply {
            calculateAverageBloodPressure(serverData, id, resultMap)
            val binding = SummaryLayoutBinding.inflate(LayoutInflater.from(context))
            binding.root.tag = id + rootSuffix
            binding.tvKey.text = getString(R.string.average_blood_pressure)
            binding.tvValue.text =
                getString(R.string.average_mmhg_string, systolicAverageSummary.toString(), diastolicAverageSummary.toString())
            parentLayout.findViewWithTag<LinearLayout>(getFormResultView())
                ?.addView(binding.root)
        }
    }

    private fun calculateAverageBloodPressure(serverData: List<FormLayout>, id: String, resultMap: Map<String, Any>) {
        var systolic = 0.0
        var diastolic = 0.0
        var enteredCount = 0
        systolicAverageSummary = systolic.toInt()
        diastolicAverageSummary = diastolic.toInt()
        FormResultGenerator.findGroupId(serverData, id)?.let {
            val subMap = resultMap[it] as Map<String, Any>
            if (subMap.containsKey(id)) {
                val actualMapList = subMap[id]
                if (actualMapList is ArrayList<*>) {
                    actualMapList.forEach { map ->
                        val sys = getMapValue(map, DefinedParams.Systolic)
                        val dia = getMapValue(map, DefinedParams.Diastolic)
                        if (sys > 0 && dia > 0) {
                            enteredCount++
                            systolic += sys
                            diastolic += dia
                        }
                    }
                    systolicAverageSummary = (systolic / enteredCount).roundToInt()
                    diastolicAverageSummary = (diastolic / enteredCount).roundToInt()
                }
            }
        }
    }

    private fun getMapValue(map: Any?, value: String): Double {
        var d = 0.0
        map?.let {
            if (it is Map<*, *> && it.containsKey(value))
                d = it[value] as Double
        }
        return d
    }

    private fun createAgeSummaryView(
        serverData: List<FormLayout>,
        formLayout: FormLayout,
        resultMap: Map<String, Any>,
        translate: Boolean
    ) {
        formLayout.apply {
            FormResultGenerator.findGroupId(serverData, id)?.let {
                val subMap = resultMap[it] as Map<String, Any>
                if (subMap.containsKey(id)) {
                    val actualValue = subMap[id]
                    if (actualValue is Map<*, *> && actualValue.containsKey(DefinedParams.Age)) {
                        val ageInYears = actualValue[DefinedParams.Age]
                        if (ageInYears is Number) {
                            val binding =
                                SummaryLayoutBinding.inflate(LayoutInflater.from(context))
                            binding.tvKey.text = getString(R.string.age)
                            binding.tvValue.text = ageInYears.toInt().toString()
                            getFamilyView(bioDataRootSuffix)
                                ?.addView(binding.root)
                        }
                    }
                }
            }
        }
    }

    private fun initialCardLayout(title: String, tag: String): View {
        val binding = CardLayoutBinding.inflate(LayoutInflater.from(context))
        binding.llFamilyRoot.tag = tag
        binding.cardTitle.text = title
        return binding.root
    }


    private fun createSummaryView(
        formLayout: FormLayout,
        resultMap: Map<String, Any>,
        translate: Boolean
    ) {
        formLayout.apply {
            if (resultMap.containsKey(family)) {
                val subMap = resultMap[family] as Map<String, Any>
                if (subMap.containsKey(id)) {
                    val binding = SummaryLayoutBinding.inflate(LayoutInflater.from(context))
                    binding.root.tag = id + rootSuffix
                    if (translate){
                        binding.tvKey.text = titleCulture?:title
                    }else{
                        binding.tvKey.text = title
                    }
                    binding.tvValue.text = getActualValue(subMap[id], optionsList)
                    getFamilyView(bioDataRootSuffix)?.addView(binding.root)
                }
            }
        }
    }

    private fun getActualValue(value: Any?, optionsList: ArrayList<Map<String, Any>>?): String {
        if (optionsList != null) {
            optionsList.let { list ->
                list.forEach { map ->
                    getActualName(map, value)?.let {
                        return it
                    }
                }
            }
        } else {
            when (value) {
                is String -> return value
                is Map<*, *> -> {
                    getActual(value)?.let {
                        return it
                    }
                }
                is ArrayList<*> -> {
                    value.forEach { map ->
                        getListActual(map)?.let {
                            return it
                        }
                    }
                }
                else -> {
                    //Else block execution
                }
            }
        }
        return ""
    }

    private fun getListActual(map: Any?): String? {
        if (map is Map<*, *> && map.containsKey(DefinedParams.NAME)) {
            val actual = map[DefinedParams.NAME]
            if (actual is String)
                return actual
        }
        return null
    }

    private fun getActual(value: Map<*, *>): String? {
        if (value.containsKey(DefinedParams.NAME)) {
            val actual = value[DefinedParams.NAME]
            if (actual is String) {
                return actual
            }
        }
        return null
    }

    private fun getActualName(map: Map<String, Any>, value: Any?): String? {
        if (map.containsKey(DefinedParams.id)) {
            val id = map[DefinedParams.id]
            id?.let {
                if (it == value)
                    return map[DefinedParams.NAME] as String
            }
        }
        return null
    }

    fun getFormResultView(): String {
        return resultRootSuffix
    }

    fun getSystolicAverage(): Int? {
        return systolicAverageSummary
    }

    fun getDiastolicAverage(): Int? {
        return diastolicAverageSummary
    }

    fun setPHQ4Score(phq4Score: Int) {
        this.phq4Score = phq4Score
    }

    fun getPHQ4Score(): Int? {
        return phq4Score
    }

    fun setFBSBloodGlucose(glucose: Double) {
        fbsBloodGlucose = glucose
    }

    fun setRBSBloodGlucose(glucose: Double) {
        rbsBloodGlucose = glucose
    }

    fun getFBSBloodGlucose(): Double {
        return fbsBloodGlucose ?: 0.0
    }

    fun getRBSBloodGlucose(): Double {
        return rbsBloodGlucose ?: 0.0
    }
}