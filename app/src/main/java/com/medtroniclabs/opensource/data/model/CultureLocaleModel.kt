package com.medtroniclabs.opensource.data.model

data class CultureLocaleModel(
    val userId: Long,
    val cultureId: Long,
    val name: String? = null,
)

data class CulturePreference(
    var cultureId: Long,
    var name: String,
    var isTranslationEnabled: Boolean
)
