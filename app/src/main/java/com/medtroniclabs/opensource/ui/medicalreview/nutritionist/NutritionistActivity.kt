package com.medtroniclabs.opensource.ui.medicalreview.nutritionist

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.LifeStyleManagement
import com.medtroniclabs.opensource.data.model.Lifestyle
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.data.model.PatientLifestyleModel
import com.medtroniclabs.opensource.databinding.ActivityNutritionistBinding
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.LifeStyleConstants
import com.medtroniclabs.opensource.ui.medicalreview.dialog.GeneralSuccessDialog
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import java.util.*

class NutritionistActivity : BaseActivity(), ValidationListener, View.OnClickListener {

    private lateinit var binding: ActivityNutritionistBinding
    private val viewModel: NutritionistViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNutritionistBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            homeAndBackVisibility = Pair(true, true)
        )
        initView()
        attachObserver()
    }

    private fun attachObserver() {
        patientViewModel.patientDetailsResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> loadPatientInfo(resourceState.data)
            }
        }
        viewModel.patientLifestyleList.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadPatientLifestyleList(resourceState.data)
                }
            }
        }
        viewModel.updatePatientLifestyle.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    GeneralSuccessDialog.newInstance(
                        this,
                        getString(R.string.life_style), getString(R.string.life_style_saved_successfully),
                        needHomeNav = false, callback = {
                            if (it)
                                getLifestyleList()
                        }
                    ).show(supportFragmentManager, GeneralSuccessDialog.TAG)
                }
            }
        }
        viewModel.historyLifestyleList.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    binding.clLifeStyleHistory.visibility = View.VISIBLE
                    loadHistoryLifestyleList(resourceState.data)
                }
            }
        }
    }

    private fun loadPatientLifestyleList(data: ArrayList<LifeStyleManagement>?) {
        binding.tvNoRecord.visibility = View.GONE
        data?.let { list ->
            if (list.isEmpty()) {
                binding.rvPatientLifestyleList.visibility = View.GONE
                binding.tvNoRecord.visibility = View.VISIBLE
            } else {
                binding.rvPatientLifestyleList.visibility = View.VISIBLE
                val nutritionistAdapter = NutritionistAdapter(list, this)
                binding.rvPatientLifestyleList.layoutManager = LinearLayoutManager(this)
                binding.rvPatientLifestyleList.adapter = nutritionistAdapter
            }
        }
        resetDefaults()
    }

    private fun resetDefaults() {
        binding.apply {
            btnDone.isEnabled = false
            clLifeStyleHistory.visibility = View.GONE
            tvViewHistory.text = getString(R.string.view_history)
        }
    }

    private fun loadPatientInfo(data: PatientDetailsModel?) {
        data?.let { it ->
            binding.tvProgramId.text = it.programId?.toString() ?: "-"
            binding.tvNationalId.text = it.nationalId ?: "-"
            binding.tvPatientRisk.text = it.cvdRiskLevel ?: "-"
            it.bmi?.let { bmi ->
                binding.tvBMI.text = String.format(Locale.US, "%.2f", bmi)
            }
            it.patientLifestyles?.forEach {
                when (it.lifestyleType) {
                    LifeStyleConstants.SMOKING -> binding.tvSmokingStatus.text =
                        if (it.lifestyleAnswer.isNullOrBlank()) "-" else it.lifestyleAnswer
                    LifeStyleConstants.ALCOHOL -> binding.tvAlcoholStatus.text =
                        if (it.lifestyleAnswer.isNullOrBlank()) "-" else it.lifestyleAnswer
                    LifeStyleConstants.DIET_NUTRITION -> binding.tvDietNutrition.text =
                        if (it.lifestyleAnswer.isNullOrBlank()) "-" else it.lifestyleAnswer
                    LifeStyleConstants.PHYSICAL_ACTIVITY -> binding.tvPhysicalActivity.text =
                        if (it.lifestyleAnswer.isNullOrBlank()) "-" else it.lifestyleAnswer + " hrs/week"
                }
            }

            it.firstName?.let { firstName ->
                val text = StringConverter.appendTexts(firstText = firstName, data.lastName)
                setTitle(
                    StringConverter.appendTexts(
                        firstText = text,
                        data.age?.toInt().toString(),
                        data.gender,
                        separator = "-"
                    )
                )
            }
        }
    }

    private fun initView() {
        viewModel.patient_track_id = intent.getLongExtra(IntentConstants.IntentPatientID, -1L)
        viewModel.patient_visit_id = intent.getLongExtra(IntentConstants.IntentVisitId, -1L)
        viewModel.tenant_id = SecuredPreference.getSelectedSiteEntity()?.tenantId
        patientViewModel.patientId = viewModel.patient_track_id
        binding.btnDone.safeClickListener(this)
        binding.tvViewHistory.safeClickListener(this)
        binding.btnAddLifestyle.safeClickListener(this)

        getPatientDetails()

        getLifestyleList()
    }

    private fun getPatientDetails() {
        patientViewModel.getPatientDetails(
            context = this,
            isAssessmentDataRequired = false,
            isPrescriberRequired = false,
            isLifestyleRequired = true
        )
    }

    private fun getLifestyleList() {
        val request = PatientLifestyleModel(
            patientTrackId = viewModel.patient_track_id,
            tenantId = viewModel.tenant_id,
            nutritionist = true,
            nutritionHistoryRequired = false
        )
        viewModel.getPatientLifestyleList(request)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnDone -> updateLifestyles()
            R.id.tvViewHistory -> showLifeStyleHistory()
            R.id.btnAddLifestyle -> createNewLifestyle()
        }
    }

    private fun showLifeStyleHistory() {
        if (binding.clLifeStyleHistory.visibility == View.GONE) {
            binding.tvViewHistory.text = getString(R.string.hide_history)
            getHistoryLifestyleList()
        } else {
            binding.tvViewHistory.text = getString(R.string.view_history)
            binding.clLifeStyleHistory.visibility = View.GONE
        }
    }

    private fun updateLifestyles() {
        val lifeStyleList = ArrayList<Lifestyle>()
        viewModel.patientLifestyleList.value?.data?.forEach {
            if (!it.lifestyleAssessment.isNullOrBlank()) {
                lifeStyleList.add(
                    Lifestyle(
                        id = it.id,
                        lifestyleAssessment = it.lifestyleAssessment,
                        otherNote = it.otherNote
                    )
                )
            }
        }

        if (lifeStyleList.isNotEmpty()) {
            val request = PatientLifestyleModel(
                patientTrackId = viewModel.patient_track_id,
                patientVisitId = viewModel.patient_visit_id,
                lifestyles = lifeStyleList
            )
            viewModel.updatePatientLifestyle(request)
        } else {
            showErrorDialogue(getString(R.string.error),getString(R.string.default_user_input_error), false) {}
        }
    }

    private fun getHistoryLifestyleList() {
        val request = PatientLifestyleModel(
            patientTrackId = viewModel.patient_track_id,
            tenantId = viewModel.tenant_id,
            nutritionist = true,
            nutritionHistoryRequired = true
        )
        viewModel.getHistoryLifestyleList(request)
    }

    private fun loadHistoryLifestyleList(data: ArrayList<LifeStyleManagement>?) {
        binding.tvNoHistoryRecord.visibility = View.GONE
        data?.let { list ->
            if (list.isEmpty()) {
                binding.rvHistoryLifestyleList.visibility = View.GONE
                binding.tvNoHistoryRecord.visibility = View.VISIBLE
            } else {
                binding.rvHistoryLifestyleList.visibility = View.VISIBLE
                val nutritionistHistoryAdapter = NutritionistHistoryAdapter(list)
                binding.rvHistoryLifestyleList.layoutManager = LinearLayoutManager(this)
                binding.rvHistoryLifestyleList.adapter = nutritionistHistoryAdapter
            }
        }
    }

    override fun checkValidation() {
        var isValid = false
        viewModel.patientLifestyleList.value?.data?.forEach {
            if (!it.lifestyleAssessment.isNullOrBlank()) {
                isValid = true
                return@forEach
            }
        }
        binding.btnDone.isEnabled = isValid
    }

    private fun createNewLifestyle() {
        CreateLifestyleDialogFragment.newInstance { isPositiveResult ->
            if (isPositiveResult) {
                resetDefaults()
                GeneralSuccessDialog.newInstance(
                    this,
                    getString(R.string.life_style),
                    getString(R.string.life_style_created_successfully),
                    needHomeNav = viewModel.patientLifestyleList.value?.data?.isEmpty() == true
                ).show(supportFragmentManager, GeneralSuccessDialog.TAG)
            }
        }.show(supportFragmentManager, CreateLifestyleDialogFragment.TAG)
    }
}