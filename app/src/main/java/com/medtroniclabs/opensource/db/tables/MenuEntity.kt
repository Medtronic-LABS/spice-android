package com.medtroniclabs.opensource.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "MenuEntity")
data class MenuEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var name: String,
    var role: String,
    var menuId:String,
    var createdAt: Long = System.currentTimeMillis(),
    var userId: Long,
    @ColumnInfo(name = "culture_value")
    val cultureValue: String ?= null
) : Serializable