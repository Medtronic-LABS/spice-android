package com.medtroniclabs.opensource.ui.screening

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.RadioGroup
import androidx.activity.viewModels
import com.medtroniclabs.opensource.custom.flexboxradiogroup.FlexBoxRadioGroup
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.*
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.databinding.ActivityGeneralDetailsBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.convertType
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import com.medtroniclabs.opensource.ui.screening.viewmodel.GeneralDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GeneralDetailsActivity : BaseActivity(), View.OnClickListener,
    RadioGroup.OnCheckedChangeListener, FlexBoxRadioGroup.OnCheckedChangeListener {

    private lateinit var binding: ActivityGeneralDetailsBinding
    private var adapter: CustomSpinnerAdapter? = null

    private val viewModel: GeneralDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeneralDetailsBinding.inflate(layoutInflater)
        setMainContentView(binding.root,
            true, getString(R.string.screening),
            homeAndBackVisibility = Pair(true, true),
            callbackHome = {
                if (viewModel.isFromAssessment) {
                    startAsNewActivity(Intent(this, LandingActivity::class.java))
                } else {
                    handleHomeClick()
                }
            })
        init()
        setListeners()
        attachObserver()
    }

    private fun setListeners() {

        binding.etSiteChange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                pos: Int,
                itemId: Long
            ) {
                adapter?.getData(pos)?.let {
                    processSiteSelection(it)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                /**
                 * this method is not used
                 */
            }

        }
    }

    /**
     * Initialize Views, Listeners And Other Objects
     */
    private fun init() {
        viewModel.isFromAssessment =
            intent.getBooleanExtra(IntentConstants.IntentScreening, false)
        binding.tvTitleCategory.markMandatory()
        binding.tvTitleType.markMandatory()
        binding.btnNext.safeClickListener(this)
        binding.rgCategoryRow.setOnCheckedChangeListener(this)
        binding.rgType.setOnCheckedChangeListener(this)
        binding.rbFacility.isChecked = true
        viewModel.fetchAccountSiteList()
    }

    private fun attachObserver() {
        viewModel.siteDetailSaved.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                    binding.btnNext.isEnabled = true
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    if (resourceState.data != null && resourceState.data) {
                        val intent = Intent(this, StatsActivity::class.java)
                        startActivity(intent)
                    }
                    binding.btnNext.isEnabled = true
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    binding.btnNext.isEnabled = true
                }
            }
        }

        viewModel.accountSiteListLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    val list = ArrayList<Map<String, Any>>()
                    list.add(
                        hashMapOf<String, Any>(
                            DefinedParams.NAME to MedicalReviewConstant.DefaultIDLabel,
                            DefinedParams.ID to MedicalReviewConstant.DefaultSelectID
                        )
                    )
                    resourceState.data?.forEach { site ->
                        StringConverter.convertSiteModelToMap(site)?.let {
                            list.add(it)
                        }
                    }
                    adapter = CustomSpinnerAdapter(this)
                    adapter?.setData(list)
                    binding.etSiteChange.adapter = adapter

                    SecuredPreference.getSelectedSiteEntity()?.let { siteEntity ->
                        val index =
                            list.indexOfFirst { it.containsKey(DefinedParams.id) && it[DefinedParams.id] != null && convertType(it[DefinedParams.id]) == siteEntity.id }
                        if (index >= 0) {
                            binding.etSiteChange.setSelection(index, true)
                        }
                    }
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnNext -> {
                viewModel.siteDetail.let {
                    if ((it.siteId ?: 0) > 0) {
                        binding.btnNext.isEnabled = false
                        if (binding.etOthers.visibility == View.VISIBLE) {
                            binding.etOthers.text?.let { text ->
                                if (text.isNotBlank()) {
                                    viewModel.siteDetail.categoryDisplayType = text.trim().toString()
                                }
                            }
                        }
                        viewModel.saveChosenSiteDetails()
                    } else {
                        showErrorDialogue(getString(R.string.error),getString(R.string.facility_error),false){}
                    }
                }
            }
        }
    }

    /**
     * Validate All The Required Fields Are Selected
     * */
    private fun validateToEnableNext(): Boolean {
        val categorySelected = binding.rgCategoryRow.checkedRadioButtonId != -1
        val typeSelected = binding.rgType.checkedRadioButtonId != -1
        return categorySelected && typeSelected
    }

    override fun onCheckedChanged(radioGroup: RadioGroup, radioButton: Int) {
        when (radioGroup.id) {
            R.id.rgCategoryRow -> {
                when (radioButton) {
                    R.id.rbFacility -> {
                        viewModel.siteDetail.category = TranslatedStaticStrings.Facility
                        viewModel.siteDetail.categoryDisplayName = getString(R.string.clinic)
                        changeScreenTypeDetails(radioButton)
                    }
                    R.id.rbCommunity -> {
                        viewModel.siteDetail.category = TranslatedStaticStrings.Community
                        viewModel.siteDetail.categoryDisplayName = getString(R.string.community)
                        changeScreenTypeDetails(radioButton)
                    }
                }
            }
        }

        val fieldsAreValidated = validateToEnableNext()
        binding.btnNext.isEnabled = fieldsAreValidated
    }

    override fun onCheckedChanged(group: FlexBoxRadioGroup?, checkedId: Int) {
        val isFacilitySelected = viewModel.siteDetail.category == TranslatedStaticStrings.Facility
        when (group?.checkedRadioButtonId) {
            R.id.rbTypeBtn1 -> {
                binding.etOthers.visibility = View.GONE
                if (isFacilitySelected) {
                    viewModel.siteDetail.categoryDisplayType = getString(R.string.opd_triage)
                    viewModel.siteDetail.categoryType = DefinedParams.OPDTriage
                } else {
                    viewModel.siteDetail.categoryDisplayType = getString(R.string.door_to_door)
                    viewModel.siteDetail.categoryType = DefinedParams.DoorToDoor
                }
            }
            R.id.rbTypeBtn2 -> {
                binding.etOthers.visibility = View.GONE
                if (isFacilitySelected) {
                    viewModel.siteDetail.categoryDisplayType = getString(R.string.outpatient)
                    viewModel.siteDetail.categoryType = DefinedParams.outpatient
                } else {
                    viewModel.siteDetail.categoryDisplayType = getString(R.string.camp)
                    viewModel.siteDetail.categoryType = DefinedParams.Camp
                }
            }
            R.id.rbTypeBtn3 -> {
                binding.etOthers.visibility = View.GONE
                if (isFacilitySelected) {
                    viewModel.siteDetail.categoryDisplayType = getString(R.string.inpatient)
                    viewModel.siteDetail.categoryType = DefinedParams.inpatient
                }
            }
            R.id.rbTypeBtn4 -> {
                binding.etOthers.visibility = View.GONE
                if (isFacilitySelected) {
                    viewModel.siteDetail.categoryDisplayType = getString(R.string.pharmacy)
                    viewModel.siteDetail.categoryType = DefinedParams.Pharmacy
                }
            }
            R.id.rbTypeBtn5 -> {
                if (isFacilitySelected) {
                    binding.etOthers.visibility = View.VISIBLE
                    binding.etOthers.text = null
                    viewModel.siteDetail.categoryDisplayType = getString(R.string.Other)
                    viewModel.siteDetail.categoryType = DefinedParams.Other
                }
            }
        }
        val fieldsAreValidated = validateToEnableNext()
        binding.btnNext.isEnabled = fieldsAreValidated
    }

    /**
     *   To show category type.
     *   The category type should be shown only on the selected 'Category' is 'Community'.
     * */
    private fun changeScreenTypeDetails(category: Int) {
        binding.rgType.clearCheck()
        binding.rbTypeBtn3.visibility = View.GONE
        binding.rbTypeBtn4.visibility = View.GONE
        binding.rbTypeBtn5.visibility = View.GONE
        binding.etOthers.visibility = View.GONE
        when (category) {
            R.id.rbFacility -> {
                binding.rbTypeBtn3.visibility = View.VISIBLE
                binding.rbTypeBtn4.visibility = View.VISIBLE
                binding.rbTypeBtn5.visibility = View.VISIBLE
                binding.rbTypeBtn1.text = getString(R.string.opd_triage)
                binding.rbTypeBtn2.text = getString(R.string.outpatient)
                binding.rbTypeBtn3.text = getString(R.string.inpatient)
                binding.rbTypeBtn4.text = getString(R.string.pharmacy)
                binding.rbTypeBtn5.text = getString(R.string.Other)
            }
            R.id.rbCommunity -> {
                binding.rbTypeBtn1.text = getString(R.string.door_to_door)
                binding.rbTypeBtn2.text = getString(R.string.camp)
            }
        }
    }

    private fun handleHomeClick() {
        this@GeneralDetailsActivity.finish()
    }

    private fun processSiteSelection(map: Map<String, Any>) {

        map.let {
            viewModel.siteDetail.siteName =
                if (it.containsKey(DefinedParams.NAME)) it[DefinedParams.NAME] as String else ""
            viewModel.siteDetail.siteId =
                if (it.containsKey(DefinedParams.ID)) convertType(it[DefinedParams.ID]) else MedicalReviewConstant.DefaultSelectID
            viewModel.siteDetail.tenantId =
                if (it.containsKey(DefinedParams.TenantId)) convertType(it[DefinedParams.TenantId]) else MedicalReviewConstant.DefaultSelectID
        }
    }

}
