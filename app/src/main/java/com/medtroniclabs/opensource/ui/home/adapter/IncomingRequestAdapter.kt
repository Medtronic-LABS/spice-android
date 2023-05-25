package com.medtroniclabs.opensource.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.common.TransferStatusEnum
import com.medtroniclabs.opensource.data.model.PatientTransfer
import com.medtroniclabs.opensource.databinding.RowIncomingRequestMessageBinding
import com.medtroniclabs.opensource.ui.home.ApproveRejectListener

class IncomingRequestAdapter(
    val list: ArrayList<PatientTransfer>,
    val listener: ApproveRejectListener
) :
    RecyclerView.Adapter<IncomingRequestAdapter.IncomingRequestViewHolder>() {

    class IncomingRequestViewHolder(val binding: RowIncomingRequestMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomingRequestViewHolder {
          return IncomingRequestViewHolder (
            RowIncomingRequestMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: IncomingRequestViewHolder, position: Int) {
        val model = list[position]
        holder.binding.tvReason.text = model.transferReason
        holder.binding.btnAccept.setOnClickListener {
            listener.onTransferStatusUpdate(TransferStatusEnum.ACCEPTED.name,model.id,model.tenantId,model.transferReason)
        }
        holder.binding.btnReject.setOnClickListener {
            listener.onTransferStatusUpdate(TransferStatusEnum.REJECTED.name,model.id,model.tenantId,model.transferReason)
        }

        holder.binding.tvViewDetail.setOnClickListener {
            listener.onViewDetail(model.id)
        }

        holder.binding.tvPatientName.text = "${model.patient.firstName} ${model.patient.lastName}"
    }

    override fun getItemCount(): Int {
        return list.size
    }
}