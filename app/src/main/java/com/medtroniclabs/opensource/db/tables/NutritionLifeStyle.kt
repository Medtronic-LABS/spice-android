package com.medtroniclabs.opensource.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "NutritionLifeStyle")
data class NutritionLifeStyle(
    @PrimaryKey
    @SerializedName("id")
    val _id: Long,
    var name: String? = null,
    @ColumnInfo(name = "display_order")
    var displayOrder: Int? = null,
    @ColumnInfo(name = "culture_value")
    val cultureValue: String ?= null)

