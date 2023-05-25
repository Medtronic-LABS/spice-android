package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.data.model.AssessmentListRequest
import com.medtroniclabs.opensource.data.model.BPResponse
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.databinding.DialogBloodPressureBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.customviews.DividerNCDItemDecoration
import com.medtroniclabs.opensource.ui.medicalreview.AssessmentReadingActivity
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel

class BPDialog : DialogFragment(), View.OnClickListener {

    private lateinit var binding: DialogBloodPressureBinding
    private val viewModel: PatientDetailViewModel by activityViewModels()
    private var patientDetails: PatientDetailsModel? = null

    companion object {
        const val TAG = "BPDialog"
        fun newInstance(): BPDialog {
            return BPDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBloodPressureBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        initializeView()
        attachListeners()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initializeView() {
        binding.titleCard.ivClose.safeClickListener(this)
        binding.btnAddNew.safeClickListener(this)
    }

    private fun attachListeners() {

        viewModel.patientDetailsResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    resourceState.data?.let {
                        patientDetails = it
                    }
                }
                else -> {
                    //Invoked if response state is not success
                }
            }
        }

        viewModel.patientBPLogListResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { bpLogResponse ->
                        bpLogResponse.bpLogList?.let {
                            setAdapter(it)
                        }
                    }
                }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivClose -> {
                viewModel.getPatientDetails(requireContext(),false)
                dismiss()
            }
            R.id.btnAddNew -> {
                showAddNewBPView()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        patientDetails?.let { patientDetails ->
            val request = AssessmentListRequest(
                patientDetails._id,
                false,
                sortField = DefinedParams.BPTakenOn
            )
            viewModel.getPatientBPLogList(requireContext(),request)
        }
    }

    private fun showLoading() {
       binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
       binding.loadingProgress.visibility = View.GONE
    }

    private fun setAdapter(listOfBPResponse: ArrayList<BPResponse>) {
        binding.rvBPList.layoutManager = LinearLayoutManager(binding.root.context)
        binding.rvBPList.addItemDecoration(
            DividerNCDItemDecoration(activity, R.drawable.divider)
        )
        binding.rvBPList.adapter = BPAdapter(requireContext(), listOfBPResponse)
    }

    private fun showAddNewBPView() {
        patientDetails?.let {
            val intent = Intent(context, AssessmentReadingActivity::class.java)
            intent.putExtra(IntentConstants.INTENT_FORM_ID, DefinedParams.bp_log)
            intent.putExtra(IntentConstants.IntentPatientDetails, it)
            context?.startActivity(intent)
        }
    }
}