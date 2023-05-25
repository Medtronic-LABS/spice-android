package com.medtroniclabs.opensource.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Country")
data class CountryEntity(
    @PrimaryKey
    @SerializedName("id")
    val _id: Long,
    val name: String,
    @ColumnInfo(name = "unit_measurement")
    val unitMeasurement: String?
)