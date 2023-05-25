package com.medtroniclabs.opensource.ui.indicator.interfaces

interface IndicatorType {
    companion object {
        /**
         * don't have indicator to show.
         */
        const val NONE = 0

        /**
         * the indicator shape like water-drop
         */
        const val CIRCULAR_BUBBLE = 1

        /**
         * the indicator corners is rounded shape
         */
        const val ROUNDED_RECTANGLE = 2

        /**
         * the indicator corners is square shape
         */
        const val RECTANGLE = 3

        /**
         * set custom indicator you want
         */
        const val CUSTOM = 4
    }
}