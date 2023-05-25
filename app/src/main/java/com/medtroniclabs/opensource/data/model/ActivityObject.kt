package com.medtroniclabs.opensource.data.model


data class ActivityObject(
    val roleName: String,
    val menus: Map<String, String>,
    val cultureValues:Map<String,String>
)