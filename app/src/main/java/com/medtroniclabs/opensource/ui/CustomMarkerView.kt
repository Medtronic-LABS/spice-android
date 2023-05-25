package com.medtroniclabs.opensource.ui

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.medtroniclabs.opensource.R

class CustomMarkerView(
    context: Context?,
    layoutResource: Int,
    val systolicXYValues: ArrayList<Entry>?,
    val diastolicXYValues: ArrayList<Entry>?,
    val pulseXYValues: ArrayList<Entry>?
) :
    MarkerView(context, layoutResource) {
    var tvSystolic: TextView = findViewById(R.id.tvSystolic)
    var tvDiastolic: TextView = findViewById(R.id.tvDiastolic)
    var tvPulse: TextView = findViewById(R.id.tvPulse)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val systolicValue = systolicXYValues?.filter { e.x == it.x }
            if (systolicValue != null && systolicValue.isNotEmpty()) {
                tvSystolic.text = "Systolic: ${systolicValue[0].y}"
            }
            val diastolicValue = diastolicXYValues?.filter { e.x == it.x }
            if (diastolicValue != null && diastolicValue.isNotEmpty()) {
                tvDiastolic.text = "Diastolic: ${diastolicValue[0].y}"
            }

            val pulseValue = pulseXYValues?.filter { e.x == it.x }
            if (pulseValue != null && pulseValue.isNotEmpty()) {
                tvPulse.text = "Pulse: ${pulseValue[0].y}"
            }

        }
        super.refreshContent(e, highlight)
    }

}
