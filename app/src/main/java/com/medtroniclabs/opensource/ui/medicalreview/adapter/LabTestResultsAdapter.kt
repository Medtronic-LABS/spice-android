package com.medtroniclabs.opensource.ui.medicalreview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.internal.LinkedTreeMap
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.capitalizeFirstChar
import com.medtroniclabs.opensource.appextensions.fetchString
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.common.CustomSpinnerAdapter
import com.medtroniclabs.opensource.common.MedicalReviewConstant
import com.medtroniclabs.opensource.data.model.UnitMetricEntity
import com.medtroniclabs.opensource.databinding.LabTestResultItemBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams

class LabTestResultsAdapter(private val labTestName: String?) :
    RecyclerView.Adapter<LabTestResultsAdapter.ActivitiesViewHolder>() {

    private val resultsList: ArrayList<HashMap<String, Any>> = ArrayList()
    private val defaultUnitList: ArrayList<UnitMetricEntity> = ArrayList()

    class ActivitiesViewHolder(val binding: LabTestResultItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    fun setData(
        inputList: ArrayList<HashMap<String, Any>>,
        unitList: ArrayList<UnitMetricEntity>?
    ) {
        resultsList.addAll(inputList)
        if (unitList != null)
            defaultUnitList.addAll(unitList)
        notifyItemRangeChanged(0, resultsList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivitiesViewHolder {
        return ActivitiesViewHolder(
            LabTestResultItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun getResultsList() = resultsList

    override fun onBindViewHolder(holder: ActivitiesViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val data = resultsList[position]
        val resultName = data[DefinedParams.NAME] as String? ?: ""
        if (resultName.isBlank() || resultName.equals("Other", ignoreCase = true)) {
            data[DefinedParams.NAME] = labTestName ?: ""
        }
        holder.binding.tvTitleLbl.text = data[DefinedParams.NAME] as String? ?: ""
        holder.binding.tvTitleLbl.capitalizeFirstChar()
        holder.binding.tvTitleLbl.markMandatory()
        holder.binding.tvUnitLbl.text = holder.context.getString(R.string.unit_text)
        holder.binding.tvUnitLbl.markMandatory()

        val adapter = CustomSpinnerAdapter(holder.context)
        adapter.setData(getUnitsMap(resultsList[position]))
        holder.binding.etUnit.adapter = adapter

        handleValidation(data, holder)

        handleResultValue(data, holder)

        handleUnitDropDown(data, holder, adapter, position)
    }

    private fun handleValidation(
        data: java.util.HashMap<String, Any>,
        holder: ActivitiesViewHolder
    ) {
        if (data.containsKey(DefinedParams.Is_Result_Value_Valid)) {
            val isValid = data[DefinedParams.Is_Result_Value_Valid]
            if (isValid is Boolean && isValid) {
                holder.binding.tvError.visibility = View.GONE
            } else {
                holder.binding.tvError.visibility = View.VISIBLE
                holder.binding.tvError.text =
                    holder.context.getString(R.string.default_user_input_error)
            }
        } else
            holder.binding.tvError.visibility = View.GONE

        if (data.containsKey(DefinedParams.Is_Unit_Valid)) {
            val isValid = data[DefinedParams.Is_Unit_Valid]
            if (isValid is Boolean && isValid) {
                holder.binding.tvUnitError.visibility = View.GONE
            } else {
                holder.binding.tvUnitError.visibility = View.VISIBLE
            }
        } else
            holder.binding.tvUnitError.visibility = View.GONE
    }

    private fun handleResultValue(
        data: java.util.HashMap<String, Any>,
        holder: ActivitiesViewHolder
    ) {
        if (data.containsKey(DefinedParams.Result_Value))
            holder.binding.etValue.setText(data[DefinedParams.Result_Value] as String)
        else
            holder.binding.etValue.setText("")

        holder.binding.etValue.addTextChangedListener { editable ->
            if (editable.isNullOrBlank()) {
                if (data.containsKey(DefinedParams.Result_Value)) {
                    data.remove(DefinedParams.Result_Value)
                    data[DefinedParams.Is_Result_Value_Valid] = false
                    // holder.binding.tvError.visibility = View.VISIBLE
                }
            } else {
                data[DefinedParams.Result_Value] = editable.fetchString()
                //holder.binding.tvError.visibility = View.GONE
                data[DefinedParams.Is_Result_Value_Valid] = true
            }
        }
    }

    private fun handleUnitDropDown(
        data: java.util.HashMap<String, Any>,
        holder: ActivitiesViewHolder,
        adapter: CustomSpinnerAdapter,
        position: Int
    ) {
        if (data.containsKey(DefinedParams.Unit)) {
            val selectedIndex = getIndex(resultsList[position], data[DefinedParams.Unit])
            if (selectedIndex >= 0)
                holder.binding.etUnit.setSelection(selectedIndex, true)
            else
                holder.binding.etUnit.setSelection(0, true)
        } else
            holder.binding.etUnit.setSelection(0, true)

        holder.binding.etUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                val selectedItem = adapter.getData(position = pos)
                selectedItem?.let {
                    val selectedId = it[DefinedParams.ID]
                    if ((selectedId is Long && selectedId <=0 )) {
                        if (data.containsKey(DefinedParams.Unit)) {
                            data[DefinedParams.Is_Unit_Valid] = false
                            data.remove(DefinedParams.Unit)
                        }
                    } else {
                        data[DefinedParams.Unit] =
                            it[DefinedParams.NAME] as Any
                        data[DefinedParams.Is_Unit_Valid] = true
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                /**
                 * this method is not used
                 */
            }
        }
    }

    private fun getIndex(currentItem: HashMap<String, Any>, unitValue: Any?): Int {
        var selectedIndex = -1
        if (currentItem.containsKey(DefinedParams.Lab_Result_Range)) {
            val list = currentItem[DefinedParams.Lab_Result_Range]
            if (list is ArrayList<*>) {
                list.forEachIndexed { index, range ->
                    if (range is LinkedTreeMap<*, *> && range.containsKey(DefinedParams.Unit)) {
                        val spinnerVal = range[DefinedParams.Unit] as String
                        getSelectedIndex(index, spinnerVal, unitValue)?.let {
                            selectedIndex = it
                        }
                        return@forEachIndexed
                    }
                }
            }
        }
        if (selectedIndex == -1 && defaultUnitList.isNotEmpty()) {
            selectedIndex = defaultUnitList.indexOfFirst {
                it.unit == if (unitValue is String) unitValue else ""
            }
        }

        //+1 is included because we have added '--Select--' at the 0th index
        return if (selectedIndex != -1) selectedIndex + 1 else 0
    }

    private fun getSelectedIndex(index: Int, spinnerVal: String, unitValue: Any?): Int? {
        return if (spinnerVal == fetchChosenVal(unitValue)) index else null
    }

    private fun fetchChosenVal(unitValue: Any?): String {
        return if (unitValue is String)
            unitValue
        else
            ""
    }

    private fun getUnitsMap(currentItem: HashMap<String, Any>): ArrayList<Map<String, Any>> {
        val unitList = ArrayList<Map<String, Any>>()

        if (currentItem.containsKey(DefinedParams.Lab_Result_Range)) {
            val list = currentItem[DefinedParams.Lab_Result_Range]
            if (list is ArrayList<*>) {
                list.forEach { range ->
                    if (range is LinkedTreeMap<*, *> && range.containsKey(DefinedParams.Unit)) {
                            val map = HashMap<String, Any>()
                            map[DefinedParams.ID] = (range[DefinedParams.ID] as Double).toLong()
                            map[DefinedParams.NAME] = range[DefinedParams.Unit] as String
                            unitList.add(map)
                    }
                }
            }
        }

        if (unitList.isEmpty() && defaultUnitList.isNotEmpty()) {
            defaultUnitList.forEach { unit ->
                val unitMap = HashMap<String, Any>()
                unitMap[DefinedParams.ID] = unit._id
                unitMap[DefinedParams.NAME] = unit.unit
                unitList.add(unitMap)
            }
        }

        val hashMap = HashMap<String, Any>()
        hashMap[DefinedParams.ID] = MedicalReviewConstant.DefaultSelectID
        hashMap[DefinedParams.NAME] = MedicalReviewConstant.DefaultIDLabel
        unitList.add(0, hashMap)

        return unitList
    }

    override fun getItemCount(): Int {
        return resultsList.size
    }

    fun validateInputs(): Boolean {
        var isValid = true
        resultsList.forEachIndexed { index, hashMap ->
            validateResultValue(hashMap)?.let {
                isValid = it
            }

            validateUnitValue(hashMap)?.let {
                isValid = it
            }
            hashMap[DefinedParams.ID] = (hashMap[DefinedParams.ID] as Number).toLong()
        }
        return isValid
    }

    private fun validateResultValue(hashMap: java.util.HashMap<String, Any>): Boolean? {
        var isValid: Boolean? = null
        if (hashMap.containsKey(DefinedParams.Result_Value)) {
            if ((hashMap[DefinedParams.Result_Value] as String?).isNullOrBlank()) {
                hashMap[DefinedParams.Is_Result_Value_Valid] = false
                isValid = false
            } else {
                hashMap[DefinedParams.Is_Result_Value_Valid] = true
            }
        } else {
            hashMap[DefinedParams.Is_Result_Value_Valid] = false
            isValid = false
        }
        return isValid
    }

    private fun validateUnitValue(hashMap: java.util.HashMap<String, Any>): Boolean? {
        var isValid: Boolean? = null
        if (hashMap.containsKey(DefinedParams.Unit)) {
            if ((hashMap[DefinedParams.Unit] as String?).isNullOrBlank()) {
                hashMap[DefinedParams.Is_Unit_Valid] = false
                isValid = false
            } else {
                if ((hashMap[DefinedParams.Unit] as String) == MedicalReviewConstant.DefaultIDLabel) {
                    hashMap[DefinedParams.Is_Unit_Valid] = false
                    isValid = false
                } else
                    hashMap[DefinedParams.Is_Unit_Valid] = true
            }
        } else {
            hashMap[DefinedParams.Is_Unit_Valid] = false
            isValid = false
        }
        return isValid
    }
}