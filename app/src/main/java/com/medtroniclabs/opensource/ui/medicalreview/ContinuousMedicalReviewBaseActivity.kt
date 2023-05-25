package com.medtroniclabs.opensource.ui.medicalreview

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.appextensions.safePopupMenuClickListener
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.custom.DeleteConfirmationDialog
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.BadgeResponseModel
import com.medtroniclabs.opensource.data.model.FillPrescriptionRequest
import com.medtroniclabs.opensource.data.model.PatientRemoveRequest
import com.medtroniclabs.opensource.databinding.ActivityContinuousMedicalReviewBaseBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.RoleConstant
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import com.medtroniclabs.opensource.ui.medicalreview.dialog.GeneralSuccessDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.TransferArchiveDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.TreatmentPlanDialog
import com.medtroniclabs.opensource.ui.medicalreview.fragment.PatientMedicalReviewHistory
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.MedicalReviewBaseViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import com.medtroniclabs.opensource.ui.patientedit.PatientEditActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContinuousMedicalReviewBaseActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding: ActivityContinuousMedicalReviewBaseBinding

    private val viewModel: PatientDetailViewModel by viewModels()
    private val medicalReviewBaseViewModel: MedicalReviewBaseViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContinuousMedicalReviewBaseBinding.inflate(layoutInflater)
        setMainContentView(binding.root, true, homeAndBackVisibility = Pair(true, null), callback = {
            this@ContinuousMedicalReviewBaseActivity.finish()
        }, callbackHome = {
            onHomeClicked()
        })
        initializeView()
        attachObserver()
    }


    private fun onMoreIconClicked(view: View) {
        val popupMenu = PopupMenu(this@ContinuousMedicalReviewBaseActivity, view)
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
        when(itemId) {
            R.id.patient_edit -> {
                viewModel.patientDetails?.let { model ->
                    val intent = Intent(this@ContinuousMedicalReviewBaseActivity, PatientEditActivity::class.java)
                    intent.putExtra(DefinedParams.Patient_Id, model.patientId)
                    intent.putExtra(DefinedParams.PatientTrackId, model._id)
                    intent.putExtra(DefinedParams.fromMedicalReview,true)
                    startActivity(intent)
                }
            }

            R.id.transfer_patient -> {
                if (medicalReviewBaseViewModel.validateTransferResponse.value?.state != ResourceState.LOADING
                    && !medicalReviewBaseViewModel.isTransferDialogVisible) {
                    SecuredPreference.getSelectedSiteEntity()?.tenantId?.let {
                        val request = FillPrescriptionRequest(
                            patientTrackId = viewModel.patientTrackId,
                            tenantId = it
                        )
                        medicalReviewBaseViewModel.validatePatientTransfer(this,request)
                    }
                }
            }

            R.id.patient_delete -> {
                viewModel.patientDetails?.let {  model ->
                    val deleteConfirmationDialog = DeleteConfirmationDialog.newInstance(
                        getString(R.string.alert),
                        getString(R.string.patient_delete_confirmation,model.firstName,model.lastName),
                        { status,reason,otherReason ->
                            if (status) {
                                SecuredPreference.getSelectedSiteEntity()?.apply {
                                    viewModel.patientRemove(this@ContinuousMedicalReviewBaseActivity, PatientRemoveRequest(model._id,tenantId,model.patientId,reason,otherReason))
                                }
                            }
                        },
                        this,
                        true,
                        okayButton = getString(R.string.yes),
                        cancelButton = getString(R.string.no)
                    )
                    deleteConfirmationDialog.show(supportFragmentManager, DeleteConfirmationDialog.TAG)
                }
            }
        }
    }


    private fun onHomeClicked() {
        startAsNewActivity(Intent(this, LandingActivity::class.java))
    }

    private fun attachObserver() {
        viewModel.patientDetailsResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> {
                    setRedRiskPatient(resourceState.data?.isRedRiskPatient)
                    viewModel.patientDetails = resourceState.data
                    val isPatientEnrolled = (resourceState.data?.patientId ?: 0) > 0
                    showVerticalMoreIcon(
                        isPatientEnrolled,
                        callback = { view ->
                            onMoreIconClicked(view)
                        })
                }
            }
        }
        medicalReviewBaseViewModel.badgeCountResponse.observe(this) { resourceState ->
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
                        loadBadgeCount(it)
                    }
                }
            }
        }

        viewModel.patientRemoveResponse.observe(this) { resourceState ->
            when(resourceState.state){
                ResourceState.LOADING -> {
                    showLoading()
                }
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
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        showErrorDialogue(getString(R.string.error), it, false) {}
                    }
                }
            }
        }

        medicalReviewBaseViewModel.validateTransferResponse.observe(this) { resourceState ->
            when(resourceState.state)
            {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    medicalReviewBaseViewModel.isTransferDialogVisible = false
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
                    if(!medicalReviewBaseViewModel.isTransferDialogVisible) {
                        medicalReviewBaseViewModel.isTransferDialogVisible = true
                        TransferArchiveDialog.newInstance().show(
                            supportFragmentManager,
                            TransferArchiveDialog.TAG
                        )
                    }
                }
            }
        }
    }

    private fun loadBadgeCount(badgeResponseModel: BadgeResponseModel) {
        badgeResponseModel.let {
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

    private fun initializeView() {
        val patientId = intent.getLongExtra(IntentConstants.IntentPatientID, -1)
        viewModel.origin = intent.getStringExtra(DefinedParams.Origin)
        viewModel.patientVisitId = intent.getLongExtra(IntentConstants.IntentVisitId, -1L)
        viewModel.patientId = patientId
        medicalReviewBaseViewModel.patientTrackId = patientId
        val bundle = Bundle().apply {
            putLong(PatientInfoFragment.PATIENT_ID, patientId)
            putBoolean(PatientInfoFragment.SHOW_TOP_CARD, false)
            putBoolean(PatientInfoFragment.IS_FROM_CMR, true)
        }
        replaceFragmentInId<PatientInfoFragment>(
            binding.patientDetailFragment.id,
            bundle = bundle,
            tag = PatientInfoFragment.TAG
        )
        replaceFragmentInId<PatientMedicalReviewHistory>(binding.patientHistoryFragment.id, bundle = Bundle().apply {
            putString(DefinedParams.Origin, viewModel.origin)
        })

        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
        binding.ivLifestyle.safeClickListener(this)
        binding.btnMedicalReview.safeClickListener(this)
        binding.ivInvestigation.safeClickListener(this)
        binding.ivPrescription.safeClickListener(this)
        binding.ivTreatmentPlan.safeClickListener(this)

        if (CommonUtils.isNurse()) {
            binding.bottomNavigationView.visibility = View.GONE
            binding.btnMedicalReview.visibility = View.GONE
        } else {
            binding.bottomNavigationView.visibility = View.VISIBLE
            binding.btnMedicalReview.visibility = View.VISIBLE
        }
    }

    private fun swipeRefresh() {
        viewModel.getPatientDetails(this,false)
        if (!CommonUtils.isNurse())
            medicalReviewBaseViewModel.getBadgeCount()
        binding.refreshLayout.isRefreshing = false
    }

    private inline fun <reified fragment : Fragment> replaceFragmentInId(
        id: Int? = null,
        bundle: Bundle? = null,
        tag: String? = null
    ) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<fragment>(id ?: binding.patientDetailFragment.id, args = bundle, tag = tag)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnMedicalReview -> {
                viewModel.patientDetailsResponse.value?.data?.let {
                    val intent = Intent(this, MedicalReviewBaseActivity::class.java)
                    intent.putExtra(IntentConstants.IntentPatientID, it._id)
                    intent.putExtra(IntentConstants.IntentVisitId, viewModel.patientVisitId)
                    intent.putExtra(IntentConstants.ShowContinuousMedicalReview, true)
                    startActivity(intent)
                }
            }
            binding.ivInvestigation.id, binding.tvInvestigation.id -> {
                startActivity(
                    Intent(
                        this,
                        LabTestCreateActivity::class.java
                    ).apply {
                        putExtra(IntentConstants.IntentPatientID, viewModel.patientId)
                        putExtra(IntentConstants.IntentVisitId, viewModel.patientVisitId)
                    }
                )
            }

            binding.ivPrescription.id -> {
                val intent = Intent(this, PrescriptionCopyActivity::class.java)
                intent.putExtra(IntentConstants.IntentPatientID, viewModel.patientId)
                intent.putExtra(IntentConstants.IntentVisitId, viewModel.patientVisitId)
                startActivity(intent)
            }
            R.id.ivTreatmentPlan -> {
                if(connectivityManager.isNetworkAvailable())
                    TreatmentPlanDialog().show(supportFragmentManager, TreatmentPlanDialog.TAG)
                else
                    showErrorDialogue(getString(R.string.error),getString(R.string.no_internet_error), false){}
            }

            binding.ivLifestyle.id -> {
                startActivity(
                    Intent(
                        this,
                        LifestyleBaseActivity::class.java
                    ).apply {
                        putExtra(IntentConstants.IntentPatientID, viewModel.patientId)
                        putExtra(IntentConstants.IntentVisitId, viewModel.patientVisitId)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        swipeRefresh()
    }
}