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
import com.medtroniclabs.opensource.data.model.BloodGlucose
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.databinding.DialogBloodGlucoseBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.customviews.DividerNCDItemDecoration
import com.medtroniclabs.opensource.ui.medicalreview.AssessmentReadingActivity
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import kotlin.math.roundToInt

class BloodGlucoseDialog: DialogFragment(), View.OnClickListener {

    private lateinit var binding: DialogBloodGlucoseBinding
    private val viewModel: PatientDetailViewModel by activityViewModels()
    private var patientDetails: PatientDetailsModel? = null

    companion object {
        const val TAG = "BloodGlucoseDialog"
        fun newInstance(): BloodGlucoseDialog {
            return BloodGlucoseDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBloodGlucoseBinding.inflate(inflater, container, false)
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

    override fun onResume() {
        super.onResume()
        patientDetails?.let { patientDetails ->
            val request = AssessmentListRequest(
                patientDetails._id,
                false,
                sortField = DefinedParams.BGTakenOn
            )
            viewModel.getPatientBloodGlucoseList(requireContext(),request)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.density * DefinedParams.DialogWidth).roundToInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivClose -> {
                viewModel.getPatientDetails(requireContext(),false)
                dismiss()
            }
            R.id.btnAddNew -> {
                showGlucoseView()
            }
        }
    }

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
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

        viewModel.patientBloodGlucoseListResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { bloodGlucose ->
                        bloodGlucose.glucoseLogList?.let {
                            setAdapter(it)
                        }
                    }
                }
            }
        }
    }

    private fun setAdapter(bloodGlucoseList: ArrayList<BloodGlucose>) {
        binding.rvBloodGlucose.layoutManager = LinearLayoutManager(binding.root.context)
        binding.rvBloodGlucose.addItemDecoration(
            DividerNCDItemDecoration(activity, R.drawable.divider)
        )
        binding.rvBloodGlucose.adapter = BloodGlucoseAdapter(requireContext(), bloodGlucoseList)
    }

    private fun showGlucoseView() {
        patientDetails?.let {
            val intent = Intent(context, AssessmentReadingActivity::class.java)
            intent.putExtra(IntentConstants.INTENT_FORM_ID, DefinedParams.GlucoseLog)
            intent.putExtra(IntentConstants.IntentPatientDetails, it)
            context?.startActivity(intent)
        }
    }

}