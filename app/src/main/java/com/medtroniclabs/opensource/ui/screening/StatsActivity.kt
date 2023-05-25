package com.medtroniclabs.opensource.ui.screening

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.databinding.ActivityScreeningStatsBinding
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import com.medtroniclabs.opensource.ui.screening.viewmodel.StatsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatsActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding: ActivityScreeningStatsBinding

    private val viewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityScreeningStatsBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            getString(R.string.screening),
            homeAndBackVisibility = Pair(true, true),
            callback = { onBackClicked() },
            callbackHome = { onHomeClicked() }
        )
        initializeGeneralDetailsView()
    }

    override fun onClick(button: View) {
        when (button.id) {
            R.id.btnScreenNextPatient -> {
                binding.btnScreenNextPatient.isEnabled = false
                val intent = Intent(this@StatsActivity, TermsAndConditionActivity::class.java)
                intent.putExtra(IntentConstants.IntentScreening, true)
                startActivity(intent)
                binding.btnScreenNextPatient.isEnabled = true
            }
        }
    }

    /**
     * Method To Initialize Views, Listeners And Other Objects
     */
    private fun initializeGeneralDetailsView() {
        viewModel.isStatsFromSummary = intent.getBooleanExtra(IntentConstants.IntentStatsFromSummary, false)
        SecuredPreference.getChosenSiteEntity()?.let { siteDetails ->
            binding.tvSiteName.text = siteDetails.siteName
            binding.tvScreeningCategory.text = siteDetails.categoryDisplayName
            if (!siteDetails.categoryDisplayType.isNullOrBlank()) {
                binding.tvScreeningType.text = siteDetails.categoryDisplayType
                binding.clScreeningTypeHolder.visibility = View.VISIBLE
            } else {
                binding.clScreeningTypeHolder.visibility = View.INVISIBLE
            }
        }
        viewModel.screenedPatientCount.observe(this) { count ->
            binding.tvPeopleScreened.text = count.toString()
        }
        viewModel.referredPatientCount.observe(this) { count ->
            binding.tvPeopleReferred.text = count.toString()
        }
        binding.btnScreenNextPatient.safeClickListener(this)
    }

    private fun onHomeClicked() {
        startAsNewActivity(Intent(this, LandingActivity::class.java))
    }

    private fun onBackClicked() {
        if (viewModel.isStatsFromSummary) {
            onHomeClicked()
        } else {
            finish()
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackClicked()
        }
    }


}