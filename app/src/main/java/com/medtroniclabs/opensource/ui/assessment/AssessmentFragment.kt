package com.medtroniclabs.opensource.ui.assessment

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import com.google.gson.JsonObject
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.dp
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.appextensions.textSizeSsp
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.common.ViewUtil
import com.medtroniclabs.opensource.custom.GeneralInfoDialog
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.custom.SecuredPreference.getUnitMeasurementType
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.data.ui.SymptomModel
import com.medtroniclabs.opensource.databinding.FragmentAssessmentBinding
import com.medtroniclabs.opensource.db.tables.MedicalComplianceEntity
import com.medtroniclabs.opensource.formgeneration.FormGenerator
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.calculateProvisionalDiagnosis
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.capitalize
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils.getMeasurementTypeValues
import com.medtroniclabs.opensource.formgeneration.listener.FormEventListener
import com.medtroniclabs.opensource.formgeneration.model.FormLayout
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.FormResultGenerator
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AssessmentFragment : BaseFragment(), View.OnClickListener, FormEventListener {

    lateinit var binding: FragmentAssessmentBinding

    private val viewModel: AssessmentViewModel by activityViewModels()
    private val patientDetailViewModel: PatientDetailViewModel by activityViewModels()

    private lateinit var formGenerator: FormGenerator

    private var isDOBNeedToChange: Boolean = true

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let { resultIntent ->
                    formGenerator.onSpinnerActivityResult(resultIntent)
                }
            }
        }

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            (activity as AssessmentHolderActivity).onHomeIconCheckClicked()
        }
        callback.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        initializeFormGenerator()
        attachObserver()
        loadListeners()
    }

    private fun loadSymptomsAndCompliance() {
        binding.symptomCard.root.visibility = View.VISIBLE
        binding.symptomCard.cardSymptomTitle.text = getString(R.string.symptoms)
        binding.symptomCard.symptomsSpinner.tvTitle.text = getString(R.string.since_the_last_assessment_symptom_info)
        binding.symptomCard.symptomsSpinner.etUserInput.setHintTextColor(requireContext().getColor(R.color.secondary_black))
        binding.symptomCard.symptomsSpinner.etUserInput.safeClickListener {
            SymptomsChooseDialog.newInstance()
                .show(childFragmentManager, SymptomsChooseDialog.TAG)
        }
        binding.symptomCard.complianceSpinner.tvTitle.text = getString(R.string.complaince_title)
        viewModel.getMedicationParentComplianceList()
        binding.symptomCard.complianceSpinner.etUserInput.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                    binding.symptomCard.complianceSpinner.etUserInput.adapter?.let { adapter ->
                        adapter as ComplianceSpinnerAdapter
                        adapter.getData(pos)?.let {
                            viewModel.selectedMedication.value = if (it._id > 0) it else null
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }

            }
        binding.symptomCard.otherHypertensionSymptom.tvTitle.text =
            getString(R.string.other_hypertension_symptom)
        binding.symptomCard.otherHypertensionSymptom.tvTitle.markMandatory()
        binding.symptomCard.otherDiabetesSymptom.tvTitle.text =
            getString(R.string.other_diabetes_symptom)
        binding.symptomCard.otherDiabetesSymptom.tvTitle.markMandatory()
    }

    private fun loadRadioButtons(
        list: List<MedicalComplianceEntity>,
        complianceRadioGroup: RadioGroup,
        parent: Int
    ) {
        complianceRadioGroup.removeAllViews()
        list.forEachIndexed { index, model ->
            val radioButton = RadioButton(complianceRadioGroup.context)
            radioButton.id = index
            radioButton.tag = model._id
            radioButton.setPadding(20.dp, 0, 20.dp, 0)
            val colorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(-android.R.attr.state_checked),
                    intArrayOf(android.R.attr.state_checked)
                ), intArrayOf(
                    ContextCompat.getColor(
                        complianceRadioGroup.context,
                        R.color.purple
                    ),  // disabled
                    ContextCompat.getColor(complianceRadioGroup.context, R.color.purple) // enabled
                )
            )

            radioButton.buttonTintList = colorStateList
            radioButton.invalidate()
            radioButton.textSizeSsp = DefinedParams.SSP16
            if (SecuredPreference.getIsTranslationEnabled()) {
                radioButton.text = model.cultureValue ?: model.name
            }else {
                radioButton.text = model.name
            }
            radioButton.layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
            radioButton.setTextColor(
                ContextCompat.getColor(
                    complianceRadioGroup.context,
                    R.color.navy_blue
                )
            )
            radioButton.typeface =
                ResourcesCompat.getFont(complianceRadioGroup.context, R.font.inter_regular)
            complianceRadioGroup.addView(radioButton)
        }

        complianceRadioGroup.setOnCheckedChangeListener { _, id ->
            if (id > -1) {
                val selectedModel = list[id]
                if (parent == 1) {
                    if (selectedModel.isChildExists) {
                        viewModel.getMedicationChildComplianceList(selectedModel._id)
                    } else hideComplianceOptions()

                    binding.symptomCard.otherComplianceReason.setText("")
                    binding.symptomCard.otherComplianceReason.visibility = View.GONE
                } else if (parent == 2) {
                    if (selectedModel.name.startsWith("other", true)) {
                        binding.symptomCard.otherComplianceReason.visibility = View.VISIBLE
                    } else {
                        binding.symptomCard.otherComplianceReason.setText("")
                        binding.symptomCard.otherComplianceReason.visibility = View.GONE
                    }
                }
                addOrRemoveValuesFromResultMap(selectedModel)
            }
        }
    }

    private fun hideComplianceOptions() {
        binding.symptomCard.childCompliance.clearCheck()
        binding.symptomCard.childCompliance.visibility = View.GONE
        binding.symptomCard.tvChildErrorMessage.visibility = View.GONE
    }

    private fun addOrRemoveValuesFromResultMap(
        selectedModel: MedicalComplianceEntity
    ) {
        val map: HashMap<String, Any>
        if (viewModel.complianceMap != null) {
            map = HashMap<String, Any>()
            if (selectedModel.parentComplianceId == null) {
                viewModel.complianceMap!!.clear()
            } else {
                val newList = ArrayList<HashMap<String, Any>>()
                viewModel.complianceMap!!.forEach { map ->
                    if (map.containsKey(DefinedParams.compliance_id) && map[DefinedParams.compliance_id] == selectedModel.parentComplianceId) {
                        newList.add(map)
                    }
                }
                viewModel.complianceMap!!.clear()
                viewModel.complianceMap!!.addAll(newList)
            }
            if (selectedModel.isChildExists) {
                map[DefinedParams.is_child_exists] = true
            }
            map[DefinedParams.compliance_id] = selectedModel._id
            map[DefinedParams.NAME] = selectedModel.name
            viewModel.complianceMap!!.add(map)
        } else {
            viewModel.complianceMap = ArrayList()
            map = HashMap<String, Any>()
            map[DefinedParams.compliance_id] = selectedModel._id
            map[DefinedParams.NAME] = selectedModel.name
            if (selectedModel.isChildExists) {
                map[DefinedParams.is_child_exists] = true
            }
            viewModel.complianceMap!!.add(map)
        }
    }

    private fun initializeView() {
        binding.symptomCard.otherHypertensionSymptom.etUserInput.hint =
            getString(R.string.type_your_comments)
        binding.symptomCard.otherDiabetesSymptom.etUserInput.hint =
            getString(R.string.type_your_comments)
        loadPatientEditView()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.llAge.etDateOfBirth.id -> {
                val yearMonthDate =
                    DateUtils.getYearMonthAndDate(binding.llAge.etDateOfBirth.text.toString())
                openDatePicker(
                    yearMonthDate.first,
                    yearMonthDate.second,
                    yearMonthDate.third
                )
            }
            binding.llAge.etAgeInYears.id -> {
                binding.llAge.etAgeInYears.text?.toString()?.let { age ->
                    if (age.isEmpty())
                        binding.llAge.etDateOfBirth.text = ""
                    else
                        binding.llAge.etDateOfBirth.text = DateUtils.getDOBFromAge(age)
                }
            }
        }
    }

    private fun loadPatientEditView() {
        binding.llName.tvTitle.text = getString(R.string.first_name)
        binding.llName.tvTitle.markMandatory()
        binding.llName.etUserInput.hint = getString(R.string.enter_first_name)
        binding.llName.ivConfirm.visibility = View.GONE
        binding.llName.tvErrorMessage.visibility = View.GONE

        binding.llLastName.tvTitle.text = getString(R.string.last_name)
        binding.llLastName.tvTitle.markMandatory()
        binding.llLastName.etUserInput.hint = getString(R.string.enter_last_name)
        binding.llLastName.ivConfirm.visibility = View.GONE
        binding.llLastName.tvErrorMessage.visibility = View.GONE

        binding.llGender.tvTitle.text = getString(R.string.gender)
        binding.llGender.tvTitle.markMandatory()
        binding.llGender.rgGroup.orientation = LinearLayout.HORIZONTAL
        binding.llAge.etAgeInYears.inputType = 2
        binding.llAge.tvAge.markMandatory()
        binding.llTobacco.tvTitle.text = getString(R.string.tobacoo_question)
        binding.llTobacco.tvTitle.markMandatory()
        binding.llTobacco.rgGroup.orientation = LinearLayout.HORIZONTAL
        val rbList = arrayListOf("Male", "Female", "Non-Binary")
        createRadioButtonGroup(rbList, binding.llGender.rgGroup)
        val rbTobaccoList = arrayListOf("Yes", "No")
        createRadioButtonGroup(rbTobaccoList, binding.llTobacco.rgGroup)
        binding.llAge.llDateOfBirth.visibility = View.GONE
        binding.llAge.spaceView.visibility = View.GONE
    }

    private fun createRadioButtonGroup(rbList: ArrayList<String>, rgGroup: RadioGroup) {
        rbList.forEach { rbText ->
            val radioButton = RadioButton(requireContext())
            radioButton.text = rbText
            radioButton.tag = rbText
            radioButton.setPadding(20.dp, 0, 20.dp, 0)
            val colorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(-android.R.attr.state_checked),
                    intArrayOf(android.R.attr.state_checked)
                ), intArrayOf(
                    requireContext().getColor(R.color.purple),
                    requireContext().getColor(R.color.purple)
                )
            )

            radioButton.buttonTintList = colorStateList
            radioButton.invalidate()
            radioButton.textSizeSsp = DefinedParams.SSP16
            radioButton.layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
            radioButton.setTextColor(requireContext().getColor(R.color.navy_blue))
            radioButton.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
            rgGroup.addView(radioButton)
        }
    }

    private fun initializeFormGenerator() {
        formGenerator =
            FormGenerator(
                requireContext(),
                binding.llForm,
                resultLauncher,
                this,
                binding.scrollView,
                SecuredPreference.getIsTranslationEnabled()
            )
        viewModel.fetchWorkFlow(UIConstants.AssessmentUniqueID,DefinedParams.Workflow)
        viewModel.getRiskEntityList()
    }

    override fun onFormSubmit(
        resultHashMap: HashMap<String, Any>,
        serverData: List<FormLayout?>?
    ) {

        val unitGenericType = getUnitMeasurementType()

        if (binding.llPatientEdit.visibility == View.VISIBLE) {
            if (validateAssessmentInput()) {
                if (binding.symptomCard.root.visibility == View.VISIBLE) {
                    validateSymptomsAndCompliance(resultHashMap, serverData, unitGenericType)
                } else {
                    saveAssessmentBaseValues(resultHashMap, serverData, unitGenericType)
                }

            }
        } else {
            if (binding.symptomCard.root.visibility == View.VISIBLE) {
                validateSymptomsAndCompliance(resultHashMap, serverData, unitGenericType)
            } else {
                saveAssessmentBaseValues(resultHashMap, serverData, unitGenericType)
            }
        }

    }

    private fun validateSymptomsAndCompliance(
        resultHashMap: HashMap<String, Any>,
        serverData: List<FormLayout?>?,
        unitGenericType: String
    ) {
        if (validateCompliance() && validateSymptom()) {
            if (viewModel.complianceMap != null) {
                resultHashMap[DefinedParams.compliances] = viewModel.complianceMap!!
            }
            if (!viewModel.selectedSymptoms.value.isNullOrEmpty()) {
                resultHashMap[DefinedParams.symptoms] =
                    getSymptomsList(viewModel.selectedSymptoms.value!!)
            }
            saveAssessmentBaseValues(resultHashMap, serverData, unitGenericType)
        }
    }

    private fun saveAssessmentBaseValues(
        resultHashMap: HashMap<String, Any>,
        serverData: List<FormLayout?>?,
        unitGenericType: String
    ) {
        viewModel.bioDataMap?.let {
            resultHashMap.putAll(it)
        }
        processValuesAndProceed(resultHashMap, serverData, unitGenericType)
    }

    private fun getSymptomsList(list: List<SymptomModel>): ArrayList<HashMap<String, Any>> {
        val resultList = ArrayList<HashMap<String, Any>>()
        list.forEach {
            val map = HashMap<String, Any>()
            map[DefinedParams.NAME] = it.symptom
            map[DefinedParams.symptom_id] = it._id
            it.otherSymptom?.let { other ->
                map[DefinedParams.other_symptom] = other
            }
            it.type?.let { type ->
                map[DefinedParams.type] = type
            }
            resultList.add(map)
        }

        return resultList
    }

    override fun onBPInstructionClicked(
        title: String,
        informationList: ArrayList<String>,
        description: String?
    ) {
        GeneralInfoDialog.newInstance(
            title,
            description,
            informationList
        ).show(childFragmentManager, GeneralInfoDialog.TAG)
    }

    override fun onRenderingComplete() {
        // formGenerator.hideFormSubmitButton(btnSubmit)
        loadSymptomsAndCompliance()
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        if (localDataCache is String) {
            when (localDataCache) {
                DefinedParams.PHQ4, DefinedParams.PHQ9 -> {
                    viewModel.fetchMentalHealthQuestions(id, localDataCache)
                }
                DefinedParams.Fetch_MH_Questions -> {
                    formGenerator.fetchMHQuestions(viewModel.mentalHealthQuestions.value?.data)
                }
            }
        }
    }

    override fun onFormError() {
        validateAssessmentInput()
        if (binding.symptomCard.root.visibility == View.VISIBLE) {
            validateCompliance()
            validateSymptom()
        }
    }

    override fun onJsonMismatchError() {
        showAlertWith(message = getString(R.string.check_form_attribute), isNegativeButtonNeed = false, positiveButtonName = getString(R.string.ok)) {

        }
    }

    private fun attachObserver() {
        fetchPatientDetails()

        viewModel.patientIdDetails.observe(viewLifecycleOwner) { resourceData ->
            when (resourceData.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceData?.message?.let { message ->
                        (activity as BaseActivity).showErrorDialogue(getString(R.string.error),message, isNegativeButtonNeed = false){}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    autoPopulateDetails(data = resourceData.data)
                }
            }
        }
        viewModel.formResponseLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                }
            }
        }

        viewModel.formResponseListLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                }
            }
        }


        viewModel.medicationChildComplianceResponse.observe(viewLifecycleOwner) { list ->
            loadData(list)
        }

        viewModel.selectedSymptoms.observe(viewLifecycleOwner) { selectedSymptoms ->
            loadSymptomsData(selectedSymptoms)
        }
        viewModel.mentalHealthQuestions.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    formGenerator.loadMentalHealthQuestions(resourceState.data)
                }
            }
        }
        patientDetailViewModel.patientDetailsResponse.observe(viewLifecycleOwner) { resourceData ->
            when (resourceData.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    val error = resourceData?.message ?: getString(R.string.error)
                    error.let { errMessage ->
                        (activity as BaseActivity).showAlertWith(
                            errMessage,
                            getString(R.string.retry),
                            true,
                        ) { callBack ->
                            handleCallBack(callBack)
                        }
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    autoPopulateDetails(data = resourceData.data)
                }
            }
        }

        viewModel.medicationParentComplianceResponse.observe(viewLifecycleOwner) { list ->
            val adapter = ComplianceSpinnerAdapter(requireContext(),SecuredPreference.getIsTranslationEnabled())
            val complianceList = ArrayList(list)
            complianceList.add(0, MedicalComplianceEntity(_id = (-1).toLong(), name = getString(R.string.please_select)))
            adapter.setData(complianceList)
            binding.symptomCard.complianceSpinner.etUserInput.adapter = adapter
        }

        viewModel.selectedMedication.observe(viewLifecycleOwner) { model ->
            model?.let { selectedModel ->
                if (selectedModel.isChildExists) {
                    viewModel.getMedicationChildComplianceList(selectedModel._id)
                } else hideComplianceOptions()
                addOrRemoveValuesFromResultMap(selectedModel)
            } ?: kotlin.run {
                hideComplianceOptions()
            }
            binding.symptomCard.otherComplianceReason.setText("")
            binding.symptomCard.otherComplianceReason.visibility = View.GONE
        }
    }

    private fun handleCallBack(callBack: Boolean) {
        if (callBack)
            fetchPatientDetails()
        else
            activity?.finish()
    }

    private fun loadSymptomsData(selectedSymptoms: List<SymptomModel>?) {
        if (selectedSymptoms.isNullOrEmpty()) {
            binding.symptomCard.symptomsSpinner.etUserInput.text = ""
            binding.symptomCard.otherHypertensionSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.otherHypertensionSymptom.etUserInput.setText("")
            binding.symptomCard.otherDiabetesSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.otherDiabetesSymptom.etUserInput.setText("")
        } else
            binding.symptomCard.symptomsSpinner.etUserInput.text =
                getSelectedSymptomsText(selectedSymptoms)
    }

    private fun loadData(medicalComplianceList: List<MedicalComplianceEntity>?) {
        medicalComplianceList?.let { list ->
            binding.symptomCard.childCompliance.clearCheck()
            if (list.isNotEmpty()) {
                binding.symptomCard.childCompliance.visibility = View.VISIBLE
                loadRadioButtons(list, binding.symptomCard.childCompliance, 2)
            } else {
                binding.symptomCard.childCompliance.visibility = View.GONE
                binding.symptomCard.tvChildErrorMessage.visibility = View.GONE
            }
        }
    }

    private fun fetchPatientDetails() {
        patientDetailViewModel.patientId?.let {
            if (it > 0)
                patientDetailViewModel.getPatientDetails(context = requireContext(),false)
        }
    }

    private fun loadFormDataToScreen(data: ArrayList<Pair<String, String>>?, visibility: Int) {
        binding.llForm.visibility = visibility
        val formList = data
        formList?.let {
            formGenerator.combineAccountWorkflows(it,UIConstants.AssessmentUniqueID)
        }
    }

    private fun getSelectedSymptomsText(selectedSymptoms: List<SymptomModel>): String {
        val otherSelected =
            selectedSymptoms.filter { it.symptom.startsWith(DefinedParams.Other, true) }
        val noSymptomsCount =
            selectedSymptoms.filter { it.symptom.startsWith("No symptoms", true) }.size
        val stringBuilder: StringBuilder = StringBuilder()
        if (noSymptomsCount == selectedSymptoms.size) {
            stringBuilder.append(getString(R.string.no))
        } else {
            if (otherSelected.isNullOrEmpty()) {
                stringBuilder.append((selectedSymptoms.size - noSymptomsCount))
            } else {
                stringBuilder.append((selectedSymptoms.size - noSymptomsCount) - otherSelected.size)
            }
        }
        stringBuilder.append(getString(R.string.empty_space))
        stringBuilder.append(getString(R.string.symptoms))
        if (!otherSelected.isNullOrEmpty()) {
            stringBuilder.append(getString(R.string.empty_space))
            stringBuilder.append(getString(R.string.other_selected))
            if (otherSelected.any { it.type == DefinedParams.Compliance_Type_Diabetes }) {
                binding.symptomCard.otherDiabetesSymptom.llRoot.visibility = View.VISIBLE
            } else {
                binding.symptomCard.otherDiabetesSymptom.llRoot.visibility = View.GONE
                binding.symptomCard.otherDiabetesSymptom.etUserInput.setText("")
            }
            if (otherSelected.any { it.type == DefinedParams.Compliance_Type_Hypertension }) {
                binding.symptomCard.otherHypertensionSymptom.llRoot.visibility = View.VISIBLE
            } else {
                binding.symptomCard.otherHypertensionSymptom.llRoot.visibility = View.GONE
                binding.symptomCard.otherHypertensionSymptom.etUserInput.setText("")
            }
        } else {
            binding.symptomCard.otherDiabetesSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.otherDiabetesSymptom.etUserInput.setText("")
            binding.symptomCard.otherHypertensionSymptom.llRoot.visibility = View.GONE
            binding.symptomCard.otherHypertensionSymptom.etUserInput.setText("")
        }
        stringBuilder.append(getString(R.string.empty_space))
        stringBuilder.append(getString(R.string.selected))
        return stringBuilder.toString()
    }

    private fun autoPopulateDetails(
        data: PatientDetailsModel? = null,
    ) {
        data?.message?.let {
            hidePatientInfoCard()
            loadFormDataToScreen(viewModel.formResponseListLiveData.value?.data, View.VISIBLE)
            //formGenerator.showFormSubmitButton(btnSubmit)
        } ?: kotlin.run {
            data?.let {
                loadFormDataToScreen(viewModel.formResponseListLiveData.value?.data, View.VISIBLE)
                //formGenerator.showFormSubmitButton(btnSubmit)
                showPatientInfoCard(data)
            }
        }
    }

    private fun hidePatientInfoCard() {
        binding.llPatientInfo.visibility = View.GONE
        viewModel.bioDataMap = null
    }

    private fun showPatientInfoCard(screeningDetailsModel: PatientDetailsModel) {
        viewModel.bioDataMap = HashMap()
        binding.llPatientInfo.visibility = View.VISIBLE
        binding.llPatientEdit.visibility = View.GONE

        binding.nationalId.tvKey.text = getString(R.string.national_id_medical_review)
        screeningDetailsModel.nationalId?.let {
            binding.nationalId.tvValue.text = it
            viewModel.bioDataMap!![DefinedParams.National_Id] = it
        }

        binding.patientName.tvKey.text = getString(R.string.name)
        var name = ""
        screeningDetailsModel.firstName?.let {
            name = it
            viewModel.bioDataMap!![DefinedParams.First_Name] = it
        }

        screeningDetailsModel.lastName?.let {
            name = "$name $it"
            viewModel.bioDataMap!![DefinedParams.Last_Name] = it
        }

        binding.patientName.tvValue.text = capitalize(name)

        screeningDetailsModel.nationalId?.let {
            viewModel.bioDataMap!![DefinedParams.National_Id] = it
        }

        binding.gender.tvKey.text = getString(R.string.gender)
        screeningDetailsModel.gender?.let {
            binding.gender.tvValue.text = it.replaceFirstChar(Char::titlecase)
            viewModel.bioDataMap!![DefinedParams.Gender] = it
        }

        binding.dobAge.tvKey.text = getString(R.string.age)
        var date: String? = null
        screeningDetailsModel.age?.let { age ->
            date = CommonUtils.getDecimalFormatted(age)
            viewModel.bioDataMap!![DefinedParams.Age] = age
        }
        screeningDetailsModel.isRegularSmoker?.let {
            viewModel.bioDataMap!![DefinedParams.is_regular_smoker] = it
        }
        screeningDetailsModel._id.let {
            viewModel.bioDataMap!![DefinedParams.PatientTrackId] = it
        }

        binding.dobAge.tvValue.text = date
        formGenerator.showHeightWeight(screeningDetailsModel.height, screeningDetailsModel.weight)
    }

    private fun loadListeners() {
        binding.llGender.rgGroup.setOnCheckedChangeListener { rgGroup, checkedId ->
            rgGroup.children.iterator().forEach {
                val child = it as RadioButton
                if (child.id == checkedId) {
                    child.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
                    child.setTextColor(requireContext().getColor(R.color.purple))
                } else {
                    child.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
                    child.setTextColor(requireContext().getColor(R.color.navy_blue))
                }
            }
        }
    }

    private fun openDatePicker(
        year: Int? = null,
        month: Int? = null,
        date: Int? = null
    ) {
        val date:Triple<Int?,Int?,Int?> = Triple(year,month,date)
        ViewUtil.showDatePicker(
            context = requireContext(),
            disableFutureDate = true,
            date = date
        ) { _, mYear, mMonth, mDayOfMonth ->
            val stringDate = "$mDayOfMonth-$mMonth-$mYear"
            val parsedDate = DateUtils.getDatePatternDDMMYYYY().parse(stringDate)
            parsedDate?.let {
                isDOBNeedToChange = false
                binding.llAge.etDateOfBirth.text = DateUtils.getDateDDMMYYYY().format(it)
                binding.llAge.etAgeInYears.setText(DateUtils.getAge(stringDate))
            }
        }
    }

    private fun validateAssessmentInput(): Boolean {
        var isValidData = true

        if (binding.llPatientEdit.visibility == View.VISIBLE) {
            viewModel.bioDataMap = HashMap()

            checkName()?.let {
                isValidData = it
            }

            checkLastName()?.let {
                isValidData = it
            }

            checkGender()?.let {
                isValidData = it
            }

            checkAge()?.let {
                isValidData = it
            }

            checkTobacco()?.let {
                isValidData = it
            }
        }

        return isValidData
    }

    private fun checkTobacco(): Boolean? {
        var validData: Boolean? = null
        val isValid = binding.llTobacco.rgGroup.checkedRadioButtonId > 0
        if (isValid) {
            viewModel.bioDataMap!![DefinedParams.is_regular_smoker] =
                getRadioButtonText(binding.llTobacco.rgGroup)
            hideErrorMessage(binding.llTobacco.tvErrorMessage)
        } else {
            validData = false
            showErrorMessage(
                getString(R.string.default_user_input_error),
                binding.llTobacco.tvErrorMessage
            )
        }
        return validData
    }

    private fun checkAge(): Boolean? {
        var validData: Boolean? = null
        var isValid = binding.llAge.etDateOfBirth.text?.toString()?.isNotEmpty() ?: false
        isValid = binding.llAge.etAgeInYears.text?.toString()?.isNotEmpty() ?: isValid
        if (isValid) {
            val ageValue = binding.llAge.etAgeInYears.text.toString().toDouble()
            if (ageValue >= DefinedParams.MinimumAge && ageValue < DefinedParams.MaximumAge) {
                val ageMap = HashMap<String, Any>()
                ageMap[DefinedParams.DateOfBirth] = binding.llAge.etDateOfBirth.text.toString()
                ageMap[DefinedParams.Age] = ageValue
                viewModel.bioDataMap!![DefinedParams.DOB_Details] = ageMap
                hideErrorMessage(binding.llAge.tvErrorMessage)
            } else {
                validData = false
                showErrorMessage(
                    getString(
                        R.string.general_min_max_validation,
                        DefinedParams.MinimumAge.toString(),
                        DefinedParams.MaximumAge.toString()
                    ),
                    binding.llAge.tvErrorMessage
                )
            }
        } else {
            validData = false
            showErrorMessage(
                getString(R.string.default_user_input_error),
                binding.llAge.tvErrorMessage
            )
        }
        return validData
    }

    private fun checkGender(): Boolean? {
        var validData: Boolean? = null
        val isValid = binding.llGender.rgGroup.checkedRadioButtonId > 0
        if (isValid) {
            viewModel.bioDataMap!![DefinedParams.Gender] =
                getRadioButtonText(binding.llGender.rgGroup)
            hideErrorMessage(binding.llGender.tvErrorMessage)
        } else {
            validData = false
            showErrorMessage(
                getString(R.string.default_user_input_error),
                binding.llGender.tvErrorMessage
            )
        }
        return validData
    }

    private fun checkLastName(): Boolean? {
        var validData: Boolean? = null
        val isValid = binding.llLastName.etUserInput.text?.toString()?.isNotEmpty() ?: false
        if (isValid) {
            viewModel.bioDataMap!![DefinedParams.Last_Name] =
                binding.llLastName.etUserInput.text.toString()
            hideErrorMessage(binding.llLastName.tvErrorMessage)
        } else {
            validData = false
            showErrorMessage(
                getString(R.string.default_user_input_error),
                binding.llLastName.tvErrorMessage
            )
        }
        return validData
    }

    private fun checkName() : Boolean? {
        var validData: Boolean? = null
        val isValid: Boolean =
            binding.llName.etUserInput.text?.toString()?.isNotEmpty() ?: false
        if (isValid) {
            viewModel.bioDataMap!![DefinedParams.First_Name] =
                binding.llName.etUserInput.text.toString()
            hideErrorMessage(binding.llName.tvErrorMessage)
        } else {
            validData = false
            showErrorMessage(
                getString(R.string.default_user_input_error),
                binding.llName.tvErrorMessage
            )
        }
        return validData
    }

    private fun showErrorMessage(message: String, view: TextView, titleView: TextView? = null) {
        titleView?.let { title ->
            scrollToView(binding.scrollView, title)
        }
        view.visibility = View.VISIBLE
        view.text = message
    }

    private fun validateCompliance(): Boolean {
        var isValidData = true

        if (viewModel.complianceMap != null) {
            viewModel.complianceMap?.let { complianceMap ->
                if (complianceMap.isNotEmpty()) {
                    val selectedModel = complianceMap[0]
                    if (selectedModel.containsKey(DefinedParams.is_child_exists) && selectedModel[DefinedParams.is_child_exists] as Boolean) {
                        if (complianceMap.size < 2) {
                            isValidData = false
                            showErrorMessage(
                                getString(R.string.default_user_input_error),
                                binding.symptomCard.tvChildErrorMessage
                            )
                        } else
                            otherComplianceCheck(complianceMap)?.let {
                                isValidData = it
                            }
                    } else
                        hideErrorMessage(binding.symptomCard.complianceSpinner.tvErrorMessage)
                } else
                    hideErrorMessage(binding.symptomCard.complianceSpinner.tvErrorMessage)
            }
        } else
            hideErrorMessage(binding.symptomCard.complianceSpinner.tvErrorMessage)

        return isValidData
    }

    private fun otherComplianceCheck(complianceMap: ArrayList<HashMap<String, Any>>): Boolean? {
        var isValid: Boolean? = null
        if (binding.symptomCard.otherComplianceReason.visibility == View.VISIBLE) {
            val reason = binding.symptomCard.otherComplianceReason.text
            if (reason.isNullOrBlank()) {
                isValid = false
                showErrorMessage(
                    getString(R.string.default_user_input_error),
                    binding.symptomCard.tvChildErrorMessage
                )
            } else {
                complianceMap[1][DefinedParams.other_compliance] =
                    reason.toString()
                hideErrorMessage(binding.symptomCard.tvChildErrorMessage)
            }
        } else
            hideErrorMessage(binding.symptomCard.tvChildErrorMessage)
        return isValid
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

    private fun hideErrorMessage(view: TextView) {
        view.visibility = View.GONE
    }

    private fun getRadioButtonText(radioGroup: RadioGroup): String {
        val radiobutton = radioGroup.findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
        return radiobutton.tag.toString()
    }

    private fun processValuesAndProceed(
        resultHashMap: HashMap<String, Any>,
        serverData: List<FormLayout?>?,
        unitGenericType: String
    ) {
        val map = HashMap<String, Any>()
        map.putAll(resultHashMap)
        CommonUtils.processLastMealTime(map)
        CommonUtils.calculateBloodGlucose(map, formGenerator)
        CommonUtils.calculateBMI(map, unitGenericType)
        CommonUtils.processHeightMap(map, unitGenericType)
        CommonUtils.calculateAverageBloodPressure(requireContext(), map)
        var isConfirmDiagnosis = false
        viewModel.patientIdDetails.value?.data?.let { searchResponse ->
            isConfirmDiagnosis = searchResponse.isConfirmDiagnosis
        }
        calculateProvisionalDiagnosis(
            map, isConfirmDiagnosis,
            formGenerator.getSystolicAverage(), formGenerator.getDiastolicAverage(),
            formGenerator.getFbsBloodGlucose(), formGenerator.getRbsBloodGlucose(),
            getMeasurementTypeValues(map)
        )
        map[DefinedParams.TenantId] = patientDetailViewModel.getPatientTenantId()
        formGenerator.setPhQ4Score(CommonUtils.calculatePHQScore(map))
        SecuredPreference.getSelectedSiteEntity()?.apply {
            id.let {
                map[DefinedParams.SiteId] = it
                calculateCVDRiskFactor(map)
                map[DefinedParams.UnitMeasurement] = getUnitMeasurementType()
                try {
                    val result = serverData?.let {
                        FormResultGenerator().groupValues(
                            context = requireContext(),
                            serverData = it,
                            map
                        )
                    }
                    result?.first?.let { groupedMap ->
                        if (connectivityManager.isNetworkAvailable()) {
                            val rootJson: JsonObject = StringConverter.getJsonObject(groupedMap)
                            viewModel.createPatientAssessment(rootJson)
                        } else {
                            (activity as BaseActivity).showErrorDialogue(
                                getString(R.string.error),
                                getString(R.string.no_internet_error),
                                isNegativeButtonNeed = false){}
                        }
                    }
                } catch (_: Exception) {
                    //Exception - Catch block
                }
            }
        }
    }

    private fun validateSymptom(): Boolean {
        var isValidData = true
        if (viewModel.selectedSymptoms.value != null) {
            viewModel.selectedSymptoms.value?.let {
                val selectedSymptom = viewModel.selectedSymptoms.value
                if (!selectedSymptom.isNullOrEmpty()) {
                    val other = selectedSymptom.filter {
                        it.symptom.startsWith(
                            DefinedParams.Other,
                            true
                        )
                    }
                    if (other.isNotEmpty()) {
                        checkComplianceDiabetes(other)?.let {
                            isValidData = it
                        }
                        checkComplianceHypertension(other)?.let {
                            isValidData = it
                        }
                    } else
                        hideErrorMessage(binding.symptomCard.symptomsSpinner.tvErrorMessage)
                } else
                    hideErrorMessage(binding.symptomCard.symptomsSpinner.tvErrorMessage)
            }
        } else
            hideErrorMessage(binding.symptomCard.symptomsSpinner.tvErrorMessage)
        return isValidData
    }

    private fun checkComplianceDiabetes(other: List<SymptomModel>): Boolean? {
        var isValid: Boolean? = null
        if (other.any { it.type == DefinedParams.Compliance_Type_Diabetes }) {
            hideErrorMessage(binding.symptomCard.symptomsSpinner.tvErrorMessage)
            val otherDiabetesText =
                binding.symptomCard.otherDiabetesSymptom.etUserInput.text
            if (otherDiabetesText.isNullOrBlank()) {
                isValid = false
                showErrorMessage(
                    getString(R.string.default_user_input_error),
                    binding.symptomCard.otherDiabetesSymptom.tvErrorMessage
                )
            } else {
                val filteredOther = other.first { symptomOther ->
                    symptomOther.type == DefinedParams.Compliance_Type_Diabetes
                }
                filteredOther.otherSymptom = otherDiabetesText.toString()
                hideErrorMessage(binding.symptomCard.otherDiabetesSymptom.tvErrorMessage)
            }
        } else {
            hideErrorMessage(binding.symptomCard.otherDiabetesSymptom.tvErrorMessage)
        }
        return isValid
    }

    private fun checkComplianceHypertension(other: List<SymptomModel>): Boolean? {
        var isValid: Boolean? = null
        if (other.any { it.type == DefinedParams.Compliance_Type_Hypertension }) {
            hideErrorMessage(binding.symptomCard.symptomsSpinner.tvErrorMessage)
            val otherHyperTensionText =
                binding.symptomCard.otherHypertensionSymptom.etUserInput.text
            if (otherHyperTensionText.isNullOrBlank()) {
                isValid = false
                showErrorMessage(
                    getString(R.string.default_user_input_error),
                    binding.symptomCard.otherHypertensionSymptom.tvErrorMessage
                )
            } else {
                val filteredOther = other.first { symptomOther ->
                    symptomOther.type == DefinedParams.Compliance_Type_Hypertension
                }
                filteredOther.otherSymptom = otherHyperTensionText.toString()
                hideErrorMessage(binding.symptomCard.otherHypertensionSymptom.tvErrorMessage)
            }
        } else {
            hideErrorMessage(binding.symptomCard.otherHypertensionSymptom.tvErrorMessage)
        }
        return isValid
    }

    private fun calculateCVDRiskFactor(map: HashMap<String, Any>) {
        if (map.containsKey(DefinedParams.BMI) && viewModel.list.isNotEmpty()) {
            val riskFactor = CommonUtils.calculateRiskFactor(
                map,
                viewModel.list,
                (map[DefinedParams.BMI] as Double),
                formGenerator.getSystolicAverage()
            )

            riskFactor?.let { riskFactorMap ->
                map[DefinedParams.CVD_Risk_Score] =
                    riskFactorMap[DefinedParams.CVD_Risk_Score] as Int
                map[DefinedParams.CVD_Risk_Score_Display] =
                    riskFactorMap[DefinedParams.CVD_Risk_Score_Display] as String
                map[DefinedParams.CVD_Risk_Level] =
                    riskFactorMap[DefinedParams.CVD_Risk_Level] as String
            }
        }
    }

    companion object {
        const val TAG = "AssessmentFragment"
    }

}
