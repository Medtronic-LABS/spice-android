package com.medtroniclabs.opensource.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LanguageEntity")
data class LanguageEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var languageDetail: String,
    var time: Long = System.currentTimeMillis()
)