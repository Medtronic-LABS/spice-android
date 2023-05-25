package com.medtroniclabs.opensource.ui.medicalreview.pharamcist

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.data.model.FillPrescriptionListResponse
import com.medtroniclabs.opensource.databinding.LayoutPrescriptionRefillAdapterBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams

class PrescriptionRefillAdapter(val list: ArrayList<FillPrescriptionListResponse>) :
    RecyclerView.Adapter<PrescriptionRefillAdapter.PrescriptionRefillViewHolder>() {

    inner class PrescriptionRefillViewHolder(val binding: LayoutPrescriptionRefillAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int, model: FillPrescriptionListResponse) {
            binding.tvMedicationName.text = model.medicationName
            binding.tvDosage.text =
                getDosageValue(model.dosageUnitValue, model.dosageUnitName)
            binding.tvFrequency.text = model.dosageFrequencyName
            binding.tvMedicationPrescribedDays.text = "${model.remainingPrescriptionDays}"
            binding.ivFormType.setImageDrawable(
                getFormDosage(
                    model.dosageFormName,
                    binding.root.context
                )
            )
            binding.ivDosageFrom.setImageDrawable(
                getFormDosage(
                    model.dosageFormName,
                    binding.root.context
                )
            )
            binding.tvDaysFilled.setText("${model.prescriptionFilledDays ?: ""}")

            binding.tvDaysFilled.addTextChangedListener { editable ->
                list[layoutPosition].let {
                    if (editable.isNullOrBlank()) {
                        it.prescriptionFilledDays = null
                    } else {
                        it.prescriptionFilledDays = editable.toString().toIntOrNull()
                    }
                }
            }

            binding.clMedicationName.safeClickListener {
                list[layoutPosition].let {
                    it.isSelected = !it.isSelected
                }
                notifyItemChanged(layoutPosition)
            }
            if (model.isSelected) {
                binding.ivDropDown.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_drop_down_medium_blue
                    )
                )
                binding.prescriptionDropDown.visibility = View.VISIBLE
                binding.tvMedicationName.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.blue
                    )
                )
            } else {
                binding.ivDropDown.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_drop_down_grey
                    )
                )
                binding.prescriptionDropDown.visibility = View.GONE
                binding.tvMedicationName.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.black
                    )
                )
            }

            binding.tvPrescribedDate.text = DateUtils.convertDateTimeToDate(
                model.createdAt,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                DateUtils.DATE_FORMAT_ddMMMyyyy
            )

            binding.tvDosageForm.text =
                model.dosageFormName.ifBlank { "-" }
            binding.tvBrand.text = model.brandName
            binding.tvClassification.text = model.classificationName
            binding.etInstruction.setText(model.instructionModified ?: model.instructionNote)
            binding.etInstruction.addTextChangedListener { editable ->
                list[layoutPosition].let {
                    if (editable.isNullOrBlank()) {
                        it.instructionModified = ""
                    } else {
                        it.instructionModified = editable.toString()
                    }
                    it.instructionUpdated = !model.instructionNote.equals(it.instructionModified, false)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PrescriptionRefillViewHolder {
        return PrescriptionRefillViewHolder(
            LayoutPrescriptionRefillAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PrescriptionRefillViewHolder, position: Int) {
        list.let {
            holder.bind(position, it[position])
        }
    }

    fun getFormDosage(dosageFormName: String, context: Context): Drawable? {

        when (dosageFormName) {
            DefinedParams.Injection_Injectable_Solution -> {
                return ContextCompat.getDrawable(context, R.drawable.ic_injection_form)
            }
            DefinedParams.Liquid_Oral -> {
                return ContextCompat.getDrawable(context, R.drawable.ic_syrup)
            }
            DefinedParams.Tablet -> {
                return ContextCompat.getDrawable(context, R.drawable.ic_tablet)
            }
            DefinedParams.Capsule -> {
                return ContextCompat.getDrawable(context, R.drawable.ic_capsule)
            }
        }
        return null
    }

    fun getDosageValue(dosageUnitValue: String, dosageUnitName: String?): String {
        var text: String = dosageUnitValue
        dosageUnitName?.let {
            text += it
        }
        return text
    }

    override fun getItemCount(): Int {
        return list.size
    }
}