package com.medtroniclabs.opensource.ui.medicalreview.labTechnician

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.databinding.ActivityLabtestListBinding
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.LabTestViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LabTestListActivity: BaseActivity() {

    private lateinit var binding: ActivityLabtestListBinding

    private val viewModel: LabTestViewModel by viewModels()

    private val patientViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabtestListBinding.inflate(layoutInflater)
        setMainContentView(binding.root, true, homeAndBackVisibility = Pair(true, true))
        initView()
        attachObserver()
    }

    private fun initView() {
        viewModel.patientTrackId = intent.getLongExtra(IntentConstants.IntentPatientID, -1L)
        viewModel.patientVisitId = intent.getLongExtra(IntentConstants.IntentVisitId, -1L)
        patientViewModel.patientId = viewModel.patientTrackId
        replaceFragmentInId<LabTestResultsFragment>(binding.labTestListFragment.id)
        patientViewModel.getPatientDetails(this,false)
        viewModel.getLabTestList(true)
    }

    private fun attachObserver() {

        patientViewModel.patientDetailsResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadPatientInfo(resourceState.data)
                }
            }
        }
    }

    private fun loadPatientInfo(data: PatientDetailsModel?) {
        data?.let {
            binding.tvProgramId.text = it.programId?.toString() ?: "-"
            binding.tvNationalId.text = it.nationalId ?: "-"

            binding.tvDiagnoses.text = getString(R.string.separator_hyphen)
            val diagnosisList =
                if (it.isConfirmDiagnosis) it.confirmDiagnosis else it.provisionalDiagnosis
            diagnosisList?.let { list ->
                if (list.size > 0) {
                    var diagnosisText = list.joinToString(separator = ", ")
                    if (!it.isConfirmDiagnosis)
                        diagnosisText =
                            "$diagnosisText ${getString(R.string.provisional_text)}"
                    binding.tvDiagnoses.text = diagnosisText
                }
            }
            it.cvdRiskScore?.let { score ->
                binding.tvCVD.text = StringConverter.appendTexts(
                    "${score}%",
                    it.cvdRiskLevel, separator = "-"
                )
                val textColor = CommonUtils.cvdRiskColorCode(score, this)
                binding.tvCVD.setTextColor(textColor)
            }
            data.firstName?.let { firstName ->
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

    private inline fun <reified fragment : Fragment> replaceFragmentInId(
        id: Int? = null,
        bundle: Bundle? = null,
        tag: String? = null
    ) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<fragment>(id ?: binding.labTestListFragment.id, args = bundle, tag = tag)
        }
    }
}