package com.medtroniclabs.opensource.ui.medicalreview.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.data.model.LifeStyleManagement
import com.medtroniclabs.opensource.databinding.LifestyleManagementAdapterBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.LifeStyleInterface

class LifeStyleManagementAdapter(
    private val lifeStyleInterface: LifeStyleInterface,
    val translationEnabled: Boolean
) :
    RecyclerView.Adapter<LifeStyleManagementAdapter.ViewHolder>() {

    private var adapterList = ArrayList<LifeStyleManagement>()
    private var selectedItem: Long = -1L

    inner class ViewHolder(val binding: LifestyleManagementAdapterBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val context: Context = binding.root.context

        fun bind(position: Int, item: LifeStyleManagement) {
            binding.headerDivider.visibility =
                if (position == adapterList.size - 1) View.GONE else View.VISIBLE
            if (translationEnabled){
                binding.tvRefferedFor.text = item.cultureValue ?: (item.referredFor?:getLifeStyleTranslatedName(item.lifestyle,translationEnabled))
            }else{
                binding.tvRefferedFor.text = item.referredFor?:getLifeStyleTranslatedName(
                    item.lifestyle,
                    translationEnabled
                )
            }
            binding.tvRefDate.text = "-"
            binding.tvAssessedDate.text = context.getString(R.string.not_assessed)
            binding.tvAssessedDate.setTextColor(getTextColor(context, item.assessedDate))

            item.referredDate?.let {
                binding.tvRefDate.text = DateUtils.convertDateTimeToDate(
                    it,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DateUtils.DATE_FORMAT_ddMMMyyyy
                )
            }

            item.assessedDate?.let {
                binding.tvAssessedDate.text = DateUtils.convertDateTimeToDate(
                    it,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DateUtils.DATE_FORMAT_ddMMMyyyy
                )
            }
            binding.tvRefBy.text = "-"

            binding.tvRefBy.text = getReferredBy(item.referredBy, item.referredByDisplay)

            binding.tvRefBy.setTextColor(getTextColor(context, item.referredBy))

            binding.tvAssessedBy.text = context.getString(R.string.not_available)
            getAssessedBy(item.assessedBy)?.let {
                binding.tvAssessedBy.text = it
            }
            binding.tvAssessedBy.setTextColor(getTextColor(context, item.assessedBy))

            viewVisibility(item.id, binding)

            binding.ivDelete.visibility =
                if (item.id != null && item.assessedDate == null) View.VISIBLE else View.GONE

            binding.clinicianNotesLayout.root.visibility = View.VISIBLE
            binding.clinicianNotesLayout.tvKey.text = context.getString(R.string.clinician_notes)
            binding.clinicianNotesLayout.tvValue.text =
                if (item.clinicianNote.isNullOrBlank()) "-" else item.clinicianNote

            binding.ivDropDown.rotation = 0f

            binding.ivDropDown.safeClickListener(this)
            binding.tvRefferedFor.safeClickListener(this)
            binding.ivRemove.safeClickListener(this)
            binding.ivDelete.safeClickListener(this)
            binding.root.safeClickListener(this)

            if (item.id == selectedItem) {
                binding.resultsLayout.visibility = View.VISIBLE
                rotateArrow180f(binding.ivDropDown)
                selectedItem = -1L
                updateResultLayout(context, binding, item.assessedDate, item)
            } else
                binding.resultsLayout.visibility = View.GONE
        }

        override fun onClick(mView: View?) {
            when (mView?.id) {
                binding.ivRemove.id -> {
                    if (layoutPosition < adapterList.size) {
                        adapterList[layoutPosition].let { item ->
                            if (item.id == null)
                                lifeStyleInterface.removeElement(item)
                            else
                                showMessage(context, item)
                        }
                    }
                }

                binding.ivDelete.id -> {
                    binding.ivRemove.performClick()
                }

                binding.tvRefferedFor.id, binding.root.id -> {
                    if (binding.ivDropDown.visibility == View.VISIBLE)
                        binding.ivDropDown.performClick()
                }

                binding.ivDropDown.id -> {
                    if (binding.resultsLayout.visibility == View.GONE) {
                        if (layoutPosition < adapterList.size) {
                            selectedItem = adapterList[layoutPosition].id ?: -1L
                            notifyItemChanged(layoutPosition)
                        }
                    } else {
                        rotateArrow0f(binding.ivDropDown)
                        binding.resultsLayout.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun getLifeStyleTranslatedName(lifestyle: List<Any>?, translationEnabled: Boolean): String {

        val name = lifestyle?.mapNotNull { mod ->
            if (mod is Map<*,*>){
                if (translationEnabled){
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

    private fun viewVisibility(_id: Long?, binding: LifestyleManagementAdapterBinding) {
        if (_id != null) {
            binding.ivDropDown.visibility = View.VISIBLE
            binding.ivRemove.visibility = View.GONE
        } else {
            binding.ivDropDown.visibility = View.INVISIBLE
            binding.ivRemove.visibility = View.VISIBLE
        }
    }

    private fun showMessage(context: Context, item: LifeStyleManagement) {
        if ((context as BaseActivity).connectivityManager.isNetworkAvailable()) {
            context.showAlertDialogWithComments(
                title = context.getString(R.string.confirmation),
                message = context.getString(R.string.delete_confirmation),
                positiveButtonName = context.getString(
                    R.string.ok
                ),
                isNegativeButtonNeed = true,
                negativeButtonName = context.getString(R.string.cancel),
                showComment = false
            ) { isPositiveResult, _ ->
                if (isPositiveResult) {
                    lifeStyleInterface.removeElement(item)
                }
            }
        } else {
            context.showErrorDialogue(
                context.getString(R.string.error),
                context.getString(R.string.no_internet_error),
                false,
                ){}
        }
    }

    private fun getAssessedBy(assessedBy: HashMap<String, String>?): String? {
        assessedBy?.let {
            var text = ""
            if (assessedBy.containsKey(DefinedParams.First_Name))
                text = assessedBy[DefinedParams.First_Name] as String
            if (assessedBy.containsKey(DefinedParams.Last_Name))
                text = "$text ${assessedBy[DefinedParams.Last_Name] as String}"
            return text
        }
        return null
    }

    private fun getReferredBy(referredBy: Any?, referredByDisplay: String?): String {
        var text = ""
        if (referredBy != null && referredBy is Map<*, *>) {
            referredBy.let { map ->
                if (map.containsKey(DefinedParams.First_Name))
                    text = map[DefinedParams.First_Name] as String
                if (map.containsKey(DefinedParams.Last_Name))
                    text = "$text ${map[DefinedParams.Last_Name] as String}"
            }
        } else {
            text = referredByDisplay ?: ""
        }
        return text
    }

    private fun updateResultLayout(
        context: Context,
        binding: LifestyleManagementAdapterBinding,
        assessedDate: String?,
        item: LifeStyleManagement
    ) {
        if (assessedDate != null) {
            binding.assessmentNotesLayout.root.visibility = View.VISIBLE
            binding.otherNotesLayout.root.visibility = View.VISIBLE

            binding.assessmentNotesLayout.tvKey.text =
                context.getString(R.string.lifestyle_assessment_label)
            binding.assessmentNotesLayout.tvValue.text =
                if (item.lifestyleAssessment.isNullOrBlank()) "-" else item.lifestyleAssessment

            binding.otherNotesLayout.tvKey.text = context.getString(R.string.other_notes)
            binding.otherNotesLayout.tvValue.text =
                if (item.otherNote.isNullOrBlank()) "-" else item.otherNote
        } else {
            binding.assessmentNotesLayout.root.visibility = View.GONE
            binding.otherNotesLayout.root.visibility = View.GONE
        }
    }

    private fun rotateArrow180f(view: View) {
        val ivArrow = ObjectAnimator.ofFloat(view, "rotation", 0f, 180f)
        ivArrow.start()
    }

    private fun rotateArrow0f(view: View) {
        val ivArrow = ObjectAnimator.ofFloat(view, "rotation", 180f, 0f)
        ivArrow.start()
    }

    private fun getTextColor(context: Context, enteredBy: Any?): Int {
        return if (enteredBy == null) context.getColor(R.color.disabled_text_color) else context.getColor(
            R.color.navy_blue
        )
    }

    fun getData() = adapterList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LifestyleManagementAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        adapterList.let {
            holder.bind(position, it[position])
        }
    }

    override fun getItemCount(): Int = adapterList.size

    fun submitData(list: ArrayList<LifeStyleManagement>) {
        adapterList = ArrayList(list)
        notifyItemRangeChanged(0, adapterList.size)
    }
}