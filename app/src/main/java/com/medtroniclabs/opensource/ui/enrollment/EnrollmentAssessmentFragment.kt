package com.medtroniclabs.opensource.ui.enrollment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.custom.GeneralInfoDialog
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.custom.SecuredPreference.getUnitMeasurementType
import com.medtroniclabs.opensource.databinding.FragmentEnrollmentAssessmentBinding
import com.medtroniclabs.opensource.formgeneration.FormGenerator
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.calculateCVDRiskFactor
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.getMeasurementTypeValues
import com.medtroniclabs.opensource.formgeneration.listener.FormEventListener
import com.medtroniclabs.opensource.formgeneration.model.FormLayout
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.FormResultGenerator
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.enrollment.viewmodel.EnrollmentFormBuilderViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel

class EnrollmentAssessmentFragment : BaseFragment(), FormEventListener {

    lateinit var binding: FragmentEnrollmentAssessmentBinding

    private val viewModel: EnrollmentFormBuilderViewModel by activityViewModels()

    private val patientDetailsViewModel: PatientDetailViewModel by activityViewModels()

    private lateinit var formGenerator: FormGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            (activity as EnrollmentFormBuilderActivity).showOnBackPressedAlert()
        }
        callback.isEnabled = true
        viewModel.getRiskEntityList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEnrollmentAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeFormGenerator()
    }

    private fun initializeFormGenerator() {
        formGenerator =
            FormGenerator(
                requireActivity(),
                binding.llForm,
                resultLauncher,
                this,
                binding.scrollView,
                SecuredPreference.getIsTranslationEnabled()
            )
        viewModel.fetchWorkFlow(UIConstants.AssessmentUniqueID)
        attachObservers()
    }

    private fun attachObservers() {

        viewModel.enrollPatientLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        (activity as BaseActivity).showErrorDialogue(getString(R.string.error),it, isNegativeButtonNeed = false){}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    (activity as EnrollmentFormBuilderActivity).replaceFragmentInId<EnrollmentSummaryFragment>()
                }
            }
        }

        viewModel.formResponseLiveData.observe(viewLifecycleOwner) { resourceState ->
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
                        formGenerator.populateViews(it)
                        patientDetailsViewModel.screeningDetailResponse.value?.data?.let { search ->
                            val heightValue = search[DefinedParams.Height]
                            val weightValue = search[DefinedParams.Weight]
                            val height: Double? = fetchDouble(heightValue)
                            val weight: Double? = fetchDouble(weightValue)
                            formGenerator.showHeightWeight(
                                height,
                                weight
                            )
                        }
                    }
                }
            }
        }

        viewModel.mentalHealthQuestions.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    formGenerator.loadMentalHealthQuestions(resourceState.data)
                }
            }
        }
    }

    private fun fetchDouble(value: Any?): Double? {
        return if (value is Double)
            value
        else
            null
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

    companion object {
        @JvmStatic
        fun newInstance() =
            EnrollmentAssessmentFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onFormSubmit(
        resultHashMap: HashMap<String, Any>,
        serverData: List<FormLayout?>?
    ) {

        val unitGenericType = getUnitMeasurementType()
        val map = HashMap<String, Any>(resultHashMap)
        if (!map.containsKey(DefinedParams.GlucoseLog)) {
            if (viewModel.groupedEnrollmentHashMap.containsKey(DefinedParams.GlucoseLog)) {
                viewModel.groupedEnrollmentHashMap.remove(DefinedParams.GlucoseLog)
            }
            CommonUtils.processLastMealTime(map)
            CommonUtils.calculateBloodGlucose(map, formGenerator)
        }
        CommonUtils.calculateBMI(map, unitGenericType)
        CommonUtils.processHeightMap(map, unitGenericType)
        CommonUtils.calculateAverageBloodPressure(requireContext(), map)
        var isConfirmDiagnosis = false
        patientDetailsViewModel.screeningDetailResponse.value?.data?.let { searchResponse ->
            if (searchResponse.containsKey(DefinedParams.is_confirm_diagnosis) && searchResponse[DefinedParams.is_confirm_diagnosis] is Boolean) {
                    isConfirmDiagnosis = searchResponse[DefinedParams.is_confirm_diagnosis] as Boolean
            }
        }
        CommonUtils.calculateProvisionalDiagnosis(
            map, isConfirmDiagnosis,
            formGenerator.getSystolicAverage(), formGenerator.getDiastolicAverage(),
            formGenerator.getFbsBloodGlucose(), formGenerator.getRbsBloodGlucose(),
            getMeasurementTypeValues(map)
        )
        formGenerator.setPhQ4Score(CommonUtils.calculatePHQScore(map))
        val enrollmentMap = viewModel.groupedEnrollmentHashMap
        val bioMetricMap = enrollmentMap[DefinedParams.Bio_Metrics] as HashMap<String, Any>
        map.putAll(bioMetricMap)
        calculateCVDRiskFactor(map, viewModel.list, formGenerator.getSystolicAverage())
        map[DefinedParams.UnitMeasurement] = getUnitMeasurementType()
        viewModel.patient_track_id?.let {
            map[DefinedParams.PatientTrackId] = it
        }
        val result = serverData?.let {
            FormResultGenerator().groupValues(
                context = requireContext(),
                serverData = it,
                map,
                requestFrom = UIConstants.enrollmentUniqueID
            )
        }
        result?.second?.let {
            it.putAll(enrollmentMap)
            StringConverter.convertGivenMapToString(it)?.let { str ->
                viewModel.enrollPatient(requireContext(), str)
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
        ).show(childFragmentManager, GeneralInfoDialog.TAG)
    }

    override fun onRenderingComplete() {
        formGenerator.onValidationCompleted(
            screeningDetails = patientDetailsViewModel.screeningDetailResponse.value?.data,
            isAssessmentRequired = viewModel.assessment_required
        )
        patientDetailsViewModel.screeningDetailResponse.value?.data?.let {
            var height: Double? = null
            var weight: Double? = null
            val heightValue = it[DefinedParams.Height]
            val weightValue = it[DefinedParams.Weight]
            if (heightValue is Double) {
                height = heightValue
            }
            if (weightValue is Double) {
                weight = weightValue
            }
            formGenerator.showHeightWeight(
                height,
                weight
            )
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        if (localDataCache is String) {
            when (localDataCache) {
                DefinedParams.PHQ4, DefinedParams.PHQ9 -> {
                    viewModel.fetchMentalHealthQuestions(id, localDataCache)
                }
                DefinedParams.Fetch_MH_Questions -> {
                    formGenerator.fetchMHQuestions(viewModel.mentalHealthQuestions.value?.data)
                }
            }
        }
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
            positiveButtonName = getString(
                R.string.ok
            )
        ) {

        }
    }

}