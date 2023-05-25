package com.medtroniclabs.opensource.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ConsentEntity")
data class ConsentEntity(
    @PrimaryKey
    var formType: String,
    var consentHtmlRaw: String,
    var uploadStatus: Boolean = false,
    var createdAt: Long = System.currentTimeMillis(),
    var userId: Long
)