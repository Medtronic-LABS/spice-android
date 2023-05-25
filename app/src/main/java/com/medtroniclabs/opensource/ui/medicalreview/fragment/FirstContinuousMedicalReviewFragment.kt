package com.medtroniclabs.opensource.ui.medicalreview.fragment

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.touchObserver
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.databinding.FragmentFirstMedicalReviewBinding
import com.medtroniclabs.opensource.db.tables.ComplaintsEntity
import com.medtroniclabs.opensource.db.tables.PhysicalExaminationEntity
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.TagListCustomView
import com.medtroniclabs.opensource.ui.medicalreview.MedicalReviewBaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.MedicalReviewBaseViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel

class FirstContinuousMedicalReviewFragment : Fragment() {

    lateinit var binding: FragmentFirstMedicalReviewBinding

    private val viewModel: MedicalReviewBaseViewModel by activityViewModels()
    private val patientViewModel: PatientDetailViewModel by activityViewModels()

    private lateinit var chiefComplaintsTagListCustomView: TagListCustomView
    private lateinit var physicalExaminationTagListCustomView: TagListCustomView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFirstMedicalReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        getDataList()
        attachObserver()
        if (requireActivity() is MedicalReviewBaseActivity) {
            (requireActivity() as MedicalReviewBaseActivity).changeButtonTextToFinishMedicalReview(
                getString(R.string.next), getString(R.string.finish_review)
            )
        }
    }

    private fun attachObserver() {
        viewModel.physicalExaminationResponse.observe(viewLifecycleOwner) { list ->
            list?.let { examinations ->
                physicalExaminationTagListCustomView.addChipItemList(
                    examinations,
                    viewModel.physicalExamsList
                )
            }
        }

        viewModel.chiefCompliants.observe(viewLifecycleOwner) { list ->
            list?.let { complaints ->
                chiefComplaintsTagListCustomView.addChipItemList(
                    complaints,
                    viewModel.complaintsList
                )
            }
        }

        patientViewModel.patientDetailsResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    loadPatientInformation(resourceState.data)
                }
                else -> {
                    //Invoked if response state is not success
                }
            }
        }
    }

    private fun statusCodeValidation() {
        var errorView: View? = null
        childFragmentManager.findFragmentById(binding.comorbidityContainer.id)?.let { fragment ->
            if(fragment is ComorbiditiesAndComplicationsFragment)
            {
                val isValid = fragment.validateInputs()
                if(!isValid)
                    errorView = binding.comorbidityContainer
            }
        }
        if (binding.etClinicalNotes.text?.trim().isNullOrEmpty()) {
            binding.tvClinicalNoteErrorMessage.visibility = View.VISIBLE
            binding.tvClinicalNoteErrorMessage.text =
                getString(R.string.please_check_your_inputs)
            if(errorView == null)
                errorView = binding.cardClinicalNote
        }

        if(errorView == null)
        {
            binding.tvClinicalNoteErrorMessage.visibility = View.GONE
            if ((viewModel.medicalReviewEditModel.patientTrackId ?: 0) <= 0) {
                (context as BaseActivity).showAlertWith(
                    getString(R.string.pull_down_refresh_message),
                    getString(R.string.refresh),
                    false
                ) {
                    doRefresh()
                }
            } else {
                if(viewModel.ShowContinuousMedicalReview) {
                    viewModel.continuousMedicalRequest.comorbidities =
                        viewModel.initialEncounterRequest.comorbidities
                    viewModel.continuousMedicalRequest.complications =
                        viewModel.initialEncounterRequest.complications
                } else {
                    viewModel.continuousMedicalRequest.comorbidities = null
                    viewModel.continuousMedicalRequest.complications = null
                }
                viewModel.medicalReviewEditModel.continuousMedicalReview =
                    viewModel.continuousMedicalRequest
                (activity as MedicalReviewBaseActivity).showHideContainer(true)
            }
        }
        else scrollToView(errorView!!)
    }

    private fun doRefresh() {
        if (activity is MedicalReviewBaseActivity) {
            (activity as MedicalReviewBaseActivity).swipeRefresh(true)
        }
    }

    private fun scrollToView(view: View) {
        if (activity is MedicalReviewBaseActivity) {
            val baseActivity = (activity as MedicalReviewBaseActivity)
            val scrollView = baseActivity.getScrollview()
            scrollToView(scrollView, view)
        }
    }

    /**
     * Used to scroll to the given view.
     *
     * @param scrollViewParent Parent ScrollView
     * @param view View to which we need to scroll.
     */
    private fun scrollToView(scrollViewParent: NestedScrollView, view: View) {
        // Get deepChild Offset
        val childOffset = Point()
        getDeepChildOffset(scrollViewParent, view.parent, view, childOffset)
        // Scroll to child.
        scrollViewParent.smoothScrollTo(0, childOffset.y)
    }

    /**
     * Used to get deep child offset.
     *
     *
     * 1. We need to scroll to child in scrollview, but the child may not the direct child to scrollview.
     * 2. So to get correct child position to scroll, we need to iterate through all of its parent views till the main parent.
     *
     * @param mainParent        Main Top parent.
     * @param parent            Parent.
     * @param child             Child.
     * @param accumulatedOffset Accumulated Offset.
     */
    private fun getDeepChildOffset(
        mainParent: ViewGroup,
        parent: ViewParent,
        child: View,
        accumulatedOffset: Point
    ) {
        val parentGroup = parent as ViewGroup
        accumulatedOffset.x += child.left
        accumulatedOffset.y += child.top
        if (parentGroup == mainParent) {
            return
        }
        getDeepChildOffset(mainParent, parentGroup.parent, parentGroup, accumulatedOffset)
    }

    private fun loadPatientInformation(data: PatientDetailsModel?) {
        data?.apply {
            viewModel.medicalReviewEditModel.patientTrackId = _id
            SecuredPreference.getSelectedSiteEntity()?.apply {
                viewModel.medicalReviewEditModel.tenantId = tenantId
            }
            patientViewModel.patientVisitId?.let {
                viewModel.medicalReviewEditModel.patientVisitId = it
            }
        }
    }

    private fun initializeView() {
        if(viewModel.ShowContinuousMedicalReview)
        {
            binding.comorbidityContainer.visibility = View.VISIBLE
            childFragmentManager.commit {
                setReorderingAllowed(true)
                add<ComorbiditiesAndComplicationsFragment>(
                    binding.comorbidityContainer.id,
                    tag = ComorbiditiesAndComplicationsFragment.TAG
                )
            }
        }
        else
            binding.comorbidityContainer.visibility = View.GONE
        chiefComplaintsTagListCustomView =
            TagListCustomView(requireContext(), binding.tagviewChiefComplaints) { _ ->
                setSelectedComplaint()
            }
        physicalExaminationTagListCustomView =
            TagListCustomView(requireContext(), binding.tagPhysicalExamination) { _ ->
                setSelectedPhysicalExamination()
            }
        binding.tvClinicalNotesTitle.markMandatory()
        binding.etChiefComplaintsComments.touchObserver()
        binding.etClinicalNotes.touchObserver()
        binding.etPhysicalExaminationComments.touchObserver()
    }

    private fun setSelectedPhysicalExamination() {
        val selectedTag = physicalExaminationTagListCustomView.getSelectedTags()
        val list: ArrayList<Long> = ArrayList()
        val selecedExams: ArrayList<String> = ArrayList()
        selectedTag.forEach {
            if (it is PhysicalExaminationEntity) {
                list.add(it._id)
                selecedExams.add(it.name)
            }
        }
        viewModel.physicalExamsList = selecedExams
        viewModel.continuousMedicalRequest.physicalExams = list
    }

    private fun setSelectedComplaint() {
        val selectedTag = chiefComplaintsTagListCustomView.getSelectedTags()
        val list: ArrayList<Long> = ArrayList()
        val selecedName: ArrayList<String> = ArrayList()
        selectedTag.forEach {
            if (it is ComplaintsEntity) {
                list.add(it._id)
                selecedName.add(it.name)
            }
        }
        viewModel.complaintsList = selecedName
        viewModel.continuousMedicalRequest.complaints = list
    }

    private fun getDataList() {
        viewModel.getPhysicalExaminationEntity()
        viewModel.getComplaints()
        prefillComments()

        binding.etChiefComplaintsComments.addTextChangedListener { chiefComplaints ->
            viewModel.continuousMedicalRequest.complaintComments =
                if (chiefComplaints.isNullOrBlank()) null else chiefComplaints.trim().toString()
        }

        binding.etClinicalNotes.addTextChangedListener { clinicalNotes ->
            viewModel.continuousMedicalRequest.clinicalNote =
                if (clinicalNotes.isNullOrBlank()) null else clinicalNotes.trim().toString()
        }

        binding.etPhysicalExaminationComments.addTextChangedListener { physicalExamination ->
            viewModel.continuousMedicalRequest.physicalExamComments =
                if (physicalExamination.isNullOrBlank()) null else physicalExamination.trim()
                    .toString()
        }
    }

    private fun prefillComments() {
        viewModel.continuousMedicalRequest.complaintComments?.let { complaints ->
            binding.etChiefComplaintsComments.setText(complaints)
        }
        viewModel.continuousMedicalRequest.clinicalNote?.let { note ->
            binding.etClinicalNotes.setText(note)
        }
        viewModel.continuousMedicalRequest.physicalExamComments?.let { physicalExam ->
            binding.etPhysicalExaminationComments.setText(physicalExam)
        }
    }

    fun validateValues() {
        statusCodeValidation()
    }

    companion object
    {
        const val TAG = "FirstContinuousMedicalReviewFragment"
    }

}