package com.medtroniclabs.opensource.common

import android.text.InputFilter
import android.text.Spanned

class EmojiInputFilter : InputFilter {
    override fun filter(
        source: CharSequence,
        p1: Int,
        p2: Int,
        p3: Spanned?,
        p4: Int,
        p5: Int
    ): CharSequence {
        return source.filter {
            Character.getType(it) != Character.SURROGATE.toInt()
                    && Character.getType(it) != Character.OTHER_SYMBOL.toInt()
                    && Character.getType(it) != Character.NON_SPACING_MARK.toInt()
        }
    }
}