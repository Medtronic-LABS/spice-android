package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.databinding.DialogReviewStatusBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import com.medtroniclabs.opensource.ui.screening.TermsAndConditionActivity

class ReviewStatusDialog : DialogFragment(), View.OnClickListener {

    companion object {
        const val TAG = "ReviewStatusDialog"
        fun newInstance(): ReviewStatusDialog {
            return ReviewStatusDialog()
        }
    }

    private val viewModel: PatientDetailViewModel by activityViewModels()

    private lateinit var binding: DialogReviewStatusBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogReviewStatusBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        isCancelable = false
        binding.btnEnroll.visibility = View.VISIBLE
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
        binding.btnEnroll.safeClickListener(this)
    }

    private fun attachListeners() {
        viewModel.patientDetailsResponse.observe(viewLifecycleOwner) { resourceState ->
            if (resourceState.state == ResourceState.SUCCESS) {
                resourceState.data?.let { patientDetails ->
                    patientDetails.enrollmentAt?.let {
                        binding.btnEnroll.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.labelHeader.ivClose.id,
            binding.btnDone.id -> {
                dismiss()
                if (requireActivity() is BaseActivity) {
                    (requireActivity() as BaseActivity).startAsNewActivity(
                        Intent(
                            requireContext(),
                            LandingActivity::class.java
                        )
                    )
                }
            }
            binding.btnEnroll.id -> {
                dismiss()
                val intent = Intent(requireContext(), TermsAndConditionActivity::class.java)
                intent.putExtra(IntentConstants.IntentEnrollment, true)
                intent.putExtra(DefinedParams.Patient_Id, viewModel.patientId)
                intent.putExtra(DefinedParams.Screening_Id, viewModel.screening_id)
                intent.putExtra(IntentConstants.isFromSummaryPage, true)
                if (requireActivity() is BaseActivity) {
                    (requireActivity() as BaseActivity).startAsNewActivity(intent)
                }
            }
        }
    }
}