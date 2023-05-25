package com.medtroniclabs.opensource.ui.medicalreview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.data.model.PatientListRespModel
import com.medtroniclabs.opensource.databinding.ListItemPatientsBinding
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.ui.medicalreview.PatientSelectionListener

class PatientsListAdapter(
    val listener: PatientSelectionListener
) :
    PagingDataAdapter<PatientListRespModel, PatientsListAdapter.PatientsListViewHolder>(
        PatientListComparator
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientsListViewHolder {
        return PatientsListViewHolder(
            ListItemPatientsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PatientsListViewHolder, position: Int) {
        getItem(position)?.let { searchItem ->
            holder.bind(searchItem)
            holder.binding.cardPatient.safeClickListener {
                listener.onSelectedPatient(searchItem)
            }
        }
    }

    inner class PatientsListViewHolder(val binding: ListItemPatientsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PatientListRespModel) = with(binding) {
            item.firstName?.let { fName ->
                val text = StringConverter.appendTexts(firstText = fName, item.lastName)
                tvCardPatientName.text = CommonUtils.capitalize(StringConverter.appendTexts(
                    firstText = text,
                    "${item.age?.toInt()}",
                    CommonUtils.getGenderConstant(item.gender),
                    separator = "-"
                ))
            }
            tvCardNationalID.text = item.nationalId
            tvCardPatientID.text = if (item.programId == null) "-" else item.programId.toString()
            clPatientRoot.background = if (item.redRiskPatient == true) ContextCompat.getDrawable(
                clPatientRoot.context,
                R.drawable.bg_red_risk
            ) else null
        }
    }

    object PatientListComparator : DiffUtil.ItemCallback<PatientListRespModel>() {
        override fun areItemsTheSame(
            oldItem: PatientListRespModel,
            newItem: PatientListRespModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PatientListRespModel,
            newItem: PatientListRespModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}