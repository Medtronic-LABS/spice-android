package com.medtroniclabs.opensource.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FormEntity")
data class FormEntity(
    @PrimaryKey
    var formType: String,
    var formString: String,
    var uploadStatus: Boolean = false,
    var createdAt: Long = System.currentTimeMillis(),
    var userId: Long
)