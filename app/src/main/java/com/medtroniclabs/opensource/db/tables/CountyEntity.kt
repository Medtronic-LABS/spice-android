package com.medtroniclabs.opensource.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "County")
data class CountyEntity(
    @PrimaryKey
    @SerializedName("id")
    val _id: Long,
    val name: String,
    @SerializedName("countryId")
    val country: Long
)