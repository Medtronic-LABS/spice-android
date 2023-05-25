package com.medtroniclabs.opensource.formgeneration

import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.databinding.CardLayoutBinding
import com.medtroniclabs.opensource.databinding.SummaryLayoutBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.model.ConditionalModel
import com.medtroniclabs.opensource.formgeneration.model.FormLayout

class FormResultViewer(
    private val context: Context,
    private val parentLayout: LinearLayout
) : ContextWrapper(context) {

    fun populateResultViews(serverData: List<FormLayout>, resultMap: HashMap<String, Any>) {
        parentLayout.removeAllViews()
        createCardViewFamily()
        val isTranslate = SecuredPreference.getIsTranslationEnabled()
        serverData.forEach {
            if (it.isEditable && resultMap.containsKey(it.id)) {
                createSummaryLayout(it.title, it.titleCulture, resultMap[it.id],it.id,it,isTranslate)
            }
        }
        setConditionalElementsVisible(serverData,resultMap)
    }

    private fun createSummaryLayout(
        title: String,
        titleCulture: String?,
        any: Any?,
        id: String,
        formLayout: FormLayout,
        isTranslate: Boolean
    ) {
        val binding = SummaryLayoutBinding.inflate(LayoutInflater.from(context))
        binding.tvKey.text = if(isTranslate) (titleCulture ?: title) else title
        if (any is String && any.isNotBlank()) {
            binding.tvValue.text = any
        } else if (any is Boolean) {
            binding.tvValue.text = if(any) context.getString(R.string.yes) else context.getString(R.string.no)
        } else {
            binding.tvValue.text = getString(R.string.separator_hyphen)
        }
        binding.root.tag = id
        addFamilyView(binding.root)
        setViewVisibility(visibility = formLayout.visibility,binding.root)
    }

    private fun addFamilyView(root: View) {
        val view = parentLayout.findViewWithTag<LinearLayout>(context.getString(R.string.patient_details))
        if (view != null) {
            view.addView(root)
        } else {
            parentLayout.addView(root)
        }
    }


    private fun createCardViewFamily() {
        val binding = CardLayoutBinding.inflate(LayoutInflater.from(context))
        binding.cardTitle.text = context.getString(R.string.patient_details)
        binding.llFamilyRoot.tag = context.getString(R.string.patient_details)
        parentLayout.addView(binding.root)
    }

    private fun setViewVisibility(visibility: String?, root: View) {
        when (visibility) {
            DefinedParams.VISIBLE -> {
                root.visibility = View.VISIBLE
            }
            DefinedParams.INVISIBLE -> {
                root.visibility = View.INVISIBLE
            }
            DefinedParams.GONE -> {
                root.visibility = View.GONE
            }
            else -> {
                root.visibility = View.VISIBLE
            }
        }
    }

    private fun setConditionalElementsVisible(
        serverData: List<FormLayout>,
        resultMap: HashMap<String, Any>
    ) {
       val filterData = serverData.filter { it.isEditable && it.condition !=null && it.condition!!.isNotEmpty() }
       filterData.forEach { formLayout ->
           val actualValue = resultMap[formLayout.id]
           formLayout.condition?.forEach { conditionalModel ->
               if (actualValue is String) {
                   checkCondition(conditionalModel,actualValue)
               }else if (actualValue is Boolean){
                   if (actualValue){
                       checkCondition(conditionalModel,DefinedParams.Yes)
                   }else {
                       checkCondition(conditionalModel,DefinedParams.No)
                   }
               }
           }
       }
    }

    private fun checkCondition(model: ConditionalModel, actualValue: String?) {
        model.apply {
            targetId?: return
            val targetView = parentLayout.findViewWithTag<ConstraintLayout>(targetId)
            targetView?.let { view ->
                if (!eq.isNullOrBlank()) {
                    if (eq!= null && eq!!.equals(actualValue,true)) {
                        view.visibility = View.VISIBLE
                    } else {
                        view.visibility = View.GONE
                    }
                }
            }
        }
    }

}