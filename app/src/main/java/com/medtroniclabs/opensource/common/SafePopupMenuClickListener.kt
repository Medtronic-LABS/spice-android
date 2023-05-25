package com.medtroniclabs.opensource.common

import android.os.SystemClock
import android.view.MenuItem

class SafePopupMenuClickListener(private val clickListener: androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener): androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener {
    private var lastTimeClicked: Long = 0
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < 1000) {
            return false
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        return clickListener.onMenuItemClick(item)
    }
}