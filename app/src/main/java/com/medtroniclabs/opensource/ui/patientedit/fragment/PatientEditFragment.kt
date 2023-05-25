package com.medtroniclabs.opensource.ui.patientedit.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.databinding.FragmentPatientEditBinding
import com.medtroniclabs.opensource.formgeneration.FormGenerator
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType
import com.medtroniclabs.opensource.formgeneration.listener.FormEventListener
import com.medtroniclabs.opensource.formgeneration.model.FormLayout
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.medicalreview.dialog.GeneralSuccessDialog
import com.medtroniclabs.opensource.ui.patientedit.PatientEditActivity
import com.medtroniclabs.opensource.ui.patientedit.viewmodel.PatientEditViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PatientEditFragment : BaseFragment(), FormEventListener, View.OnClickListener {

    lateinit var binding: FragmentPatientEditBinding

    private val viewModel: PatientEditViewModel by activityViewModels()

    private lateinit var formGenerator: FormGenerator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPatientEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeFormBuilder()
        attachObserver()
        setListener()
    }

    private fun setListener() {
        binding.actionButton.safeClickListener(this)
    }

    private fun initializeFormBuilder() {
        formGenerator = FormGenerator(
            requireContext(),
            binding.llForm,
            resultLauncher,
            this,
            binding.scrollView,
            SecuredPreference.getIsTranslationEnabled()
        )
        viewModel.fetchWorkFlow(UIConstants.enrollmentUniqueID)
    }

    private fun attachObserver() {
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
                    formList?.let { list ->
                        val data = list.filter { it.viewType != ViewType.VIEW_TYPE_FORM_CARD_FAMILY && it.isEditable }
                        binding.actionButton.visibility = View.VISIBLE
                        binding.actionButton.isEnabled = data.isNotEmpty()
                        formGenerator.populateEditableViews(list)
                        viewModel.patientID?.let { detail ->
                            viewModel.getPatientBasicDetail(detail)
                        }
                    }
                }
            }
        }
        viewModel.patientDetailMap.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    formGenerator.onValidationCompleted(
                        screeningDetails = resourceState.data,
                        isAssessmentRequired = false
                    )
                }
            }
        }

        viewModel.updatePatientMap.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        (activity as BaseActivity).showErrorDialogue(
                            getString(R.string.error),
                            it,
                            isNegativeButtonNeed = false
                        ) {}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    showSuccessDialog()
                }
            }
        }
    }

    private fun showSuccessDialog(){
        GeneralSuccessDialog.newInstance(
            requireContext(),
            getString(R.string.patient_details),
            getString(R.string.patient_detail_updated_successfully),
            needHomeNav = false,
            callback = {
                if (it){
                    if (viewModel.fromMedicalReview && activity is PatientEditActivity){
                        activity?.finish()
                    }else{
                        (activity as BaseActivity).redirectToHome()
                    }
                }
            }).show(childFragmentManager, GeneralSuccessDialog.TAG)
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

    override fun onFormSubmit(resultHashMap: HashMap<String, Any>, serverData: List<FormLayout?>?) {
        viewModel.patient_tracker_id?.let { patientTrackerId ->
            val map = HashMap<String,Any>()
            map.putAll(resultHashMap)
            viewModel.patientID?.let { patientId ->
                map[DefinedParams.ID] = patientId
            }
            map[DefinedParams.PatientTrackId] = patientTrackerId
            viewModel.updatePatientDetail(requireContext(),map)
        }
    }

    override fun onBPInstructionClicked(
        title: String,
        informationList: ArrayList<String>,
        description: String?
    ) {
        /**
         * this method is not used
         */
    }

    override fun onRenderingComplete() {
        /**
         * this method is not used
         */
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
        /**
         * this method is not used
         */
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.actionButton.id -> {
                formGenerator.formSubmitAction(view)
            }
        }
    }
    companion object {
        const val TAG = "PatientEditFragment"
    }
}