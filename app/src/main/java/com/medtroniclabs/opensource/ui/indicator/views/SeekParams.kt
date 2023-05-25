package com.medtroniclabs.opensource.ui.indicator.views

import com.medtroniclabs.opensource.ui.views.IndicatorSeekBar

class SeekParams internal constructor(  //for continuous series seek bar
    // The SeekBar whose progress has changed
    var seekBar: IndicatorSeekBar
) {
    //The current progress level.The default value for min is 0, max is 100.
    var progress = 0

    //The current progress level.The default value for min is 0.0, max is 100.0.
    var progressFloat = 0f

    //True if the progress change was initiated by the user, otherwise by setProgress() programmatically.
    var fromUser = false

    //for discrete series seek bar
    //the thumb location on tick when the section changed, continuous series will be zero.
    var thumbPosition = 0

    //the text below tick&thumb when the section changed.
    var tickText: String? = null
}