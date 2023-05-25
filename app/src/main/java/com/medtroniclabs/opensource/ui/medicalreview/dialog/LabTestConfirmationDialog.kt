package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.customGetSerializable
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.LabTestModel
import com.medtroniclabs.opensource.databinding.FragmentLabTestConfirmationDialogBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.LabTestViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LabTestConfirmationDialog() : DialogFragment(), View.OnClickListener {

    companion object {
        const val TAG = "LabTestConfirmationDialog"
        const val KEY_MODEL = "KEY_MODEL"

        @JvmStatic
        fun newInstance(model: LabTestModel): LabTestConfirmationDialog {
            val fragment = LabTestConfirmationDialog()
            fragment.arguments = Bundle().apply {
                putSerializable(KEY_MODEL, model)
            }
            return fragment
        }
    }


    private lateinit var model: LabTestModel
    private lateinit var binding: FragmentLabTestConfirmationDialogBinding
    private val viewModel: LabTestViewModel by activityViewModels()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLabTestConfirmationDialogBinding.inflate(inflater,container,false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.attributes?.windowAnimations = R.style.dialogEnterExitAnimation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readArguments()
        setListeners()
    }

    private fun readArguments() {
        arguments?.let { args ->
            if(args.containsKey(KEY_MODEL))
            {
                (args.customGetSerializable(KEY_MODEL) as LabTestModel?)?.let { labTestModel ->
                    model = labTestModel
                }
            }
        }
    }

    private fun setListeners() {
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnConfirm.safeClickListener(this)
    }


    override fun onClick(mView: View?) {

        when(mView?.id)
        {
            binding.labelHeader.ivClose.id -> dismiss()

            binding.btnCancel.id -> dismiss()

            binding.btnConfirm.id -> {
                if (connectivityManager.isNetworkAvailable()) {
                    val request = HashMap<String, Any>()
                    request[DefinedParams.Comment] = model.resultComments ?: ""
                    SecuredPreference.getSelectedSiteEntity()?.tenantId?.let {
                        request[DefinedParams.TenantId] = it
                    }
                    request[DefinedParams.PatientTrackId] = viewModel.patientTrackId
                    request[DefinedParams.ID] = model._id ?: -1
                    viewModel.reviewLabTestResult(requireContext(),request)
                    dismiss()
                } else {
                    (activity as BaseActivity).showErrorDialogue(getString(R.string.error),getString(R.string.no_internet_error), false){}
                }
            }
        }
    }
}