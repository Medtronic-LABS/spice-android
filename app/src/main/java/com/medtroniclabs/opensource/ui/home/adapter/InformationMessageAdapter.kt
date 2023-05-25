package com.medtroniclabs.opensource.ui.home.adapter

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.common.TransferStatusEnum
import com.medtroniclabs.opensource.data.model.PatientTransfer
import com.medtroniclabs.opensource.databinding.RowInformationMessageAdapterBinding
import com.medtroniclabs.opensource.ui.home.ApproveRejectListener

class InformationMessageAdapter(
    private val patientList: ArrayList<PatientTransfer>,
    val listener: ApproveRejectListener) :
    RecyclerView.Adapter<InformationMessageAdapter.InformationMessageViewHolder>() {

    class InformationMessageViewHolder(val binding: RowInformationMessageAdapterBinding): RecyclerView.ViewHolder(binding.root){
        val context = binding.root.context
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InformationMessageViewHolder {
        return InformationMessageViewHolder(
            RowInformationMessageAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: InformationMessageViewHolder, position: Int) {
        val model = patientList[position]
        holder.binding.tvInformation.text = getInformationText(model,holder.context)
        holder.binding.btnCancel.visibility = getButtonCancelViewVisibility(model)
        holder.binding.btnCancel.setOnClickListener {
            listener.onTransferStatusUpdate(TransferStatusEnum.CANCELED.name,model.id,model.tenantId,model.transferReason)
        }
    }

    private fun getButtonCancelViewVisibility(model: PatientTransfer): Int {
        return if (model.transferStatus == TransferStatusEnum.PENDING.name){
            View.VISIBLE
        }else{
            View.GONE
        }
    }

    private fun getInformationText(model: PatientTransfer, context: Context): CharSequence {
        val informationTextBuilder:SpannableStringBuilder
        when(model.transferStatus){
            TransferStatusEnum.PENDING.name -> {
              informationTextBuilder = SpannableStringBuilder(
                    context.getString(R.string.pending_message_format,model.patient.firstName,model.transferSite?.name?:"")
                )

            }
            TransferStatusEnum.ACCEPTED.name -> {
                informationTextBuilder = SpannableStringBuilder(
                    context.getString(R.string.accepted_message_format,model.patient.firstName,model.transferSite?.name?:"")
                )
            }
            TransferStatusEnum.REJECTED.name -> {
                informationTextBuilder = SpannableStringBuilder(
                    context.getString(R.string.rejected_message_format,model.patient.firstName,model.transferSite?.name?:"")
                )
            }
            else -> {
                informationTextBuilder = SpannableStringBuilder(
                    context.getString(R.string.information_message_template,model.patient.firstName,model.transferSite?.name?:"",model.transferStatus.toLowerCase())
                )
            }
        }

        model.transferSite?.name?.let { name ->
            informationTextBuilder.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.site_notification)),
                informationTextBuilder.indexOf(name),
                informationTextBuilder.indexOf(name)+ name.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return informationTextBuilder
    }

    override fun getItemCount(): Int {
        return patientList.size
    }
}