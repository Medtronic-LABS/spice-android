package com.medtroniclabs.opensource.ui.medicalreview

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.LifeStyleAnswerUIModel
import com.medtroniclabs.opensource.data.model.LifeStyleUIModel


class LifeStyleCustomView : LinearLayout {

    private lateinit var viewContext: Context

    var answerList: ArrayList<LifeStyleAnswerUIModel>? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    private fun init(context: Context) {
        viewContext = context
        orientation = HORIZONTAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun addViewElements(
        lifeStyleUI: LifeStyleUIModel,
        callback: ((model: LifeStyleUIModel, answerModel: LifeStyleAnswerUIModel) -> Unit?)?
    ) {
        removeAllViews()
        answerList = lifeStyleUI.lifestyleAnswer
        val isTranslationEnabled = SecuredPreference.getIsTranslationEnabled()
        answerList?.apply {
            forEachIndexed { index, answerModel ->
                val textView = TextView(viewContext, null, 0, R.style.Form_MH_Style_with_padding)
                val param = LayoutParams(
                    0,
                    LayoutParams.MATCH_PARENT,
                    1.0f
                )
                textView.isSelected = answerModel.isSelected
                textView.layoutParams = param
                if (isTranslationEnabled){
                    textView.text = answerModel.cultureAnswerValue ?:answerModel.name
                }else{
                    textView.text = answerModel.name
                }
                getBackgroundDrawable(index, lifeStyleUI.lifestyleAnswer)?.let {
                    textView.background = it
                }
                textView.safeClickListener {
                    if (!answerModel.isSelected) {
                        forEach {
                            it.isSelected = false
                            it.comments = null
                        }
                        answerModel.isSelected = true
                        addViewElements(lifeStyleUI, callback)
                    }
                    callback?.invoke(lifeStyleUI, answerModel)
                }
                if (answerModel.isSelected)
                    callback?.invoke(lifeStyleUI, answerModel)
                addView(textView)
            }
        }
    }

    private fun getBackgroundDrawable(
        index: Int,
        list: ArrayList<LifeStyleAnswerUIModel>
    ): Drawable? {
        when (index) {
            0 -> return ContextCompat.getDrawable(viewContext, R.drawable.left_mh_view_selector)
            list.size - 1 -> return ContextCompat.getDrawable(
                viewContext,
                R.drawable.right_mh_view_selector
            )
        }
        return null
    }

    fun getSelectedAnswersList(): ArrayList<LifeStyleAnswerUIModel>? {
        return answerList
    }
}