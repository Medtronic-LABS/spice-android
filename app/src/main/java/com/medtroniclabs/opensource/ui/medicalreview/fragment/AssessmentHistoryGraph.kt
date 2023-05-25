package com.medtroniclabs.opensource.ui.medicalreview.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.charts.Chart
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
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.customGetSerializable
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.common.MedicalReviewConstant
import com.medtroniclabs.opensource.data.model.BPLogListResponse
import com.medtroniclabs.opensource.data.model.BloodGlucoseListResponse
import com.medtroniclabs.opensource.databinding.FragmentHistoryGraphBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.mgdl
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel


class AssessmentHistoryGraph : BaseFragment(), OnChartGestureListener, OnChartValueSelectedListener,
    View.OnClickListener {

    private var isBPSummary: Boolean = false
    private var graphDetails: Any? = null
    private lateinit var binding: FragmentHistoryGraphBinding
    private var dateList: ArrayList<Triple<Int, String, Boolean>>? = null
    private var unitValue: ArrayList<Pair<Int, String?>>? = null
    private val option: Int = 1
    private var systolicXYValues: ArrayList<Entry>? = null
    private var diastolicXYValues: ArrayList<Entry>? = null
    private var lineDataSets: ArrayList<ILineDataSet>? = null
    private val viewModel: PatientDetailViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            it.getString(AssessmentHistoryFragment.TAG)?.let { tag ->
                isBPSummary = (tag == AssessmentHistoryFragment.BP_TAG)
            }
            (it.customGetSerializable(IntentConstants.graphDetails) as Any?)?.let { details ->
                graphDetails = details
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        graphDetails?.let { graph ->
            (graph as? BPLogListResponse)?.let { list ->
                setGraphProperty(binding.lineChart, 1, bpDetails = list)
            }
            (graph as? BloodGlucoseListResponse)?.let { list ->
                setGraphProperty(binding.lineChart, 1, bgDetails = list)
            }
        }
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.selectedBGDropDown.observe(viewLifecycleOwner) {
            graphDetails?.let { graph ->
                (graph as? BloodGlucoseListResponse)?.let { list ->
                    setGraphProperty(
                        binding.lineChart,
                        1,
                        bgDetails = list,
                        selectedBGDropDown = it
                    )
                }
            }
        }
    }

    private fun setGraphProperty(
        lineChart: LineChart, option: Int,
        bpDetails: BPLogListResponse? = null, bgDetails: BloodGlucoseListResponse? = null,
        selectedBGDropDown: Int = 3
    ) {
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

        lineChart.setNoDataText(getString(R.string.no_data_dound))
        val paint = lineChart.getPaint(Chart.PAINT_INFO)
        paint.color = ContextCompat.getColor(requireContext(), R.color.cobalt_blue)
        paint.textSize = 24f
        lineChart.setNoDataTextTypeface(
            ResourcesCompat.getFont(
                requireContext(),
                R.font.inter_regular
            )
        )

        // set offsets to display label values without clip.
        lineChart.zoom(1.0f, 0f, 1f, 0f)
        lineChart.invalidate()
        loadChartData(bpDetails, bgDetails)
        plotGraph(bpDetails, bgDetails, selectedBGDropDown)
        // get the legend (only possible after setting data)
        val legend = lineChart.legend
        legend.yOffset = 20f
        legend.isEnabled = false
        val markerView = AssessmentMarkerView(
            requireContext(),
            R.layout.custom_marker_view_layout,
            systolicXYValues,
            diastolicXYValues,
            dateList,
            isBPSummary,
            unitValue
        )
        lineChart.marker = markerView
        val xAxis = lineChart.xAxis
        xAxis.setDrawGridLines(true)
        xAxis.axisMinimum = 0f
        xAxis.setCenterAxisLabels(false)
        xAxis.spaceMax = 1f
        xAxis.isEnabled = true
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.typeface = Typeface.SANS_SERIF
        xAxis.typeface = Typeface.DEFAULT_BOLD
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.graph_xaxis_textcolor)
        xAxis.setDrawAxisLine(true)
        xAxis.gridColor = ContextCompat.getColor(requireContext(), R.color.graph_limit_linecolor)
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 1f
        binding.lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override
            fun getFormattedValue(value: Float): String {
                dateList?.let { list ->
                    val containedList = list.filter { it.first.toFloat() == value }
                    return if (containedList.isNotEmpty()) {
                        containedList[0].second
                    } else {
                        ""
                    }
                } ?: kotlin.run {
                    return ""
                }
            }
        }

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.setDrawAxisLine(true)
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = if (isBPSummary) 350f else getUpperLimit()
        yAxisLeft.yOffset = 1f
        yAxisLeft.isEnabled = true
        yAxisLeft.typeface = Typeface.SANS_SERIF
        yAxisLeft.typeface = Typeface.DEFAULT_BOLD
        yAxisLeft.textColor =
            ContextCompat.getColor(requireContext(), R.color.graph_xaxis_textcolor)
        yAxisLeft.gridColor =
            ContextCompat.getColor(requireContext(), R.color.graph_limit_linecolor)
        val yAxisRight = lineChart.axisRight
        yAxisRight.isEnabled = false
        yAxisLeft.removeAllLimitLines() // reset all limit lines to avoid overlapping lines
        binding.lineChart.isScaleYEnabled = false
        var lineSetOneTitle = ""
        var lineSetTwoTitle = ""
        var lineSetOne = 0f
        var lineSetTwo = 0f
        bpDetails?.bpThreshold?.let {
            lineSetOneTitle = "(Dia ${it.diastolic})"
            lineSetTwoTitle = "(Sys ${it.systolic})"
            lineSetOne = it.diastolic.toFloat()
            lineSetTwo = it.systolic.toFloat()

            yAxisLeft.addLimitLine(
                getLimitLine(
                    lineSetOne,
                    lineSetOneTitle,
                    LimitLine.LimitLabelPosition.LEFT_TOP,
                    color = R.color.ncd_accent
                )
            )
            yAxisLeft.addLimitLine(
                getLimitLine(
                    lineSetTwo,
                    lineSetTwoTitle,
                    LimitLine.LimitLabelPosition.LEFT_TOP,
                    color = R.color.primary_medium_blue
                )
            )
        }

        bgDetails?.glucoseThreshold?.let { thresholdList ->
            bgDetails.glucoseLogList?.find { it.glucoseUnit == DefinedParams.mmoll }?.let {
                thresholdList.find { it.unit == DefinedParams.mmoll }?.let { res ->
                    lineSetOneTitle = "(FBS: ${res.fbs}(${DefinedParams.mmoll}))"
                    lineSetTwoTitle = "(RBS: ${res.rbs}(${DefinedParams.mmoll}))"
                    lineSetOne = res.fbs.toFloat()
                    lineSetTwo = res.rbs.toFloat()

                    yAxisLeft.addLimitLine(
                        getLimitLine(
                            lineSetOne,
                            lineSetOneTitle,
                            LimitLine.LimitLabelPosition.LEFT_TOP,
                            color = R.color.ncd_accent
                        )
                    )

                    yAxisLeft.addLimitLine(
                        getLimitLine(
                            lineSetTwo,
                            lineSetTwoTitle,
                            LimitLine.LimitLabelPosition.LEFT_TOP,
                            color = R.color.primary_medium_blue
                        )
                    )
                }
            }
            bgDetails.glucoseLogList?.find { it.glucoseUnit == mgdl }?.let {
                thresholdList.find { it.unit == mgdl }?.let { res ->
                    lineSetOneTitle = "(FBS: ${res.fbs}($mgdl))"
                    lineSetTwoTitle = "(RBS: ${res.rbs}($mgdl))"
                    lineSetOne = res.fbs.toFloat()
                    lineSetTwo = res.rbs.toFloat()

                    yAxisLeft.addLimitLine(
                        getLimitLine(
                            lineSetOne,
                            lineSetOneTitle,
                            LimitLine.LimitLabelPosition.LEFT_TOP,
                            color = R.color.ncd_accent
                        )
                    )
                    yAxisLeft.addLimitLine(
                        getLimitLine(
                            lineSetTwo,
                            lineSetTwoTitle,
                            LimitLine.LimitLabelPosition.LEFT_TOP,
                            color = R.color.primary_medium_blue
                        )
                    )
                }
            }
        }

        //  ll1.setTypeface(tf);

        var limit = 0f
        bpDetails?.bpLogList?.let {
            limit = it.size.toFloat()
        }
        bgDetails?.glucoseLogList?.let {
            limit = it.size.toFloat()
        }
        if (option == 1) {
            lineChart.setVisibleXRangeMaximum(7f)
        } else {
            binding.lineChart.zoomToCenter(limit / 7f, 0f)
        }
        binding.lineChart.moveViewToX(limit)
        lineChart.invalidate()

    }

    private fun getLimitLine(
        lineSetOne: Float,
        lineSetOneTitle: String,
        pos: LimitLine.LimitLabelPosition,
        size: Float = 12F,
        color: Int
    ): LimitLine {
        return LimitLine(lineSetOne, lineSetOneTitle).apply {
            lineWidth = 1f
            enableDashedLine(10f, 10f, 0f)
            labelPosition = pos
            textSize = size
            textColor = requireContext().getColor(color)
            lineColor = requireContext().getColor(color)
        }
    }

    private fun getUpperLimit(): Float {
        var upperLimit = 40f
        graphDetails?.let { graph ->
            (graph as? BloodGlucoseListResponse)?.let { data ->
                data.glucoseLogList?.filter { it.glucoseUnit == mgdl }?.let {
                    if (it.isNotEmpty())
                        upperLimit = 600f
                }
            }
        }
        return upperLimit
    }


    private fun loadChartData(
        bpDetails: BPLogListResponse? = null,
        bgDetails: BloodGlucoseListResponse? = null
    ) {
        diastolicXYValues = ArrayList()
        systolicXYValues = ArrayList()
        dateList = ArrayList()
        unitValue = ArrayList()
        bpDetails?.bpLogList?.let { list ->
            list.forEachIndexed { index, bpResponse ->
                systolicXYValues?.add(
                    Entry(
                        (index + 1).toFloat(),
                        bpResponse.avgSystolic.toFloat()
                    )
                )
                diastolicXYValues?.add(
                    Entry(
                        (index + 1).toFloat(),
                        bpResponse.avgDiastolic.toFloat()
                    )
                )
                val date = DateUtils.convertDateTimeToDate(
                    bpResponse.bpTakenOn,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ, DateUtils.DATE_FORMAT_ddMMyy_GRAPH
                ).ifEmpty { getString(R.string.separator_hyphen) }
                dateList?.add(Triple(index + 1, date, true))
            }
        }
        bgDetails?.glucoseLogList?.let { list ->
            list.forEachIndexed { index, bgResponse ->
                bgResponse.glucoseValue?.let { glucoseValue ->
                    var isfbs = false
                    if (bgResponse.glucoseType?.lowercase() == DefinedParams.rbs) {
                        systolicXYValues?.add(Entry((index + 1).toFloat(), glucoseValue.toFloat()))
                    } else {
                        isfbs = true
                        diastolicXYValues?.add(
                            Entry(
                                (index + 1).toFloat(),
                                glucoseValue.toFloat()
                            )
                        )
                    }
                    val date = DateUtils.convertDateTimeToDate(
                        bgResponse.glucoseDateTime,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DateUtils.DATE_FORMAT_ddMMyy_GRAPH
                    )
                    dateList?.add(Triple(index + 1, date, isfbs))
                    unitValue?.add(Pair(index + 1, bgResponse.glucoseUnit))
                }
            }
        }
    }

    private fun plotGraph(
        bpDetails: BPLogListResponse? = null,
        bgDetails: BloodGlucoseListResponse? = null,
        selectedBGDropDown: Int
    ) {
        var dataSetOneTitle = ""
        var dataSetTwoTitle = ""
        bpDetails?.bpThreshold?.let {
            dataSetOneTitle = "Sys: ${it.systolic} (Optimal)"
            dataSetTwoTitle = "Dia: ${it.diastolic} (Optimal)"
        }
        bgDetails?.glucoseThreshold?.get(0)?.let {
            dataSetOneTitle = "RBS: ${it.rbs} (Optimal)"
            dataSetTwoTitle = "FBS: ${it.fbs} (Optimal)"
        }

        val dataSetOne = LineDataSet(systolicXYValues, dataSetOneTitle)
        dataSetOne.color = requireContext().getColor(R.color.primary_medium_blue)
        dataSetOne.lineWidth = 2f
        //Change color on selction of point , 2. Drawing VerticalLine on selection
        //Change color on selction of point , 2. Drawing VerticalLine on selection
        dataSetOne.highLightColor =
            requireContext().getColor(R.color.primary_medium_blue)
        dataSetOne.setDrawHorizontalHighlightIndicator(false)

        dataSetOne.circleRadius = 5f

        dataSetOne.setDrawCircleHole(true)
        //for different circle color around
        dataSetOne.setCircleColors(requireContext().getColor(R.color.primary_medium_blue))

        //Removing values
        dataSetOne.setDrawValues(false)
        dataSetOne.axisDependency = YAxis.AxisDependency.LEFT
        dataSetOne.color = requireContext().getColor(R.color.primary_medium_blue)
        dataSetOne.lineWidth = 1.5f
        dataSetOne.setDrawCircles(true)
        dataSetOne.fillAlpha = 0
        dataSetOne.fillColor = requireContext().getColor(R.color.primary_medium_blue)
        dataSetOne.highLightColor =
            ContextCompat.getColor(requireContext(), R.color.primary_medium_blue)
        dataSetOne.setDrawCircleHole(false)

        val dataSetTwo = LineDataSet(diastolicXYValues, dataSetTwoTitle)
        dataSetTwo.color =
            ContextCompat.getColor(requireContext(), R.color.ncd_accent)
        dataSetTwo.lineWidth = 2f
        //Change color on selction of point , 2. Drawing VerticalLine on selection
        //Change color on selction of point , 2. Drawing VerticalLine on selection
        dataSetTwo.highLightColor =
            ContextCompat.getColor(requireContext(), R.color.ncd_accent)
        dataSetTwo.setDrawHorizontalHighlightIndicator(false)
        dataSetTwo.circleRadius = 5f //setCircleSize(5f);

        dataSetTwo.setDrawCircleHole(false)
        //for different circle color around
        //for different circle color around
        dataSetTwo.setCircleColors(requireContext().getColor(R.color.ncd_accent))
        dataSetTwo.setDrawValues(false)

        lineDataSets = ArrayList()
        // create a data object with the datasets
        // Don't change the Data Set order added here sys, dia, pulse as its dependant for marker.
        if (isBPSummary || selectedBGDropDown == MedicalReviewConstant.fbs_rbs_code) {
            if (dataSetOne.entryCount > 0)
                lineDataSets?.add(dataSetOne)
            if (dataSetTwo.entryCount > 0)
                lineDataSets?.add(dataSetTwo)
        } else {
            if (selectedBGDropDown == MedicalReviewConstant.rbs_code && dataSetOne.entryCount > 0) {
                lineDataSets?.add(dataSetOne)
            }
            if (selectedBGDropDown == MedicalReviewConstant.fbs_code && dataSetTwo.entryCount > 0) {
                lineDataSets?.add(dataSetTwo)
            }
        }
        val data = LineData(lineDataSets)
        // set data
        lineDataSets?.let {
            if (it.size > 0)
                binding.lineChart.data = data
        }
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
        /**
         * this method is not used
         */
    }
}