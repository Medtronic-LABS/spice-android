package com.medtroniclabs.opensource.db.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "Cultures")
data class CulturesEntity(
    @PrimaryKey
    val id : Long,
    val name : String
): Serializable