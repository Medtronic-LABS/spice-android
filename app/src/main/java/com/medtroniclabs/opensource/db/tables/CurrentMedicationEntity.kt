package com.medtroniclabs.opensource.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "current_medication")
data class CurrentMedicationEntity(
    @PrimaryKey
    @SerializedName("id")
    val _id: Long,
    @ColumnInfo(name = "display_order")
    val displayOrder: Int,
    @ColumnInfo(name = "medication_name")
    @SerializedName("name")
    val medicationName: String,
    val type:String,
    @ColumnInfo(name = "culture_value")
    val cultureValue: String ?= null
)