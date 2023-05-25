package com.medtroniclabs.opensource.custom.signature.view

import android.annotation.SuppressLint
import android.view.ViewTreeObserver

object ViewTreeObserverCompat {
    /**
     * Remove a previously installed global layout callback.
     * @param observer the view observer
     * @param victim the victim
     */
    @SuppressLint("NewApi")
    fun removeOnGlobalLayoutListener(
        observer: ViewTreeObserver,
        victim: ViewTreeObserver.OnGlobalLayoutListener?
    ) { // Future (API16+)...
        observer.removeOnGlobalLayoutListener(victim)
    }
}