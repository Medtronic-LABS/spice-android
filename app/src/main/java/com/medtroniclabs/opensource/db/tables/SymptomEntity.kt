package com.medtroniclabs.opensource.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "SymptomEntity")
class SymptomEntity(
    @PrimaryKey
    @SerializedName("id")
    val _id: Long,
    @SerializedName("name")
    val symptom: String,
    val type: String? = null,
    @ColumnInfo(name = "culture_value")
    val cultureValue: String ?= null,
    @ColumnInfo(name = "display_order")
    var displayOrder: Int? = null
)



