package com.medtroniclabs.opensource.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "lifestyle")
data class LifestyleEntity(
    @PrimaryKey
    @SerializedName("id")
    val _id: Long,
    @ColumnInfo(name = "display_order")
    val displayOrder: Int,
    @SerializedName("name")
    val lifestyle: String,
    @ColumnInfo(name = "lifestyle_answer")
    @SerializedName("answers")
    val lifestyleAnswer: ArrayList<LifeStyleAnswer>,
    @ColumnInfo(name = "lifestyle_type")
    @SerializedName("type")
    val lifestyleType: String,
    @ColumnInfo(name = "culture_question_value")
    @SerializedName("cultureValue")
    val cultureQuestionValue: String ?= null
)

data class LifeStyleAnswer(
    val name: String,
    @SerializedName("is_answer_dependent") val isAnswerDependent: Boolean,
    @SerializedName("cultureValueAnswers")
    val cultureAnswerValue:String ? = null
)