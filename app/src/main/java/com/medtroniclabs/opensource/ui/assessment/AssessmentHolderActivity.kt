package com.medtroniclabs.opensource.ui.assessment

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.databinding.ActivityAssessmentHolderBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel

class AssessmentHolderActivity : BaseActivity() {

    lateinit var binding: ActivityAssessmentHolderBinding

    private val viewModel: AssessmentViewModel by viewModels()
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssessmentHolderBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.assessment),
            homeAndBackVisibility = Pair(true, null),
            callbackHome = {
                onHomeIconCheckClicked()
            },
            callback = {
                onHomeIconCheckClicked()
            }
        )
        getIntentValues()
        initializeView()
    }

    fun onHomeIconCheckClicked() {
        val assessmentFragment = supportFragmentManager.findFragmentByTag(AssessmentFragment.TAG)
        if (assessmentFragment != null) {
            showErrorDialogue(getString(R.string.alert),getString(R.string.exit_reason), isNegativeButtonNeed = true) {
                if (it) {
                    startAsNewActivity(Intent(this, LandingActivity::class.java))
                }
            }
        } else {
            startAsNewActivity(Intent(this, LandingActivity::class.java))
        }
    }

    private fun getIntentValues() {
        patientDetailViewModel.patientId = intent.getLongExtra(DefinedParams.Patient_Id, -1L)
    }

    private fun initializeView() {
        loadFragment(1)
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.assessmentResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadFragment(2)
                }
            }
        }
    }

    private fun loadFragment(status: Int) {

        when (status) {
            1 -> {
                replaceFragmentInId<AssessmentFragment>(
                    binding.fragmentContainerAssessment.id,
                    tag = AssessmentFragment.TAG
                )
            }
            2 -> {
                replaceFragmentInId<AssessmentSummaryFragment>(
                    binding.fragmentContainerAssessment.id,
                    tag = AssessmentSummaryFragment.TAG
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
    }

}