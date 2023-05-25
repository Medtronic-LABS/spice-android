package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.fetchString
import com.medtroniclabs.opensource.appextensions.hideKeyboard
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.appextensions.setError
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.RegionSiteResponse
import com.medtroniclabs.opensource.data.model.SiteRoleResponse
import com.medtroniclabs.opensource.data.model.TransferCreateRequest
import com.medtroniclabs.opensource.databinding.DialogTransferArchiveBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.medicalreview.adapter.RoleUserAutoCompleteAdapter
import com.medtroniclabs.opensource.ui.medicalreview.adapter.SiteAutoCompleteAdapter
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.MedicalReviewBaseViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TransferArchiveDialog : DialogFragment(), View.OnClickListener {

    private lateinit var binding: DialogTransferArchiveBinding
    private val medicalReviewViewModel: MedicalReviewBaseViewModel by activityViewModels()
    private val detailsViewModel: PatientDetailViewModel by activityViewModels()
    @Inject
    lateinit var connectivityManager: ConnectivityManager
    private lateinit var myAdapter: SiteAutoCompleteAdapter
    private lateinit var userAdapter: RoleUserAutoCompleteAdapter
    private var canSearch: Boolean = true
    private var userSearch: Boolean = true
    private var transferReason: String? = null
    private var selectedSite: RegionSiteResponse? = null
    private var selectedUser: SiteRoleResponse? = null

    companion object{
        val TAG = "TransferArchiveDialog"
        fun newInstance():TransferArchiveDialog{
            return TransferArchiveDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogTransferArchiveBinding.inflate(inflater, container, false)
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
        isCancelable = false
        medicalReviewViewModel.isTransferDialogVisible = true
        initializeView()
        setListeners()
        attachObserver()
    }

    private fun initializeView() {
        detailsViewModel.patientDetailsResponse.value?.data?.let { details ->
            binding.labelHeader.titleView.text =
                CommonUtils.capitalize("${getString(R.string.transfer)} ${details.firstName} ${details.lastName}${"?"}")
            binding.tvReasonHeader.text =
                CommonUtils.capitalize("${getString(R.string.reason_to_transfer)} ${details.firstName} ${details.lastName}")
        }
        binding.tvFacility.markMandatory()
        binding.tvPhysician.markMandatory()
        binding.tvReasonHeader.markMandatory()
        myAdapter = SiteAutoCompleteAdapter(requireContext())
        userAdapter = RoleUserAutoCompleteAdapter(requireContext())
    }

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }

    private fun setListeners() {
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnTransfer.safeClickListener(this)

        binding.etFacility.setOnItemClickListener { _, _, index, _ ->
            canSearch = false
            selectedSite = null
            medicalReviewViewModel.searchSiteResponse.value?.data?.let { site ->
                site.let { list ->
                    val size = list.size
                    if (size > 0 && index < size) {
                        selectedSite = list[index]
                    }
                    binding.etPhysician.isEnabled = true
                }
            }
        }

        binding.etFacility.threshold = 2
        binding.etFacility.addTextChangedListener(textWatcher)

        binding.etReason.addTextChangedListener {
            if (it.isNullOrBlank()) {
                transferReason = null
            } else {
                transferReason = it.fetchString()
            }
        }

        binding.etPhysician.setOnItemClickListener { _, _, index, _ ->
            userSearch = false
            selectedUser = null
            medicalReviewViewModel.searchRoleUserResponse.value?.data?.let { user ->
                user.let { list ->
                    val size = list.size
                    if (size > 0 && index < size) {
                        selectedUser = list[index]
                    }
                }
            }
        }
        binding.etPhysician.threshold = 2
        binding.etPhysician.addTextChangedListener(roleTextWatcher)
    }

    private fun attachObserver() {
        medicalReviewViewModel.searchSiteResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {

                }
                ResourceState.SUCCESS -> {
                    resourceState.data?.let {
                        loadSearchDropDown(it)
                    }
                }
                ResourceState.ERROR -> {
                    resourceState.message?.let {
                        (activity as BaseActivity).showErrorDialogue(
                            getString(R.string.error),
                            it,
                            isNegativeButtonNeed = false
                        ) {}
                    }
                }
            }
        }

        medicalReviewViewModel.searchRoleUserResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {

                }
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { it ->
                        loadUserSearchDropDown(it)
                    }
                }
                ResourceState.ERROR -> {
                    resourceState.message?.let {
                        (activity as BaseActivity).showErrorDialogue(
                            getString(R.string.error),
                            it,
                            isNegativeButtonNeed = false
                        ) {}
                    }

                }
            }
        }

        medicalReviewViewModel.patientTransferResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    medicalReviewViewModel.searchRoleUserResponse.setError()
                    medicalReviewViewModel.searchSiteResponse.setError()
                    medicalReviewViewModel.patientTransferResponse.setError()
                    resourceState.data?.let { map ->
                        if (map.containsKey(DefinedParams.message)) {
                            val message = map[DefinedParams.message]
                            if (message is String) {
                                GeneralSuccessDialog.newInstance(
                                    requireContext(),
                                    getString(R.string.transfer),
                                    message,
                                    needHomeNav = false
                                )
                                    .show(parentFragmentManager, GeneralSuccessDialog.TAG)
                            }
                        }
                    }
                    dismiss()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        val title = if (message.equals(
                                UIConstants.noInternetMessage,
                                true
                            )
                        ) getString(R.string.error) else getString(R.string.patient_transfer)
                        (activity as BaseActivity).showErrorDialogue(
                            title,
                            getString(R.string.no_internet_error),
                            false
                        ) {}
                    }
                }
            }
        }
    }


    private fun loadSearchDropDown(data: ArrayList<RegionSiteResponse>) {
        if(data.isEmpty())
            binding.etFacility.dismissDropDown()
        myAdapter.setData(data)
        binding.etFacility.setAdapter(myAdapter)
        if (data.size > 0 && canSearch) {
            binding.etFacility.showDropDown()
        }
        if(binding.etFacility.text.isNullOrEmpty()){
            binding.etFacility.dismissDropDown()
        }
    }

    private fun loadUserSearchDropDown(data: ArrayList<SiteRoleResponse>) {
        userAdapter.setData(data)
        binding.etPhysician.setAdapter(userAdapter)
        if (data.size > 0 && userSearch) {
            binding.etPhysician.showDropDown()
        }
        if(binding.etPhysician.text.isNullOrEmpty()){
            binding.etPhysician.dismissDropDown()
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            binding.labelHeader.ivClose.id, R.id.btnCancel -> {
                medicalReviewViewModel.searchSiteResponse.setError()
                medicalReviewViewModel.searchRoleUserResponse.setError()
                medicalReviewViewModel.isTransferDialogVisible = false
                dismiss()
            }
            R.id.btnTransfer -> {
                binding.btnTransfer.context.hideKeyboard(binding.btnTransfer)
                if (validateInputs()) {
                    submitArchive()
                }
            }
        }
    }

    private fun submitArchive() {
        SecuredPreference.getSelectedSiteEntity()?.let { site ->
            val transferRequest = TransferCreateRequest(
                patientTrackId = medicalReviewViewModel.patientTrackId,
                tenantId = site.tenantId,
                transferTo = selectedUser?.id,
                transferSite = selectedSite?.id,
                oldSite = site.id,
                transferReason = transferReason
            )
            medicalReviewViewModel.createPatientTransfer(requireContext(),transferRequest)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        var errorView: TextView? = null

        if (selectedSite == null) {
            isValid = false
            binding.tvFacilityErrorMessage.visibility = View.VISIBLE
        } else {
            binding.tvFacilityErrorMessage.visibility = View.GONE
        }

        if (selectedUser == null) {
            isValid = false
            binding.tvPhysicianErrorMessage.visibility = View.VISIBLE
        } else {
            binding.tvPhysicianErrorMessage.visibility = View.GONE
        }

        if (transferReason == null) {
            isValid = false
            binding.tvReasonErrorMessage.requestFocus()
            binding.tvReasonErrorMessage.visibility = View.VISIBLE
        } else {
            binding.tvReasonErrorMessage.visibility = View.GONE
        }

        return isValid
    }

    private fun showErrorMessage(message: String, view: TextView) {
        view.visibility = View.VISIBLE
        view.text = message
    }

    private val textWatcher = object : TextWatcher {
        private var lastLength = 0
        override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            s?.let { lastLength = it.length }
        }

        override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (canSearch) {
                text?.toString()?.let {
                    if (it.isNotBlank() && it.length > 1) {
                        medicalReviewViewModel.searchSite(requireContext(),it)
                    }
                }
            } else
                canSearch = true
            clearSelectedSite()
        }

        override fun afterTextChanged(s: Editable?) {
            s?.let {
                if (lastLength > it.length) {
                    binding.etFacility.text = null
                    selectedSite = null
                    binding.etPhysician.text = null
                    binding.etPhysician.isEnabled = false
                    selectedUser = null
                }
            }
        }
    }

    private fun clearSelectedSite() {
        if (selectedSite != null)
            selectedSite = null
    }

    private val roleTextWatcher = object : TextWatcher {
        private var lastLength = 0
        override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            s?.let { lastLength = it.length }
        }

        override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (userSearch) {
                text?.toString()?.let {
                    if (it.isNotBlank() && it.length > 1) {
                        selectedSite?.tenantId?.let { site ->
                            medicalReviewViewModel.searchRoleUser(requireContext(),site, it)
                        }
                    }
                }
            } else
                userSearch = true
            clearSelectedProvider()
        }

        override fun afterTextChanged(s: Editable?) {
            s?.let {
                if (lastLength > it.length) {
                    binding.etPhysician.text = null
                    selectedUser = null
                }
            }
        }
    }

    private fun clearSelectedProvider() {
        if(selectedUser!= null)
            selectedUser = null
    }
}