package com.medtroniclabs.opensource.db.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "MedicalComplianceEntity")
data class MedicalComplianceEntity(
    @PrimaryKey
    @SerializedName("id")
    val _id: Long,
    val name: String,
    @ColumnInfo(name = "parent_compliance_id")
    val parentComplianceId: Long? = null,
    @ColumnInfo(name = "is_child_exists")
    @SerializedName("childExists")
    var isChildExists: Boolean = false,
    @ColumnInfo(name = "display_order")
    val displayOrder : Int ?= null,
    @ColumnInfo(name = "culture_value")
    val cultureValue: String ?= null
)