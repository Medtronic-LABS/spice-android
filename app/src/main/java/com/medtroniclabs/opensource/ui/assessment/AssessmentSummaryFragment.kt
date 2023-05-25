package com.medtroniclabs.opensource.ui.assessment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.capitalizeFirstChar
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.assesssment.AssessmentPatientResponse
import com.medtroniclabs.opensource.data.assesssment.MedicalComplianceResponse
import com.medtroniclabs.opensource.data.assesssment.SymptomResponse
import com.medtroniclabs.opensource.databinding.CardLayoutBinding
import com.medtroniclabs.opensource.databinding.FragmentAssessmentSummaryBinding
import com.medtroniclabs.opensource.databinding.SummaryLayoutBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.landing.LandingActivity

class AssessmentSummaryFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentAssessmentSummaryBinding

    private val viewModel: AssessmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachObserver()
        attachListeners()
    }

    private fun attachListeners() {
        binding.btnDoneSummary.safeClickListener(this)
    }

    private fun attachObserver() {
        viewModel.assessmentResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { data ->
                        addChildViews(data)
                    }
                }
            }
        }
    }


    private fun addChildViews(data: AssessmentPatientResponse) {
        binding.llRoot.removeAllViews()
        updateRedRiskDetails(data.riskLevel, data.riskMessage)
        addCardView(getString(R.string.bio_data), 1, data)
        addCardView(getString(R.string.result), 2, data)
    }

    private fun updateRedRiskDetails(riskLevel: String?, riskMessage: String?) {
        binding.clRedRisk.visibility =
            if (riskMessage.isNullOrBlank() || riskLevel.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.tvRedRiskStatus.text = riskMessage ?: ""
        riskLevel?.let {
            when (it) {
                DefinedParams.Red_Risk_Low -> {
                    binding.ivRedRisk.setImageResource(R.drawable.ic_red_risk_green)
                    binding.clRedRisk.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_red_risk_green)
                }
                DefinedParams.Red_Risk_Moderate -> {
                    binding.ivRedRisk.setImageResource(R.drawable.ic_red_risk_orange)
                    binding.clRedRisk.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_red_risk_orange)
                }
                DefinedParams.Red_Risk_High -> {
                    binding.ivRedRisk.setImageResource(R.drawable.ic_red_risk_red)
                    binding.clRedRisk.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_red_risk)
                }
            }
        }
    }

    private fun addCardView(cardTitle: String, cardCode: Int, data: AssessmentPatientResponse) {
        val cardBinding = CardLayoutBinding.inflate(layoutInflater)
        cardBinding.cardTitle.text = cardTitle
        inflateCardChild(cardCode, cardBinding.llFamilyRoot, data)
        binding.llRoot.addView(cardBinding.root)
    }

    private fun inflateCardChild(
        cardCode: Int,
        llFamilyRoot: LinearLayout,
        data: AssessmentPatientResponse
    ) {
        when (cardCode) {
            1 -> {
                llFamilyRoot.apply {
                    data.patientDetails?.apply {
                        addView(
                            inflateChildView(
                                getString(R.string.name),
                                CommonUtils.capitalize("$firstName $lastName")
                            )
                        )
                        addView(
                            inflateChildView(
                                getString(R.string.gender),
                                gender.replaceFirstChar(Char::titlecase)
                            )
                        )
                        addView(
                            inflateChildView(
                                getString(R.string.age),
                                CommonUtils.getDecimalFormatted(age)
                            )
                        )
                        programId?.let { programID ->
                            addView(
                                inflateChildView(
                                    getString(R.string.program_id),
                                    CommonUtils.getDecimalFormatted(programID)
                                )
                            )
                        }
                        addView(
                            inflateChildView(
                                getString(R.string.national_id),
                                nationalId
                            )
                        )
                    }
                }
            }

            2 -> {
                llFamilyRoot.apply {
                    data.confirmDiagnosis?.let {
                        if (it.size > 0) {
                            val diagnosisString = it.joinToString(separator = ", ")
                            addView(
                                inflateChildView(
                                    getString(R.string.diagnosis),
                                    diagnosisString
                                )
                            )
                        }
                    }
                    data.bpLog?.apply {
                        addView(
                            inflateChildView(
                                getString(R.string.cvd_risk_level),
                                "${CommonUtils.getDecimalFormatted(cvdRiskScore)}% - $cvdRiskLevel",
                                applyBoldStyle = true,
                                textColor = CommonUtils.cvdRiskColorCode(
                                    cvdRiskScore,
                                    requireContext()
                                )
                            )
                        )
                        addView(
                            inflateChildView(
                                getString(R.string.average_bp_text),
                                getString(
                                    R.string.average_mmhg_string,
                                    CommonUtils.getDecimalFormatted(avgSystolic),
                                    CommonUtils.getDecimalFormatted(avgDiastolic)
                                )
                            )
                        )
                        addView(
                            inflateChildView(
                                getString(R.string.bmi),
                                CommonUtils.getDecimalFormatted(bmi)
                            )
                        )

                    }
                    data.glucoseLog?.apply {
                        glucoseValue?.let { gluoseValue ->
                            addView(
                                inflateChildView(
                                    getString(
                                        R.string.bg_with_type,
                                        glucoseType?.uppercase() ?: "-"
                                    ),
                                    "${CommonUtils.getDecimalFormatted(gluoseValue)} ${
                                        CommonUtils.getGlucoseUnit(
                                            data.glucoseLog.glucoseUnit,
                                            true
                                        )
                                    }"
                                )
                            )
                        }
                    }
                    if (SecuredPreference.isPHQ4Enabled()) {
                        data.phq4?.apply {
                            addView(
                                inflateChildView(
                                    getString(R.string.phq4_score),
                                    "${CommonUtils.getDecimalFormatted(phq4Score)} - $phq4RiskLevel"
                                )
                            )
                        }
                    }

                    data.symptoms?.apply {
                        addView(
                            inflateChildView(
                                getString(R.string.symptoms),
                                getSelectedSymptomsText(this)
                            )
                        )
                    }

                    data.medicalCompliance?.apply {
                        addView(
                            inflateChildView(
                                getString(R.string.medical_adherence),
                                getSelectedMedicalComplianceText(this)
                            )
                        )
                    }
                }
            }
        }

    }

    private fun getSelectedMedicalComplianceText(list: ArrayList<MedicalComplianceResponse>): String {
        val resultString = StringBuilder()
        val isTranslationEnabled = SecuredPreference.getIsTranslationEnabled()
        list.forEachIndexed { index, medicalComplianceResponse ->
            if (isTranslationEnabled){
                resultString.append(translatedMedicalCompliance(medicalComplianceResponse.name))
            }else {
                resultString.append(medicalComplianceResponse.name)
            }
            if (list.size > 1 && index == 0) {
                resultString.append(getString(R.string.empty_space))
                resultString.append(getString(R.string.separator_hyphen))
                resultString.append(getString(R.string.empty_space))
            }
            if (!medicalComplianceResponse.otherCompliance.isNullOrBlank()) {
                resultString.append(getString(R.string.empty_space))
                resultString.append(getString(R.string.separator_hyphen))
                resultString.append(getString(R.string.empty_space))
                resultString.append(medicalComplianceResponse.otherCompliance.trim().capitalizeFirstChar())
            }
            if (index > 0 && index != list.size - 1) {
                resultString.append(getString(R.string.comma_symbol))
            }
            resultString.append(getString(R.string.empty_space))
        }
        return resultString.toString()
    }

    private fun translatedMedicalCompliance(name: String): String {
        var model = viewModel.medicationParentComplianceResponse.value?.find { it.name == name }
         if (model == null){
             model = viewModel.medicationChildComplianceResponse.value?.find { it.name == name }
         }
         model?.cultureValue?.let { culture ->
             return culture
         }?:kotlin.run {
             return name
         }
    }

    private fun getSelectedSymptomsText(list: ArrayList<SymptomResponse>): String {
        val resultString = StringBuilder()
        val isTranslationEnabled = SecuredPreference.getIsTranslationEnabled()
        list.forEachIndexed { index, symptomResponse ->
            if (isTranslationEnabled){
                resultString.append(getTranslatedSymptomName(symptomResponse.name))
            }else {
                resultString.append(symptomResponse.name)
            }
            if (!symptomResponse.otherSymptom.isNullOrBlank()) {
                resultString.append(getString(R.string.empty_space))
                resultString.append(getString(R.string.separator_hyphen))
                resultString.append(getString(R.string.empty_space))
                resultString.append(symptomResponse.otherSymptom.trim().capitalizeFirstChar())
            } else if (symptomResponse.name.startsWith(
                    "No Symptom",
                    true
                ) && !symptomResponse.type.isNullOrBlank()
            ) {
                resultString.append(getString(R.string.empty_space))
                resultString.append(getString(R.string.separator_hyphen))
                resultString.append(getString(R.string.empty_space))
                resultString.append(symptomResponse.type.trim().capitalizeFirstChar())
            }
            if (index != list.size - 1) {
                resultString.append(getString(R.string.comma_symbol))
            }
            resultString.append(getString(R.string.empty_space))
        }
        return resultString.toString()
    }

    private fun getTranslatedSymptomName(name: String): String {
      val model = viewModel.symptomListResponse.value?.find { it.symptom == name }
        return if (model?.cultureValue != null&& model.cultureValue.isNotEmpty()){
            model.cultureValue
        }else{
            name
        }
    }

    private fun inflateChildView(
        labelKey: String,
        value: String,
        applyBoldStyle: Boolean? = null,
        textColor: Int? = null
    ): View {
        val summaryBinding = SummaryLayoutBinding.inflate(layoutInflater)
        summaryBinding.tvKey.text = labelKey
        summaryBinding.tvValue.text = value
        summaryBinding.tvRowSeparator.text = ":"
        applyBoldStyle?.let {
            summaryBinding.tvValue.typeface =
                ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
        }
        textColor?.let {
            summaryBinding.tvValue.setTextColor(it)
        }
        return summaryBinding.root
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnDoneSummary -> {
                (requireActivity() as BaseActivity).startAsNewActivity(
                    Intent(
                        requireActivity(),
                        LandingActivity::class.java
                    )
                )
            }
        }
    }


    companion object {
        const val TAG = "AssessmentSummaryFragment"
    }

}