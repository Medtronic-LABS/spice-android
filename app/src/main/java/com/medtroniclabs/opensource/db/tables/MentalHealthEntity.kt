package com.medtroniclabs.opensource.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MentalHealthEntity")
data class MentalHealthEntity(
    @PrimaryKey
    var formType: String,
    var formString: String?,
    var createdAt: Long = System.currentTimeMillis(),
    var userId: Long
)
