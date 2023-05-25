package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.data.model.BloodGlucose
import com.medtroniclabs.opensource.databinding.RowBloodGlucoseBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.getGlucoseUnit

class BloodGlucoseAdapter(
    private val context: Context,
    private val bloodGlucoseList: ArrayList<BloodGlucose>
) : RecyclerView.Adapter<BloodGlucoseAdapter.BloodGlucoseHolder>() {

    class BloodGlucoseHolder(val binding: RowBloodGlucoseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val viewContext: Context = binding.root.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BloodGlucoseHolder {
        return BloodGlucoseHolder(
            RowBloodGlucoseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BloodGlucoseHolder, position: Int) {
        holder.binding.tvNoRecord.visibility = View.GONE
        if (bloodGlucoseList.isNullOrEmpty()) {
            holder.binding.tvNoRecord.visibility = View.VISIBLE
            holder.binding.tvNoRecord.text = context.getString(R.string.no_record_blood_glucose)
        } else {
            val model = bloodGlucoseList[position]
            holder.binding.tvAssessmentDate.text = DateUtils.convertDateTimeToDate(
                model.glucoseDateTime,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ, DateUtils.DATE_FORMAT_ddMMMyyyy
            )
            holder.binding.tvRandomBloodSugar.text = "-"
            holder.binding.tvFastingBloodSugar.text = "-"
            holder.binding.tvHbA1c.text = "-"
            model.glucoseType?.let { glucoseType ->
                if (glucoseType.lowercase() == DefinedParams.rbs) {
                    holder.binding.tvRandomBloodSugar.text =
                        "${CommonUtils.getDecimalFormatted(model.glucoseValue)} ${getGlucoseUnit(model.glucoseUnit, false)}"
                } else {
                    holder.binding.tvFastingBloodSugar.text =
                        "${CommonUtils.getDecimalFormatted(model.glucoseValue)} ${getGlucoseUnit(model.glucoseUnit, false)}"
                }
            }
            model.hba1c?.let {
                holder.binding.tvHbA1c.text = "${CommonUtils.getDecimalFormatted(it)} ${getGlucoseUnit(model.hba1cUnit, false)}"
            }
        }
    }


    override fun getItemCount(): Int {
        return if (bloodGlucoseList.isNullOrEmpty()) {
            1
        } else {
            bloodGlucoseList.size
        }
    }
}