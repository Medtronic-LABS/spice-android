package com.medtroniclabs.opensource.ui.medicalreview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.gson.JsonObject
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.customSerializableExtra
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.custom.GeneralInfoDialog
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.custom.SecuredPreference.getUnitMeasurementType
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.databinding.FragmentEnrollmentAssessmentBinding
import com.medtroniclabs.opensource.formgeneration.FormGenerator
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.formgeneration.listener.FormEventListener
import com.medtroniclabs.opensource.formgeneration.model.FormLayout
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.AssessmentReadingViewModel

class AssessmentReadingActivity : BaseActivity(), FormEventListener {

    private lateinit var binding: FragmentEnrollmentAssessmentBinding
    private lateinit var formTypeId: String
    private lateinit var patientDetails: PatientDetailsModel
    private val viewModel: AssessmentReadingViewModel by viewModels()
    private lateinit var formGenerator: FormGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentEnrollmentAssessmentBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            title = getString(R.string.add_new_reading),
            homeAndBackVisibility = Pair(null, true)
        )
        formTypeId = intent.getStringExtra(IntentConstants.INTENT_FORM_ID).toString()
        (intent.customSerializableExtra(IntentConstants.IntentPatientDetails) as PatientDetailsModel?)?.let { intentPatientDetails ->
            patientDetails = intentPatientDetails
        }
        initializeFormGenerator()
    }

    private fun initializeFormGenerator() {
        formGenerator =
            FormGenerator(this, binding.llForm, resultLauncher, this, binding.scrollView,
                SecuredPreference.getIsTranslationEnabled())
        viewModel.fetchWorkFlow(UIConstants.AssessmentUniqueID)
        viewModel.getRiskEntityList()
        attachObservers()
    }

    private fun attachObservers() {

        viewModel.formResponseLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    val formList = resourceState.data?.formLayout
                    formList?.let {
                        val filter = it.filter { formLayout ->
                            formLayout.id == formTypeId
                                    || formLayout.family == formTypeId
                                    || formLayout.id == DefinedParams.btnSubmit
                        }
                        formGenerator.populateViews(filter)
                    }
                }
            }
        }

        viewModel.BPLogResponseLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    finish()
                }
            }
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let { resultIntent ->
                    formGenerator.onSpinnerActivityResult(resultIntent)
                }
            }
        }

    override fun onFormSubmit(
        resultHashMap: HashMap<String, Any>,
        serverData: List<FormLayout?>?
    ) {

        val unitGenericType = getUnitMeasurementType()
        val map = HashMap<String, Any>(resultHashMap)
        map[DefinedParams.TenantId] = patientDetails.tenantId ?: ""
        when (formTypeId) {
            DefinedParams.bp_log -> { // Add new BP log
                map[DefinedParams.PatientTrackId] = patientDetails._id
                CommonUtils.processHeightMap(map, unitGenericType)
                CommonUtils.calculateBMI(map, unitGenericType)
                CommonUtils.calculateAverageBloodPressure(this, map)
                patientDetails.isRegularSmoker?.let {
                    map[DefinedParams.Is_Regular_Smoker] = it
                }
                // For CVD risk calculation.
                patientDetails.age?.let {
                    map[DefinedParams.Age] = it
                }
                patientDetails.gender?.let {
                    map[DefinedParams.Gender] = it
                }
                CommonUtils.calculateCVDRiskFactor(
                    map,
                    viewModel.list,
                    formGenerator.getSystolicAverage()
                )
                // Removed after calculation
                map.remove(DefinedParams.Age)
                map.remove(DefinedParams.Gender)
                map[DefinedParams.UnitMeasurement] = getUnitMeasurementType()
                StringConverter.convertGivenMapToString(map)?.let {
                    if (connectivityManager.isNetworkAvailable()) {
                        val rootJson: JsonObject = StringConverter.getJsonObject(it)
                        viewModel.createNewBPLogForPatient(rootJson)
                    } else {
                        showErrorDialogue(getString(R.string.error),getString(R.string.no_internet_error), false){}
                    }
                }
            }
            DefinedParams.GlucoseLog -> {
                // Add new Glucose log
                if (map.isNullOrEmpty()) {
                    showErrorDialogue(getString(R.string.error),getString(R.string.default_user_input_error), false){}
                } else if(map.containsKey(DefinedParams.BloodGlucoseID) || map.containsKey(DefinedParams.Hemoglobin)) {
                    map[DefinedParams.PatientTrackId] = patientDetails._id
                    CommonUtils.processHeightMap(map, unitGenericType)
                    CommonUtils.processLastMealTime(map)
                    CommonUtils.calculateBloodGlucose(map, formGenerator)
                    StringConverter.convertGivenMapToString(map)?.let {
                        if (connectivityManager.isNetworkAvailable()) {
                            val rootJson: JsonObject =
                                StringConverter.getJsonObject(it)
                            viewModel.createNewBloodGlucoseForPatient(rootJson)
                        } else {
                            showErrorDialogue(getString(R.string.error),getString(R.string.no_internet_error), false){}
                        }
                    }
                } else {
                    showErrorDialogue(getString(R.string.error),getString(R.string.need_glucose_entry), false){}
                }
            }
        }
    }

    override fun onBPInstructionClicked(
        title: String,
        informationList: ArrayList<String>,
        description: String?
    ) {
        GeneralInfoDialog.newInstance(
            title,
            description,
            informationList
        ).show(supportFragmentManager, GeneralInfoDialog.TAG)
    }

    override fun onRenderingComplete() {
        patientDetails.let {
            formGenerator.showHeightWeight(it.height, it.weight)
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        /**
         * this method is not used
         */
    }

    override fun onFormError() {
        /**
         * this method is not used
         */
    }

    override fun onJsonMismatchError() {
        showAlertWith(
            message = getString(R.string.check_form_attribute),
            isNegativeButtonNeed = false,
            positiveButtonName = getString(R.string.ok)
        ) {

        }
    }

}