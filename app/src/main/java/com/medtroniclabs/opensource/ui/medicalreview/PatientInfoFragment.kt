package com.medtroniclabs.opensource.ui.medicalreview

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.databinding.FragmentPatientInfoBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.getGlucoseUnit
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.medicalreview.dialog.BPDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.BloodGlucoseDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.ConfirmDiagnosisDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.MentalHealthAssessmentDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.PregnantDetailsDialogFragment
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PatientInfoFragment : BaseFragment(), View.OnClickListener, PregnancySubmitListener {

    private lateinit var binding: FragmentPatientInfoBinding
    private val viewModel: PatientDetailViewModel by activityViewModels()
    private var patientDetails: PatientDetailsModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.patientId = it.getLong(PATIENT_ID)
            viewModel.topCardShow = it.getBoolean(SHOW_TOP_CARD, true)
            viewModel.showMentalHealthCard = it.getBoolean(SHOW_MENTAL_HEALTH_CARD, false)
            viewModel.isFromCMR = it.getBoolean(IS_FROM_CMR, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPatientInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        attachListeners()
        viewModel.getPatientDetails(requireContext(),false)
        if (viewModel.topCardShow) {
            showAssessmentCard()
        } else {
            hideAssessmentCard()
        }
        showDiagnosis()
        changeMHCardVisibility(viewModel.showMentalHealthCard)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getDiagnosisList()
    }

    private fun showDiagnosis() {
        binding.diagnosisGroup.visibility = if (viewModel.topCardShow) View.GONE else View.VISIBLE
    }

    private fun showPHQScore() {
        binding.phqGroup.visibility = View.GONE
        binding.gad7Group.visibility = View.GONE
        if (SecuredPreference.isPHQ4Enabled()) {
            patientDetails?.let { details ->
                if (details.isGad7) {
                    binding.gad7Group.visibility = View.VISIBLE
                }
                if (details.isPhq9) {
                    binding.phqGroup.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun initializeViews() {
        binding.assessmentCard.tvBGViewDetail.safeClickListener(this)
        binding.assessmentCard.tvBPViewDetail.safeClickListener(this)
        binding.assessmentCard.tvDiagnosisConfirm.safeClickListener(this)
        binding.assessmentCard.tvMHAssessment.safeClickListener(this)
        binding.assessmentCard.tvgad7Assessment.safeClickListener(this)
    }

    private fun attachListeners() {

        viewModel.refreshGraphDetails.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    viewModel.getPatientDetails(requireContext(),false)
                }
                else -> {
                    //Invoked if response state is not success
                }
            }
        }

        viewModel.patientDetailsResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadDetails(resourceState.data)
                }
            }
        }

        viewModel.isMentalHealthUpdated.observe(viewLifecycleOwner) {
            viewModel.getPatientDetails(requireContext(),false)
        }

        viewModel.pregnancyDetails.observe(viewLifecycleOwner) {
            renderPregnancyDetails(age = it)
        }
    }

    private fun loadDetails(response: PatientDetailsModel?) {
        try{
            if (response == null) {
                return
            }
            patientDetails = response

            viewModel.patientTrackId = response._id
            response.firstName?.let {
                val text = StringConverter.appendTexts(firstText = it, response.lastName)
                setTitle(
                    CommonUtils.capitalize(StringConverter.appendTexts(
                        firstText = text,
                        response.age?.toInt().toString(),
                        response.gender,
                        separator = "-"
                    ))
                )
            }

            binding.tvEnrollDate.text = response.enrollmentAt?.let {
                DateUtils.convertDateTimeToDate(
                    it,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DateUtils.DATE_FORMAT_ddMMMyyyy
                )
            } ?: getString(R.string.pending_enrollment)

            binding.tvProgramId.text = response.programId?.toString() ?: "-"
            binding.tvNationalId.text = response.nationalId ?: "-"
            response.phoneNumber?.let {
                var number = ""
                SecuredPreference.getCountryCode()?.let { code ->
                    number = "+${code} "
                }
                number += it
                binding.tvContact.text = number
            } ?: kotlin.run {
                binding.tvContact.text = "-"
            }
            binding.tvCVD.text = "-"
            response.cvdRiskScore?.let {
                binding.tvCVD.text = StringConverter.appendTexts(
                    "${it}%",
                    response.cvdRiskLevel, separator = "-"
                )

                val textColor = CommonUtils.cvdRiskColorCode(it, requireContext())
                binding.tvCVD.setTextColor(textColor)
            }
            response.bmi?.let {
                binding.tvBMI.text = CommonUtils.getDecimalFormatted(it)
            } ?: kotlin.run {
                binding.tvBMI.text = "-"
            }

            var text: String? = null

            if ((response.confirmDiagnosis?.size ?: 0) > 0) {
                text =
                    if (SecuredPreference.getIsTranslationEnabled()) {
                        getTranslatedDiagnosisList(response.confirmDiagnosis)?.joinToString(
                            separator = ", "
                        )
                    } else {
                        response.confirmDiagnosis?.joinToString(separator = ", ")
                    }
            } else if ((response.provisionalDiagnosis?.size ?: 0) > 0) {
                text = response.provisionalDiagnosis?.joinToString(separator = ", ")
            }
            if (response.isConfirmDiagnosis) {
                binding.assessmentCard.tvDiagnosisConfirm.text = getString(R.string.edit_diagnosis)
                binding.assessmentCard.tvDiagnosis.text = text ?: "-"
            } else {
                binding.assessmentCard.tvDiagnosisConfirm.text =
                    getString(R.string.confirm_diagnoses)
                binding.assessmentCard.tvDiagnosis.text =
                    text?.let { "$it ${getString(R.string.provisional_text)}" } ?: "-"
            }

            binding.assessmentCard.tvBPValue.text = "-"
            response.avgSystolic?.toInt()?.let { systolic ->
                response.avgDiastolic?.toInt()?.let { diastolic ->
                    binding.assessmentCard.tvBPValue.text =
                        getString(R.string.average_mmhg, systolic, diastolic)
                }
            }

            binding.assessmentCard.tvBGValue.text = "-"
            response.glucoseValue?.let {
                binding.assessmentCard.tvBGValue.text =
                    "${CommonUtils.getDecimalFormatted(it)} ${
                        getGlucoseUnit(
                            response.glucoseUnit,
                            false
                        )
                    }"
            }

            if (response.isPregnant) {
                renderPregnancyDetails()
            }

            mentalHealthCard(response)

            topCard(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun topCard(response: PatientDetailsModel) {
        if (!viewModel.topCardShow) {
            showPHQScore()
            binding.tvPHQ.text =
                CommonUtils.getMentalHealthScoreWithRisk(response, DefinedParams.PHQ9)
            binding.tvGAD7.text =
                CommonUtils.getMentalHealthScoreWithRisk(response, DefinedParams.GAD7)
            binding.tvDiagnosis.text = binding.assessmentCard.tvDiagnosis.text
        }
    }

    private fun mentalHealthCard(response: PatientDetailsModel) {
        if (viewModel.showMentalHealthCard) {
            var assessmentDetails = getString(R.string.separator_hyphen)
            binding.assessmentCard.tvMHAssessment.visibility = View.GONE
            binding.assessmentCard.tvgad7Assessment.visibility = View.GONE
            binding.assessmentCard.tvMHAssessment.text = getString(R.string.start_assessment)
            binding.assessmentCard.tvgad7Assessment.text = getString(R.string.start_assessment)
            when {
                response.isPhq9 && response.isGad7 -> {
                    binding.assessmentCard.tvgad7Assessment.visibility = View.VISIBLE
                    binding.assessmentCard.tvMHAssessment.visibility = View.VISIBLE
                    binding.assessmentCard.tvMHAssessment.tag = DefinedParams.PHQ9
                    binding.assessmentCard.tvgad7Assessment.text = getString(R.string.start_gad7)
                    binding.assessmentCard.tvMHAssessment.text = getString(R.string.start_phq9)
                }
                response.isPhq9 -> {
                    binding.assessmentCard.tvMHAssessment.tag = DefinedParams.PHQ9
                    binding.assessmentCard.tvMHAssessment.visibility = View.VISIBLE
                    binding.assessmentCard.tvMHAssessment.text = getString(R.string.start_assessment)
                }
                response.isGad7 -> {
                    binding.assessmentCard.tvgad7Assessment.visibility = View.VISIBLE
                    binding.assessmentCard.tvgad7Assessment.text = getString(R.string.start_assessment)
                }
                response.phq4Score == null -> {
                    binding.assessmentCard.tvMHAssessment.tag = DefinedParams.PHQ4
                    binding.assessmentCard.tvMHAssessment.visibility = View.VISIBLE
                }
            }
            val phq9Text = CommonUtils.getMentalHealthScoreWithRisk(response, DefinedParams.PHQ9)
            if (phq9Text.isNotEmpty() && phq9Text != getString(R.string.separator_hyphen)) {
                binding.assessmentCard.tvMHAssessment.text = getString(R.string.view_detail)
                assessmentDetails = getString(R.string.phq9_score, "- $phq9Text")
            }
            val gad7Text = CommonUtils.getMentalHealthScoreWithRisk(response, DefinedParams.GAD7)
            if (gad7Text.isNotEmpty() && gad7Text != getString(R.string.separator_hyphen)) {
                val gad7 = getString(R.string.gad7_score, "- $gad7Text")
                assessmentDetails = if (assessmentDetails == getString(R.string.separator_hyphen)) {
                    gad7
                } else {
                    "$assessmentDetails / $gad7"
                }
                binding.assessmentCard.tvgad7Assessment.text = getString(R.string.view_detail)
            }
            binding.assessmentCard.tvMHValue.text = assessmentDetails
        }
    }

    private fun renderPregnancyDetails(
        age: String? = null
    ) {
        patientDetails?.let { patientDetails ->
            if (SecuredPreference.isPregnancyWorkFlowEnabled()
                && patientDetails.gender == DefinedParams.Female
            ) {
                binding.pregnancyGroup.visibility = View.VISIBLE
                binding.tvPregnancyId.text = getString(R.string.separator_hyphen)
                binding.btnEditPregnancy.safeClickListener(this)
                binding.btnEditPregnancy.visibility =
                    if (CommonUtils.isNurse()) View.GONE else View.VISIBLE
                binding.btnEditPregnancy.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                var menstrualDate: Date? = null
                patientDetails.lastMenstrualPeriodDate?.let {
                    menstrualDate = SimpleDateFormat(
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        Locale.ENGLISH
                    )
                        .parse(it)
                }
                var deliveryDate: Date? = null
                patientDetails.estimatedDeliveryDate?.let {
                    deliveryDate = SimpleDateFormat(
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        Locale.ENGLISH
                    )
                        .parse(it)
                }
                CommonUtils.calculateGestationalAge(menstrualDate, deliveryDate)?.let {
                    binding.tvPregnancyId.text = it
                }
                age?.let {
                    binding.tvPregnancyId.text = it
                }
            }
        }
    }

    companion object {

        const val PATIENT_ID = "Patient_ID"
        const val TAG = "PatientInfoFragment"
        const val SHOW_TOP_CARD = "show_top_card"
        const val SHOW_MENTAL_HEALTH_CARD = "show_mental_health"
        const val IS_FROM_CMR = "is_from_cmr"

        @JvmStatic
        fun newInstance(patientId: Long? = null, showTopCard: Boolean = true) =
            PatientInfoFragment().apply {
                arguments = Bundle().apply {
                    putLong(PATIENT_ID, patientId ?: -1)
                    putBoolean(SHOW_TOP_CARD, showTopCard)
                }
            }
    }

    override fun onClick(mView: View?) {
        when (mView?.id) {
            binding.assessmentCard.tvBPViewDetail.id -> {
                BPDialog.newInstance().show(childFragmentManager, BPDialog.TAG)
            }
            binding.assessmentCard.tvBGViewDetail.id -> {
                BloodGlucoseDialog.newInstance().show(childFragmentManager, BloodGlucoseDialog.TAG)
            }
            binding.assessmentCard.tvDiagnosisConfirm.id -> {
                ConfirmDiagnosisDialog.newInstance(false)
                    .show(childFragmentManager, ConfirmDiagnosisDialog.TAG)
            }
            binding.assessmentCard.tvMHAssessment.id -> {
                val isViewOnly =
                    binding.assessmentCard.tvMHAssessment.text == getString(R.string.view_detail)
                binding.assessmentCard.tvMHAssessment.tag?.let {
                    if (it is String) {
                        var type = it
                        if (type.isEmpty()) {
                            type = DefinedParams.PHQ9
                        }

                        MentalHealthAssessmentDialog.newInstance(assessmentType = type, isViewOnly).show(
                            childFragmentManager,
                            MentalHealthAssessmentDialog.TAG
                        )
                    }
                }
            }
            binding.assessmentCard.tvgad7Assessment.id -> {
                val isViewOnly =
                    binding.assessmentCard.tvgad7Assessment.text == getString(R.string.view_detail)
                MentalHealthAssessmentDialog.newInstance(assessmentType = DefinedParams.GAD7, isViewOnly).show(
                    childFragmentManager,
                    MentalHealthAssessmentDialog.TAG
                )
            }
            binding.btnEditPregnancy.id -> {
                viewModel.allowDismiss = false
                PregnantDetailsDialogFragment.newInstance(this).show(
                    childFragmentManager,
                    PregnantDetailsDialogFragment.TAG
                )
            }
        }
    }

    fun hideAssessmentCard() {
        binding.assessmentCard.root.visibility = View.GONE
    }

    fun showAssessmentCard() {
        binding.assessmentCard.root.visibility = View.VISIBLE
    }

    fun changeMHCardVisibility(value: Boolean) {
        binding.assessmentCard.cardMH.visibility = if (value) View.VISIBLE else View.GONE
    }

    override fun onSubmit(gestationalAge: String?) {
        gestationalAge?.let { age ->
            renderPregnancyDetails(age = age)
        }
    }

    private fun getTranslatedDiagnosisList(confirmDiagnosis: ArrayList<String>?): ArrayList<String> {
        val translatedList = ArrayList<String>()
        confirmDiagnosis?.let { list ->
            viewModel.diagnosisList?.let { diagnosisListEntity ->
                list.forEach { diagnosisData ->
                    val model = diagnosisListEntity.find { it.diagnosis.equals(diagnosisData) }
                    if (model?.cultureValue != null)
                        translatedList.add(model.cultureValue)
                    else
                        translatedList.add(diagnosisData)
                }
            }
        }
        return translatedList
    }
}