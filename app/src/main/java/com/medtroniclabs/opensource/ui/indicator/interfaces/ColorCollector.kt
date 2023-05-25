package com.medtroniclabs.opensource.ui.indicator.interfaces

import androidx.annotation.ColorInt

interface ColorCollector {
    /**
     * to collect each section track's color
     *
     * @param colorIntArr ColorInt the container for each section tracks' color.
     * this array's length will auto equals section track' count.
     * @return True if apply color , otherwise no change
     */
    fun collectSectionTrackColor(@ColorInt colorIntArr: IntArray?): Boolean
}