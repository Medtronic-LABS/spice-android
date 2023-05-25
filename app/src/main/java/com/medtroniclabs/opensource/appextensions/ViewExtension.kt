package com.medtroniclabs.opensource.appextensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.common.SafeClickListener
import com.medtroniclabs.opensource.common.SafePopupMenuClickListener
import com.medtroniclabs.opensource.common.ViewUtil.getResId
import java.io.Serializable
import java.util.*

fun TextView.markMandatory() {
    text = buildSpannedString {
        append(text)
        color(Color.RED) { append(" *") } // Mind the space prefix.
    }
}

fun TextView.markNonMandatory() {
    text = buildSpannedString {
        append(text)
        color(Color.RED) { append(" *") } // Mind the space prefix.
    }
}

fun TextView.capitalizeFirstChar() {
    text = text.toString().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()  }
}

var TextView.textSizeSsp: Int?
    get() {
        return textSize.toInt()
    }
    set(size) {
        size ?: return
        val sizeString = "_${size}ssp"
        val resId = getResId(sizeString, R.dimen::class.java)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelOffset(resId).toFloat())
    }

/**
 * Disable the copy paste value for edit text
 */
fun TextView.disableCopyPaste() {
    isLongClickable = false
    setTextIsSelectable(false)
    customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
        override fun onActionItemClicked(p0: android.view.ActionMode?, p1: MenuItem?): Boolean {
            return false
        }

        override fun onCreateActionMode(p0: android.view.ActionMode?, p1: Menu?): Boolean {
            return false
        }

        override fun onPrepareActionMode(p0: android.view.ActionMode?, p1: Menu?): Boolean {
            return false
        }


        override fun onDestroyActionMode(p0: android.view.ActionMode?) {
            /**
             * this method is default override
             */
        }
    }
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.ssp: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

/**
 * Returns the `location` object as a human readable string.
 */
fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}

fun View.safeClickListener(clickListener: View.OnClickListener) {
    val safeClickListener = SafeClickListener(clickListener)
    setOnClickListener(safeClickListener)
}

fun androidx.appcompat.widget.PopupMenu.safePopupMenuClickListener(clickListener: androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener){
    val safePopupMenuClickListener = SafePopupMenuClickListener(clickListener)
    setOnMenuItemClickListener(safePopupMenuClickListener)
}

@Suppress("DEPRECATION")
inline fun <reified T : Serializable> Bundle.customGetSerializable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, T::class.java)
    } else {
        getSerializable(key) as? T
    }
}

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.customGetParcelable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, T::class.java)
    } else {
        getParcelable(key) as? T
    }
}

inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

inline fun <reified T : Serializable> Intent.customSerializableExtra(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

fun EditText.touchObserver() {
    setOnTouchListener { view, event ->
        view.parent.requestDisallowInterceptTouchEvent(true)
        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL) {
            view.parent.requestDisallowInterceptTouchEvent(false)
        } else {
            performClick()
        }
        return@setOnTouchListener false
    }
}

fun EditText.fetchString(): String {
    return text.toString().trim()
}

fun Editable.fetchString(): String {
    return toString().trim()
}

fun String.capitalizeFirstChar(): String {
    return toString().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}