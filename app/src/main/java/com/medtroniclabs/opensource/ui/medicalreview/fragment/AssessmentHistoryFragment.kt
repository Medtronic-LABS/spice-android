package com.medtroniclabs.opensource.ui.medicalreview.fragment

import android.os.Bundle
import android.os.SystemClock
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.CustomSpinnerAdapter
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.common.MedicalReviewConstant
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.databinding.FragmentAssessmentHistoryBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.getGlucoseUnit
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.MySpannable
import com.medtroniclabs.opensource.ui.medicalreview.dialog.BloodGlucoseReadingDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.BloodPressureVitalsDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.SymptomsDialog
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel

class AssessmentHistoryFragment : BaseFragment(), View.OnClickListener {

    private var isBPSummary: Boolean = false
    private lateinit var binding: FragmentAssessmentHistoryBinding
    private val viewModel: PatientDetailViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            it.getString(TAG)?.let { tag ->
                isBPSummary = (tag == BP_TAG)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.selectedBGDropDown.observe(viewLifecycleOwner) { num ->
            if (!isBPSummary) {
                viewModel.patientBloodGlucoseListResponse.value?.data?.let { response ->
                    response.glucoseLogList?.let { logList ->
                        var type: String? = null
                        if (num == 1) type = DefinedParams.fbs else if (num == 2) type =
                            DefinedParams.rbs
                        if (type != null) {
                            val haveData = logList.firstOrNull { it.glucoseType == type }
                            if (haveData != null)
                                renderLatestBGLogDetails(response.latestGlucoseLog)
                            else
                                renderLatestBGLogDetails(null)
                        } else
                            renderLatestBGLogDetails(response.latestGlucoseLog)
                    }
                }
            }
        }

        viewModel.refreshGraphDetails.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    getValues()
                }
                else -> {
                    //Invoked if response state is not success
                }
            }
        }
        viewModel.patientDetailsResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    getValues()
                }
                else -> {
                    //Invoked if response state is not success
                }
            }
        }
        viewModel.patientBPLogGraphListResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showGraphLoader()
                }
                ResourceState.ERROR -> {
                    hideGraphLoader()
                }
                ResourceState.SUCCESS -> {
                    hideGraphLoader()
                    if (isBPSummary)
                        loadBPValues(resourceState.data)
                }
            }
        }

        viewModel.patientBloodGlucoseListResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showGraphLoader()
                }
                ResourceState.ERROR -> {
                    hideGraphLoader()
                }
                ResourceState.SUCCESS -> {
                    hideGraphLoader()
                    if (!isBPSummary)
                        loadBGValues(resourceState.data)
                }
            }
        }
    }

    private fun hideGraphLoader() {
        binding.clGraph.visibility = View.VISIBLE
        binding.btnAddNewReading.visibility = View.VISIBLE
        binding.CenterProgress.visibility = View.GONE

        assessmentBpBg()
    }

    private fun assessmentBpBg() {
        if (CommonUtils.isNurse()) {
            binding.btnAddNewReading.text =
                if (isBPSummary) getString(R.string.add_blood_pressure_readings) else getString(
                    R.string.add_blood_glucose_readings
                )
            binding.btnAddNewReading.visibility = View.VISIBLE
        } else
            binding.btnAddNewReading.visibility = View.GONE
    }

    private fun showGraphLoader() {
        binding.clGraph.visibility = View.GONE
        binding.btnAddNewReading.visibility = View.GONE
        binding.CenterProgress.visibility = View.VISIBLE
    }

    private fun loadBPValues(bpLog: BPLogListResponse?) {
        bpLog?.let {
            it.total?.let {
                viewModel.totalBPTotalCount = it//7
            }
            if (binding.ivGraphNext.tag == isBPSummary && binding.ivGraphPrevious.tag == isBPSummary) {
                binding.ivGraphNext.isEnabled = bpLog.skip != 0
                binding.ivGraphPrevious.isEnabled = viewModel.totalBPTotalCount != null && viewModel.totalBPCount < (viewModel.totalBPTotalCount!! / DefinedParams.PageLimit)
            }
            it.latestBpLog?.let {
                viewModel.latestBpLogResponse = it
            }
            renderLatestBPLogDetails(viewModel.latestBpLogResponse)
            renderBPLogsToGraph(it)
        }
    }

    private fun renderLatestBPLogDetails(latestBPLog: BPResponse?) {
        latestBPLog?.apply {
            binding.llLastAssessment.tvValue.text = DateUtils.convertDateTimeToDate(
                bpTakenOn,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                DateUtils.DATE_FORMAT_ddMMMyyyy
            ).ifEmpty { getString(R.string.separator_hyphen) }
            binding.llValue.tvValue.text = getString(
                R.string.average_mmhg_string,
                CommonUtils.getDecimalFormatted(avgSystolic),
                CommonUtils.getDecimalFormatted(avgDiastolic)
            )
            setSymptoms(symptoms?.joinToString(separator = ", ") ?: "")
        }
    }

    private fun setSymptoms(symptoms: String) {
        val expandText = getString(R.string.see_more)

        val tv = binding.llSymptoms.tvValue
        tv.text = symptoms.ifEmpty { "-" }

        val vto = tv.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val obs = tv.viewTreeObserver
                obs.removeOnGlobalLayoutListener(this)
                if (tv.lineCount >= 2) {
                    val firstLineLength = tv.layout.getLineEnd(0)
                    val secondLineLength = tv.layout.getLineEnd(1) - firstLineLength
                    val minCharsToShow = 5
                    if (secondLineLength > 1) {
                        val shrinkLength =
                            if (secondLineLength >= minCharsToShow) minCharsToShow else secondLineLength - 1
                        val actualText = tv.text.subSequence(0, firstLineLength + shrinkLength)
                            .toString() + expandText
                        tv.movementMethod = LinkMovementMethod.getInstance()
                        tv.setText(
                            getSpannableString(
                                symptoms,
                                actualText,
                                expandText
                            ), TextView.BufferType.SPANNABLE
                        )
                    }
                }
            }
        })
    }

    private fun getSpannableString(
        symptoms: String,
        str: String,
        spannableText: String
    ): SpannableStringBuilder {
        val ssb = SpannableStringBuilder(str)
        if (str.contains(spannableText)) {
            ssb.setSpan(object : MySpannable(requireContext(), false) {
                private var lastTimeClicked: Long = 0
                override fun onClick(p0: View) {
                    if (SystemClock.elapsedRealtime() - lastTimeClicked < 1000) {
                        return
                    }
                    lastTimeClicked = SystemClock.elapsedRealtime()
                    SymptomsDialog.newInstance(symptoms)
                        .show(childFragmentManager, SymptomsDialog.TAG)
                }
            }, str.indexOf(spannableText), str.indexOf(spannableText) + spannableText.length, 0)
        }
        return ssb
    }

    private fun renderBPLogsToGraph(log: BPLogListResponse) {
        log.bpLogList?.let { bpLogs ->
            if (bpLogs.isNotEmpty()) {
                hideNoRecordView()
                val bundle = Bundle().apply {
                    putString(TAG, BP_TAG)
                    putSerializable(IntentConstants.graphDetails, log)
                }
                var dataSetOneTitle = ""
                var dataSetTwoTitle = ""
                log.bpThreshold?.let {
                    dataSetOneTitle = "Sys: ${it.systolic} (Optimal)"
                    dataSetTwoTitle = "Dia: ${it.diastolic} (Optimal)"
                }
                binding.tvLineOne.text = getString(R.string.systolic)
                binding.tvLineTwo.text = getString(R.string.diastolic)
                replaceFragmentInId<AssessmentHistoryGraph>(bundle = bundle)
            } else {
                showNoRecordView()
            }
        } ?: kotlin.run {
            showNoRecordView()
        }
    }

    private fun loadBGValues(data: BloodGlucoseListResponse?) {
        data?.let { it ->
            if (binding.ivGraphNext.tag == isBPSummary && binding.ivGraphPrevious.tag == isBPSummary) {
                binding.ivGraphNext.isEnabled = it.skip != 0
                binding.ivGraphPrevious.isEnabled =
                    viewModel.totalBGCount < (it.total / DefinedParams.PageLimit)
            }
            renderLatestBGLogDetails(data.latestGlucoseLog)
            renderBGLogsToGraph(it)
        }
    }

    private fun renderLatestBGLogDetails(latestGlucoseLog: BloodGlucose?) {
        latestGlucoseLog?.apply {
            if (glucoseDateTime != null) {
                binding.llLastAssessment.tvValue.text = DateUtils.convertDateTimeToDate(
                    glucoseDateTime,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DateUtils.DATE_FORMAT_ddMMMyyyy
                )
            }

            binding.llValue.tvValue.text =
                if (glucoseValue != null) "${CommonUtils.getDecimalFormatted(glucoseValue)} ${getGlucoseUnit(glucoseUnit, false)}" else "-"
            setSymptoms(symptoms?.joinToString(separator = ", ") ?: "")
        }?: kotlin.run {
            binding.llLastAssessment.tvValue.text = getString(R.string.separator_hyphen)
            binding.llValue.tvValue.text = getString(R.string.separator_hyphen)
            binding.llSymptoms.tvValue.text = getString(R.string.separator_hyphen)
        }
    }

    private fun renderBGLogsToGraph(log: BloodGlucoseListResponse) {
        log.glucoseLogList?.let { bpLogs ->
            val filteredValue = bpLogs.filter { it.glucoseType != null }
            if (filteredValue.isNotEmpty()) {
                hideNoRecordView()
                val bundle = Bundle().apply {
                    putString(TAG, BG_TAG)
                    putSerializable(IntentConstants.graphDetails, log)
                }
                binding.tvLineOne.text = DefinedParams.RBS
                binding.tvLineTwo.text = DefinedParams.FBS
                replaceFragmentInId<AssessmentHistoryGraph>(bundle = bundle)
            } else {
                showNoRecordView()
            }
        } ?: kotlin.run {
            showNoRecordView()
        }
    }

    private fun getValues(forward: Boolean? = null) {
        SecuredPreference.getSelectedSiteEntity()?.let { siteEntity ->
            viewModel.patientId?.let {
                val request = AssessmentListRequest(
                    it,
                    true,
                    limit = DefinedParams.PageLimit,
                    sortField = if (isBPSummary) DefinedParams.BPTakenOn else DefinedParams.BGTakenOn,
                    tenantId = siteEntity.tenantId
                )
                if (isBPSummary) {
                    viewModel.getAssessmentPatientDetails(requireContext())
                    when {
                        forward == null -> {
                            viewModel.totalBPCount = 0
                        }
                        forward -> {
                            viewModel.totalBPCount = viewModel.totalBPCount - 1
                        }
                        else -> {
                            viewModel.totalBPCount = viewModel.totalBPCount + 1
                        }
                    }
                    request.skip = viewModel.totalBPCount * DefinedParams.PageLimit
                    viewModel.getPatientBPLogListForGraph(requireContext(),request, forward)
                } else {
                    when {
                        forward == null -> {
                            viewModel.totalBGCount = 0
                        }
                        forward -> {
                            viewModel.totalBGCount = viewModel.totalBGCount - 1
                        }
                        else -> {
                            viewModel.totalBGCount = viewModel.totalBGCount + 1
                        }
                    }
                    request.skip = viewModel.totalBGCount * DefinedParams.PageLimit
                    viewModel.getPatientBloodGlucoseList(requireContext(),request)
                }
            }
        }
    }

    private fun initializeViews() {
        if (isBPSummary) {
            binding.cardTitle.text = getString(R.string.blood_pressure)
            binding.llValue.tvKey.text = getString(R.string.bp_value)
        } else {
            binding.cardTitle.text = getString(R.string.blood_glucose)
            binding.llValue.tvKey.text = getString(R.string.blood_glucose_value)
        }
        binding.llValue.tvRowSeparator.text = getString(R.string.separator_colon)
        binding.llValue.tvValue.text = getString(R.string.separator_hyphen)

        binding.llLastAssessment.tvKey.text = getString(R.string.last_assessment_date)
        binding.llLastAssessment.tvRowSeparator.text = getString(R.string.separator_colon)
        binding.llLastAssessment.tvValue.text = getString(R.string.separator_hyphen)

        binding.llSymptoms.tvKey.text = getString(R.string.symptoms)
        binding.llSymptoms.tvRowSeparator.text = getString(R.string.separator_colon)
        binding.llSymptoms.tvValue.text = getString(R.string.separator_hyphen)

        binding.ivGraphNext.safeClickListener(this)
        binding.ivGraphPrevious.safeClickListener(this)
        binding.ivGraphNext.tag = isBPSummary
        binding.ivGraphPrevious.tag = isBPSummary

        val adapter = CustomSpinnerAdapter(requireContext())
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf(
                DefinedParams.NAME to DefinedParams.RBS_FBS,
                DefinedParams.ID to MedicalReviewConstant.fbs_rbs_code
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.RBS,
                DefinedParams.ID to MedicalReviewConstant.rbs_code
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.FBS,
                DefinedParams.ID to MedicalReviewConstant.fbs_code
            )
        )
        adapter.setData(dropDownList)
        binding.spinnerRbsFbs.adapter = adapter
        binding.spinnerRbsFbs.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    if (selectedItem != null && selectedItem.containsKey(DefinedParams.ID)) {
                        viewModel.selectedBGDropDown.value = selectedItem[DefinedParams.ID] as Int
                    } else {
                        viewModel.selectedBGDropDown.value = 3
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }

            }

        if (!isBPSummary) {
            binding.spinnerRbsFbs.visibility = View.VISIBLE
        } else {
            binding.spinnerRbsFbs.visibility = View.INVISIBLE
        }

        binding.btnAddNewReading.safeClickListener(this)
    }

    companion object {
        const val BP_TAG = "BPSummaryFragment"
        const val BG_TAG = "BGSummaryFragment"
        const val TAG = "TAG"

        @JvmStatic
        fun newInstance() = AssessmentHistoryFragment()
    }

    private inline fun <reified fragment : Fragment> replaceFragmentInId(
        id: Int? = null,
        bundle: Bundle? = null,
        tag: String? = null
    ) {
        childFragmentManager.commit {
            setReorderingAllowed(true)
            replace<fragment>(
                id ?: binding.llGraph.id,
                args = bundle,
                tag = tag
            )
        }
    }

    private fun showNoRecordView() {
        binding.llGraph.visibility = View.GONE
        binding.tvNoData.visibility = View.VISIBLE
        binding.llSummaryHolder.visibility = View.INVISIBLE
    }

    private fun hideNoRecordView() {
        binding.llGraph.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE
        binding.llSummaryHolder.visibility = View.VISIBLE
    }

    override fun onClick(view: View) {
        when (view.id) {

            R.id.ivGraphNext -> {
                getValues(true)
            }

            R.id.ivGraphPrevious -> {
                getValues(false)
            }
            R.id.btnAddNewReading -> {
                if (isBPSummary) {
                    BloodPressureVitalsDialog.newInstance().show(
                        childFragmentManager,
                        BloodPressureVitalsDialog.TAG
                    )
                } else {
                    BloodGlucoseReadingDialog.newInstance().show(
                        childFragmentManager,
                        BloodGlucoseReadingDialog.TAG
                    )
                }
            }
        }
    }
}
