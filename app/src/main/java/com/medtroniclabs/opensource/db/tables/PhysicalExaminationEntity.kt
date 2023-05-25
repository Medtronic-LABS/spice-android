package com.medtroniclabs.opensource.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "physical_examination")
class PhysicalExaminationEntity(
    @PrimaryKey
    @SerializedName("id")
    val _id: Long,
    @ColumnInfo(name = "display_order")
    val displayOrder: Int,
    val name: String,
    @ColumnInfo(name = "culture_value")
    val cultureValue: String ?= null
)