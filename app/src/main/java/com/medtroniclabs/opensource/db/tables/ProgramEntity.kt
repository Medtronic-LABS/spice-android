package com.medtroniclabs.opensource.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "ProgramEntity")
data class ProgramEntity(
    @PrimaryKey
    @SerializedName("id")
    val _id: Long,
    val name: String,
    val sites: ArrayList<ProgramSites>,
    @ColumnInfo(name = "tenant_id")
    val tenantId: Long
)

data class ProgramSites(val id: Long, val name: String)