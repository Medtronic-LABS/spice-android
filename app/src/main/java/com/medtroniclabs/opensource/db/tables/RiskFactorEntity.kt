package com.medtroniclabs.opensource.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "RiskFactorEntity")
data class RiskFactorEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var nonLabEntity: String,
    var uploadStatus: Boolean = false,
    var createdAt: Long = System.currentTimeMillis()
)