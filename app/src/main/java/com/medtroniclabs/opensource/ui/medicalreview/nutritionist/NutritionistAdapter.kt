package com.medtroniclabs.opensource.ui.medicalreview.nutritionist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.LifeStyleManagement
import com.medtroniclabs.opensource.databinding.LayoutNutritionistAdapterBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams

class NutritionistAdapter(val list: ArrayList<LifeStyleManagement>, val listener: ValidationListener) :
    RecyclerView.Adapter<NutritionistAdapter.NutritionistViewHolder>() {

    inner class NutritionistViewHolder(val binding: LayoutNutritionistAdapterBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val context: Context = binding.root.context

        init {
            binding.clRoot.safeClickListener(this)
        }

        fun bind(model: LifeStyleManagement) {
            //Referred For
            binding.tvReferredFor.text = getLifeStyleTranslatedName(model.lifestyle,SecuredPreference.getIsTranslationEnabled())

            //Referred Date
            binding.tvReferredDate.text = DateUtils.convertDateTimeToDate(
                model.referredDate,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                DateUtils.DATE_FORMAT_ddMMMyyyy
            )

            //Ordered By
            val referredBy = model.referredBy as Map<*, *>
            var firstName = ""
            var lastName = ""
            if (referredBy[DefinedParams.First_Name] is String) {
                firstName = referredBy[DefinedParams.First_Name].toString()
            }
            if (referredBy[DefinedParams.Last_Name] is String) {
                lastName = referredBy[DefinedParams.Last_Name].toString()
            }
            binding.tvReferredBy.text = "$firstName $lastName"

            //Clinical Notes
            binding.tvClinicalNotes.text =
                if (model.clinicianNote.isNullOrBlank())
                    context.getString(R.string.separator_hyphen)
                else
                    model.clinicianNote

            //Lifestyle Assessment
            binding.etLifestyleAssessment.setText(model.lifestyleAssessment ?: "")

            binding.etLifestyleAssessment.addTextChangedListener { editable ->
                list[layoutPosition].let {
                    if (editable != null) {
                        it.lifestyleAssessment = editable.toString().trim()
                    }
                }
                listener.checkValidation()
            }

            //Other Notes
            binding.etOtherNotes.setText(model.otherNote ?: "")

            binding.etOtherNotes.addTextChangedListener { editable ->
                list[layoutPosition].let {
                    if (editable != null) {
                        it.otherNote = editable.toString().trim()
                    }
                }
            }

            if (model.isExpanded) {
                binding.clBody.visibility = View.VISIBLE
                binding.ivArrow.rotation = 180f
            } else {
                binding.clBody.visibility = View.GONE
                binding.ivArrow.rotation = 0f
            }
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.clRoot -> {
                    list.forEachIndexed { index, element ->
                        if (index == layoutPosition)
                            element.isExpanded = !element.isExpanded
                    }
                    notifyItemChanged(layoutPosition)
                }
            }
        }
    }

    private fun getLifeStyleTranslatedName(lifestyle: List<Any>?, isTranslationEnabled: Boolean): String {
        val name = lifestyle?.mapNotNull { mod ->
            if (mod is Map<*,*>){
                if (isTranslationEnabled){
                    mod[DefinedParams.cultureValue] ?: mod[DefinedParams.NAME]
                }else{
                    mod[DefinedParams.NAME]
                }
            }else{
                mod
            }
        }?.joinToString(separator = ", ")

        return  name ?: lifestyle?.joinToString(separator = ", ")?:""
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NutritionistViewHolder {
        return NutritionistViewHolder(
            LayoutNutritionistAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NutritionistViewHolder, position: Int) {
        list.let {
            holder.bind(it[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}