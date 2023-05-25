package com.medtroniclabs.opensource.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "SiteEntity")
data class SiteEntity(
    @PrimaryKey
    var id: Long,
    var name: String,
    var userSite: Boolean = false,
    var role: String? = null,
    var roleName:String?=null,
    var createdAt: Long = System.currentTimeMillis(),
    var userId: Long,
    var tenantId: Long,
    var isDefault: Boolean = false,
    @ColumnInfo("isQualipharmEnabledSite")
    @SerializedName("is_qualipharm_enabled_site")
    var isQualipharmEnabledSite: Boolean? = null
) : Serializable