package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class  LifeStyleUIModel(
    val _id: Long,
    @SerializedName("display_order")
    val displayOrder: Int,
    val lifestyle: String,
    @SerializedName("lifestyle_answer")
    val lifestyleAnswer: ArrayList<LifeStyleAnswerUIModel>,
    @SerializedName("lifestyle_type")
    val lifestyleType: String,
    @SerializedName("culture_question_value")
    val cultureQuestionValue:String ? = null
)


data class LifeStyleAnswerUIModel(
    val name: String,
    var isSelected: Boolean = false,
    @SerializedName("is_answer_dependent")
    val isAnswerDependent: Boolean,
    var comments: String? = null,
    @SerializedName("culture_answer_value")
    val cultureAnswerValue:String?= null
)