package com.medtroniclabs.opensource.ui.medicalreview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.data.model.PatientPrescription
import com.medtroniclabs.opensource.databinding.AdapterMedicationHistoryBinding

class MedicationHistoryAdapter(
    private val medicationLists: ArrayList<PatientPrescription>
) :
    RecyclerView.Adapter<MedicationHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: AdapterMedicationHistoryBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        override fun onClick(mView: View?) {
            //View - OnClickListener
        }

        fun bind(position: Int, item: PatientPrescription) {
            binding.apply {
                with(item) {
                    createdAt.let { prescribedAt ->
                        tvPrescribedDate.text = DateUtils.convertDateTimeToDate(
                            getTime(prescribedAt),
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmss,
                            DateUtils.DATE_FORMAT_ddMMMyyyy
                        )
                    }
                    tvDosage.text = dosageUnitValue
                    tvUnit.text = getDosageUnit(dosageUnitName ?: "")
                    tvForm.text = dosageFormName
                    tvFrequency.text = dosageFrequencyName
                    tvPrescribedDays.text = prescribedDays.toString()
                    tvInformation.text = instructionNote
                }
            }
        }
    }

    private fun getTime(dateFormat: String): String {
        return dateFormat.split("+")[0]
    }

    private fun getDosageUnit(dosageUnit: String): String {
        val dosage: String
        val str = dosageUnit.split(" ")
        dosage = str[0].trim()
        return dosage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AdapterMedicationHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        medicationLists.let {
            holder.bind(position, it[position])
        }
    }

    override fun getItemCount(): Int = medicationLists.size
}