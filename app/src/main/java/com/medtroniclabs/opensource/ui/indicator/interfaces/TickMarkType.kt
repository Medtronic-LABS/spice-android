package com.medtroniclabs.opensource.ui.indicator.interfaces

interface TickMarkType {
    companion object {
        /**
         * don't show the tickMarks
         */
        const val NONE = 0

        /**
         * show tickMarks shape as regular oval
         */
        const val OVAL = 1

        /**
         * show tickMarks shape as regular square
         */
        const val SQUARE = 2

        /**
         * show tickMarks shape as vertical line , line size is 2 dp.
         */
        const val DIVIDER = 3
    }
}