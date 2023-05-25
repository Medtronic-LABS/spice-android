package com.medtroniclabs.opensource.common

import android.os.SystemClock
import android.view.View

class SafeClickListener(private val clickListener: View.OnClickListener) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < 1000) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        clickListener.onClick(v)
    }
}