package com.medtroniclabs.opensource.ui

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.MedicalReviewConstant.enabled
import com.medtroniclabs.opensource.common.MedicalReviewConstant.otherTypeText
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.databinding.CustomLayoutTagviewComponentBinding
import com.medtroniclabs.opensource.databinding.OtherChipLayoutBinding
import com.medtroniclabs.opensource.db.tables.*
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.BloodGlucoseID
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.Diabetes
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.Hypertension
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.TYPE_DELETE
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.TransferPatient
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.diagnosisEnableDisable
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.getActualNameOfChip


class TagListCustomView(
    val context: Context,
    private val chipGroup: ChipGroup,
    val otherSingleSelect: Boolean? = null,
    val otherCallBack: ((name: String, isChecked: Boolean) -> Unit)? = null,
    val callBack: ((isEmpty: Boolean) -> Unit)? = null
) {

    fun addChipItemList(
        chipItemList: List<Any>,
        selectedChipItemList: List<Any>? = null,
        diagnosisMap: HashMap<String, MutableList<String>>? = null
    ) {
        chipGroup.removeAllViews()
        chipItemList.forEach { data ->
            getChipText(data)?.let { chipData ->
                if (!chipData.second.startsWith(otherTypeText, ignoreCase = true))
                    chipBinding(data, chipData, selectedChipItemList, diagnosisMap)
                else
                    otherChipBinding(data, chipData, selectedChipItemList)
            }
        }
    }

    private fun otherChipBinding(
        data: Any,
        chipData: Pair<String?, String>,
        selectedChipItemList: List<Any>?
    ) {
        val binding = OtherChipLayoutBinding.inflate(LayoutInflater.from(context))
        binding.root.tag = otherTypeText
        if (SecuredPreference.getIsTranslationEnabled()){
            binding.tvOther.text = chipData.first ?: chipData.second
        }else{
            binding.tvOther.text = chipData.second
        }
        binding.tvOther.tag = data
        binding.tvOther.safeClickListener {
            otherOnClick(binding.tvOther, binding.tagView)
        }

        chipGroup.addView(binding.root)
        selectedChipItemList?.let { selectedChipItemList ->
            val isAlreadySelected = selectedChipItemList.contains(DefinedParams.Compliance_Type_Other)
            if (isAlreadySelected) {
                binding.tvOther.performClick()
            }
        }
    }

    private fun otherOnClick(
        tvOther: AppCompatTextView,
        tagView: View
    ) {
        if (tagView.tag == null) {
            tagView.tag = enabled
            tvOther.typeface =
                ResourcesCompat.getFont(context, R.font.inter_bold)
            tvOther.background =
                ContextCompat.getDrawable(context, R.drawable.other_button_enabled)
            tvOther.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
            resetNone()
            if(otherSingleSelect == true)
                resetOtherChip()
        } else {
            tagView.tag = null
            tvOther.typeface =
                ResourcesCompat.getFont(context, R.font.inter_regular)
            tvOther.background =
                ContextCompat.getDrawable(context, R.drawable.other_view_background)
            tvOther.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.primary_medium_blue
                )
            )
        }
        if (otherCallBack != null)
            otherCallBack.invoke(otherTypeText, tagView.tag != null)
        else
            callBack?.invoke(chipGroup.checkedChipIds.isEmpty())
    }


    private fun resetOtherChip() {
        chipGroup.clearCheck()
    }

    private fun chipBinding(
        data: Any,
        chipData: Pair<String?, String>,
        selectedChipItemList: List<Any>?,
        diagnosisMap: HashMap<String, MutableList<String>>?
    ) {
        val binding = CustomLayoutTagviewComponentBinding.inflate(LayoutInflater.from(context))
        val chip = binding.root
        chip.chipBackgroundColor =
            getColorStateList(
                context.getColor(R.color.medium_blue),
                context.getColor(R.color.white)
            )
        chip.tag = data
         if (SecuredPreference.getIsTranslationEnabled()){
             chip.text = chipData.first?:chipData.second
         }else{
             chip.text = chipData.second
         }
        chip.setChipBackgroundColorResource(R.color.diagnosis_confirmation_selector)
        chip.chipStrokeWidth = 3f
        chip.setTextColor(
            getColorStateList(
                context.getColor(R.color.white),
                context.getColor(R.color.navy_blue)
            )
        )
        chip.chipStrokeColor = getColorStateList(
            context.getColor(R.color.medium_blue),
            context.getColor(R.color.mild_gray)
        )

        chip.setOnCheckedChangeListener { _, isChecked ->
            if(otherSingleSelect == true && isChecked)
                uncheckOtherChip()
            if (otherCallBack != null)
                otherCallBack.invoke(chipData.second, isChecked)
            else
                callBack?.invoke(chipGroup.checkedChipIds.isEmpty())

            if (isChecked) {
                diagnosisMap?.let {
                    diagnosisEnableDisable(
                        diagnosisMap,
                        chipGroup,
                        chipData.second,
                        arrayListOf(Diabetes, Hypertension, BloodGlucoseID, TransferPatient, TYPE_DELETE)
                    )
                }
                chip.typeface = ResourcesCompat.getFont(context, R.font.inter_bold)
                chip.chipStrokeWidth = 0f
            } else {
                chip.typeface = ResourcesCompat.getFont(context, R.font.inter_regular)
                chip.chipStrokeWidth = 3f
            }
            if (chipData.second.equals(context.getString(R.string.none), ignoreCase = true)) {
                if (isChecked)
                    onNoneSelected()
            } else if (isChecked)
                resetNone()
        }
        if(otherSingleSelect == true)
            chipGroup.isSingleSelection = true
        chipGroup.addView(binding.root)

        selectedChipItemList?.let { chipItemList ->
            val dataValue = getChipText(data)
            dataValue?.second?.let { chipItem ->
                val isAlreadySelected = chipItemList.contains(chipItem)
                if (isAlreadySelected) {
                    chip.isChecked = true
                }
            }
        }
    }

    private fun uncheckOtherChip() {
        chipGroup.findViewWithTag<LinearLayout>(otherTypeText)?.let { chipLayout ->
            val tvOther = chipLayout.getChildAt(0)
            val tagView = chipLayout.getChildAt(1)
            tagView?.tag?.let {
                if (tvOther != null && tvOther is AppCompatTextView) {
                    otherOnClick(tvOther, tagView)
                }
            }
        }
    }

    private fun getChipText(data: Any): Pair<String?,String>? {
        when (data) {
            is ComorbidityEntity -> {
                return Pair(data.cultureValue,data.comorbidity)
            }
            is ComplicationEntity -> {
                return Pair(data.cultureValue,data.complication)
            }
            is CurrentMedicationEntity -> {
                return Pair(data.cultureValue,data.medicationName)
            }
            is PhysicalExaminationEntity -> {
                return Pair(data.cultureValue,data.name)
            }
            is ComplaintsEntity -> {
                return Pair(data.cultureValue,data.name)
            }
            is NutritionLifeStyle -> {
                data.name?.let {
                    return Pair(data.cultureValue,data.name!!)
                }?:kotlin.run {
                    return null
                }
            }
            is DiagnosisEntity -> {
                return Pair(data.cultureValue,data.diagnosis)
            }
            is ShortageReasonEntity -> {
                return Pair(data.cultureValue,data.reason)
            }
            is String -> {
                return Pair(null,data)
            }
            else -> return null
        }
    }

    private fun getColorStateList(
        selectedColor: Int,
        unSelectedColor: Int
    ): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_selected),
            intArrayOf(-android.R.attr.state_selected),
        )

        val colors = intArrayOf(
            selectedColor,
            unSelectedColor,
            selectedColor,
            unSelectedColor
        )
        return ColorStateList(states, colors)
    }

    fun getSelectedTags(): List<Any> {
        val tags = arrayListOf<Any>()
        for (chipId in 0..chipGroup.childCount) {
            chipGroup.getChildAt(chipId)?.let {
                if (it is Chip && it.isChecked) {
                    tags.add(it.tag)
                }
            }
        }
        chipGroup.findViewWithTag<LinearLayout>(otherTypeText)?.let { layout ->
            layout.getChildAt(1)?.tag?.let {
                layout.getChildAt(0)?.tag?.let { tag ->
                    tags.add(tag)
                }
            }
        }
        return tags
    }

    private fun resetNone() {
        for (chipId in chipGroup.checkedChipIds) {
            val chip = chipGroup.findViewById<Chip>(chipId)
            var chipActualText:String ?= getActualNameOfChip(chip.tag)
            if (chip != null && chipActualText.equals(context.getString(R.string.none), true) && chip.isChecked) {
                chip.isChecked = false
                break
            }
        }
    }

    fun clearSelection() {
        chipGroup.clearCheck()
    }

    private fun onNoneSelected() {
        for (chipId in chipGroup.checkedChipIds) {
            chipGroup.findViewById<Chip>(chipId)?.let { chip ->
                val actualText = getActualNameOfChip(chip.tag)
                if (!actualText.equals(context.getString(R.string.none), true)) {
                    chip.isChecked = false
                }
            }
        }
        chipGroup.findViewWithTag<LinearLayout>(otherTypeText)?.let { chipLayout ->
            chipLayout.getChildAt(1)?.tag?.let {
                chipLayout.getChildAt(0)?.let { view ->
                    (view as AppCompatTextView).performClick()
                }
            }
        }
    }
}