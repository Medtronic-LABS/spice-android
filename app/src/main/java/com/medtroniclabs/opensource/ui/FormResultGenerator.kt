package com.medtroniclabs.opensource.ui

import android.content.Context
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType
import com.medtroniclabs.opensource.formgeneration.model.FormLayout

class FormResultGenerator {

    private var groupedResultMap: HashMap<String, Any> = HashMap()

    fun groupValues(
        context: Context,
        serverData: List<FormLayout?>,
        resultMap: HashMap<String, *>,
        requestFrom: String? = null
    ): Pair<String?, HashMap<String, Any>> {
        val customWorkflowList = HashMap<String, String>()
        serverData.forEach { serverViewModel ->
            if (serverViewModel?.isCustomWorkflow == true)
                customWorkflowList[serverViewModel.id] = serverViewModel.customWorkflowId ?: ""
            when (serverViewModel?.viewType) {
                ViewType.VIEW_TYPE_FORM_CARD_FAMILY -> createGroup(serverViewModel.id)
                else -> {
                    addToGroup(
                        serverViewModel?.family,
                        serverViewModel?.id!!,
                        resultMap[serverViewModel.id]
                    )
                    resultMap.remove(serverViewModel.id)
                }
            }
        }

        for (key in resultMap.keys) {
            groupResults(serverData, key, resultMap[key])
        }

        if (customWorkflowList.size > 0) {
            val list = ArrayList<Any>()
            customWorkflowList.entries.forEach { entryMap ->
                if (groupedResultMap.containsKey(entryMap.key)) {
                    groupedResultMap.remove(entryMap.key)?.let {
                        val map = HashMap<String, Any>()
                        map[DefinedParams.ID] = entryMap.value
                        map[entryMap.key] = it
                        list.add(map)
                    }
                }
            }
            groupedResultMap[DefinedParams.Customized_Workflows] = list
        }

        return Pair(StringConverter.convertGivenMapToString(groupedResultMap), groupedResultMap)
    }

    fun groupValuesAsMap(
        context: Context,
        serverData: List<FormLayout?>,
        resultMap: HashMap<String, *>
    ): HashMap<String, Any> {

        serverData.forEach { serverViewModel ->
            serverViewModel?.let {
                when (serverViewModel.viewType) {
                    ViewType.VIEW_TYPE_FORM_CARD_FAMILY -> createGroup(serverViewModel.id)
                    else -> {
                        addToGroup(
                            serverViewModel.family,
                            serverViewModel.id,
                            resultMap[serverViewModel.id]
                        )
                        resultMap.remove(serverViewModel.id)
                    }
                }
            }
        }

        for (key in resultMap.keys) {
            groupResults(serverData, key, resultMap[key])
        }

        return groupedResultMap
    }

    private fun addToGroup(family: String?, id: String, any: Any?) {

        family?.let {
            if (!groupedResultMap.containsKey(it))
                createGroup(it)
            val subMap = groupedResultMap[it] as HashMap<String, Any>
            any?.let { value ->
                subMap.put(id, value)
            }
        }
    }

    private fun groupResults(serverData: List<FormLayout?>, id: String, any: Any?) {

        val familyGroup = findGroupId(serverData, id)

        var subMap: HashMap<String, Any>? = null

        if (familyGroup != null) {
            if (!groupedResultMap.containsKey(familyGroup))
                createGroup(familyGroup)
            subMap = groupedResultMap[familyGroup] as HashMap<String, Any>?
        }

        any?.let { value ->
            if (subMap != null)
                subMap.put(id, value)
            else groupedResultMap.put(id, value)
        }

    }

    private fun createGroup(id: String) {
        val tempMap = HashMap<String, Any>()
        if (!groupedResultMap.containsKey(id))
            groupedResultMap[id] = tempMap
    }

    companion object {
        fun findGroupId(serverData: List<FormLayout?>, id: String): String? {
            val baseId = when (id) {
                DefinedParams.BMI -> DefinedParams.Height
                DefinedParams.Glucose_Value, DefinedParams.Glucose_Type, DefinedParams.Glucose_Date_Time, DefinedParams.GlucoseId, DefinedParams.BloodGlucoseUnit, DefinedParams.GlucoseLogId -> DefinedParams.BloodGlucoseID
                DefinedParams.PHQ4_Result, DefinedParams.PHQ4_Score, DefinedParams.PHQ4_Risk_Level -> DefinedParams.PHQ4_Mental_Health
                DefinedParams.PHQ9_Result, DefinedParams.PHQ9_Score, DefinedParams.PHQ9_Risk_Level -> DefinedParams.PHQ9_Mental_Health
                DefinedParams.Avg_Blood_pressure, DefinedParams.Avg_Pulse, DefinedParams.Avg_Systolic, DefinedParams.Avg_Diastolic, DefinedParams.bp_log_id -> DefinedParams.BPLog_Details
                DefinedParams.Initial -> DefinedParams.First_Name
                DefinedParams.isHTNDiagnosis -> DefinedParams.HTNPatientType
                DefinedParams.isDiabetesDiagnosis -> DefinedParams.DiabetesPatientType
                else -> {
                    if (id.endsWith(DefinedParams.unitMeasurement_KEY)) {
                        val parts = id.split(DefinedParams.unitMeasurement_KEY)
                        if (parts.isNotEmpty()) {
                            parts[0]
                        } else {
                            id
                        }
                    } else {
                        id
                    }
                }
            }
            return serverData.find { it?.id == baseId }?.family
        }
    }
}