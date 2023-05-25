package com.medtroniclabs.opensource.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "unit_metric_entity")
data class UnitMetricEntity(
    @PrimaryKey
    @SerializedName("id")
    val _id: Long,
    @SerializedName("name")
    val unit: String,
    val type: String
)