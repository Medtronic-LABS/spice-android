package com.medtroniclabs.opensource.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TreatmentPlanEntity")
data class TreatmentPlanEntity(
    @PrimaryKey
    var frequencyKey: String,
    var value: String,
    var createdAt: Long = System.currentTimeMillis(),
    var userId: Long
)
