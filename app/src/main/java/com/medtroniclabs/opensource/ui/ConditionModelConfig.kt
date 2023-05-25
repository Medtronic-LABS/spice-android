package com.medtroniclabs.opensource.ui

sealed class ConditionModelConfig {
    object VISIBILITY : ConditionModelConfig()
    object ENABLED : ConditionModelConfig()
}
