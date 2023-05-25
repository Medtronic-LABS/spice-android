package com.medtroniclabs.opensource.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "shortage_reason")
data class ShortageReasonEntity(
    @PrimaryKey
    @SerializedName("id")
    val _id: Long,
    @SerializedName("name")
    val reason: String,
    val type: String,
    @ColumnInfo(name = "culture_value")
    val cultureValue: String ?= null
)