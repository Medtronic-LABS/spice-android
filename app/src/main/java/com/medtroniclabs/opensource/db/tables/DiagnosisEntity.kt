package com.medtroniclabs.opensource.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "diagnosis")
data class DiagnosisEntity(
    @PrimaryKey
    @SerializedName("id")
    val _id: Long,
    @SerializedName("name")
    val diagnosis: String,
    val type: String? = null,
    val gender: String? = null,
    @ColumnInfo("culture_value")
    val cultureValue:String?= null,
    @ColumnInfo(name = "display_order")
    var displayOrder: Int? = null
)