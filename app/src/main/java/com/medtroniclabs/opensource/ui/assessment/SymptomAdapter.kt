package com.medtroniclabs.opensource.ui.assessment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.data.ui.SymptomModel
import com.medtroniclabs.opensource.databinding.LayoutSymptomAdapterBinding
import com.medtroniclabs.opensource.databinding.LayoutSymptomTitleBinding

class SymptomAdapter(val list: ArrayList<SymptomModel>, val translationToggle: Boolean) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class SymptomViewHolder(val binding: LayoutSymptomAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    class SymptomTitleViewHolder(val binding: LayoutSymptomTitleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            val binding = LayoutSymptomAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SymptomViewHolder(binding)
        } else {
            val binding = LayoutSymptomTitleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SymptomTitleViewHolder(binding)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val symptom = list[position]
        if (holder is SymptomViewHolder) {
            holder.binding.checkSymptom.isChecked = symptom.isSelected
            if (translationToggle){
                holder.binding.checkSymptom.text = symptom.cultureValue ?: symptom.symptom
            }else{
                holder.binding.checkSymptom.text = symptom.symptom
            }
            holder.binding.root.safeClickListener {
                if (symptom.symptom.startsWith("No symptoms", true)) {
                    if (!symptom.isSelected) {
                        resetSelection(symptom.type, list)
                        symptom.isSelected = !symptom.isSelected
                        notifyDataSetChanged()
                    } else {
                        symptom.isSelected = !symptom.isSelected
                        notifyItemChanged(position)
                    }
                } else {
                    val filteredModel = list.filter {
                        it.symptom.startsWith("No symptoms", true) && it.isSelected &&
                                it.type == symptom.type
                    }
                    updateSelection(position, symptom, filteredModel)
                }
            }
        } else if (holder is SymptomTitleViewHolder)
            holder.binding.SymptomTitle.text = symptom.type
    }

    private fun updateSelection(
        position: Int,
        symptom: SymptomModel,
        filteredModel: List<SymptomModel>
    ) {
        if (filteredModel.isNotEmpty()) {
            resetSelection(symptom.type, list)
            symptom.isSelected = !symptom.isSelected
            notifyDataSetChanged()
        } else {
            symptom.isSelected = !symptom.isSelected
            notifyItemChanged(position)
        }
    }


    private fun resetSelection(type: String?, list: ArrayList<SymptomModel>) {
        list.filter { it.type == type }.forEach {
            it.isSelected = false
        }
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].viewType
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getSelectedSymptomList(): List<SymptomModel> {
        return list.filter { it.isSelected }
    }

}