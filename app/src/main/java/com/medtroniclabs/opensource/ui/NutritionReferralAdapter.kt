package com.medtroniclabs.opensource.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.databinding.NutritionReferralItemBinding

class NutritionReferralAdapter(
    private val referralList: ArrayList<NutritionReferralModel>,
    val isNoReferralCallback: (isNoReferral: Boolean) -> Unit?
) :
    RecyclerView.Adapter<NutritionReferralAdapter.ReferralViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferralViewHolder {
        return ReferralViewHolder(
            NutritionReferralItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ReferralViewHolder, position: Int) {
        val referralItem = referralList[position]
        var text = ""
        for (referral in referralItem.referrals) {
            if (text.isNotEmpty())
                text += ","
            text += referral
        }
        holder.binding.tvReferredFor?.text = text
        holder.binding.tvReferredDate?.text = referralItem.dateReferred
        holder.binding.tvNotesByClinician?.text = referralItem.notes
        holder.binding.tvAssessedOn?.text = referralItem.dateReferred
        holder.binding.tvReferredDate?.text = referralItem.dateReferred
    }

    override fun getItemCount(): Int {
        return referralList.size
    }

    fun addReferral(nutritionReferralModel: NutritionReferralModel) {
        referralList.add(nutritionReferralModel)
        notifyItemInserted(referralList.size)
    }

    inner class ReferralViewHolder(val binding: NutritionReferralItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.ivDelete?.safeClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.ivDelete -> {
                    referralList.removeAt(absoluteAdapterPosition)
                    if (absoluteAdapterPosition == 0) {
                        isNoReferralCallback.invoke(true)
                    }
                    this@NutritionReferralAdapter.notifyItemRemoved(absoluteAdapterPosition)
                }
            }
        }

    }
}