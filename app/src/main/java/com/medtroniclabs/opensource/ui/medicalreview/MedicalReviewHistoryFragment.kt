package com.medtroniclabs.opensource.ui.medicalreview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.MedicalReview
import com.medtroniclabs.opensource.data.model.MedicalReviewBaseRequest
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.data.model.SummaryResponse
import com.medtroniclabs.opensource.data.model.VisitDateModel
import com.medtroniclabs.opensource.databinding.FragmentMedicalReviewHistoryBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.medicalreview.adapter.BulletPointsAdapter
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.MedicalReviewPatientHistoryViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel

class MedicalReviewHistoryFragment : BaseFragment(), View.OnClickListener, DateSelectionListener {
    private lateinit var binding: FragmentMedicalReviewHistoryBinding
    private val patientViewModel: PatientDetailViewModel by activityViewModels()
    val viewModel: MedicalReviewPatientHistoryViewModel by activityViewModels()
    var listPopupWindow: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMedicalReviewHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getArgumentValues()
        setAdapterViews()
        setListeners()
        attachObserver()
    }

    private fun getArgumentValues() {
        viewModel.isFromCMR = arguments?.getBoolean(IntentConstants.isFromCMR) ?: false
        viewModel.origin = arguments?.getString(DefinedParams.Origin)
        viewModel.isMedicalReviewSummary = false
        arguments?.let {
            if (it.containsKey(IntentConstants.isMedicalReviewSummary))
                viewModel.isMedicalReviewSummary =
                    it.getBoolean(IntentConstants.isMedicalReviewSummary, false)
        }
    }

    private fun setAdapterViews() {
        val layoutManager = FlexboxLayoutManager(context)
        binding.tvMedication.layoutManager = layoutManager
        val labTestManager = FlexboxLayoutManager(context)
        binding.tvLabTest.layoutManager = labTestManager
    }

    private fun attachObserver() {
        patientViewModel.patientDetailsResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.CenterProgress.visibility = View.VISIBLE
                }
                ResourceState.SUCCESS -> {
                    getMedicalReviewHistory(resourceState.data)
                }
                ResourceState.ERROR -> {
                    binding.CenterProgress.visibility = View.GONE
                }
            }
        }
        viewModel.patientMedicalHistoryListResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.cardMedicalReviewHistory.visibility = View.GONE
                    binding.CenterProgress.visibility = View.VISIBLE
                    binding.ivRefresh.visibility = View.GONE
                }
                ResourceState.SUCCESS -> {
                    binding.cardMedicalReviewHistory.visibility = View.VISIBLE
                    binding.ivRefresh.visibility = View.GONE
                    binding.CenterProgress.visibility = View.GONE
                    loadReviewData(resourceState.data, true)
                }
                ResourceState.ERROR -> {
                    binding.cardMedicalReviewHistory.visibility = View.GONE
                    binding.CenterProgress.visibility = View.GONE
                    binding.ivRefresh.visibility = View.VISIBLE
                }
            }
        }
        viewModel.patientMedicalHistoryListResponseByID.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.cardMedicalReviewHistory.visibility = View.GONE
                    binding.CenterProgress.visibility = View.VISIBLE
                    binding.ivRefresh.visibility = View.GONE
                }
                ResourceState.SUCCESS -> {
                    binding.cardMedicalReviewHistory.visibility = View.VISIBLE
                    binding.CenterProgress.visibility = View.GONE
                    binding.ivRefresh.visibility = View.GONE
                    loadReviewData(resourceState.data, false)
                }
                ResourceState.ERROR -> {
                    binding.ivRefresh.visibility = View.VISIBLE
                    binding.cardMedicalReviewHistory.visibility = View.GONE
                    binding.CenterProgress.visibility = View.GONE
                }
            }
        }

        viewModel.reviewHistoryList.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.cardMedicalReviewHistory.visibility = View.GONE
                    binding.CenterProgress.visibility = View.VISIBLE
                    binding.ivRefresh.visibility = View.GONE
                }
                ResourceState.SUCCESS -> {
                    binding.cardMedicalReviewHistory.visibility = View.VISIBLE
                    binding.CenterProgress.visibility = View.GONE
                    binding.ivRefresh.visibility = View.GONE
                    resourceState.data?.let {
                        val request = SummaryResponse(
                            medicalReviews = it.patientMedicalReview,
                            medicalReviewDates = it.patientReviewDates,
                            patientDetails = null,
                            reviewerDetails = null,
                            prescriptions = ArrayList(),
                            investigations = ArrayList()
                        )
                        loadReviewData(request, it.canUpdateDate)
                    }
                }
                ResourceState.ERROR -> {
                    binding.ivRefresh.visibility = View.VISIBLE
                    binding.cardMedicalReviewHistory.visibility = View.GONE
                    binding.CenterProgress.visibility = View.GONE
                }
            }
        }
    }

    private fun loadReviewData(data: SummaryResponse?, updateDate: Boolean) {
        data?.apply {
            if (medicalReviews.isNullOrEmpty() && prescriptions.isNullOrEmpty() && investigations.isNullOrEmpty()) {
                binding.tvEmptyInformation.visibility = View.VISIBLE
                binding.cardMedicalReviewHistory.visibility = View.GONE
            } else {
                binding.tvEmptyInformation.visibility = View.GONE
                canUpdateDate(updateDate, medicalReviewDates)
                binding.tvComplaints.text = getComplaints(medicalReviews)
                binding.tvExaminations.text = getExaminations(medicalReviews)
                binding.tvNotes.text = getClinicalNotes(medicalReviews)
                binding.ivPrevious.isEnabled = checkForPreviousItem() != -1
                binding.ivNext.isEnabled = checkNextItem() != -1
                if (!reviewedAt.isNullOrBlank()) {
                    binding.tvDate.text = DateUtils.convertDateTimeToDate(
                        reviewedAt,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DateUtils.DATE_FORMAT_ddMMMyyyy
                    )
                } else {
                    if (!medicalReviews.isNullOrEmpty() && !medicalReviews[0].reviewedAt.isNullOrBlank()) {
                        binding.tvDate.text = DateUtils.convertDateTimeToDate(
                            medicalReviews[0].reviewedAt,
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                            DateUtils.DATE_FORMAT_ddMMMyyyy
                        )
                    } else {
                        binding.tvDate.text = getString(R.string.separator_hyphen)
                    }
                }

                renderPrescriptionDetails(this)
                renderInvestigationDetails(this)
            }
        } ?: kotlin.run {
            binding.tvEmptyInformation.visibility = View.VISIBLE
            binding.cardMedicalReviewHistory.visibility = View.GONE
        }
    }

    private fun canUpdateDate(updateDate: Boolean, medicalReviewDates: ArrayList<VisitDateModel>?) {
        if (updateDate) {
            medicalReviewDates?.let { reviewDates ->
                if (reviewDates.isNotEmpty()) {
                    viewModel.SelectedPatientMedicalReviewId =
                        reviewDates[reviewDates.size - 1]._id
                    loadDatesMenu(reviewDates)
                }
            }
        } else {
            val view =
                listPopupWindow?.contentView?.findViewById<RecyclerView>(R.id.rvDateList)
            if (view != null && view.adapter is DateListAdapter) {
                getMedicalReviewHistoryModifiedResponse()?.let {
                    it.medicalReviewDates?.let { reviewDates ->
                        view.adapter = DateListAdapter(
                            reviewDates,
                            viewModel.SelectedPatientMedicalReviewId,
                            this@MedicalReviewHistoryFragment
                        )
                    }
                }
            }
        }
    }

    private fun renderInvestigationDetails(summaryResponse: SummaryResponse) {
        binding.tvLabTestLbl.visibility = View.GONE
        binding.tvSeparator6.visibility = View.GONE
        binding.tvNoLabTest.visibility = View.GONE
        binding.tvLabTest.visibility = View.GONE
        if (viewModel.isFromCMR) {
            binding.tvLabTestLbl.visibility = View.VISIBLE
            binding.tvSeparator6.visibility = View.VISIBLE
            if (summaryResponse.investigations.isNullOrEmpty())
                binding.tvNoLabTest.visibility = View.VISIBLE
            else {
                binding.tvLabTest.visibility = View.VISIBLE
                binding.tvLabTest.adapter = BulletPointsAdapter(summaryResponse.investigations)
            }
        }
    }

    private fun getClinicalNotes(patientMedicalReview: ArrayList<MedicalReview>): String {
        val resultStringBuilder = StringBuilder()
        if (patientMedicalReview.isNotEmpty()) {
            patientMedicalReview.forEachIndexed { count, list ->
                list.clinicalNote?.let { clinicalNote ->
                    if (clinicalNote.isNotEmpty()) {
                        resultStringBuilder.append(clinicalNote)
                    }
                    if (count != patientMedicalReview.size - 1) {
                        patientMedicalReview.get(count + 1).clinicalNote?.let {
                            if (it.isNotEmpty()) {
                                resultStringBuilder.append(getString(R.string.comma_symbol))
                            }
                        }
                    }
                }
            }
        }
        return resultStringBuilder.toString().ifEmpty { getString(R.string.separator_hyphen) }
    }

    private fun renderPrescriptionDetails(summaryResponse: SummaryResponse) {
        binding.tvMedicationLbl.visibility = View.GONE
        binding.tvSeparator5.visibility = View.GONE
        binding.tvNoMedication.visibility = View.GONE
        binding.tvMedication.visibility = View.GONE
        if (viewModel.isFromCMR) {
            binding.tvMedicationLbl.visibility = View.VISIBLE
            binding.tvSeparator5.visibility = View.VISIBLE
            if (summaryResponse.prescriptions.isNullOrEmpty())
                binding.tvNoMedication.visibility = View.VISIBLE
            else {
                binding.tvMedication.visibility = View.VISIBLE
                binding.tvMedication.adapter =
                    BulletPointPrescriptionAdapter(summaryResponse.prescriptions!!)
            }
        }
    }

    private fun getExaminations(patientMedicalReview: ArrayList<MedicalReview>): String {
        val resultStringBuilder = StringBuilder()
        if (patientMedicalReview.isNotEmpty()) {
            patientMedicalReview.forEachIndexed { count, list ->
                list.physicalExams?.let { physicalExams ->
                    val localExams = physicalExams
                    if (localExams.isNotEmpty()) {
                        val isTranslationEnabled = SecuredPreference.getIsTranslationEnabled()
                        localExams.forEachIndexed { index, generalModel ->
                            if (isTranslationEnabled){
                                resultStringBuilder.append(generalModel.cultureValue?:generalModel.name)
                            }else {
                                resultStringBuilder.append(generalModel.name)
                            }
                            getAppendString(index != localExams.size - 1)?.let { append ->
                                resultStringBuilder.append(getString(R.string.comma_symbol))
                            }
                        }
                    }
                    list.physicalExamComments?.let { comments ->
                        resultStringBuilder.append(" [${comments}]")
                    }
                    if (count != patientMedicalReview.size - 1) {
                        patientMedicalReview.get(count + 1).physicalExams?.let {
                            val condition = it.isNotEmpty() && resultStringBuilder.isNotEmpty()
                            getAppendString(condition)?.let { append ->
                                resultStringBuilder.append(append)
                            }
                        }
                    }
                }
            }
        }
        return resultStringBuilder.toString().ifEmpty { getString(R.string.separator_hyphen) }
    }

    private fun getAppendString(condition: Boolean): String? {
        return if (condition) getString(R.string.comma_symbol) else null
    }

    private fun getComplaints(patientMedicalReview: ArrayList<MedicalReview>): String {
        val resultStringBuilder = StringBuilder()
        if (patientMedicalReview.isNotEmpty()) {
            patientMedicalReview.forEachIndexed { count, list ->
                list.complaints?.let { complaints ->
                    val localComplaints = complaints
                    if (localComplaints.isNotEmpty()) {
                        val isTranslationEnabled = SecuredPreference.getIsTranslationEnabled()
                        localComplaints.forEachIndexed { index, generalModel ->
                            if (isTranslationEnabled){
                                resultStringBuilder.append(generalModel.cultureValue?:generalModel.name)
                            }else{
                                resultStringBuilder.append(generalModel.name)
                            }
                            getAppendString(index != localComplaints.size - 1)?.let { append ->
                                resultStringBuilder.append(append)
                            }
                        }
                    }

                    list.complaintComments?.let { comments ->
                        resultStringBuilder.append(" [${comments}]")
                    }
                    if (count != patientMedicalReview.size - 1) {
                        patientMedicalReview.get(count + 1).complaints?.let {
                            val condition = it.isNotEmpty() && resultStringBuilder.isNotEmpty()
                            getAppendString(condition)?.let { append ->
                                resultStringBuilder.append(getString(R.string.comma_symbol))
                            }
                        }
                    }
                }
            }
        }
        return resultStringBuilder.toString().ifEmpty { getString(R.string.separator_hyphen) }
    }

    private fun getMedicalReviewHistory(data: PatientDetailsModel?) {
        data?.let { patient ->
            SecuredPreference.getSelectedSiteEntity()?.let {
                val request = MedicalReviewBaseRequest(
                    patientTrackId = patient._id,
                    tenantId = it.tenantId,
                    isLatestRequired = true,
                    isMedicalReviewSummary = viewModel.isMedicalReviewSummary
                )
                if (viewModel.isMedicalReviewSummary)
                    viewModel.getPatientMedicalReviewHistoryList(request)
                else
                    viewModel.getPatientMedicalHistory(request)
            }
        }
    }

    private fun setListeners() {
        binding.ivChoose.safeClickListener(this)
        binding.ivNext.safeClickListener(this)
        binding.ivPrevious.safeClickListener(this)
        binding.ivRefresh.safeClickListener(this)
    }


    override fun onClick(mView: View?) {
        when (mView) {
            binding.ivChoose -> {
                listPopupWindow?.isOutsideTouchable = true
                listPopupWindow?.isFocusable = true
                listPopupWindow?.showAsDropDown(binding.ivChoose)
            }
            binding.ivNext -> {
                getNextItemToCurrent()
            }
            binding.ivPrevious -> {
                getPreviousItemToCurrent()
            }
            binding.ivRefresh -> {
                doRefresh()
            }
        }
    }

    private fun doRefresh() {
        patientViewModel.patientDetailsResponse.value?.data?.let { patient ->
            SecuredPreference.getSelectedSiteEntity()?.let {
                if (viewModel.SelectedPatientMedicalReviewId == null) {
                    val request = MedicalReviewBaseRequest(
                        patientTrackId = patient._id,
                        tenantId = it.tenantId,
                        patientVisitId = viewModel.SelectedPatientMedicalReviewId
                    )
                    if (viewModel.isMedicalReviewSummary)
                        viewModel.getPatientMedicalReviewHistoryList(request)
                    else
                        viewModel.getPatientMedicalHistory(request)
                } else {
                    val request = MedicalReviewBaseRequest(
                        patientTrackId = patient._id,
                        tenantId = it.tenantId,
                        isLatestRequired = true
                    )

                    if (viewModel.isMedicalReviewSummary)
                        viewModel.getPatientMedicalReviewHistoryList(request)
                    else
                        viewModel.getPatientMedicalHistory(request)
                }
            }
        }
    }

    private fun loadDatesMenu(dates: ArrayList<VisitDateModel>) {
        val inflater =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_popup_window, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvDateList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )
        val adapter = DateListAdapter(dates, viewModel.SelectedPatientMedicalReviewId, this)
        recyclerView.adapter = adapter
        listPopupWindow = PopupWindow(
            view,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDateSelected(_id: Long) {
        SecuredPreference.getSelectedSiteEntity()?.let { site ->
            patientViewModel.patientDetailsResponse.value?.data?.let { patientInfo ->
                viewModel.SelectedPatientMedicalReviewId = _id
                val request = MedicalReviewBaseRequest(
                    patientTrackId = patientInfo._id,
                    tenantId = site.tenantId,
                    _id
                )
                if (viewModel.isMedicalReviewSummary)
                    viewModel.getPatientMedicalReviewHistoryList(request, false)
                else
                    viewModel.getPatientMedicalHistoryByID(request)
            }
        }
        listPopupWindow?.dismiss()
    }

    private fun getPreviousItemToCurrent() {
        val selectedIndex = checkForPreviousItem()
        getMedicalReviewHistoryModifiedResponse()?.apply {
            medicalReviewDates?.let { reviewDates ->
                if (selectedIndex != -1) {
                    SecuredPreference.getSelectedSiteEntity()?.let { site ->
                        patientViewModel.patientDetailsResponse.value?.data?.let { patientInfo ->
                            viewModel.SelectedPatientMedicalReviewId =
                                reviewDates[selectedIndex]._id
                            val request = MedicalReviewBaseRequest(
                                patientTrackId = patientInfo._id,
                                tenantId = site.tenantId,
                                reviewDates[selectedIndex]._id
                            )
                            if (viewModel.isMedicalReviewSummary)
                                viewModel.getPatientMedicalReviewHistoryList(request, false)
                            else
                                viewModel.getPatientMedicalHistoryByID(request)
                        }
                    }
                }
            }
        }
    }

    private fun checkForPreviousItem(): Int {
        var selectedIndex = -1
        getMedicalReviewHistoryModifiedResponse()?.apply {
            medicalReviewDates?.let { reviewDates ->
                reviewDates.forEachIndexed { index, dateModel ->
                    if (dateModel._id == viewModel.SelectedPatientMedicalReviewId) {
                        selectedIndex = index - 1
                    }
                }
            }
        }
        return selectedIndex
    }

    private fun checkNextItem(): Int {
        var selectedIndex = -1
        getMedicalReviewHistoryModifiedResponse()?.apply {
            medicalReviewDates?.let { reviewDates ->
                reviewDates.forEachIndexed { index, dateModel ->
                    if (dateModel._id == viewModel.SelectedPatientMedicalReviewId && index + 1 < reviewDates.size) {
                        selectedIndex = index + 1
                    }
                }
            }
        }
        return selectedIndex
    }

    private fun getNextItemToCurrent() {
        val selectedIndex = checkNextItem()
        getMedicalReviewHistoryModifiedResponse()?.apply {
            if (selectedIndex != -1) {
                SecuredPreference.getSelectedSiteEntity()?.let { site ->
                    medicalReviewDates?.let { reviewDates ->
                        patientViewModel.patientDetailsResponse.value?.data?.let { patientInfo ->
                            viewModel.SelectedPatientMedicalReviewId =
                                reviewDates[selectedIndex]._id
                            val request = MedicalReviewBaseRequest(
                                patientTrackId = patientInfo._id,
                                tenantId = site.tenantId,
                                patientVisitId = reviewDates[selectedIndex]._id
                            )

                            if (viewModel.isMedicalReviewSummary)
                                viewModel.getPatientMedicalReviewHistoryList(request, false)
                            else
                                viewModel.getPatientMedicalHistoryByID(request)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        apiCalls()
    }

    private fun apiCalls() {
        patientViewModel.getPatientDetails(requireContext(),false)
    }

    fun getMedicalReviewHistoryModifiedResponse(): SummaryResponse? {
        var response: SummaryResponse? = null
        if (viewModel.isMedicalReviewSummary) {
            viewModel.reviewHistoryList.value?.data?.let {
                val request = SummaryResponse(
                    medicalReviews = it.patientMedicalReview,
                    medicalReviewDates = it.patientReviewDates,
                    patientDetails = null,
                    reviewerDetails = null,
                    prescriptions = ArrayList(),
                    investigations = ArrayList()
                )
                response = request
            }
        } else {
            response = viewModel.patientMedicalHistoryListResponse.value?.data
        }

        return response
    }
}