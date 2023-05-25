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
import com.medtroniclabs.opensource.appextensions.setSuccess
import com.medtroniclabs.opensource.custom.GeneralInfoDialog
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.ScreeningDetail
import com.medtroniclabs.opensource.databinding.FragmentEnrollmentFormBinding
import com.medtroniclabs.opensource.formgeneration.FormGenerator
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.formgeneration.listener.FormEventListener
import com.medtroniclabs.opensource.formgeneration.model.FormLayout
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.FormResultGenerator
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.enrollment.viewmodel.EnrollmentFormBuilderViewModel
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel

class EnrollmentFormFragment : BaseFragment(), FormEventListener {

    private lateinit var binding: FragmentEnrollmentFormBinding
    private val viewModel: EnrollmentFormBuilderViewModel by activityViewModels()
    private val patientDetailsViewModel: PatientDetailViewModel by activityViewModels()
    private lateinit var formGenerator: FormGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            (activity as EnrollmentFormBuilderActivity).showOnBackPressedAlert()
        }

        callback.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEnrollmentFormBinding.inflate(inflater, container, false)
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
        viewModel.fetchWorkFlow(UIConstants.enrollmentUniqueID,DefinedParams.Workflow)
        attachObservers()
    }

    private fun attachObservers() {
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
                    formResponse(resourceState.data?.formLayout)
                }
            }
        }
        viewModel.formResponseListLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    formRepsonseList(resourceState.data)
                }
            }
        }
        patientDetailsViewModel.screeningDetailResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    viewModel.assessment_required = true
                    formGenerator.onValidationCompleted(
                        screeningDetails = resourceState.data,
                        isAssessmentRequired = viewModel.assessment_required
                    )
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        showError(it)
                    }
                }
            }
        }

        viewModel.enrollPatientLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        (activity as BaseActivity).showErrorDialogue(getString(R.string.error),it, false){}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    (activity as EnrollmentFormBuilderActivity).replaceFragmentInId<EnrollmentAssessmentFragment>()
                }
            }
        }

        viewModel.localDataCacheResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    resourceState.data?.let {
                        formGenerator.setSpinnerData(it)
                    }
                }
                else -> {
                    //Invoked if response state is not success
                }
            }
        }

        viewModel.programListResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    resourceState.data?.let {
                        formGenerator.setSpinnerData(it)
                    }
                }
                else -> {
                    //Invoked if response state is not success
                }
            }
        }
    }

    private fun formResponse(formLayout: List<FormLayout>?) {
        formLayout?.let {
            formGenerator.populateViews(it)
            if (viewModel.isFromDirectEnrollment) {
                viewModel.assessment_required = true
                viewModel.patient_track_id = null
                formGenerator.changeButtonTitle(viewModel.assessment_required)
            } else {
                patientDetailsViewModel.patientId?.let { patientId ->
                    if (patientId > 0) {
                        viewModel.screeningId?.let { screeningID ->
                            val request = ScreeningDetail(patientId.toLong(), screeningID.toLong(), true)
                            patientDetailsViewModel.getScreeningDetails(
                                request
                            )
                        }
                    }
                }
            }
        }
    }

    private fun formRepsonseList(formList: ArrayList<Pair<String, String>>?) {
        formList?.let {
            formGenerator.combineAccountWorkflows(it, UIConstants.enrollmentUniqueID)
            if (viewModel.isFromDirectEnrollment) {
                viewModel.assessment_required = true
                viewModel.patient_track_id = null
                formGenerator.changeButtonTitle(viewModel.assessment_required)
            } else {
                patientDetailsViewModel.patientId?.let { patientId ->
                    if (patientId > 0) {
                        viewModel.screeningId?.let { screeningID ->
                            val request = ScreeningDetail(patientId, screeningID, true)
                            patientDetailsViewModel.getScreeningDetails(
                                request
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getValue(responseData: java.util.HashMap<String, Any>, key: String): Any? {
        return if (responseData.containsKey(key))
            responseData[key]
        else
            null
    }

    private fun showError(it: String) {
        (activity as BaseActivity).showErrorDialogue(getString(R.string.error), it, false) { ok ->
            if (ok) {
                (activity as BaseActivity).startAsNewActivity(
                    Intent(
                        context,
                        LandingActivity::class.java
                    )
                )
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            EnrollmentFormFragment().apply {
            }
    }

    override fun onFormSubmit(resultHashMap: HashMap<String, Any>, serverData: List<FormLayout?>?) {
        val map = HashMap<String, Any>(resultHashMap)
        SecuredPreference.getSelectedSiteEntity()?.let {
            map.put(DefinedParams.SiteId, it.id)
        }
        map[DefinedParams.Initial] = viewModel.patientInitial
        CommonUtils.calculateHtnDiabetesDiagnosis(map)
        val result = serverData?.let {
            FormResultGenerator().groupValues(
                context = requireContext(),
                serverData = it,
                map
            )
        }
        result?.second?.let {
            viewModel.groupedEnrollmentHashMap = it
        }
        viewModel.formResponseLiveData.setSuccess(data = null)
        result?.first?.let {
            if (viewModel.assessment_required) {
                hideLoading()
                (activity as EnrollmentFormBuilderActivity).replaceFragmentInId<EnrollmentAssessmentFragment>()
            } else
                viewModel.enrollPatient(requireContext(), it)
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
        if (viewModel.isFromDirectEnrollment) {
            formGenerator.showFormSubmitButton(DefinedParams.btnSubmit)
        } else {
            formGenerator.hideFormSubmitButton(DefinedParams.btnSubmit)
        }
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        if (localDataCache is String) {
            viewModel.loadDataCacheByType(id, localDataCache, selectedParent)
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

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let { resultIntent ->
                    formGenerator.onSpinnerActivityResult(resultIntent)
                }
            }
        }
}