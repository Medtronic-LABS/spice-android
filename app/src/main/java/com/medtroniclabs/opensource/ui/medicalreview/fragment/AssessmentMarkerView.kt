package com.medtroniclabs.opensource.ui.medicalreview.fragment

import android.content.Context
import android.view.View
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils

class AssessmentMarkerView(
    context: Context?,
    layoutResource: Int,
    val systolicXYValues: ArrayList<Entry>?,
    val diastolicXYValues: ArrayList<Entry>?,
    val dateValues: ArrayList<Triple<Int, String, Boolean>>?,
    val isBPSummary: Boolean,
    val unitValue: ArrayList<Pair<Int, String?>>?
) :
    MarkerView(context, layoutResource) {
    var tvSystolic: TextView = findViewById(R.id.tvSystolic)
    var tvDiastolic: TextView = findViewById(R.id.tvDiastolic)
    var tvPulse: TextView = findViewById(R.id.tvPulse)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val unitSuffix = getUnitSuffix(unitValue?.filter { e.x == it.first.toFloat() })

            setSystolicValue(it, unitSuffix)

            setDiastolicValue(it, unitSuffix)

            setDateValues(it)
        }
        super.refreshContent(e, highlight)
    }

    private fun setSystolicValue(entry: Entry, unitSuffix: String?) {
        val systolicValue = systolicXYValues?.filter { entry.x == it.x }
        if (systolicValue != null && systolicValue.isNotEmpty()) {
            val title = if (isBPSummary) "Sys" else DefinedParams.RBS
            tvSystolic.text =
                "$title: ${CommonUtils.getDecimalFormattedNew(systolicValue[0].y)} ${unitSuffix ?: ""}"
        }
    }

    private fun setDiastolicValue(entry: Entry, unitSuffix: String?) {
        val diastolicValue = diastolicXYValues?.filter { entry.x == it.x }
        if (diastolicValue != null && diastolicValue.isNotEmpty()) {
            val title = if (isBPSummary) "Dia" else DefinedParams.FBS
            tvDiastolic.text =
                "$title: ${CommonUtils.getDecimalFormattedNew(diastolicValue[0].y)} ${unitSuffix ?: ""}"
        }
    }

    private fun setDateValues(entry: Entry) {
        val dateValue = dateValues?.filter { entry.x == it.first.toFloat() }
        if (dateValue != null && dateValue.isNotEmpty()) {
            val selectedItem = dateValue[0]
            tvPulse.text = "${selectedItem.second}"
            if (!isBPSummary) {
                if (selectedItem.third) {
                    tvSystolic.visibility = View.GONE
                    tvDiastolic.visibility = View.VISIBLE
                } else {
                    tvDiastolic.visibility = View.GONE
                    tvSystolic.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getUnitSuffix(filter: List<Pair<Int, String?>>?): String? {
        if (filter != null && filter.isNotEmpty()) {
            return filter[0].second
        }
        return ""
    }
}
