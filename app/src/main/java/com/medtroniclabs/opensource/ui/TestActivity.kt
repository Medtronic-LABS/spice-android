package com.medtroniclabs.opensource.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.databinding.ActivityTestBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class TestActivity : AppCompatActivity(), OnChartGestureListener, OnChartValueSelectedListener,
    View.OnClickListener {

    lateinit var binding: ActivityTestBinding
    var dateList = ArrayList<String>()
    var option: Int = 1
    private var systolicXYValues: ArrayList<Entry>? = null
    private var diastolicXYValues: ArrayList<Entry>? = null
    private var pulseXYValues: ArrayList<Entry>? = null
    private var lineDataSets: ArrayList<ILineDataSet>? = null
    private val dateFormat: DateFormat = SimpleDateFormat("dd-MMM-yyyy")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setGraphProperty(binding.lineChart, 1, 30f)
        binding.btnSwitch.safeClickListener(this)
    }

    private fun setGraphProperty(lineChart: LineChart, option: Int, limit: Float) {
        if (!lineChart.isEmpty) { // reset graph when frequency changed
            lineChart.moveViewToX(0f)
            lineChart.resetZoom()
            lineChart.fitScreen()
            lineChart.clearValues()
            lineChart.clear()
        }
        lineChart.description.isEnabled = false
        // enable touch gestures
        // enable touch gestures
        lineChart.setTouchEnabled(true)
        lineChart.dragDecelerationFrictionCoef = 0.9f
        // enable scaling and dragging
        // enable scaling and dragging
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)

        lineChart.isScaleXEnabled = true
        lineChart.setBackgroundColor(Color.parseColor("#ffffff")) //set whatever color you prefer

        lineChart.setDrawGridBackground(false) // this is a must

        lineChart.setDrawBorders(false)
        lineChart.setOnChartValueSelectedListener(this)

        // set offsets to display label values without clip.
        lineChart.zoom(1.0f, 0f, 1f, 0f)
        lineChart.invalidate()
        loadChartData(limit)
        plotGraph()
        // get the legend (only possible after setting data)
        // get the legend (only possible after setting data)
        val legend = lineChart.legend
        legend.yOffset = 20f
        legend.isEnabled = true
        val markerView = CustomMarkerView(
            this,
            R.layout.custom_marker_view_layout,
            systolicXYValues,
            diastolicXYValues,
            pulseXYValues
        )
        lineChart.marker = markerView
        val xAxis = lineChart.xAxis
        xAxis.setDrawGridLines(true)
        xAxis.axisMinimum = 0f
        xAxis.setCenterAxisLabels(false)
        xAxis.spaceMax = 1f
        xAxis.isEnabled = true
        xAxis.labelRotationAngle = 45f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.typeface = Typeface.SANS_SERIF
        xAxis.typeface = Typeface.DEFAULT_BOLD
        xAxis.textColor = ContextCompat.getColor(this, R.color.graph_xaxis_textcolor)
        xAxis.setDrawAxisLine(true)
        xAxis.gridColor = ContextCompat.getColor(this, R.color.graph_limit_linecolor)
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 1f
        binding.lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override
            fun getFormattedValue(value: Float): String {
                return if (value < dateList.size) {
                    dateList[(value).toInt()]
                } else {
                    value.toString()
                }
            }
        }

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.setDrawAxisLine(true)
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 350f
        yAxisLeft.yOffset = 1f
        yAxisLeft.isEnabled = true
        yAxisLeft.typeface = Typeface.SANS_SERIF
        yAxisLeft.typeface = Typeface.DEFAULT_BOLD
        yAxisLeft.textColor = ContextCompat.getColor(this, R.color.graph_xaxis_textcolor)
        yAxisLeft.gridColor = ContextCompat.getColor(this, R.color.graph_limit_linecolor)
        val yAxisRight = lineChart.axisRight
        yAxisRight.isEnabled = false

        val limitLine1 = LimitLine(50f, "Min Value")
        limitLine1.lineWidth = 1f
        limitLine1.enableDashedLine(10f, 10f, 0f)
        limitLine1.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        limitLine1.textSize = 10f
        limitLine1.lineColor = getColor(R.color.target_bp_color)

        val limitLine2 = LimitLine(300f, "Max Value")
        limitLine2.lineWidth = 1f
        limitLine2.enableDashedLine(10f, 10f, 0f)
        limitLine2.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        limitLine2.textSize = 10f
        limitLine2.lineColor = getColor(R.color.target_bp_color)

        //  ll1.setTypeface(tf);
        yAxisLeft.removeAllLimitLines() // reset all limit lines to avoid overlapping lines
        binding.lineChart.isScaleYEnabled = false
        yAxisLeft.addLimitLine(limitLine1)
        yAxisLeft.addLimitLine(limitLine2)
        if (option == 1) {
            lineChart.setVisibleXRangeMaximum(7f)
        } else {
            binding.lineChart.zoomToCenter(limit / 7f, 0f)
        }
        binding.lineChart.moveViewToX(limit)
        lineChart.invalidate()

    }

    private fun loadChartData(limit: Float) {
        diastolicXYValues = ArrayList()
        systolicXYValues = ArrayList()
        // pulseXYValues = ArrayList()
        var systolic = 1
        val systolicValue = 100f
        while (systolic <= limit) {
            if (systolic % 2 == 0) {
                systolicXYValues?.add(Entry(systolic.toFloat(), systolicValue + 20f))
            } else {
                systolicXYValues?.add(Entry(systolic.toFloat(), systolicValue + 40f))
            }
            systolic += 1
        }
        var diastolic = 1
        val diastolicValue = 120f
        while (diastolic <= limit) {
            if (diastolic % 2 == 0) {
                diastolicXYValues?.add(Entry(diastolic.toFloat(), diastolicValue - 20f))
            } else {
                diastolicXYValues?.add(Entry(diastolic.toFloat(), diastolicValue + 40f))
            }
            diastolic += 1
        }
        var pulse = 1f

        while (pulse <= limit) {
            pulseXYValues?.add(Entry(pulse, pulse * 4))
            pulse += 1
        }

        var dateIndex = 0
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, -limit.toInt())
        while (dateIndex <= (limit + 5)) {
            dateList.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            dateIndex +=1
        }

    }

    private fun plotGraph() {
        val diastolicDataSet = LineDataSet(diastolicXYValues, "Diastolic")
        val systolicDataSet = LineDataSet(systolicXYValues, "Systolic")
        val pulseDataSet = LineDataSet(pulseXYValues, "Pulse")
        systolicDataSet.color = ContextCompat.getColor(this, R.color.cobalt_blue)
        systolicDataSet.lineWidth = 2f
        //Change color on selction of point , 2. Drawing VerticalLine on selection
        //Change color on selction of point , 2. Drawing VerticalLine on selection
        systolicDataSet.highLightColor =
            ContextCompat.getColor(this, R.color.vertical_line_color)
        systolicDataSet.setDrawHorizontalHighlightIndicator(false)

        systolicDataSet.circleRadius = 5f

        systolicDataSet.setDrawCircleHole(true)
        //for different circle color around
        systolicDataSet.setCircleColors(getColor(R.color.high_risk_color))

        //Removing values
        systolicDataSet.setDrawValues(false)
        systolicDataSet.axisDependency = YAxis.AxisDependency.LEFT
        systolicDataSet.color = ColorTemplate.getHoloBlue()
        systolicDataSet.lineWidth = 1.5f
        systolicDataSet.setDrawCircles(true)
        systolicDataSet.fillAlpha = 0
        systolicDataSet.fillColor = ColorTemplate.getHoloBlue()
        systolicDataSet.highLightColor =
            ContextCompat.getColor(this, R.color.vertical_line_color)
        systolicDataSet.setDrawCircleHole(false)

        diastolicDataSet.color =
            ContextCompat.getColor(this, R.color.graph_circle_roundcolor)
        diastolicDataSet.lineWidth = 2f
        //Change color on selction of point , 2. Drawing VerticalLine on selection
        //Change color on selction of point , 2. Drawing VerticalLine on selection
        diastolicDataSet.highLightColor =
            ContextCompat.getColor(this, R.color.vertical_line_color)
        diastolicDataSet.setDrawHorizontalHighlightIndicator(false)
        diastolicDataSet.circleRadius = 5f //setCircleSize(5f);

        diastolicDataSet.setDrawCircleHole(false)
        //for different circle color around
        //for different circle color around
        diastolicDataSet.setCircleColors(getColor(R.color.moderate_risk_color))
        diastolicDataSet.setDrawValues(false)

        pulseDataSet.color = Color.TRANSPARENT //for devider_line

        pulseDataSet.lineWidth = 0f
        pulseDataSet.highLightColor =
            ContextCompat.getColor(this, R.color.vertical_line_color)
        pulseDataSet.setDrawHorizontalHighlightIndicator(false)
        pulseDataSet.circleRadius = 4f
        pulseDataSet.setDrawCircleHole(false)
        pulseDataSet.setCircleColor(
            ContextCompat.getColor(
                this,
                R.color.pulse_graph_color
            )
        )

        //Removing values
        pulseDataSet.setDrawValues(false)
        lineDataSets = ArrayList()
        // create a data object with the datasets
        // Don't change the Data Set order added here sys, dia, pulse as its dependant for marker.
        lineDataSets?.add(systolicDataSet)
        lineDataSets?.add(diastolicDataSet)
        lineDataSets?.add(pulseDataSet)
        val data = LineData(lineDataSets)
        // set data
        binding.lineChart.data = data
    }

    override fun onChartGestureStart(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        /**
         * this method is not used
         */
    }

    override fun onChartGestureEnd(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        /**
         * this method is not used
         */
    }

    override fun onChartLongPressed(me: MotionEvent?) {
        /**
         * this method is not used
         */
    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
        /**
         * this method is not used
         */
    }

    override fun onChartSingleTapped(me: MotionEvent?) {
        /**
         * this method is not used
         */
    }

    override fun onChartFling(
        me1: MotionEvent?,
        me2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ) {
        /**
         * this method is not used
         */
    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        /**
         * this method is not used
         */
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        /**
         * this method is not used
         */
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        /**
         * this method is not used
         */
    }

    override fun onNothingSelected() {
        /**
         * this method is not used
         */
    }

    override fun onClick(p0: View?) {
        option = if (option == 1) {
            setGraphProperty(binding.lineChart, 2, 30f)
            2
        } else {
            setGraphProperty(binding.lineChart, 1, 30f)
            1
        }
    }
}