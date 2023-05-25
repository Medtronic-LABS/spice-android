package com.medtroniclabs.opensource.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class FloatingDetectorFrameLayout : FrameLayout {
    var isObscured: Boolean? = null
    var isPartiallyObscured: Boolean? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onFilterTouchEventForSecurity(ev: MotionEvent): Boolean {
        val checkIsObscured = ev.flags and MotionEvent.FLAG_WINDOW_IS_OBSCURED != 0
        val checkIsPartiallyObscured =
            ev.flags and MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED != 0
        if (checkIsObscured != isObscured || checkIsPartiallyObscured != isPartiallyObscured) {
            isObscured = checkIsObscured
            isPartiallyObscured = checkIsPartiallyObscured
            if ((isObscured != null && isObscured!!) || (isPartiallyObscured != null && isPartiallyObscured!!)) {
//                throw IllegalStateException()
            }
        }
        return super.onFilterTouchEventForSecurity(ev)
    }
}