package com.medtroniclabs.opensource.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "frequency")
data class FrequencyEntity(
    @PrimaryKey
    @SerializedName("id")
    var _id: Long,
    var name: String,
    var description: String? = null,
    @ColumnInfo(name = "display_order")
    val displayOrder: Int
)