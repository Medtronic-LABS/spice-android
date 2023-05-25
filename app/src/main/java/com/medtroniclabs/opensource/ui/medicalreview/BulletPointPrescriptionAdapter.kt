package com.medtroniclabs.opensource.ui.medicalreview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.data.model.PatientPrescription
import com.medtroniclabs.opensource.data.model.SummaryPrescription
import com.medtroniclabs.opensource.databinding.RowBulletPointsBinding

class BulletPointPrescriptionAdapter(
    private val list: List<Any>
) : RecyclerView.Adapter<BulletPointPrescriptionAdapter.BulletPointViewHolder>() {

    class BulletPointViewHolder(val binding: RowBulletPointsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BulletPointViewHolder {
        return BulletPointViewHolder(
            RowBulletPointsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BulletPointViewHolder, position: Int) {
        val model = list[position]
        val pos = position + 1
        val slNo = "$pos."
        holder.binding.tvBullet.text = slNo
        holder.binding.tvTitle.text = getDisplayStringFrom(model, holder)
    }

    private fun getDisplayStringFrom(model: Any, holder: BulletPointViewHolder): StringBuffer {
        val prescriptionDetail = StringBuffer()
        if (model is PatientPrescription) {
            prescriptionDetail.append(model.medicationName)
            prescriptionDetail.append(holder.context.getString(R.string.separator_hyphen_space))
            patientPrescription(model, prescriptionDetail, holder)
        } else if (model is SummaryPrescription) {
            prescriptionDetail.append(model.medicationName)
            prescriptionDetail.append(holder.context.getString(R.string.separator_hyphen_space))
            summaryPrescription(model, prescriptionDetail, holder)
        }
        return prescriptionDetail
    }

    private fun summaryPrescription(
        model: SummaryPrescription,
        prescriptionDetail: StringBuffer,
        holder: BulletPointViewHolder
    ) {
        with(model) {
            if (dosageFormName.isNotEmpty()) {
                prescriptionDetail.append(dosageFormName)
            }
            if (dosageUnitValue.isNotEmpty() && dosageUnitName.isNotEmpty()) {

                prescriptionDetail.append(holder.context.getString(com.medtroniclabs.opensource.R.string.forward_slash))
                val dosagePrescribed = "$dosageUnitValue $dosageUnitName"
                prescriptionDetail.append(dosagePrescribed)
            }
            if (dosageFrequencyName.isNotEmpty()) {
                prescriptionDetail.append(holder.context.getString(com.medtroniclabs.opensource.R.string.forward_slash))
                prescriptionDetail.append(dosageFrequencyName)
            }
            if (prescribedDays.toString().isNotEmpty()) {
                val days = "$prescribedDays days"
                prescriptionDetail.append(holder.context.getString(com.medtroniclabs.opensource.R.string.forward_slash))
                prescriptionDetail.append(days)
            }
            if (instructionNote.isNotEmpty()) {
                prescriptionDetail.append(holder.context.getString(com.medtroniclabs.opensource.R.string.forward_slash))
                prescriptionDetail.append(instructionNote)
            }
        }
    }

    private fun patientPrescription(
        model: PatientPrescription,
        prescriptionDetail: StringBuffer,
        holder: BulletPointViewHolder
    ) {
        with(model) {
            if (dosageFormName != null && dosageFormName.isNotEmpty()) {
                prescriptionDetail.append(dosageFormName)
            }
            if (dosageUnitValue != null && dosageUnitValue.isNotEmpty() && dosageUnitName != null && dosageUnitName.isNotEmpty()) {
                val dosagePrescribed = "$dosageUnitValue $dosageUnitName"
                prescriptionDetail.append(holder.context.getString(com.medtroniclabs.opensource.R.string.forward_slash))
                prescriptionDetail.append(dosagePrescribed)

            }
            if (dosageFrequencyName != null && dosageFrequencyName.isNotEmpty()) {
                prescriptionDetail.append(holder.context.getString(com.medtroniclabs.opensource.R.string.forward_slash))
                prescriptionDetail.append(dosageFrequencyName)
            }
            if (prescribedDays != null && prescribedDays.toString().isNotEmpty()) {
                prescriptionDetail.append(holder.context.getString(com.medtroniclabs.opensource.R.string.forward_slash))
                val days = "$prescribedDays days"
                prescriptionDetail.append(days)
            }
            if (instructionNote != null && instructionNote.isNotEmpty()) {
                prescriptionDetail.append(holder.context.getString(com.medtroniclabs.opensource.R.string.forward_slash))
                prescriptionDetail.append(instructionNote)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}