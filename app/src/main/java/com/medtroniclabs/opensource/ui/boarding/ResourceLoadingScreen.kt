package com.medtroniclabs.opensource.ui.boarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.databinding.ActivityResourceLoadingScreenBinding
import com.medtroniclabs.opensource.db.tables.SiteEntity
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.RoleConstant
import com.medtroniclabs.opensource.ui.boarding.listener.SiteSelectionListener
import com.medtroniclabs.opensource.ui.boarding.viewmodel.ResourceLoadingViewModel
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResourceLoadingScreen : BaseActivity(), SiteSelectionListener {

    private lateinit var binding: ActivityResourceLoadingScreenBinding

    private val viewModel: ResourceLoadingViewModel by viewModels()

    private var chooseSiteDialogueFragment: ChooseSiteDialogueFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResourceLoadingScreenBinding.inflate(layoutInflater)
        setMainContentView(binding.root)
        attachObserver()
        setViewListener()
    }

    private fun setViewListener() {
        binding.actionButton.safeClickListener {
            binding.actionButton.visibility = View.GONE
            viewModel.getMetaDataInformation()
        }
    }

    private fun attachObserver() {
        viewModel.metaDataComplete.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    //Invoked if the response is in loading state
                }
                ResourceState.SUCCESS -> {
                    viewModel.getSiteList(true)
                }
                ResourceState.ERROR -> {
                    SecuredPreference.putBoolean(
                        SecuredPreference.EnvironmentKey.ISMETALOADED.name,
                        false
                    )
                    binding.actionButton.visibility = View.VISIBLE
                }
            }
        }

        viewModel.medicalReviewStaticDataResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    //Invoked if the response is in loading state
                }
                ResourceState.SUCCESS -> {
                    showDashBoard()
                }
                ResourceState.ERROR -> {
                    SecuredPreference.putBoolean(
                        SecuredPreference.EnvironmentKey.ISMETALOADED.name,
                        false
                    )
                    binding.actionButton.visibility = View.VISIBLE
                }
            }
        }

        viewModel.siteListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    //Invoked if the response is in loading state
                }
                ResourceState.SUCCESS -> {
                    loadSites(resourceState.data)
                }
                ResourceState.ERROR -> {
                    binding.actionButton.visibility = View.VISIBLE
                }
            }
        }

        viewModel.siteStoredLiveData.observe(this) { status ->
            if (status)
                checkNavigation()
            else
                showSiteError()
        }
    }

    private fun loadSites(data: List<SiteEntity>?) {
        data?.let { siteList ->
            when {
                siteList.isNotEmpty() -> {
                    handleSiteList(siteList)
                }
                else -> {
                    showSiteError()
                }
            }
        } ?: kotlin.run {
            showSiteError()
        }
    }

    private fun checkNavigation() {
        chooseSiteDialogueFragment?.dismiss()
        if (hasProviderRole()) {
            viewModel.getMedicalReviewStaticData(SecuredPreference.getCultureId())
        } else {
            showDashBoard()
        }
    }

    private fun hasProviderRole(): Boolean {
        val list = viewModel.siteListLiveData.value?.data
        if (list != null && list.isNotEmpty()) {
            val filteredList =
                list.filter { it.role == RoleConstant.PROVIDER || it.role == RoleConstant.PHYSICIAN_PRESCRIBER }
            if (filteredList.isNotEmpty()) {
                return true
            }
        }
        return false
    }

    private fun handleSiteList(siteList: List<SiteEntity>) {
        val siteFilteredList = siteList.filter { it.isDefault }
        if (siteFilteredList.isNotEmpty()) {
            viewModel.storeSelectedEntity(siteFilteredList[0])
        } else {
            showSiteError()
        }
    }

    private fun showSiteError() {
        showErrorDialogue(getString(R.string.alert),getString(R.string.no_sites_associated)) {
            finish()
        }
    }

    private fun showDashBoard() {
        SecuredPreference.putBoolean(
            SecuredPreference.EnvironmentKey.ISLOGGEDIN.name,
            true
        )
        SecuredPreference.putBoolean(
            SecuredPreference.EnvironmentKey.ISMETALOADED.name,
            true
        )
        startAsNewActivity(
            Intent(
                this@ResourceLoadingScreen,
                LandingActivity::class.java
            )
        )
    }

    override fun onSiteSelected(siteEntity: SiteEntity) {
        viewModel.storeSelectedEntity(siteEntity)
    }

    override fun onCancelButtonSelected() {
        finish()
    }


}