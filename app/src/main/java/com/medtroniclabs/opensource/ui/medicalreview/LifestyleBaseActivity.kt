package com.medtroniclabs.opensource.ui.medicalreview

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.LifeStyleManagement
import com.medtroniclabs.opensource.databinding.ActivityLifestyleBaseBinding
import com.medtroniclabs.opensource.db.tables.NutritionLifeStyle
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.TagListCustomView
import com.medtroniclabs.opensource.ui.medicalreview.adapter.LifeStyleManagementAdapter
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.LifeStyleViewModel

class LifestyleBaseActivity : BaseActivity(), View.OnClickListener, LifeStyleInterface {

    private lateinit var binding: ActivityLifestyleBaseBinding
    private lateinit var adapter: LifeStyleManagementAdapter
    private val viewModel: LifeStyleViewModel by viewModels()
    private lateinit var tagListCustomView: TagListCustomView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLifestyleBaseBinding.inflate(layoutInflater)
        try {
            val callback = onBackPressedDispatcher.addCallback(this) {
                onExitPage()
            }
            callback.isEnabled = true
            setMainContentView(
                binding.root,
                isToolbarVisible = true,
                getString(R.string.lifestyle_management),
                homeAndBackVisibility = Pair(false, null)
            )

            onNewIntent(intent)
            initializeView()
            setListeners()
            attachObservers()
            viewModel.getPatientLifestyleList()
        } catch (e: Exception) {
            //Exception - Catch block
        }
    }

    private fun onExitPage() {
        when {
            viewModel.newReferralData.value != null -> viewModel.newReferralData.value?.let {
                showErrorDialogue(
                    getString(R.string.alert),
                    getString(R.string.exit_reason_message),
                    isNegativeButtonNeed = true
                ) {
                    if (it)
                        finish()
                }
            }
            else -> finish()
        }
    }

    private fun attachObservers() {
        viewModel.patientLifestyleList.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    viewModel.getLifeStyleList()
                    resourceState?.message?.let { message ->
                        showErrorDialogue(getString(R.string.error),message, false){}
                    }
                }
                ResourceState.SUCCESS -> {
                    resourceState.data?.let {
                        viewModel.lifeStyleReferredList.value = ArrayList(it)
                        viewModel.getLifeStyleList()
                    }
                    viewModel.clearBadgeNotification()
                }
            }
        }

        viewModel.clearBadgeNotificationResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                }
            }
        }

        viewModel.createLifestyleResponse.observe(this) { resourceState ->
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
                        finish()
                    }
                }
            }
        }

        viewModel.removeLifestyleResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { map ->
                        removeLifeStyle(map)
                    }
                }
            }
        }

        viewModel.lifeStyleReferredList.observe(this) { list ->
            lifeStyleReferredList(list)
        }

        viewModel.lifestyleList.observe(this) { list ->
            list?.let {
                tagListCustomView.addChipItemList(list)
            }
        }

        viewModel.newReferralData.observe(this) { model ->
            newReferral(model)
        }
    }

    private fun removeLifeStyle(map: HashMap<String, Any>) {
        val list = viewModel.lifeStyleReferredList.value
        val index =
            list?.indexOfFirst { (map[DefinedParams.ID] as Long?) == it.id }
        if (index != null && index >= 0) {
            list?.removeAt(index)
            viewModel.lifeStyleReferredList.value = list
        }
    }

    private fun lifeStyleReferredList(list: ArrayList<LifeStyleManagement>?) {
        if (list.isNullOrEmpty())
            hideRecyclerView()
        else {
            showRecyclerView()
            adapter = LifeStyleManagementAdapter(this,SecuredPreference.getIsTranslationEnabled())
            adapter.submitData(list)
            binding.rvList.adapter = adapter
        }
    }

    private fun newReferral(model: LifeStyleManagement?) {
        try {
            binding.bottomSheet.btnNext.isEnabled = model != null
            if (viewModel.lifeStyleReferredList.value != null) {
                viewModel.lifeStyleReferredList.value!!.let {
                    if (it.isNotEmpty() && it[0].id == null)
                        it.removeAt(0)
                    if (model != null)
                        it.add(0, model)
                    viewModel.lifeStyleReferredList.setValue(it)
                }
            } else if (model != null)
                viewModel.lifeStyleReferredList.value = arrayListOf(model)
        } catch (e: Exception) {
            //Exception - Catch block
        }
    }

    private fun setListeners() {

        binding.bottomSheet.btnNext.safeClickListener(this)
        binding.bottomSheet.btnBack.safeClickListener(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getLongExtra(IntentConstants.IntentPatientID, -1)
            ?.let { viewModel.patientTrackId = it }
        intent?.getLongExtra(IntentConstants.IntentVisitId, -1L)?.let { viewModel.patientVisitId = it }
    }

    private fun initializeView() {
        tagListCustomView = TagListCustomView(this, binding.chipGroup) { isEmpty ->
            try {
                if (tagListCustomView.getSelectedTags().isEmpty()) {
                    viewModel.newReferralData.value = null
                    binding.etClinicalNotes.isEnabled = false
                } else {
                    binding.etClinicalNotes.isEnabled = true
                    populateList()
                }
            } catch (e: Exception) {
                //Exception - Catch block
            }
        }
        binding.bottomSheet.btnNext.text = getString(R.string.fp_submit)
        hideRecyclerView()
        adapter = LifeStyleManagementAdapter(this, SecuredPreference.getIsTranslationEnabled())
        binding.rvList.layoutManager = LinearLayoutManager(binding.rvList.context ?: this)
        binding.rvList.adapter = adapter
    }

    private fun populateList() {
        val model = viewModel.newReferralData.value ?: LifeStyleManagement()
        model.lifestyle = tagListCustomView.getSelectedTags()
            .map { mod -> (mod as NutritionLifeStyle)._id }
            .toList()
        model.referredFor = tagListCustomView.getSelectedTags()
            .mapNotNull { mod -> (mod as NutritionLifeStyle).name }
            .joinToString(separator = ", ")
        model.cultureValue = tagListCustomView.getSelectedTags().mapNotNull {
            mod -> (mod as NutritionLifeStyle).cultureValue
        }.joinToString (separator = ", ")
        model.patientVisitId = viewModel.patientVisitId
        model.patientTrackId = viewModel.patientTrackId
        if (model.referredBy == null || model.referredByDisplay == null) {
            SecuredPreference.getUserDetails().let { user ->
                model.referredBy = user._id
                model.referredByDisplay = (user.firstName ?: "") + (user.lastName ?: "")
            }
        }
        model.referredDate = DateUtils.getCurrentDateTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
        viewModel.newReferralData.value = model
    }

    private fun showRecyclerView() {
        binding.rvList.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE
    }

    private fun hideRecyclerView() {
        binding.rvList.visibility = View.GONE
        binding.tvNoData.visibility = View.VISIBLE
    }


    override fun onClick(mView: View?) {
        when (mView?.id) {
            binding.bottomSheet.btnNext.id -> {
                if (connectivityManager.isNetworkAvailable()) {
                    referLifestylePrograms()
                } else {
                    showErrorDialogue(
                        getString(R.string.error),
                        getString(R.string.no_internet_error), false
                    ) {}
                }
            }
            binding.bottomSheet.btnBack.id -> {
                onExitPage()
            }
        }
    }

    private fun referLifestylePrograms() {

        viewModel.newReferralData.value?.let { lifeStyleRequest ->
            val notes = binding.etClinicalNotes.text
            if (!notes.isNullOrBlank()){
                lifeStyleRequest.clinicianNote= notes.toString().trim()
            }
            viewModel.createPatientLifestyle(lifeStyleRequest)
        }
    }

    override fun removeElement(model: LifeStyleManagement) {
        try {
            if (model.id == null) {
                tagListCustomView.clearSelection()
                binding.etClinicalNotes.setText("")
                val index = viewModel.lifeStyleReferredList.value?.indexOfFirst { it.id == null }
                if ((index ?: -1) >= 0) {
                    val list = viewModel.lifeStyleReferredList.value
                    list?.removeAt(index!!)
                    viewModel.lifeStyleReferredList.value = list
                }
            } else {
                viewModel.removePatientLifestyle(model.id)
            }
        } catch (e: Exception) {
            //Exception - Catch block
        }
    }
}


interface LifeStyleInterface {
    fun removeElement(model: LifeStyleManagement)
}