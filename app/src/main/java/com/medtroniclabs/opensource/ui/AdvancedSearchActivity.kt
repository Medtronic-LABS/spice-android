package com.medtroniclabs.opensource.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.hideKeyboard
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.MedicalReviewBaseRequest
import com.medtroniclabs.opensource.data.model.PatientDetails
import com.medtroniclabs.opensource.data.model.PatientListRespModel
import com.medtroniclabs.opensource.databinding.ActivityAdvancedSearchBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.assessment.AssessmentHolderActivity
import com.medtroniclabs.opensource.ui.enrollment.viewmodel.PatientListViewModel
import com.medtroniclabs.opensource.ui.medicalreview.ContinuousMedicalReviewBaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.MedicalReviewBaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.PatientSelectionListener
import com.medtroniclabs.opensource.ui.medicalreview.adapter.PatientsListAdapter
import com.medtroniclabs.opensource.ui.medicalreview.dialog.FilterDialogFragment
import com.medtroniclabs.opensource.ui.medicalreview.labTechnician.LabTestListActivity
import com.medtroniclabs.opensource.ui.medicalreview.nutritionist.NutritionistActivity
import com.medtroniclabs.opensource.ui.medicalreview.pharamcist.PrescriptionRefillActivity
import com.medtroniclabs.opensource.ui.patientedit.PatientEditActivity
import com.medtroniclabs.opensource.ui.screening.GeneralDetailsActivity
import com.medtroniclabs.opensource.ui.screening.TermsAndConditionActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

open class AdvancedSearchActivity : BaseActivity(), View.OnClickListener, PatientSelectionListener {

    private lateinit var binding: ActivityAdvancedSearchBinding
    val patientListViewModel: PatientListViewModel by viewModels()
    private lateinit var patientsListAdapter: PatientsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvancedSearchBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            title = getString(R.string.search_patient),
            isToolbarVisible = true,
            homeAndBackVisibility = Pair(true, true)
        )
        getIntentValues()
        initView()
        setListeners()
        setPatientListAdapter()
        setObserver()
    }

    private fun getIntentValues() {
        patientListViewModel.origin = intent.getStringExtra(DefinedParams.Origin) ?: ""
    }

    private fun setObserver() {
        patientListViewModel.applySortFilter.observe(this) {
            if (it)
                getPatients()
        }
        patientListViewModel.patientVisitResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        showErrorDialogue(
                            getString(R.string.error),
                            message,
                            isNegativeButtonNeed = false
                        ) {}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { patientDetails ->
                        startNewReviewActivity(patientDetails)
                    }
                }
            }
        }
        patientListViewModel.totalPatientCount.observe(this) {
            updatePatientsCount(it)
        }
    }

    private fun updatePatientsCount(count: String) {
        binding.tvPatientCount.apply {
            val totPatients = count.toInt()
            if (totPatients == 0) {
                visibility = View.GONE
                checkListSize(false)
            } else {
                visibility = View.VISIBLE
                text =
                    if (totPatients > 1) "$totPatients ${getString(R.string.patients_found)}" else getString(R.string.one_patient_found)
                checkListSize(true)
            }
        }

        //Sort Count
        if (patientListViewModel.sortCount() > 0)
            binding.llSortFilter.btnSort.text =
                getString(R.string.sort, " (1)")
        else
            binding.llSortFilter.btnSort.text = getString(R.string.sort, "")

        //Filter Count
        val count = patientListViewModel.filterCount()
        if (count > 0)
            binding.llSortFilter.btnFilter.text =
                getString(R.string.filter, " (${count})")
        else
            binding.llSortFilter.btnFilter.text = getString(R.string.filter, "")
    }

    override fun onSelectedPatient(item: PatientListRespModel) {
        when (patientListViewModel.origin) {
            UIConstants.enrollmentUniqueID -> {
                if (item.patientStatus.equals(DefinedParams.ENROLLED, true)) {
                    val programId = if (item.programId == null) "-" else item.programId.toString()
                    val errorMsg: String = if (programId.isNotEmpty())
                        getString(R.string.patient_already_enrolled_with_program_id) + item.programId
                    else
                        getString(R.string.patient_already_enrolled)
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(null)
                    builder.setMessage(errorMsg)
                    builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                } else {
                    val intent = Intent(this, TermsAndConditionActivity::class.java)
                    intent.putExtra(IntentConstants.IntentEnrollment, true)
                    intent.putExtra(DefinedParams.Patient_Id, item.id ?: -1L)
                    intent.putExtra(DefinedParams.Screening_Id, item.screeningLogId ?: -1L)
                    startActivity(intent)
                }
            }
            UIConstants.AssessmentUniqueID -> {
                val intent = Intent(this, AssessmentHolderActivity::class.java)
                intent.putExtra(DefinedParams.Patient_Id, item.id)
                startActivity(intent)
            }
            else -> {
                if (SecuredPreference.getSelectedSiteEntity()?.role == RoleConstant.HRIO && patientListViewModel.origin == UIConstants.myPatientsUniqueID) {
                    val intent = Intent(this, PatientEditActivity::class.java)
                    intent.putExtra(DefinedParams.Patient_Id, item.patientId)
                    intent.putExtra(DefinedParams.PatientTrackId, item.id)
                    startActivity(intent)
                } else
                    createPatientVisit(item.initialReview, item.id)
            }
        }
    }

    private fun createPatientVisit(initialReview: Boolean, id: Long?) {
        id?.let { patientTrackId ->
            SecuredPreference.getSelectedSiteEntity()?.let { siteEntity ->
                patientListViewModel.createPatientVisit(
                    this,
                    MedicalReviewBaseRequest(patientTrackId, siteEntity.tenantId),
                    initialReview,
                    patientTrackId
                )
            }
        }
    }

    open fun startNewReviewActivity(details: PatientDetails) {
        when (patientListViewModel.origin) {
            UIConstants.PrescriptionUniqueID -> {
                val intent = Intent(this, PrescriptionRefillActivity::class.java)
                intent.putExtra(IntentConstants.IntentPatientID, details.patientID)
                intent.putExtra(IntentConstants.IntentVisitId, details.visitID)
                startActivity(intent)
            }
            UIConstants.investigation -> {
                patientListViewModel.spanCount = DefinedParams.span_count_2
                val intent = Intent(this, LabTestListActivity::class.java)
                intent.putExtra(IntentConstants.IntentPatientID, details.patientID)
                intent.putExtra(IntentConstants.IntentVisitId, details.visitID)
                startActivity(intent)
            }
            UIConstants.lifestyle -> {
                val intent = Intent(this, NutritionistActivity::class.java)
                intent.putExtra(IntentConstants.IntentPatientID, details.patientID)
                intent.putExtra(IntentConstants.IntentVisitId, details.visitID)
                startActivity(intent)
            }
            UIConstants.myPatientsUniqueID -> {
                when {
                    CommonUtils.isNurse() || details.initialReview -> {
                        val intent = Intent(this, ContinuousMedicalReviewBaseActivity::class.java)
                        intent.putExtra(IntentConstants.IntentPatientID, details.patientID)
                        intent.putExtra(IntentConstants.IntentVisitId, details.visitID)
                        intent.putExtra(DefinedParams.Origin, patientListViewModel.origin)
                        startActivity(intent)
                    }
                    else -> {
                        val intent = Intent(this, MedicalReviewBaseActivity::class.java)
                        intent.putExtra(IntentConstants.IntentPatientID, details.patientID)
                        intent.putExtra(IntentConstants.IntentVisitId, details.visitID)
                        intent.putExtra(IntentConstants.ShowContinuousMedicalReview, false)
                        intent.putExtra(DefinedParams.Origin, patientListViewModel.origin)
                        startActivity(intent)
                    }
                }
            }
            UIConstants.MedicalReviewUniqueID -> {
                val intent = Intent(this, MedicalReviewBaseActivity::class.java)
                intent.putExtra(IntentConstants.IntentPatientID, details.patientID)
                intent.putExtra(IntentConstants.IntentVisitId, details.visitID)
                intent.putExtra(DefinedParams.Origin, patientListViewModel.origin)
                intent.putExtra(IntentConstants.ShowContinuousMedicalReview, details.initialReview)
                startActivity(intent)
            }
        }
    }

    private fun getPatients() {
        patientsListAdapter.submitData(lifecycle, PagingData.empty())
        lifecycleScope.launch {
            patientListViewModel.patientsDataSource.collectLatest { pagedData ->
                patientsListAdapter.submitData(pagedData)
            }
        }
    }

    open fun initView() {
        val tabletSize =
            resources.getBoolean(R.bool.isLargeTablet) || resources.getBoolean(R.bool.isTablet)
        if (tabletSize) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            patientListViewModel.spanCount = DefinedParams.span_count_3
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            patientListViewModel.spanCount = DefinedParams.span_count_1
        }
    }

    private fun setListeners() {
        binding.llAdvancedSearch.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnSearch.safeClickListener(this)
        binding.btnAdvancedSearch.safeClickListener(this)
        binding.llSortFilter.btnFilter.safeClickListener(this)
        binding.llSortFilter.btnSort.safeClickListener(this)

        binding.etPatientSearch.addTextChangedListener(searchListener)
        binding.etFirstName.addTextChangedListener(advancedSearchListener)
        binding.etLastName.addTextChangedListener(advancedSearchListener)
        binding.etMobileNumber.addTextChangedListener(advancedSearchListener)
    }

    private fun setPatientListAdapter() {
        patientsListAdapter = PatientsListAdapter(this)
        binding.rvPatientsList.apply {
            layoutManager =
                GridLayoutManager(this@AdvancedSearchActivity, patientListViewModel.spanCount)
            adapter = patientsListAdapter
        }
        patientsListAdapter.addLoadStateListener {
            val isLoading = it.refresh is LoadState.Loading
            if (isLoading)
                showLoading()
            else
                hideLoading()
            binding.pbLoadMore.visibility =
                if (it.append is LoadState.Loading) View.VISIBLE else View.GONE
        }
    }

    private fun checkListSize(hasPatients: Boolean) {
        binding.btnEnrol.visibility = View.GONE
        binding.btnScreening.visibility = View.GONE

        binding.tvNoPatientsFound.visibility =
            if (hasPatients) View.GONE else View.VISIBLE
        binding.rvPatientsList.visibility =
            if (hasPatients) View.VISIBLE else View.GONE
        if (showSortFilter(hasPatients)) {
            binding.llSortFilter.root.visibility = View.VISIBLE
            binding.llSortFilter.btnSort.visibility = View.VISIBLE
            binding.llSortFilter.btnFilter.visibility = View.VISIBLE
        } else if (showSortPharmacistLabTestFilter(hasPatients)) {
            binding.llSortFilter.root.visibility = View.VISIBLE
            binding.llSortFilter.btnSort.visibility = View.GONE
            binding.llSortFilter.btnFilter.visibility = View.VISIBLE
        } else {
            binding.llSortFilter.root.visibility = View.GONE
        }

        if (hasPatients) {
            binding.clAdvancedSearch.visibility = View.GONE
            rotateArrow0f()
        } else {
            if (patientListViewModel.origin == UIConstants.enrollmentUniqueID) {
                binding.tvNoPatientsFound.text = getString(R.string.no_patient_found)
                binding.btnEnrol.visibility = View.VISIBLE
                binding.btnEnrol.safeClickListener(this)
                binding.btnScreening.visibility = View.GONE
            } else {
                binding.tvNoPatientsFound.text = getString(R.string.screening_after_search)
                if (patientListViewModel.origin == UIConstants.AssessmentUniqueID) {
                    binding.btnScreening.visibility = View.VISIBLE
                    binding.btnScreening.safeClickListener(this)
                    binding.btnEnrol.visibility = View.GONE
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.llAdvancedSearch -> {
                if (binding.clAdvancedSearch.visibility == View.GONE) {
                    binding.clAdvancedSearch.visibility = View.VISIBLE
                    rotateArrow180f()
                    clearIdSearch()
                } else {
                    binding.clAdvancedSearch.visibility = View.GONE
                    rotateArrow0f()
                }
            }
            binding.btnCancel -> {
                hideKeyboard(v)
                binding.llAdvancedSearch.visibility = View.VISIBLE
                binding.clAdvancedSearch.visibility = View.GONE
                rotateArrow0f()
                clearAdvancedSearch()
            }
            binding.btnSearch -> {
                hideKeyboard(v)
                if (connectivityManager.isNetworkAvailable()) {
                    if (binding.clAdvancedSearch.visibility == View.VISIBLE)
                        binding.btnCancel.performClick()
                    patientListViewModel.searchPatientId =
                        binding.etPatientSearch.text?.trim().toString()
                    clearAdvancedSearch()
                    getPatients()
                } else {
                    showErrorDialogue(
                        getString(R.string.error),
                        getString(R.string.no_internet_error),
                        isNegativeButtonNeed = false,
                    ) {}
                }
            }
            binding.btnAdvancedSearch -> {
                hideKeyboard(v)
                if (connectivityManager.isNetworkAvailable()) {
                    saveInputs()
                    clearIdSearch()
                    getPatients()
                } else {
                    showErrorDialogue(
                        getString(R.string.error),
                        getString(R.string.no_internet_error),
                        isNegativeButtonNeed = false,
                    ) {}
                }
            }
            binding.btnEnrol -> {
                val intent = Intent(this, TermsAndConditionActivity::class.java)
                intent.putExtra(IntentConstants.IntentEnrollment, true)
                intent.putExtra(IntentConstants.isFromDirectEnrollment, true)
                startActivity(intent)
            }
            binding.btnScreening -> {
                val intent = Intent(this, GeneralDetailsActivity::class.java)
                intent.putExtra(IntentConstants.IntentScreening, true)
                startActivity(intent)
            }
            binding.llSortFilter.btnFilter -> {
                FilterDialogFragment.newInstance(patientListViewModel.origin)
                    .show(supportFragmentManager, FilterDialogFragment.TAG)
            }
            binding.llSortFilter.btnSort -> {
                SortingDialogFragment.newInstance()
                    .show(supportFragmentManager, SortingDialogFragment.TAG)
            }
        }
    }
    private fun clearAdvancedSearch() {
        binding.etFirstName.text?.clear()
        binding.etLastName.text?.clear()
        binding.etMobileNumber.text?.clear()
        binding.etPatientSearch.isEnabled = true
        updateFlag()
    }

    private fun clearIdSearch() {
        binding.etPatientSearch.text?.clear()
        binding.btnSearch.isEnabled = false
        updateFlag()
    }

    private fun saveInputs() {
        binding.let {
            val fN: String = it.etFirstName.text?.trim().toString()
            val lN: String = it.etLastName.text?.trim().toString()
            val mN: String = it.etMobileNumber.text?.trim().toString()
            patientListViewModel.apply {
                searchPatientId = ""
                firstName = fN.ifEmpty { null }
                lastName = lN.ifEmpty { null }
                mobileNumber = mN.ifEmpty { null }
            }
        }
    }

    private fun rotateArrow180f() {
        val ivArrow = ObjectAnimator.ofFloat(binding.ivArrow, "rotation", 0f, 180f)
        ivArrow.start()
    }

    private fun rotateArrow0f() {
        val ivArrow = ObjectAnimator.ofFloat(binding.ivArrow, "rotation", 180f, 0f)
        ivArrow.start()
    }

    private val searchListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            /**
             * this method is not used
             */
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            /**
             * this method is not used
             */
        }

        override fun afterTextChanged(s: Editable?) {
            binding.btnSearch.isEnabled = (s?.count() ?: 0) > 0
            updateFlag()
        }
    }

    private fun updateFlag() {
        patientListViewModel.isPatientSearch =
            binding.btnSearch.isEnabled || binding.btnAdvancedSearch.isEnabled
    }

    private val advancedSearchListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            /**
             * this method is not used
             */
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            /**
             * this method is not used
             */
        }

        override fun afterTextChanged(s: Editable?) {
            enableAdvanceSearch()
        }
    }

    private fun enableAdvanceSearch() {
        val isValidInput: Boolean =
            binding.etFirstName.text?.isNotEmpty() == true || binding.etLastName.text?.isNotEmpty() == true || binding.etMobileNumber.text?.isNotEmpty() == true
        binding.btnAdvancedSearch.isEnabled = isValidInput
        updateFlag()
    }

    override fun onResume() {
        super.onResume()
        clearIdSearch()
        clearAdvancedSearch()
        binding.tvPatientCount.visibility = View.GONE
        patientsListAdapter.submitData(lifecycle, PagingData.empty())
        if (patientListViewModel.isPatientListRequired()) {
            getPatients()
        }
    }

    private fun showSortFilter(hasPatients: Boolean): Boolean {
        val role = SecuredPreference.getSelectedSiteEntity()?.role
        val havePermission = patientListViewModel.origin == UIConstants.myPatientsUniqueID &&
                (role == RoleConstant.PROVIDER || role == RoleConstant.PHYSICIAN_PRESCRIBER)
        return if (havePermission) {
            val hasSortFilter =
                patientListViewModel.sortCount() > 0 || patientListViewModel.filterCount() > 0
            val isSearch = binding.btnSearch.isEnabled || binding.btnAdvancedSearch.isEnabled
            val hideSortFilterLayout = isSearch && !hasSortFilter && !hasPatients
            !hideSortFilterLayout
        } else
            false
    }

    private fun showSortPharmacistLabTestFilter(hasPatients: Boolean): Boolean {
        val role = SecuredPreference.getSelectedSiteEntity()?.role
        val havePermission =
            (patientListViewModel.origin == UIConstants.PrescriptionUniqueID || patientListViewModel.origin == UIConstants.investigation) && (role == RoleConstant.PHARMACIST || role == RoleConstant.LAB_TECHNICIAN)
        return if (havePermission) {
            val hasSortFilter =
                patientListViewModel.sortCount() > 0 || patientListViewModel.filterCount() > 0
            val isSearch = binding.btnSearch.isEnabled || binding.btnAdvancedSearch.isEnabled
            val hideSortFilterLayout = isSearch && !hasSortFilter && !hasPatients
            !hideSortFilterLayout
        } else
            false
    }
}