package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.data.model.BPResponse
import com.medtroniclabs.opensource.databinding.RowBloodPressureBinding
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils

class BPAdapter(
    private val context: Context,
    private val bpList: ArrayList<BPResponse>
) : RecyclerView.Adapter<BPAdapter.BPHolder>()  {

    class BPHolder(val binding: RowBloodPressureBinding) : RecyclerView.ViewHolder(binding.root) {
        val viewContext: Context = binding.root.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BPHolder {
        return BPHolder(
            RowBloodPressureBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BPHolder, position: Int) {
        holder.binding.tvNoRecord.visibility = View.GONE
        if (bpList.isNullOrEmpty()) {
            holder.binding.tvNoRecord.visibility = View.VISIBLE
            holder.binding.tvNoRecord.text = context.getString(R.string.no_record_blood_pressure)
        } else {
            val bpModel = bpList[position]
            holder.binding.tvAssessmentDate.text = DateUtils.convertDateTimeToDate(
                bpModel.bpTakenOn,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ, DateUtils.DATE_FORMAT_ddMMMyyyy
            ).ifEmpty { context.getString(R.string.separator_hyphen) }
            val avgBp = "${CommonUtils.getDecimalFormatted(bpModel.avgSystolic)}/${
                CommonUtils.getDecimalFormatted(bpModel.avgDiastolic)
            }"
            holder.binding.tvAverageBP.text = avgBp

        }
    }

    override fun getItemCount(): Int {
        return if (bpList.isNullOrEmpty()) {
            1
        } else {
            bpList.size
        }
    }
}