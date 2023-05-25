package com.medtroniclabs.opensource.ui.enrollment

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.databinding.ActivityEnrollmentFormBuilderBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.enrollment.viewmodel.EnrollmentFormBuilderViewModel
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnrollmentFormBuilderActivity : BaseActivity() {

    lateinit var binding: ActivityEnrollmentFormBuilderBinding
    private val viewModel: EnrollmentFormBuilderViewModel by viewModels()
    private val patientDetailsViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnrollmentFormBuilderBinding.inflate(layoutInflater)
        intent?.getStringExtra(IntentConstants.IntentPatientInitial)?.let {
            viewModel.patientInitial = it
        }
        viewModel.screeningId = intent.getLongExtra(DefinedParams.Screening_Id, -1L)
        setMainContentView(
            binding.root,
            true,
            title = getString(R.string.enrollment),
            homeAndBackVisibility = Pair(true,true),
            callbackHome = {
                showOnBackPressedAlert()
            },
            callback = {
                showOnBackPressedAlert()
            })
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<EnrollmentFormFragment>(binding.fragmentContainerView.id)
            }
        }
        getIntentValues()
    }

    private fun getIntentValues() {
        patientDetailsViewModel.patientId = intent.getLongExtra(DefinedParams.Patient_Id, -1L)
        viewModel.patient_track_id = intent.getLongExtra(DefinedParams.Patient_Id, -1L)
        viewModel.isFromDirectEnrollment =
            intent.getBooleanExtra(IntentConstants.isFromDirectEnrollment, false)
    }

    inline fun <reified fragment : Fragment> replaceFragmentInId(id: Int? = null) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<fragment>(id ?: binding.fragmentContainerView.id)
        }
    }

    fun showOnBackPressedAlert(fragment: Fragment? = null) {
        showErrorDialogue(getString(R.string.alert),getString(R.string.exit_reason), isNegativeButtonNeed = true) {
            if (it)
                startAsNewActivity(
                    Intent(
                        this@EnrollmentFormBuilderActivity,
                        LandingActivity::class.java
                    )
                )
        }
    }
}