package com.medtroniclabs.opensource.ui.screening

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.common.IntentConstants.INTENT_ID
import com.medtroniclabs.opensource.common.IntentConstants.INTENT_SPINNER_SELECTED_ITEM
import com.medtroniclabs.opensource.common.IntentConstants.IntentSelectedSite
import com.medtroniclabs.opensource.databinding.ActivityScreeningAccountSiteChangeBinding
import com.medtroniclabs.opensource.db.tables.SiteEntity
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.screening.adapter.ScreeningAccountSiteAdapter
import com.medtroniclabs.opensource.ui.screening.listener.SiteChangeListener
import com.medtroniclabs.opensource.ui.screening.viewmodel.ScreeningAccountSiteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScreeningAccountSiteChangeActivity : BaseActivity(),SiteChangeListener {

    lateinit var binding: ActivityScreeningAccountSiteChangeBinding

    private val viewModel: ScreeningAccountSiteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreeningAccountSiteChangeBinding.inflate(layoutInflater)
        setMainContentView(binding.root, true, getString(R.string.choose_site))
        getIntentValues()
        initializeView()
        attachObserver()
    }

    private fun getIntentValues() {
        viewModel.SelectedSite = intent.getStringExtra(IntentSelectedSite)
    }

    private fun initializeView() {
        binding.rvAccountSiteList.layoutManager =
            LinearLayoutManager(binding.rvAccountSiteList.context)
        binding.rvAccountSiteList.itemAnimator = DefaultItemAnimator()
        binding.rvAccountSiteList.adapter = ScreeningAccountSiteAdapter(ArrayList(),this,viewModel.SelectedSite)
    }

    private fun attachObserver() {
        viewModel.accountSiteListLiveData.observe(this, { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                        handleSuccess(it)
                    }
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.LOADING -> {
                    showLoading()
                }
            }
        })
    }

    private fun handleSuccess(data: ArrayList<SiteEntity>) {
        binding.rvAccountSiteList.adapter = ScreeningAccountSiteAdapter(
            data,
            this,
            viewModel.SelectedSite
        )
    }

    override fun onSiteChange(siteEntity: SiteEntity) {
        val intent = Intent()
        intent.putExtra(INTENT_SPINNER_SELECTED_ITEM, siteEntity)
        intent.putExtra(INTENT_ID, IntentConstants.IntentSiteChangeValue)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}