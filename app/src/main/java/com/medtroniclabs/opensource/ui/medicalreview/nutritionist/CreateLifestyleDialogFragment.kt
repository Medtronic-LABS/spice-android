package com.medtroniclabs.opensource.ui.medicalreview.nutritionist

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.LifeStyleManagement
import com.medtroniclabs.opensource.databinding.FragmentCreateLifestyleDialogBinding
import com.medtroniclabs.opensource.db.tables.NutritionLifeStyle
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.TagListCustomView
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.LifeStyleViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateLifestyleDialogFragment(val callback: (isPositiveResult: Boolean) -> Unit) : DialogFragment(), View.OnClickListener {

    private val viewModel: LifeStyleViewModel by activityViewModels()
    private val nutritionistViewModel: NutritionistViewModel by activityViewModels()
    private lateinit var binding: FragmentCreateLifestyleDialogBinding
    private lateinit var tagListCustomView: TagListCustomView
    @Inject
    lateinit var connectivityManager : ConnectivityManager

    companion object {
        const val TAG = "CreateLifestyleDialogFragment"
        fun newInstance(callback: (isPositiveResult: Boolean) -> Unit): CreateLifestyleDialogFragment {
            return CreateLifestyleDialogFragment(callback)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateLifestyleDialogBinding.inflate(inflater, container, false)

        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.CENTER
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.setDecorFitsSystemWindows(false)
        }
        binding.root.setOnApplyWindowInsetsListener { _, windowInsets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val imeHeight = windowInsets.getInsets(WindowInsets.Type.ime()).bottom
                binding.root.setPadding(0, 0, 0, imeHeight)
                windowInsets.getInsets(WindowInsets.Type.ime() or WindowInsets.Type.systemGestures())
            }
            windowInsets
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        attachObserver()
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initViews() {
        binding.tvOtherNotesLbl.markMandatory()
        tagListCustomView = TagListCustomView(requireContext(), binding.chipGroup) {
            binding.btnConfirm.isEnabled = tagListCustomView.getSelectedTags().isNotEmpty()
        }

        viewModel.getLifeStyleList()

        binding.labelHeader.ivClose.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnConfirm.safeClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.labelHeader.ivClose.id, R.id.btnCancel -> {
                dismiss()
            }
            R.id.btnConfirm -> {
                createLifestyle()
            }
        }
    }

    private fun attachObserver() {
        viewModel.createLifestyleResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    callback.invoke(true)
                    dismiss()
                    viewModel.createLifestyleResponse.value = Resource(ResourceState.ERROR)
                }
            }
        }
        viewModel.lifestyleList.observe(this) { list ->
            list?.let {
                tagListCustomView.addChipItemList(list)
            }
        }
    }

    private fun createLifestyle() {
        if (connectivityManager.isNetworkAvailable()) {
            if (validateInputs()) {
                val requestModel = LifeStyleManagement()
                if (!binding.etLifestyleAssessment.text.isNullOrBlank())
                    requestModel.lifestyleAssessment = binding.etLifestyleAssessment.text.toString()
                if (!binding.etOtherNotes.text.isNullOrBlank())
                    requestModel.otherNote = binding.etOtherNotes.text.toString()
                requestModel.nutritionist = true
                requestModel.lifestyle = tagListCustomView.getSelectedTags()
                    .map { mod -> (mod as NutritionLifeStyle)._id }
                    .toList()
                requestModel.referredFor = tagListCustomView.getSelectedTags()
                    .mapNotNull { mod -> (mod as NutritionLifeStyle).name }
                    .joinToString(separator = ", ")
                requestModel.patientVisitId = nutritionistViewModel.patient_visit_id
                requestModel.patientTrackId = nutritionistViewModel.patient_track_id
                SecuredPreference.getUserDetails().let { user ->
                    requestModel.referredBy = user._id
                    requestModel.referredByDisplay = (user.firstName ?: "") + (user.lastName ?: "")
                    requestModel.referredDate =
                        DateUtils.getCurrentDateTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
                }
                viewModel.createPatientLifestyle(requestModel)
            }
        } else {
            (activity as BaseActivity).showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error), false
            ) {}
        }
    }

    private fun validateInputs(): Boolean {
        var isValidInput = true
        if (binding.etLifestyleAssessment.text.isNullOrBlank()) {
            isValidInput = false
            binding.tvLifestyleAssessmentError.visibility = View.VISIBLE
        } else
            binding.tvLifestyleAssessmentError.visibility = View.GONE

        if (binding.etOtherNotes.text.isNullOrBlank()) {
            isValidInput = false
            binding.tvOtherNotesError.visibility = View.VISIBLE
            binding.tvOtherNotesError.text = getString(R.string.default_user_input_error)
        } else
            binding.tvOtherNotesError.visibility = View.GONE
        return isValidInput
    }

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }
}