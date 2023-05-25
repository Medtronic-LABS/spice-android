package com.medtroniclabs.opensource.ui.enrollment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.PatientCreateResponse
import com.medtroniclabs.opensource.databinding.CardLayoutBinding
import com.medtroniclabs.opensource.databinding.FragmentEnrollmentSummaryBinding
import com.medtroniclabs.opensource.databinding.SummaryLayoutBinding
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.enrollment.viewmodel.EnrollmentFormBuilderViewModel
import com.medtroniclabs.opensource.ui.landing.LandingActivity

class EnrollmentSummaryFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentEnrollmentSummaryBinding
    private val viewModel: EnrollmentFormBuilderViewModel by activityViewModels()
    private val BIO_DATA_CARD_INDEX = 1
    private val RESULT_CARD_INDEX = 2
    private val TREATMENT_CARD_INDEX = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            (activity as BaseActivity).startAsNewActivity(
                Intent(
                    activity,
                    LandingActivity::class.java
                )
            )
        }
        callback.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEnrollmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideHomeIcon(true)
        setListeners()
        addChildViews()
    }

    private fun setListeners() {
        binding.actionButton.safeClickListener(this)
    }

    private fun addChildViews() {
        binding.llRoot.removeAllViews()
        addCardView(getString(R.string.bio_data), BIO_DATA_CARD_INDEX)
        addCardView(getString(R.string.result), RESULT_CARD_INDEX)
        addCardView(
            getString(R.string.treatment_plan), TREATMENT_CARD_INDEX,
            requireContext().getColor(R.color.cobalt_blue), requireContext().getColor(R.color.white)
        )
    }

    private fun addCardView(
        cardTitle: String,
        cardCode: Int,
        cardColor: Int? = null,
        textColor: Int? = null
    ) {
        val cardBinding = CardLayoutBinding.inflate(layoutInflater)
        cardBinding.cardTitle.text = cardTitle
        cardColor?.let {
            cardBinding.viewCardBG.setBackgroundColor(it)
        }
        textColor?.let {
            cardBinding.cardTitle.setTextColor(it)
        }
        viewModel.enrollPatientLiveData.value?.data?.let {
            inflateCardChild(
                cardCode, cardBinding.llFamilyRoot,
                it
            )
        }
        binding.llRoot.addView(cardBinding.root)
    }

    private fun inflateCardChild(
        cardCode: Int,
        llFamilyRoot: LinearLayout,
        responseModel: PatientCreateResponse
    ) {

        when (cardCode) {
            BIO_DATA_CARD_INDEX -> {
                addBioDataCardDetails(llFamilyRoot, responseModel)
            }

            RESULT_CARD_INDEX -> {
                addResultCardDetails(llFamilyRoot, responseModel)
            }

            TREATMENT_CARD_INDEX -> {
                addTreatmentPlanCardDetails(llFamilyRoot, responseModel)
            }
        }

    }

    private fun addResultCardDetails(
        llFamilyRoot: LinearLayout,
        responseModel: PatientCreateResponse
    ) {
        llFamilyRoot.let { layout ->

            val diagnosisList =
                if (responseModel.isConfirmDiagnosis == true) responseModel.confirmDiagnosis else responseModel.provisionalDiagnosis

            diagnosisList?.let {
                if (it.size > 0) {
                    var diagnosisText = it.joinToString(separator = ", ")
                    if (responseModel.isConfirmDiagnosis == false)
                        diagnosisText =
                            "$diagnosisText ${requireContext().getString(R.string.provisional_text)}"
                    layout.addView(inflateChildView(getString(R.string.diagnosis), diagnosisText))
                } else
                    layout.addView(
                        inflateChildView(
                            getString(R.string.diagnosis),
                            getString(R.string.none)
                        )
                    )
            }

            responseModel.bpLog?.apply {
                cvdRiskScore?.let { score ->
                    var cvdRisk = score.toString()
                    cvdRiskLevel.let { level ->
                        cvdRisk = "$cvdRisk% - $level"
                    }
                    layout.addView(
                        inflateChildView(
                            getString(R.string.cvd_risk_level),
                            cvdRisk,
                            applyBoldStyle = true,
                            textColor = CommonUtils.cvdRiskColorCode(
                                score.toDouble(),
                                requireContext()
                            )
                        )
                    )
                }

                layout.addView(
                    inflateChildView(
                        getString(R.string.average_bp_text),
                        getString(
                            R.string.average_mmhg_string,
                            CommonUtils.getDecimalFormatted(avgSystolic),
                            CommonUtils.getDecimalFormatted(avgDiastolic)
                        )
                    )
                )
            }

            responseModel.glucoseLog?.apply {
                var glucoseType = ""
                this.glucoseType?.let { type ->
                    glucoseType = type
                }
                glucoseValue?.let { gValue ->
                    layout.addView(
                        inflateChildView(
                            getString(R.string.bg_with_type, glucoseType.uppercase()),
                            "${CommonUtils.getDecimalFormatted(gValue)} ${
                                CommonUtils.getGlucoseUnit(
                                    glucoseUnit,
                                    true
                                )
                            }"
                        )
                    )
                }
            }

            responseModel.bpLog?.apply {
                bmi?.let {
                    layout.addView(inflateChildView(getString(R.string.bmi), it.toString()))
                }
            }

            if (SecuredPreference.isPHQ4Enabled()) {
                responseModel.phq4?.apply {
                    var phq4Text = ""
                    phq4Score?.let {
                        phq4Text = "$it"
                    }

                    phq4RiskLevel?.let {
                        phq4Text = "$phq4Text - $it"
                    }

                    layout.addView(inflateChildView(getString(R.string.phq4_score), phq4Text))
                }
            }
        }
    }

    private fun addBioDataCardDetails(
        llFamilyRoot: LinearLayout,
        responseModel: PatientCreateResponse
    ) {
        llFamilyRoot.let { layout ->
            responseModel.enrollment?.apply {
                enrollmentDate?.let {
                    layout.addView(
                        inflateChildView(
                            getString(R.string.date_of_enrollment),
                            DateUtils.convertDateTimeToDate(
                                it,
                                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                                DateUtils.DATE_ddMMyyyy
                            )
                        )
                    )
                }

                var name = ""
                firstName?.let { name = it }
                middleName?.let { name = "$name $it" }
                lastName?.let { name = "$name $it" }
                layout.addView(inflateChildView(getString(R.string.name), CommonUtils.capitalize(name)))

                gender?.let {
                    layout.addView(
                        inflateChildView(
                            getString(R.string.gender),
                            it
                        )
                    )
                }

                age?.let {
                    layout.addView(inflateChildView(getString(R.string.age), it.toString()))
                }

                programId?.let {
                    layout.addView(inflateChildView(getString(R.string.program_id), it.toString()))
                }

                nationalId?.let {
                    layout.addView(inflateChildView(getString(R.string.national_id), it))
                }

                phoneNumber?.let { phnNo ->
                    val countryCode = SecuredPreference.getCountryCode() ?: ""
                    layout.addView(
                        inflateChildView(
                            getString(R.string.mobile_number),
                            "+$countryCode $phnNo"
                        )
                    )
                }

                siteName?.let {
                    layout.addView(inflateChildView(getString(R.string.facility_name), it))
                }
            }
        }
    }

    private fun addTreatmentPlanCardDetails(
        llFamilyRoot: LinearLayout,
        responseModel: PatientCreateResponse
    ) {
        llFamilyRoot.let { layout ->
            responseModel.treatmentPlan?.let { treatmentPlans ->
                treatmentPlans.forEach { treatmentPlanResponse ->
                    treatmentPlanResponse.label?.let { frequencyType ->
                        val frequencyValue = treatmentPlanResponse.value ?: "-"
                        layout.addView(
                            inflateChildView(
                                "$frequencyType",
                                frequencyValue
                            )
                        )
                    }
                }
            }
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

    companion object {
        @JvmStatic
        fun newInstance() =
            EnrollmentSummaryFragment().apply {
            }
    }

    override fun onClick(mView: View?) {

        when (mView) {
            binding.actionButton -> {
                (activity as EnrollmentFormBuilderActivity).startAsNewActivity(
                    Intent(
                        requireContext(),
                        LandingActivity::class.java
                    )
                )
            }
        }

    }
}