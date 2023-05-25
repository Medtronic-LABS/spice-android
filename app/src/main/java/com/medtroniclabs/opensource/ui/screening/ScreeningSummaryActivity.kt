package com.medtroniclabs.opensource.ui.screening

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.activity.viewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.appextensions.setSuccess
import com.medtroniclabs.opensource.common.CustomSpinnerAdapter
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.common.MedicalReviewConstant
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.common.StringConverter.getPHQ4ReadableName
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.screening.SiteDetails
import com.medtroniclabs.opensource.databinding.ActivityScreeningSummaryBinding
import com.medtroniclabs.opensource.databinding.SummaryLayoutBinding
import com.medtroniclabs.opensource.formgeneration.FormSummaryReporter
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.getGlucoseUnit
import com.medtroniclabs.opensource.formgeneration.model.FormLayout
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.FormResultGenerator
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import com.medtroniclabs.opensource.ui.screening.viewmodel.ScreeningFormBuilderViewModel
import com.medtroniclabs.opensource.uploadservice.UploadForegroundService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScreeningSummaryActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityScreeningSummaryBinding

    private var isSiteChanged = false

    private val viewModel: ScreeningFormBuilderViewModel by viewModels()
    private var adapter: CustomSpinnerAdapter? = null
    private lateinit var formSummaryReporter: FormSummaryReporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreeningSummaryBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.screening_summary),
            homeAndBackVisibility = Pair(true, false),
            callbackHome = { onHomeClicked() }
        )
        parseActivityIntent()
        attachObserver()
        initializeFormSummaryView()
        setListeners()
    }

    private fun parseActivityIntent() {

        intent?.let {
            if (it.hasExtra(DefinedParams.ScreeningEntityId)) {
                val rowId = it.getLongExtra(DefinedParams.ScreeningEntityId, -1)
                if (rowId > -1) {
                    viewModel.screeningEntityRowId = rowId
                }
            }
        }
    }

    private fun setListeners() {
        binding.btnNext.safeClickListener(this)

        binding.etSiteChange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                pos: Int,
                itemId: Long
            ) {
                adapter?.getData(pos)?.let {
                    processSiteSelection(it)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                /**
                 * this method is not used
                 */
            }

        }
    }

    private fun attachObserver() {

        viewModel.screeningEntity.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    val screeningEntity = resourceState.data
                    screeningEntity?.let { entity ->
                        entity.screeningDetails.let { screeningDetails ->
                            StringConverter.convertStringToMap(screeningDetails)?.let { map ->
                                viewModel.formResponseLiveData.value?.data?.let {
                                    formSummaryReporter.populateSummary(it.formLayout, map,SecuredPreference.getIsTranslationEnabled())
                                    calculateOtherMetrics(it.formLayout, map)
                                }
                            }
                        }
                    }
                }
            }
        }

        viewModel.formResponseLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    val formList = resourceState.data?.formLayout
                    formList?.let {
                        getScreeningEntityRecord()
                    } ?: hideLoading()
                }
            }
        }

        viewModel.screeningLiveDate.observe(this) { resourceState ->
            when (resourceState.state) {

                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.SUCCESS -> {
                    resourceState?.let {
                        uploadScreening(resourceState.data)
                    }
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }

        viewModel.accountSiteListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    val list = ArrayList<Map<String, Any>>()
                    list.add(
                        hashMapOf<String, Any>(
                            DefinedParams.NAME to MedicalReviewConstant.DefaultIDLabel,
                            DefinedParams.ID to MedicalReviewConstant.DefaultSelectID
                        )
                    )
                    resourceState.data?.forEach { site ->
                        StringConverter.convertSiteModelToMap(site)?.let {
                            list.add(it)
                        }
                    }
                    adapter = CustomSpinnerAdapter(this)
                    adapter?.setData(list)
                    binding.etSiteChange.adapter = adapter

                    SecuredPreference.getChosenSiteEntity()?.apply {
                        viewModel.siteDetail = SiteDetails(
                            category,
                            categoryType,
                            siteName,
                            siteId,
                            tenantId,
                            categoryDisplayType
                        )
                        val index =
                            list.indexOfFirst {
                                it.containsKey(DefinedParams.id) && it[DefinedParams.id] != null && convertID(
                                    it[DefinedParams.id]
                                ) == siteId
                            }
                        if (index >= 0) {
                            binding.etSiteChange.setSelection(index, true)
                        }
                    }
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun convertID(id: Any?): Any {
        return when (id) {
            is String -> return id.toLong()
            is Double -> return id.toLong()
            is Long -> id
            else -> id.toString().toLong()
        }
    }

    private fun uploadScreening(data: Boolean?) {
        if (data == true) {
            if (connectivityManager.isNetworkAvailable())
                UploadForegroundService.startService(
                    this,
                    UploadForegroundService.ACTION_START
                )
            navigateToStatsActivity()
        }
    }

    private fun navigateToStatsActivity() {
        val intent = Intent(
            this,
            StatsActivity::class.java
        )
        intent.putExtra(IntentConstants.IntentStatsFromSummary, true)
        startAsNewActivity(intent)
    }

    private fun getScreeningEntityRecord() {
        if (viewModel.screeningEntityRowId != null) {
            viewModel.getScreeningEntity(rowId = viewModel.screeningEntityRowId!!)
        }
    }

    private fun calculateOtherMetrics(
        serverData: List<FormLayout>,
        map: Map<String, Any>
    ) {

        val unitGenericType = SecuredPreference.getUnitMeasurementType()
        showBloodGlucoseValue(serverData, map)
        showBMIValue(serverData, map)
        showPHQ4Score(serverData, map)
        showCVDRiskValue(map)
        showFurtherAssessment(unitGenericType)
    }

    private fun showBloodGlucoseValue(serverData: List<FormLayout>, map: Map<String, Any>) {

        FormResultGenerator.findGroupId(serverData, DefinedParams.Glucose_Type)?.let {
            val subMap = map[it] as Map<String, Any>
            if (subMap.containsKey(DefinedParams.Glucose_Type)) {
                val type = subMap[DefinedParams.Glucose_Type] as String
                val glucoseValue = (subMap[DefinedParams.Glucose_Value] as Double)
                var unitType: String? = null
                if (subMap.containsKey(DefinedParams.BloodGlucoseID + DefinedParams.unitMeasurement_KEY)) {
                    val unitTypeKey =
                        subMap[DefinedParams.BloodGlucoseID + DefinedParams.unitMeasurement_KEY]
                    if (unitTypeKey is String) {
                        unitType = unitTypeKey
                    }
                }
                if (type.lowercase() == DefinedParams.rbs) {
                    formSummaryReporter.setRBSBloodGlucose(glucoseValue)
                    showBindingValue(
                        getString(R.string.blood_glucose_rbs),
                        "${CommonUtils.getDecimalFormatted(glucoseValue)} ${
                            getGlucoseUnit(
                                unitType,
                                true
                            )
                        }"
                    )
                } else if (type.lowercase() == DefinedParams.fbs) {
                    formSummaryReporter.setFBSBloodGlucose(glucoseValue)
                    showBindingValue(
                        getString(R.string.blood_glucose_fbs),
                        "${CommonUtils.getDecimalFormatted(glucoseValue)} ${
                            getGlucoseUnit(
                                unitType,
                                true
                            )
                        }"
                    )
                }
            }
        }
    }

    private fun showFurtherAssessment(unitGenericType: String?) {
        val assessmentCondition = checkAssessmentCondition(unitGenericType)
        binding.riskResultLayout.isEnabled = assessmentCondition
        if (assessmentCondition) {
            binding.riskResultLayout.text = getString(R.string.referred_for_nfurther_assessment)
        } else {
            binding.riskResultLayout.text = getString(R.string.no_assessment_required)
        }
        binding.riskResultLayout.visibility = View.VISIBLE
    }

    private fun checkAssessmentCondition(unitGenericType: String?): Boolean {
        return CommonUtils.checkAssessmentCondition(
            formSummaryReporter.getSystolicAverage(),
            formSummaryReporter.getDiastolicAverage(),
            formSummaryReporter.getPHQ4Score(),
            formSummaryReporter.getFBSBloodGlucose(),
            formSummaryReporter.getRBSBloodGlucose(),
            unitGenericType ?: DefinedParams.Unit_Measurement_Metric_Type
        )
    }

    private fun showPHQ4Score(serverData: List<FormLayout>, map: Map<String, Any>) {
        FormResultGenerator.findGroupId(serverData, DefinedParams.PHQ4_Score)?.let {
            val subMap = map[it] as Map<String, Any>
            if (SecuredPreference.isPHQ4Enabled() && subMap.containsKey(DefinedParams.PHQ4_Score)) {
                val phq4Score = subMap[DefinedParams.PHQ4_Score]
                if (phq4Score is Double) {
                    formSummaryReporter.setPHQ4Score(phq4Score.toInt())
                    showBindingValue(
                        getString(R.string.phq4_score),
                        getPHQ4ReadableName(score = phq4Score.toInt())
                    )
                }
            }
        }
    }

    private fun showCVDRiskValue(map: Map<String, Any>) {
        if (map.containsKey(DefinedParams.CVD_Risk_Score_Display)) {
            val cvdRiskScoreDisplay = map[DefinedParams.CVD_Risk_Score_Display]
            val cvdRiskScore = map[DefinedParams.CVD_Risk_Score] as Double
            if (cvdRiskScoreDisplay is String) {
                showBindingValue(
                    getString(R.string.cvd_risk_level),
                    "$cvdRiskScoreDisplay",
                    CommonUtils.cvdRiskColorCode(
                        cvdRiskScore,
                        context = this@ScreeningSummaryActivity
                    )
                )
            }
        }
    }

    private fun showBMIValue(serverData: List<FormLayout>, map: Map<String, Any>) {
        FormResultGenerator.findGroupId(serverData, DefinedParams.BMI)?.let {
            val subMap = map[it] as Map<String, Any>
            if (subMap.containsKey(DefinedParams.BMI)) {
                val bmiValue = subMap[DefinedParams.BMI]
                if (bmiValue is Double) {
                    showBindingValue(
                        getString(R.string.bmi),
                        CommonUtils.getDecimalFormatted(bmiValue)
                    )
                }
            }
        }
    }

    private fun showBindingValue(title: String, value: String, valueTextColor: Int? = null) {
        val summaryBinding = SummaryLayoutBinding.inflate(layoutInflater)
        summaryBinding.tvKey.text = title
        summaryBinding.tvValue.text = value
        valueTextColor?.let {
            summaryBinding.tvValue.setTextColor(it)
        }
        binding.root.findViewWithTag<LinearLayout>(formSummaryReporter.getFormResultView())
            ?.addView(summaryBinding.root)
    }

    private fun initializeFormSummaryView() {
        binding.labelSiteChange.markMandatory()
        formSummaryReporter = FormSummaryReporter(this, binding.llFamilyRoot)
        viewModel.fetchWorkFlow(UIConstants.screeningUniqueID)
        viewModel.fetchAccountSiteList()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnNext -> {
                if (isSiteChanged) {
                    viewModel.siteDetail?.let {
                        if ((it.siteId ?: 0) > 0) {
                            updatePatientScreening()
                        } else {
                            showErrorDialogue(
                                getString(R.string.error),
                                getString(R.string.facility_error),
                                false
                            ) {}
                        }
                    }
                } else
                    viewModel.screeningLiveDate.setSuccess(data = true)
            }
        }
    }

    private fun updatePatientScreening() {
        val id = viewModel.screeningEntityRowId
        id?.let {
            viewModel.getSavedSiteDetail()?.let { siteDetail ->
                viewModel.updatePatientScreeningInformation(id, siteDetail)
            }
        }
    }

    private fun processSiteSelection(map: Map<String, Any>) {
        isSiteChanged = true
        map.let {
            viewModel.siteDetail?.siteName =
                if (it.containsKey(DefinedParams.NAME)) it[DefinedParams.NAME] as String else ""
            viewModel.siteDetail?.siteId =
                if (it.containsKey(DefinedParams.ID)) CommonUtils.convertType(it[DefinedParams.ID]) else MedicalReviewConstant.DefaultSelectID
            viewModel.siteDetail?.tenantId =
                if (it.containsKey(DefinedParams.TenantId)) CommonUtils.convertType(it[DefinedParams.TenantId]) else MedicalReviewConstant.DefaultSelectID
        }
    }


    private fun onHomeClicked() {
        startAsNewActivity(Intent(this, LandingActivity::class.java))
    }
}
