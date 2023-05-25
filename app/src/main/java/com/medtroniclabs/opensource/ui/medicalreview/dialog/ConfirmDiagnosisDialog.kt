package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.databinding.FragmentConfirmDiagnosisBinding
import com.medtroniclabs.opensource.db.tables.DiagnosisEntity
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.Hypertension
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.TagListCustomView
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.MedicalReviewBaseViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel

class ConfirmDiagnosisDialog() : DialogFragment(), View.OnClickListener {

    private var isSummary: Boolean = false
    private lateinit var binding: FragmentConfirmDiagnosisBinding
    private lateinit var tagListCustomView: TagListCustomView
    private var patientDetails: PatientDetailsModel? = null
    private val medicalReviewBaseViewModel: MedicalReviewBaseViewModel by activityViewModels()
    private val viewModel: PatientDetailViewModel by activityViewModels()

    companion object {
        const val TAG = "ConfirmDiagnosis"
        const val IS_SUMMARY = "IS_SUMMARY"
        fun newInstance(isSummary: Boolean): ConfirmDiagnosisDialog {
            val fragment = ConfirmDiagnosisDialog()
            fragment.arguments = Bundle().apply {
                putBoolean(IS_SUMMARY, isSummary)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConfirmDiagnosisBinding.inflate(inflater, container, false)

        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.CENTER
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.setDecorFitsSystemWindows(false)
        }
        binding.root.setOnApplyWindowInsetsListener { _, windowInsets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val imeHeight = windowInsets.getInsets(WindowInsets.Type.ime()).bottom
                binding.root.setPadding(0, 0, 0, imeHeight)
                windowInsets.getInsets(WindowInsets.Type.ime() or WindowInsets.Type.systemGestures())
            }
            windowInsets
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readArguments()
        viewModel.patientDetailsResponse.value?.data?.let { details ->
            val list = arrayListOf((getString(R.string.pregnancy_diabetes_text)).uppercase())
            if (!SecuredPreference.isPHQ4Enabled()) {
                list.add(getString(R.string.mental_health_text).uppercase())
            }
            val genderList = arrayListOf((details.gender!!).uppercase(), getString(R.string.both))
            medicalReviewBaseViewModel.getConfirmDiagnosisList(genderList, list)
            binding.etCommentDiagnosis.setText(
                details.diagnosisComments ?: ""
            )
        }
        setListeners()
        initializeTagView()
        attachObserver()
    }

    private fun readArguments() {
        arguments?.let {
            isSummary = it.getBoolean(IS_SUMMARY, false)
        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setListeners() {
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnConfirm.safeClickListener(this)
    }

    private fun initializeTagView() {
        tagListCustomView = TagListCustomView(requireContext(), binding.cgDiagnosis) { _ ->
            enableConfirm()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.labelHeader.ivClose.id -> {
                dismiss()
            }
            R.id.btnCancel -> {
                dismiss()
            }
            R.id.btnConfirm -> {
                saveDiagnosis()
            }
        }
    }

    private fun enableConfirm() {
        val selectedTag = tagListCustomView.getSelectedTags()
        val list: ArrayList<String> = ArrayList()
        selectedTag.forEach {
            if (it is DiagnosisEntity) {
                list.add(it.diagnosis)
            }
        }
        viewModel.confirmDiagnosisRequestData.confirmDiagnosis = list
    }

    private fun attachObserver() {

        viewModel.patientDetailsResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    resourceState.data?.let {
                        patientDetails = it
                    }
                }
                else -> {
                    //Invoked if response state is not success
                }
            }
        }

        medicalReviewBaseViewModel.diagnosisListResponse.observe(this) { responseList ->
            val list = if(patientDetails?.gender?.lowercase() != DefinedParams.Female.lowercase()){
                responseList.filter { !it.diagnosis.contains(DefinedParams.GestationalDiabetes) }
            } else responseList
            val diagnosisMap:HashMap<String, MutableList<String>>? = diagnosisGrouping(list)
            val selectedDiagnosis = ArrayList<String>()
            if (patientDetails?.confirmDiagnosis?.isNotEmpty() == true) {
                selectedDiagnosis.addAll(patientDetails?.confirmDiagnosis!!)
            }
            autoPopulateDialogue(selectedDiagnosis)?.let {
                selectedDiagnosis.addAll(it)
            }
            tagListCustomView.addChipItemList(list, selectedDiagnosis, diagnosisMap)
            enableConfirm()
        }

        viewModel.confirmDiagnosisRequest.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    dismiss()
                    if (isSummary) {
                        medicalReviewBaseViewModel.medicalReViewRequest?.let {
                            viewModel.getPatientMedicalReviewSummary(requireContext(),it, true)
                        }
                    } else {
                        viewModel.getPatientDetails(requireContext(),false)
                    }
                    viewModel.confirmDiagnosisRequest.value = Resource(ResourceState.ERROR)
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        (activity as BaseActivity).showErrorDialogue(getString(R.string.error),message, false){}
                        viewModel.confirmDiagnosisRequest.postError(null)
                    }
                }
            }
        }

        binding.etCommentDiagnosis.addTextChangedListener {
            if (it.isNullOrBlank()) {
                viewModel.confirmDiagnosisRequestData.diagnosisComments = null
            } else {
                viewModel.confirmDiagnosisRequestData.diagnosisComments = it.toString()
            }
        }
    }

    private fun autoPopulateDialogue(selectedDiagnosis: ArrayList<String>): ArrayList<String>? {
        if (isSummary){
            medicalReviewBaseViewModel.medicalReviewEditModel.initialMedicalReview?.diagnosis?.let { data ->
                if ((data.htnPatientType == DefinedParams.KNOWN)) {
                        selectedDiagnosis.add(Hypertension)
                }
                if (data.diabetesPatientType == DefinedParams.KNOWN) {
                    if(data.diabetesDiagControlledType == DefinedParams.PreDiabetic)
                    {
                        if(selectedDiagnosis.contains(DefinedParams.dmtOne))
                            selectedDiagnosis.remove(DefinedParams.dmtOne)
                        else if(selectedDiagnosis.contains(DefinedParams.dmtTwo))
                            selectedDiagnosis.remove(DefinedParams.dmtTwo)
                        else if(selectedDiagnosis.contains(DefinedParams.GestationalDiabetes))
                            selectedDiagnosis.remove(DefinedParams.GestationalDiabetes)

                        selectedDiagnosis.add(DefinedParams.PreDiabetic)
                    }
                    else
                    {
                        when (data.diabetesDiagnosis) {
                            DefinedParams.typeOne -> {
                                if(selectedDiagnosis.contains(DefinedParams.dmtTwo))
                                    selectedDiagnosis.remove(DefinedParams.dmtTwo)
                                else if(selectedDiagnosis.contains(DefinedParams.GestationalDiabetes))
                                    selectedDiagnosis.remove(DefinedParams.GestationalDiabetes)
                                else if(selectedDiagnosis.contains(DefinedParams.PreDiabetic))
                                    selectedDiagnosis.remove(DefinedParams.PreDiabetic)

                                selectedDiagnosis.add(DefinedParams.dmtOne)
                            }
                            DefinedParams.typeTwo -> {
                                if(selectedDiagnosis.contains(DefinedParams.dmtOne))
                                    selectedDiagnosis.remove(DefinedParams.dmtOne)
                                else if(selectedDiagnosis.contains(DefinedParams.GestationalDiabetes))
                                    selectedDiagnosis.remove(DefinedParams.GestationalDiabetes)
                                else if(selectedDiagnosis.contains(DefinedParams.PreDiabetic))
                                    selectedDiagnosis.remove(DefinedParams.PreDiabetic)

                                selectedDiagnosis.add(DefinedParams.dmtTwo)
                            }
                            DefinedParams.GestationalDiabetes -> {
                                if(selectedDiagnosis.contains(DefinedParams.dmtOne))
                                    selectedDiagnosis.remove(DefinedParams.dmtOne)
                                else if(selectedDiagnosis.contains(DefinedParams.dmtTwo))
                                    selectedDiagnosis.remove(DefinedParams.dmtTwo)
                                else if(selectedDiagnosis.contains(DefinedParams.PreDiabetic))
                                    selectedDiagnosis.remove(DefinedParams.PreDiabetic)

                                selectedDiagnosis.add(DefinedParams.GestationalDiabetes)
                            }
                            else -> {
                                data.diabetesDiagnosis?.let {
                                    selectedDiagnosis.add(it)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedDiagnosis.size > 0){
            return selectedDiagnosis
        }else{
            return null
        }
    }

    private fun diagnosisGrouping(list: List<DiagnosisEntity>?): HashMap<String, MutableList<String>>? {
        var diagnosisTypeAndValues = HashMap<String, MutableList<String>>()
        return list?.groupByTo(diagnosisTypeAndValues, {it.type.toString() }, { it.diagnosis })
    }

    private fun saveDiagnosis() {
        if (validateInputs()) {
            patientDetails?.let { patientDetails ->
                viewModel.confirmDiagnosisRequestData.patientTrackId = patientDetails._id
                SecuredPreference.getSelectedSiteEntity()?.let { _ ->
                    viewModel.confirmDiagnosisRequestData.tenantId = patientDetails.tenantId
                    viewModel.confirmDiagnosis(requireContext(),viewModel.confirmDiagnosisRequestData)
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValidInput = true
        if (viewModel.confirmDiagnosisRequestData.confirmDiagnosis == null || (viewModel.confirmDiagnosisRequestData.confirmDiagnosis != null && viewModel.confirmDiagnosisRequestData.confirmDiagnosis!!.size <= 0)) {
            binding.tvErrorMessage.visibility = View.VISIBLE
            isValidInput = false

        }
        return isValidInput
    }

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }
}