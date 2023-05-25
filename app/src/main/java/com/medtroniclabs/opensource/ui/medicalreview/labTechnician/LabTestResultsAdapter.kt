package com.medtroniclabs.opensource.ui.medicalreview.labTechnician

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.internal.LinkedTreeMap
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.data.model.LabTestModel
import com.medtroniclabs.opensource.databinding.LayoutLabTestResultsAdapterBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams

class LabTestResultsAdapter(
    val list: ArrayList<LabTestModel>,
    val listener: LabResultInterface,
    ) : RecyclerView.Adapter<LabTestResultsAdapter.LabTestResultsViewHolder>() {

    class LabTestResultsViewHolder(val binding: LayoutLabTestResultsAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabTestResultsViewHolder {
        return LabTestResultsViewHolder(
            LayoutLabTestResultsAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LabTestResultsViewHolder, position: Int) {
        val model = list[position]
        holder.binding.tvTestName.text = model.labTestName
        val referredBy = model.referredBy as? LinkedTreeMap<String, String>
        referredBy?.let { referred ->
            val referredByStr =
                "${referred[DefinedParams.First_Name]} ${referred[DefinedParams.Last_Name]}"
            holder.binding.tvReferredBy.text = referredByStr
        }
        holder.binding.tvReferredDate.text = DateUtils.convertDateTimeToDate(
            model.referredDate,
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_FORMAT_ddMMMyyyy
        )
        holder.binding.ivEdit.safeClickListener {
            listener.selectedLabResult(model)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}