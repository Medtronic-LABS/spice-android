package com.medtroniclabs.opensource.ui.patientedit.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.databinding.FragmentPatientViewBinding
import com.medtroniclabs.opensource.formgeneration.FormResultViewer
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.patientedit.PatientEditActivity
import com.medtroniclabs.opensource.ui.patientedit.viewmodel.PatientEditViewModel

class PatientViewFragment: BaseFragment() {

    lateinit var binding:FragmentPatientViewBinding

    private val viewModel: PatientEditViewModel by activityViewModels()

    private lateinit var formResultViewer: FormResultViewer


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPatientViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        attachObserver()

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
                    formList?.let {
                        viewModel.patientID?.let {
                            viewModel.getPatientBasicDetail(it)
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
                    patientDetailResponse(resourceState.data)
                }
            }
        }
    }

    private fun patientDetailResponse(resourceData: HashMap<String, Any>?) {
        viewModel.formResponseLiveData.value?.data?.let { formResponseModel ->
            resourceData?.let { map ->
                val data =
                    formResponseModel.formLayout.filter { it.viewType != ViewType.VIEW_TYPE_FORM_CARD_FAMILY && it.isEditable }
                if (data.isNotEmpty()) {
                    formResultViewer.populateResultViews(
                        formResponseModel.formLayout,
                        map
                    )
                } else {
                    if (activity is BaseActivity) {
                        (activity as BaseActivity).showAlertWith(
                            getString(R.string.patient_edit_admin_message),
                            positiveButtonName = getString(R.string.ok),
                            callback = {
                                if (activity is PatientEditActivity) {
                                    (activity as PatientEditActivity).finish()
                                }
                            })
                    }
                }
            }
        }
    }

    private fun initializeViews() {
        formResultViewer = FormResultViewer(requireContext(),binding.llForm)
        viewModel.fetchWorkFlow(UIConstants.enrollmentUniqueID)
    }

    companion object {
        const val TAG = "PatientViewFragment"
    }
}