package com.medtroniclabs.opensource.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "SubCounty")
class SubCountyEntity(
    @PrimaryKey
    @SerializedName("id")
    val _id: Long,
    val name: String,
    @SerializedName("countyId")
    val county: Long
)