package com.medtroniclabs.opensource.custom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.databinding.RowGeneralInfoBinding

class GeneralInfoAdapter(
    private val itemList: ArrayList<String>
): RecyclerView.Adapter<GeneralInfoAdapter.DiagnosisSelectionViewHolder>() {

    class DiagnosisSelectionViewHolder(val binding: RowGeneralInfoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiagnosisSelectionViewHolder {
        return DiagnosisSelectionViewHolder(
            RowGeneralInfoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ))
    }

    override fun onBindViewHolder(holder: DiagnosisSelectionViewHolder, position: Int) {
        holder.binding.tvRowTitle.text = itemList[position]
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

}