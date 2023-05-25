package com.medtroniclabs.opensource.ui

import android.content.Context
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.medtroniclabs.opensource.R

open class MySpannable(private val context: Context, private val isUnderline: Boolean) :
    ClickableSpan() {

    override fun updateDrawState(ds: TextPaint) {
        ds.isUnderlineText = isUnderline
        ds.color = ContextCompat.getColor(context, R.color.cobalt_blue)
    }

    override fun onClick(p0: View) {
    }
}