package com.medtroniclabs.opensource.ui.indicator.interfaces

import com.medtroniclabs.opensource.ui.views.IndicatorSeekBar
import com.medtroniclabs.opensource.ui.indicator.views.SeekParams

interface OnSeekChangeListener {
    /**
     * Notification that the progress level has changed.
     *
     *
     * Clients can use the fromUser parameter to distinguish user-initiated changes from
     * those that occurred programmatically, also, if the seek bar type is discrete series,
     * clients can use the thumbPosition parameter to check the thumb position on ticks and
     * tick text parameter to get the tick text which located at current thumb below.
     *
     * @param seekParams the params info about the seeking bar
     */
    fun onSeeking(seekParams: SeekParams?)

    /**
     * Notification that the user has started a touch gesture. Clients may want to use this
     * to disable advancing the seek bar.
     *
     * @param seekBar The SeekBar in which the touch gesture began
     */
    fun onStartTrackingTouch(seekBar: IndicatorSeekBar?)

    /**
     * Notification that the user has finished a touch gesture. Clients may want to use this
     * to re-enable advancing the seek bar.
     *
     * @param seekBar The SeekBar in which the touch gesture began
     */
    fun onStopTrackingTouch(seekBar: IndicatorSeekBar?)
}