package com.medtroniclabs.opensource.ui.patientedit

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safePopupMenuClickListener
import com.medtroniclabs.opensource.custom.DeleteConfirmationDialog
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.PatientRemoveRequest
import com.medtroniclabs.opensource.databinding.ActivityPatientEditBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.RoleConstant
import com.medtroniclabs.opensource.ui.medicalreview.dialog.GeneralSuccessDialog
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import com.medtroniclabs.opensource.ui.patientedit.fragment.PatientEditFragment
import com.medtroniclabs.opensource.ui.patientedit.fragment.PatientViewFragment
import com.medtroniclabs.opensource.ui.patientedit.viewmodel.PatientEditViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PatientEditActivity : BaseActivity() {

    private lateinit var binding: ActivityPatientEditBinding

    private val viewModel: PatientEditViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientEditBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_details),
            homeAndBackVisibility = Pair(true, null),
            homeIcon = ContextCompat.getDrawable(this,R.drawable.ic_more_vertical),
            callbackMore = {
                onMoreIconClicked(it)
            }
        )
        initializeView()
        attachObserver()
    }

    private fun attachObserver() {
        patientViewModel.patientRemoveResponse.observe(this) { resourceState ->
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
    }

    private fun onMoreIconClicked(view: View) {
        val popupMenu = PopupMenu(this@PatientEditActivity, view)
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
             viewModel.currentFragment = 2
             loadFragment()
         }

         R.id.patient_delete -> {
             viewModel.patientDetailMap.value?.data?.let { map ->
                 val deleteConfirmationDialog = DeleteConfirmationDialog.newInstance(
                     getString(R.string.alert),
                     getString(
                         R.string.patient_delete_confirmation,
                         map[DefinedParams.First_Name] as String,
                         map[DefinedParams.Last_Name] as String
                     ),
                     { status, reason, otherReason ->
                         if (status) {
                             SecuredPreference.getSelectedSiteEntity()?.apply {
                                 viewModel.patient_tracker_id?.let { id ->
                                     patientViewModel.patientRemove(
                                         this@PatientEditActivity,
                                         PatientRemoveRequest(
                                             id,
                                             tenantId,
                                             viewModel.patientID,
                                             reason,
                                             otherReason
                                         )
                                     )
                                 }
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

    private fun initializeView() {
        viewModel.patientID = intent.getLongExtra(DefinedParams.Patient_Id, -1L)
        viewModel.patient_tracker_id = intent.getLongExtra(DefinedParams.PatientTrackId, -1L)
        viewModel.fromMedicalReview = intent.getBooleanExtra(DefinedParams.fromMedicalReview,false)
        if (viewModel.fromMedicalReview){
            viewModel.currentFragment = 2
        }
        loadFragment()
    }

    private fun loadFragment() {
        when (viewModel.currentFragment) {
            1 -> {
                replaceFragmentInId<PatientViewFragment>(
                    binding.fragmentContainerAssessment.id,
                    tag = PatientViewFragment.TAG
                )
            }

            2 -> {
                replaceFragmentInId<PatientEditFragment>(
                    binding.fragmentContainerAssessment.id,
                    tag = PatientEditFragment.TAG
                )
            }
        }
    }

    private inline fun <reified fragment : Fragment> replaceFragmentInId(
        id: Int? = null,
        bundle: Bundle? = null,
        tag: String? = null
    ) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<fragment>(
                id ?: binding.fragmentContainerAssessment.id,
                args = bundle,
                tag = tag
            )
        }
        hideHomeButton(viewModel.currentFragment == 2)
    }

    override fun onBackPressed() {
        if (viewModel.currentFragment == 2&& !viewModel.fromMedicalReview ){
            viewModel.currentFragment = 1
            loadFragment()
        }else{
            this@PatientEditActivity.finish()
        }
    }

}