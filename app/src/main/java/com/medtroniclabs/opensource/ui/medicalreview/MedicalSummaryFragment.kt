package com.medtroniclabs.opensource.ui.medicalreview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.google.android.flexbox.FlexboxLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.appextensions.setError
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.LabTestHistory
import com.medtroniclabs.opensource.data.model.MedicalReview
import com.medtroniclabs.opensource.data.model.MedicalReviewBaseRequest
import com.medtroniclabs.opensource.data.model.SummaryPrescription
import com.medtroniclabs.opensource.data.model.SummaryResponse
import com.medtroniclabs.opensource.databinding.FragmentMedicalSummaryBinding
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.medicalreview.adapter.BulletPointsAdapter
import com.medtroniclabs.opensource.ui.medicalreview.dialog.ConfirmDiagnosisDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.PrescriptionDueDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.PrescriptionDueListener
import com.medtroniclabs.opensource.ui.medicalreview.dialog.ReviewStatusDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.TreatmentPlanDialog
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.MedicalReviewBaseViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MedicalSummaryFragment : BaseFragment(), View.OnClickListener, PrescriptionDueListener {

    private lateinit var binding: FragmentMedicalSummaryBinding

    private val viewModel: PatientDetailViewModel by activityViewModels()

    private val medicalReviewBaseViewModel: MedicalReviewBaseViewModel by activityViewModels()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                getPatientMedicalReviewSummary()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMedicalSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireActivity() is MedicalReviewBaseActivity) {
            (requireActivity() as MedicalReviewBaseActivity).changeButtonTextToFinishMedicalReview(
                getString(R.string.fp_submit), getString(R.string.fp_submit)
            )
        }
        medicalReviewBaseViewModel.getDiagnosisList()
        setListeners()
        attachObserver()
    }

    override fun onResume() {
        super.onResume()
        apiCalls()
    }

    fun apiCalls() {
        getPatientMedicalReviewSummary()
        setAdapterViews()
    }

    private fun getPatientMedicalReviewSummary() {
        viewModel.patientId?.let { patientId ->
            SecuredPreference.getSelectedSiteEntity()?.let { siteEntity ->
                viewModel.patientVisitId?.let { visitID ->
                    medicalReviewBaseViewModel.medicalReViewRequest =
                        MedicalReviewBaseRequest(
                            patientId,
                            siteEntity.tenantId,
                            visitID,
                            isDetailedSummaryRequired = true
                        )
                    medicalReviewBaseViewModel.medicalReViewRequest?.let {
                        viewModel.getPatientMedicalReviewSummary(requireContext(),it)
                    }
                }
            }
        }
    }

    private fun attachObserver() {
        viewModel.refreshMedicalReview.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    viewModel.refreshMedicalReview.setError()
                    getPatientMedicalReviewSummary()
                }
                else -> {
                    //Invoked if response state is not success
                }
            }
        }

        medicalReviewBaseViewModel.continuousMedicalReviewResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        (activity as BaseActivity).showErrorDialogue(
                            getString(R.string.error),
                            message,
                            false
                        ) {}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    ReviewStatusDialog.newInstance()
                        .show(childFragmentManager, ReviewStatusDialog.TAG)
                }
            }
        }

        viewModel.medicalReviewSummaryResponse.observe(viewLifecycleOwner) { resourceState ->
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
                        loadData(it)
                        if (it.refreshPatientDetails)
                            viewModel.getPatientDetails(requireContext(),false, showLoader = false)
                        it.refreshPatientDetails = false
                    }
                }
            }
        }
    }

    private fun handleValues() {
        val confirmedDiagnosisList =
            if (viewModel.medicalReviewSummaryResponse.value?.data?.patientDetails?.isConfirmDiagnosis == true)
                viewModel.medicalReviewSummaryResponse.value?.data?.patientDetails?.confirmDiagnosis
            else viewModel.patientDetailsResponse.value?.data?.confirmDiagnosis

        medicalReviewBaseViewModel.medicalReviewEditModel.let { data ->
            val result = viewModel.canShowDiagnosisAlert(
                data.initialMedicalReview?.diagnosis,
                confirmedDiagnosisList)
            if (result.second) {
                PrescriptionDueDialog.newInstance(
                    if (result.first == 1) getString(R.string.edit_confirm_diagnosis_mandatory_warning) else
                        getString(R.string.no_confirm_diagnosis_mandatory_warning),
                    this, true
                ).show(childFragmentManager, PrescriptionDueDialog.TAG)
            } else if (binding.summaryDetails.btnMedicationPrescribed.visibility == View.VISIBLE && binding.summaryDetails.btnConfirmDiagnosis.visibility == View.VISIBLE) {
                PrescriptionDueDialog.newInstance(
                    getString(R.string.no_confirm_diagnosis_prescribed_medication_warning),
                    this
                )
                    .show(childFragmentManager, PrescriptionDueDialog.TAG)
            } else if (binding.summaryDetails.btnMedicationPrescribed.visibility == View.VISIBLE) {
                PrescriptionDueDialog.newInstance(
                    getString(R.string.no_new_medicines_prescribed_warning),
                    this
                )
                    .show(childFragmentManager, PrescriptionDueDialog.TAG)
            } else if (binding.summaryDetails.btnConfirmDiagnosis.visibility == View.VISIBLE) {
                PrescriptionDueDialog.newInstance(
                    getString(R.string.no_confirm_diagnosis_warning),
                    this
                )
                    .show(childFragmentManager, PrescriptionDueDialog.TAG)
            } else {
                uploadSignature()
            }
        }
    }


    private fun loadData(response: SummaryResponse) {
        viewModel.isSummaryDetailsLoaded = true
        response.patientDetails?.let { patient ->
            if (patient.isConfirmDiagnosis) {
                if (SecuredPreference.getIsTranslationEnabled()){
                    binding.summaryDetails.tvDiagnosisDescription.text =
                        getListAsText(getTranslatedDiagnosisList(patient.confirmDiagnosis))
                }else{
                    binding.summaryDetails.tvDiagnosisDescription.text =
                        getListAsText(patient.confirmDiagnosis)
                }
                binding.summaryDetails.btnConfirmDiagnosis.visibility = View.INVISIBLE
            } else {
                binding.summaryDetails.btnConfirmDiagnosis.visibility = View.VISIBLE
                binding.summaryDetails.tvDiagnosisDescription.text =
                    getListAsText(patient.provisionalDiagnosis)
            }
        }
        response.reviewerDetails?.let {
            binding.tvClinicianNameDescription.text = "${it.firstName} ${it.lastName}"
        }
        if (response.medicalReviews.size > 0) {
            val model = response.medicalReviews[response.medicalReviews.size - 1]
            renderMedicalReviewDetails(model)
        }
        renderFirstContinuousDetails()
        renderPrescriptionsDetails(response.prescriptions)
        renderLabTestDetails(response.investigations)

        if (response.medicalReviewFrequency.isNullOrBlank()) {
            binding.summaryDetails.btnTreatmentPlan.visibility = View.VISIBLE
            binding.summaryDetails.tvNextMedicalReviewDesc.text =
                getString(R.string.separator_hyphen)
        } else {
            binding.summaryDetails.btnTreatmentPlan.visibility = View.INVISIBLE
            binding.summaryDetails.tvNextMedicalReviewDesc.text = response.medicalReviewFrequency
        }
    }

    private fun getTranslatedDiagnosisList(confirmDiagnosis: ArrayList<String>?): ArrayList<String> {
        val translatedList = ArrayList<String>()
         confirmDiagnosis?.let { list ->
             medicalReviewBaseViewModel.diagnosisList.value?.let { diagnosisListEntity ->
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

    private fun renderFirstContinuousDetails() {
        binding.tvDateOfReviewDescription.text = DateUtils.convertDateTimeToDate(
            DateUtils.getTodayDateDDMMYYYY(),
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            DateUtils.DATE_FORMAT_ddMMMyyyy
        )

        if (SecuredPreference.getIsTranslationEnabled()){
            binding.summaryDetails.tvChiefComplaintsDescription.text =
                getListAsText(
                    getTranslatedComplaintList(medicalReviewBaseViewModel.complaintsList),
                    medicalReviewBaseViewModel.medicalReviewEditModel.continuousMedicalReview?.complaintComments
                )
        }else{
            binding.summaryDetails.tvChiefComplaintsDescription.text =
                getListAsText(
                    medicalReviewBaseViewModel.complaintsList,
                    medicalReviewBaseViewModel.medicalReviewEditModel.continuousMedicalReview?.complaintComments
                )
        }

        binding.summaryDetails.tvClinicalNotesDescription.text =
            requireContext().getString(R.string.separator_hyphen)
        medicalReviewBaseViewModel.medicalReviewEditModel.continuousMedicalReview?.clinicalNote?.let {
            if (it.isNotEmpty()) {
                binding.summaryDetails.tvClinicalNotesDescription.text = it
            }
        }

        if (SecuredPreference.getIsTranslationEnabled()){
            binding.summaryDetails.tvPhysicalDescription.text =
                getListAsText(
                    getTranslatedPhysicalExamList(medicalReviewBaseViewModel.physicalExamsList),
                    medicalReviewBaseViewModel.medicalReviewEditModel.continuousMedicalReview?.physicalExamComments
                )
        }else{
            binding.summaryDetails.tvPhysicalDescription.text =
                getListAsText(
                    medicalReviewBaseViewModel.physicalExamsList,
                    medicalReviewBaseViewModel.medicalReviewEditModel.continuousMedicalReview?.physicalExamComments
                )
        }

    }

    private fun getTranslatedPhysicalExamList(physicalExamsList: ArrayList<String>): ArrayList<String> {
        val translatedList = ArrayList<String>()
        medicalReviewBaseViewModel.physicalExaminationResponse.value?.let { exam ->
            physicalExamsList.forEach { examName ->
                val model =  exam.find { it.name.equals(examName,true) }
                if (model?.cultureValue != null)
                    translatedList.add(model.cultureValue)
                else
                    translatedList.add(examName)
            }
        }
        return translatedList
    }

    private fun getTranslatedComplaintList(
        complaintsList: ArrayList<String>): ArrayList<String> {
        val translatedList = ArrayList<String>()
        medicalReviewBaseViewModel.chiefCompliants.value?.let { chiefComplaints ->
            complaintsList.forEach { complaints ->
              val model =  chiefComplaints.find { it.name.equals(complaints,true) }
              if (model?.cultureValue != null)
                  translatedList.add(model.cultureValue)
              else
                  translatedList.add(complaints)
            }
        }
        return translatedList
    }

    private fun renderLabTestDetails(investigations: ArrayList<LabTestHistory>) {
        if (investigations.isEmpty()) {
            binding.summaryDetails.rvLabTest.visibility = View.GONE
            binding.summaryDetails.btnLabTest.visibility = View.VISIBLE
        } else {
            binding.summaryDetails.rvLabTest.visibility = View.VISIBLE
            binding.summaryDetails.btnLabTest.visibility = View.GONE
            binding.summaryDetails.rvLabTest.adapter = BulletPointsAdapter(investigations)
        }
    }

    private fun renderMedicalReviewDetails(medicalReview: MedicalReview) {
        medicalReviewBaseViewModel.medicalReViewRequest?.let { request ->
            request.medicalReviewId = medicalReview._id
        }
    }

    private fun renderPrescriptionsDetails(prescriptions: ArrayList<SummaryPrescription>?) {
        if (prescriptions.isNullOrEmpty()) {
            binding.summaryDetails.tvMedicationPrescribed.visibility = View.VISIBLE
            binding.summaryDetails.rvPrescription.visibility = View.GONE
            binding.summaryDetails.btnMedicationPrescribed.visibility = View.VISIBLE
        } else {
            binding.summaryDetails.tvMedicationPrescribed.visibility = View.GONE
            binding.summaryDetails.rvPrescription.visibility = View.VISIBLE
            binding.summaryDetails.btnMedicationPrescribed.visibility = View.INVISIBLE
            binding.summaryDetails.rvPrescription.adapter =
                BulletPointPrescriptionAdapter(prescriptions)
        }
    }

    private fun setAdapterViews() {
        val layoutManager = FlexboxLayoutManager(context)
        binding.summaryDetails.rvPrescription.layoutManager = layoutManager
        val flexManager = FlexboxLayoutManager(context)
        binding.summaryDetails.rvLabTest.layoutManager = flexManager
    }

    private fun getListAsText(list: ArrayList<String>?, comments: String? = null): String {
        val resultStringBuilder = StringBuilder()
        list?.let {
            if (list.isNotEmpty()) {
                list.forEachIndexed { index, generalModel ->
                    resultStringBuilder.append(generalModel)
                    if (index != list.size - 1) {
                        resultStringBuilder.append(getString(R.string.comma_symbol))
                    }
                }
            } else {
                resultStringBuilder.append(getString(R.string.separator_hyphen))
            }
            comments?.let { cmt ->
                resultStringBuilder.append(" [${cmt}]")
            }
        }
        return resultStringBuilder.toString()
    }

    private fun uploadSignature() {
        if (connectivityManager.isNetworkAvailable()) {
            if (viewModel.isSummaryDetailsLoaded)
                medicalReviewBaseViewModel.createContinuousMedicalReview(requireContext(),medicalReviewBaseViewModel.medicalReviewEditModel)
            else {
                (activity as BaseActivity).showAlertWith(
                    getString(R.string.pull_down_refresh_message),
                    getString(R.string.refresh),
                    false
                ) {
                    if (it)
                        apiCalls()
                }
            }
        } else {
            (activity as BaseActivity).showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error),
                false
            ) {}
        }
    }

    /**
     * attach listener to view
     */
    private fun setListeners() {
        binding.summaryDetails.btnConfirmDiagnosis.safeClickListener(this)
        binding.summaryDetails.btnMedicationPrescribed.safeClickListener(this)
        binding.summaryDetails.btnTreatmentPlan.safeClickListener(this)
        viewModel.isSummaryDetailsLoaded = false
    }

    /**
     * listener for view on click events
     * @param view respected view clicked
     */
    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnConfirmDiagnosis -> {
                performConfirmDiagnosis()
            }
            R.id.btnMedicationPrescribed -> {
                val intent = Intent(context, PrescriptionCopyActivity::class.java)
                intent.putExtra(IntentConstants.IntentPatientID, viewModel.patientId)
                intent.putExtra(IntentConstants.IntentVisitId, viewModel.patientVisitId)
                intent.putExtra(IntentConstants.isFromSummaryPage, true)
                resultLauncher.launch(intent)
            }
            R.id.btnTreatmentPlan -> {
                if (connectivityManager.isNetworkAvailable())
                    TreatmentPlanDialog().show(childFragmentManager, TreatmentPlanDialog.TAG)
                else
                    (context as BaseActivity).showErrorDialogue(
                        getString(R.string.error),
                        getString(R.string.no_internet_error),
                        false,
                        ){}
            }
        }
    }

    /**
     * PrescriptionDueListener
     */
    override fun submitMedicalReview() {
        uploadSignature()
    }

    override fun performConfirmDiagnosis() {
        ConfirmDiagnosisDialog.newInstance(true)
            .show(childFragmentManager, ConfirmDiagnosisDialog.TAG)
    }

    fun validateValues() {
        handleValues()
    }

    companion object {
        const val TAG = "MedicalSummaryFragment"
    }
}