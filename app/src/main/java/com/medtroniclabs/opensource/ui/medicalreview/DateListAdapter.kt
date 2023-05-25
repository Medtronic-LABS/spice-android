package com.medtroniclabs.opensource.ui.medicalreview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.data.model.VisitDateModel
import com.medtroniclabs.opensource.databinding.LayoutDateListAdapterBinding

class DateListAdapter(
    val list: ArrayList<VisitDateModel>,
    var selectedPatientId: Long?,
    val listener: DateSelectionListener
) :
    RecyclerView.Adapter<DateListAdapter.DateViewHolder>() {
    class DateViewHolder(val binding: LayoutDateListAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        return DateViewHolder(
            LayoutDateListAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val model = list[position]
        holder.binding.tvDate.text = DateUtils.convertDateTimeToDate(
            model.visitDate,
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_FORMAT_ddMMMyyyy
        )
        if (selectedPatientId == model._id) {
            holder.binding.ivSelected.visibility = View.VISIBLE
        } else {
            holder.binding.ivSelected.visibility = View.GONE
        }
        holder.binding.root.safeClickListener {
            listener.onDateSelected(model._id)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}