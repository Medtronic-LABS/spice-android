package com.medtroniclabs.opensource.ui.medicalreview

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.appextensions.safePopupMenuClickListener
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.custom.DeleteConfirmationDialog
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.FillPrescriptionRequest
import com.medtroniclabs.opensource.data.model.PatientRemoveRequest
import com.medtroniclabs.opensource.databinding.ActivityMedicalReviewBaseBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.RoleConstant
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import com.medtroniclabs.opensource.ui.medicalreview.dialog.GeneralSuccessDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.TransferArchiveDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.TreatmentPlanDialog
import com.medtroniclabs.opensource.ui.medicalreview.fragment.FirstContinuousMedicalReviewFragment
import com.medtroniclabs.opensource.ui.medicalreview.fragment.InitialEncounterFragment
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.MedicalReviewBaseViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import com.medtroniclabs.opensource.ui.patientedit.PatientEditActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MedicalReviewBaseActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding: ActivityMedicalReviewBaseBinding

    private val patientViewModel: PatientDetailViewModel by viewModels()

    private val viewModel: MedicalReviewBaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityMedicalReviewBaseBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.initial_counter),
            homeAndBackVisibility = Pair(true, null),
            callback = {
                backNavigation(true)
            },
            callbackHome = {
                backNavigation(false)
            })
        initView()
        setListeners()
        attachObserver()
    }

    private fun backNavigation(isBack: Boolean) {
        showOnBackPressedAlert(isBack)
    }

    private fun attachObserver() {
        viewModel.badgeCountResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                        val prescriptionCount: Int = it.prescriptionDaysCompletedCount ?: 0
                        binding.ivPBatchCount.text = prescriptionCount.toString()
                        binding.ivPBatchCount.visibility =
                            if (prescriptionCount > 0) View.VISIBLE else View.INVISIBLE

                        val investigationCount: Int = it.nonReviewedTestCount ?: 0
                        binding.ivIBatchCount.text = investigationCount.toString()
                        binding.ivIBatchCount.visibility =
                            if (investigationCount > 0) View.VISIBLE else View.INVISIBLE

                        val nutritionCount: Int = it.nutritionLifestyleReviewedCount ?: 0
                        binding.ivLSBadgeCount.text = nutritionCount.toString()
                        binding.ivLSBadgeCount.visibility =
                            if (nutritionCount > 0) View.VISIBLE else View.INVISIBLE
                    }
                }
            }
        }

        patientViewModel.patientRemoveResponse.observe(this) { resourceState ->
            when(resourceState.state){
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { map ->
                        if (map.containsKey(DefinedParams.message)) {
                            val message = map[DefinedParams.message]
                            if (message is String) {
                                GeneralSuccessDialog.newInstance(
                                    this,
                                    getString(R.string.delete),
                                    message,
                                    needHomeNav = false,
                                    callback = {
                                        if(it)
                                            this.finish()
                                    }
                                )
                                    .show(supportFragmentManager, GeneralSuccessDialog.TAG)
                            }
                        }
                    }
                }
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        showErrorDialogue(getString(R.string.error), it, false) {}
                    }
                }
            }
        }

        viewModel.validateTransferResponse.observe(this) { resourceState ->
            when(resourceState.state)
            {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    viewModel.isTransferDialogVisible = false
                    resourceState?.message?.let { message ->
                        val title = if (message.equals(
                                getString(R.string.no_internet_error),
                                true
                            )
                        ) getString(R.string.error) else getString(R.string.patient_transfer)
                        showErrorDialogue(title,message, isNegativeButtonNeed = false){}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    if(!viewModel.isTransferDialogVisible) {
                        viewModel.isTransferDialogVisible = true
                        TransferArchiveDialog.newInstance().show(
                            supportFragmentManager,
                            TransferArchiveDialog.TAG
                        )
                    }
                }
            }
        }
    }

    private fun initView() {
        viewModel.ShowContinuousMedicalReview =
            intent.getBooleanExtra(IntentConstants.ShowContinuousMedicalReview, false)
        viewModel.origin = intent.getStringExtra(DefinedParams.Origin)
        val patientId = intent.getLongExtra(IntentConstants.IntentPatientID, -1L)
        patientViewModel.patientId = patientId
        val visitId = intent.getLongExtra(IntentConstants.IntentVisitId, -1L)
        patientViewModel.patientVisitId = visitId
        replaceFragmentInId<FirstContinuousMedicalReviewFragment>(id = binding.firstContinuousContainer.id)
        viewModel.patientTrackId = patientId
        val bundle = Bundle().apply {
            putLong(PatientInfoFragment.PATIENT_ID, patientId)
            if (viewModel.ShowContinuousMedicalReview)
                putBoolean(
                    PatientInfoFragment.SHOW_MENTAL_HEALTH_CARD,
                    SecuredPreference.isPHQ4Enabled()
                )

        }
        replaceFragmentInId<PatientInfoFragment>(
            binding.patientDetailFragment.id,
            bundle = bundle,
            tag = PatientInfoFragment.TAG,
            isAdd = true
        )
        if (viewModel.ShowContinuousMedicalReview) {
            binding.patientMedicalHistoryFragment.visibility = View.VISIBLE
            replaceFragmentInId<MedicalReviewHistoryFragment>(
                binding.patientMedicalHistoryFragment.id,
                bundle = Bundle().apply {
                    putBoolean(IntentConstants.isFromCMR, true)
                    putString(DefinedParams.Origin, viewModel.origin)
                }
            )
            showHideContainer(false)
        } else {
            binding.patientMedicalHistoryFragment.visibility = View.GONE
            replaceFragmentInId<InitialEncounterFragment>(
                binding.fragmentContainer.id,
                isAdd = true
            )
        }

        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh(false)
        }
    }

    fun swipeRefresh(onlyPatientDetails: Boolean) {
        supportFragmentManager.findFragmentById(R.id.firstContinuousContainer).let { currentFragment ->
            if (currentFragment is MedicalSummaryFragment)
                currentFragment.apiCalls()
            else {
                patientViewModel.getPatientDetails(this,false)
                if (!onlyPatientDetails)
                    fetchBadgeCount()
            }
        }
        binding.refreshLayout.isRefreshing = false
    }

    fun isInitialFragmentVisible() : Boolean {
        return binding.fragmentContainer.visibility == View.VISIBLE
    }

    private fun fetchBadgeCount() {
        viewModel.getBadgeCount()
    }

    /**
     * attach listener to view
     */
    private fun setListeners() {
        binding.ivLifestyle.safeClickListener(this)
        binding.ivPrescription.safeClickListener(this)
        binding.ivInvestigation.safeClickListener(this)
        binding.ivTreatmentPlan.safeClickListener(this)
        binding.tvInvestigation.safeClickListener(this)
        binding.btnNext.safeClickListener(this)
    }

    /**
     * listener for view on click events
     * @param view respected view clicked
     */
    override fun onClick(view: View) {
        when (view.id) {
            binding.ivPrescription.id -> {
                val intent = Intent(this, PrescriptionCopyActivity::class.java)
                intent.putExtra(IntentConstants.IntentPatientID, patientViewModel.patientId)
                intent.putExtra(IntentConstants.IntentVisitId, patientViewModel.patientVisitId)
                startActivity(intent)
            }
            binding.ivInvestigation.id, binding.tvInvestigation.id -> {
                startActivity(
                    Intent(
                        this,
                        LabTestCreateActivity::class.java
                    ).apply {
                        putExtra(IntentConstants.IntentPatientID, viewModel.patientTrackId)
                        putExtra(IntentConstants.IntentVisitId, patientViewModel.patientVisitId)
                    }
                )
            }
            R.id.ivTreatmentPlan -> {
                if (connectivityManager.isNetworkAvailable())
                    TreatmentPlanDialog().show(supportFragmentManager, TreatmentPlanDialog.TAG)
                else
                    showErrorDialogue(
                        getString(R.string.error),
                        getString(R.string.no_internet_error),
                        false){}
            }
            binding.ivLifestyle.id -> {
                startActivity(
                    Intent(
                        this,
                        LifestyleBaseActivity::class.java
                    ).apply {
                        putExtra(IntentConstants.IntentPatientID, viewModel.patientTrackId)
                        putExtra(IntentConstants.IntentVisitId, patientViewModel.patientVisitId)
                    }
                )
            }

            R.id.btnNext -> {
                val fragment =
                    supportFragmentManager.findFragmentById(R.id.firstContinuousContainer)
                if (binding.fragmentContainer.visibility == View.VISIBLE) {
                    //Initial encounter is visible
                    supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                        ?.let { initialFragment ->
                            if (initialFragment is InitialEncounterFragment) {
                                initialFragment.validateInputs()
                            }
                        }
                } else {
                    when (fragment) {
                        is FirstContinuousMedicalReviewFragment -> {
                            fragment.validateValues()
                        }
                        is MedicalSummaryFragment -> {
                            fragment.validateValues()
                        }
                    }
                }
            }
        }
    }

    fun showHideContainer(showSummaryFragment: Boolean) {
        showVerticalMoreIcon(false)
        if (showSummaryFragment) {
            replaceFragmentInId<MedicalSummaryFragment>(id = binding.firstContinuousContainer.id)
            if (viewModel.ShowContinuousMedicalReview) {
                binding.patientMedicalHistoryFragment.visibility = View.GONE
            }
            supportFragmentManager.findFragmentByTag(PatientInfoFragment.TAG)?.let {
                (it as PatientInfoFragment).hideAssessmentCard()
            }
        } else {
            binding.fragmentContainer.visibility = View.GONE
            binding.firstContinuousContainer.visibility = View.VISIBLE
        }
    }

    inline fun <reified fragment : Fragment> replaceFragmentInId(
        id: Int? = null,
        bundle: Bundle? = null,
        tag: String? = null,
        backStack: String? = null,
        isAdd: Boolean? = false
    ) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            if (isAdd == true)
                add<fragment>(id ?: binding.patientDetailFragment.id, args = bundle, tag = tag)
            else {
                if (backStack == null)
                    replace<fragment>(
                        id ?: binding.patientDetailFragment.id,
                        args = bundle,
                        tag = tag
                    )
                else
                    replace<fragment>(
                        id ?: binding.patientDetailFragment.id,
                        args = bundle,
                        tag = tag
                    ).addToBackStack(backStack)
            }
        }
        binding.nestedScrollViewID.fullScroll(ScrollView.FOCUS_UP)
    }

    fun changeButtonTextToFinishMedicalReview(buttonText: String, tag: String) {
        binding.btnNext.text = buttonText
        binding.btnNext.tag = tag
    }

    override fun onResume() {
        super.onResume()
        swipeRefresh(false)
    }

    private fun showOnBackPressedAlert(isBack: Boolean) {
        val firstReviewFragment =
            supportFragmentManager.findFragmentByTag(FirstContinuousMedicalReviewFragment.TAG)
        val fragment = supportFragmentManager.findFragmentById(R.id.firstContinuousContainer)

        if (!viewModel.ShowContinuousMedicalReview && (firstReviewFragment != null)) {
            showErrorDialogue(
                getString(R.string.alert),
                getString(R.string.medical_review_exit_message),
                isNegativeButtonNeed = false
            ) {}
        } else if (viewModel.ShowContinuousMedicalReview && fragment is FirstContinuousMedicalReviewFragment) {
            showAlertOnBackPress(isBack)
        } else {
            if (isBack)
                handleBackNav()
            else
                navigateToHome()
        }
    }

    private fun handleBackNav() {
        val patientInfoFragment = supportFragmentManager.findFragmentByTag(PatientInfoFragment.TAG)
        val fragment = supportFragmentManager.findFragmentById(R.id.firstContinuousContainer)
        when {
            fragment is MedicalSummaryFragment -> {
                patientInfoFragment?.let {
                    (it as PatientInfoFragment).showAssessmentCard()
                }
                if (viewModel.ShowContinuousMedicalReview) {
                    binding.patientMedicalHistoryFragment.visibility = View.VISIBLE
                }
                hideLoading()
                replaceFragmentInId<FirstContinuousMedicalReviewFragment>(id = R.id.firstContinuousContainer)
            }
            checkIfFirstContinuous(fragment) -> {
                showVerticalMoreIcon(
                    (patientViewModel.patientDetailsResponse.value?.data?.patientId ?: 0) > 0
                )
                binding.firstContinuousContainer.visibility = View.GONE
                binding.fragmentContainer.visibility = View.VISIBLE
                hideLoading()
            }
            else -> showAlertOnBackPress(true)

        }
    }

    private fun checkIfFirstContinuous(fragment: Fragment?): Boolean {
        return fragment is FirstContinuousMedicalReviewFragment && binding.firstContinuousContainer.visibility == View.VISIBLE
    }

    private fun showAlertOnBackPress(isBack: Boolean) {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPossitive ->
            if (isPossitive) {
                onBackPressPopStack(isBack)
            }
        }
    }

    private fun onBackPressPopStack(isBack: Boolean) {
        if (isBack) {
                this@MedicalReviewBaseActivity.finish()
        }else {
            startAsNewActivity(Intent(this, LandingActivity::class.java))
        }
    }

    private fun navigateToHome() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPossitive ->
            if (isPossitive) {
                startAsNewActivity(Intent(this, LandingActivity::class.java))
            }
        }
    }


    fun getScrollview(): NestedScrollView {
        return binding.nestedScrollViewID
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showOnBackPressedAlert(true)
        }
    }

    fun onMoreIconClicked(view: View) {
        val popupMenu = PopupMenu(this@MedicalReviewBaseActivity, view)
        popupMenu.menuInflater.inflate(R.menu.menu_patient_edit, popupMenu.menu)
        val role = SecuredPreference.getSelectedSiteEntity()?.role
        popupMenu.menu.getItem(1).isVisible = (role == RoleConstant.PROVIDER || role == RoleConstant.PHYSICIAN_PRESCRIBER)
        popupMenu.menu.getItem(2).isVisible = (role == RoleConstant.HRIO || role == RoleConstant.NURSE || role == RoleConstant.PROVIDER || role == RoleConstant.PHYSICIAN_PRESCRIBER)
        popupMenu.safePopupMenuClickListener(object :android.widget.PopupMenu.OnMenuItemClickListener,
            PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(menuItem: MenuItem): Boolean {
                onPatientEditMenuItemClick(menuItem.itemId)
                return true
            }
        })
        popupMenu.setForceShowIcon(true)
        popupMenu.show()
    }

    private fun onPatientEditMenuItemClick(itemId: Int) {
        when (itemId) {
            R.id.patient_edit -> {
                patientViewModel.patientDetailsResponse.value?.data?.let { model ->
                    val intent =
                        Intent(this@MedicalReviewBaseActivity, PatientEditActivity::class.java)
                    intent.putExtra(DefinedParams.Patient_Id, model.patientId)
                    intent.putExtra(DefinedParams.PatientTrackId, model._id)
                    intent.putExtra(DefinedParams.fromMedicalReview, true)
                    startActivity(intent)
                }
            }

            R.id.transfer_patient -> {
                if (viewModel.validateTransferResponse.value?.state != ResourceState.LOADING
                    && !viewModel.isTransferDialogVisible) {
                    SecuredPreference.getSelectedSiteEntity()?.tenantId?.let {
                        val request = FillPrescriptionRequest(
                            patientTrackId = viewModel.patientTrackId,
                            tenantId = it
                        )
                        viewModel.validatePatientTransfer(this,request)
                    }
                }
            }

            R.id.patient_delete -> {
                patientViewModel.patientDetailsResponse.value?.data?.let { model ->
                    val deleteConfirmationDialog = DeleteConfirmationDialog.newInstance(
                        getString(R.string.alert),
                        getString(
                            R.string.patient_delete_confirmation,
                            model.firstName,
                            model.lastName
                        ),
                        { status, reason, otherReason ->
                            if (status) {
                                SecuredPreference.getSelectedSiteEntity()?.apply {
                                    patientViewModel.patientRemove(
                                        this@MedicalReviewBaseActivity,
                                        PatientRemoveRequest(
                                            model._id,
                                            tenantId,
                                            model.patientId,
                                            reason,
                                            otherReason
                                        )
                                    )
                                }
                            }
                        },
                        this,
                        true,
                        okayButton = getString(R.string.yes),
                        cancelButton = getString(R.string.no)
                    )
                    deleteConfirmationDialog.show(
                        supportFragmentManager,
                        DeleteConfirmationDialog.TAG
                    )
                }
            }
        }
    }
}