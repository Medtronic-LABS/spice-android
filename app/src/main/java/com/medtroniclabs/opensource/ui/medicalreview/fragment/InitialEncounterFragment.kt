package com.medtroniclabs.opensource.ui.medicalreview.fragment

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.appextensions.touchObserver
import com.medtroniclabs.opensource.common.CustomSpinnerAdapter
import com.medtroniclabs.opensource.common.MedicalReviewConstant
import com.medtroniclabs.opensource.common.MedicalReviewConstant.DefaultID
import com.medtroniclabs.opensource.common.MedicalReviewConstant.DefaultIDLabel
import com.medtroniclabs.opensource.common.MedicalReviewConstant.DiabetesGestational
import com.medtroniclabs.opensource.common.MedicalReviewConstant.DiabetesPoorControlled
import com.medtroniclabs.opensource.common.MedicalReviewConstant.DiabetesPreDiabetic
import com.medtroniclabs.opensource.common.MedicalReviewConstant.DiabetesWellControlled
import com.medtroniclabs.opensource.common.MedicalReviewConstant.Yes_Currently
import com.medtroniclabs.opensource.common.MedicalReviewConstant.Yes_Past
import com.medtroniclabs.opensource.common.MedicalReviewConstant.otherTypeText
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.databinding.ChildViewLifeStyleBinding
import com.medtroniclabs.opensource.databinding.FragmentInitialEncounterBinding
import com.medtroniclabs.opensource.databinding.LayoutLifeStyleBinding
import com.medtroniclabs.opensource.db.tables.CurrentMedicationEntity
import com.medtroniclabs.opensource.db.tables.LifeStyleAnswer
import com.medtroniclabs.opensource.db.tables.LifestyleEntity
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.PreDiabetic
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.TagListCustomView
import com.medtroniclabs.opensource.ui.medicalreview.LifeStyleCustomView
import com.medtroniclabs.opensource.ui.medicalreview.MedicalReviewBaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.PregnancySubmitListener
import com.medtroniclabs.opensource.ui.medicalreview.dialog.MentalHealthAssessmentDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.PregnantDetailsDialogFragment
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.MedicalReviewBaseViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import java.util.*

class InitialEncounterFragment : Fragment(), View.OnClickListener, PregnancySubmitListener {

    lateinit var binding: FragmentInitialEncounterBinding
    private lateinit var currentMedicationTagListCustomView: TagListCustomView

    private val viewModel: MedicalReviewBaseViewModel by activityViewModels()
    private val patientViewModel: PatientDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInitialEncounterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        setListeners()
        getDataList()
        attachObserver()
    }

    private fun setListeners() {
        binding.btnStartGAD7Assessment.safeClickListener(this)
        binding.btnStartPHQ9Assessment.safeClickListener(this)
        binding.btnStartPhq4Assessment.safeClickListener(this)
        binding.btnEditPregnancy.safeClickListener(this)
        binding.cardMentalHealthHolder.visibility =
            if (SecuredPreference.isPHQ4Enabled()) View.VISIBLE else View.GONE
    }

    private fun attachObserver() {

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

        patientViewModel.isMentalHealthUpdated.observe(viewLifecycleOwner) { _ ->
            patientViewModel.getPatientDetails(requireContext(),false)
        }

        viewModel.currentMedicationResponse.observe(viewLifecycleOwner) { list ->
            list?.let {
                loadCurrentMedicationList(list)
            }
        }

        viewModel.lifeStyleListResponse.observe(viewLifecycleOwner) { list ->
            getLifeStyleUIModel(list).let { uiList ->
                viewModel.lifeStyleListUIModel = uiList
                loadLifeStyle(uiList)
            }
        }
    }

    fun validateInputs()
    {
        if (validateValues()) {
            apiCalls()
        }
    }

    private fun apiCalls() {
        if ((viewModel.medicalReviewEditModel.patientTrackId ?: 0) <= 0) {
            (context as BaseActivity).showAlertWith(
                getString(R.string.pull_down_refresh_message),
                getString(R.string.refresh),
                false
            ) {
                if (activity is MedicalReviewBaseActivity) {
                    (activity as MedicalReviewBaseActivity).swipeRefresh(true)
                }
            }
        } else {
            childFragmentManager.findFragmentById(binding.comorbidityContainer.id)
                ?.let { fragment ->
                    if (fragment is ComorbiditiesAndComplicationsFragment) {
                        fragment.updateCommorbiditiesList()
                        fragment.updateComplications()
                    }
                }
            updateCurrentMedicationsList()
            viewModel.medicalReviewEditModel.initialMedicalReview =
                viewModel.initialEncounterRequest
            (activity as MedicalReviewBaseActivity).showHideContainer(false)
        }
    }

    private fun updateCommorbiditiesList() {

    }

    private fun updateComplications() {

    }

    private fun updateCurrentMedicationsList() {
        val selectedList = currentMedicationTagListCustomView.getSelectedTags()
        val updateList = ArrayList<CurrentMedicationRequest>()
        viewModel.initialEncounterRequest.currentMedicationDetails?.medications?.forEach { currentMedication ->
            selectedList.find { it is CurrentMedicationEntity && it._id == currentMedication.currentmedicationId }
                ?.let {
                    updateList.add(currentMedication)
                }
        }
        viewModel.initialEncounterRequest.currentMedicationDetails?.medications = updateList
    }

    private fun getLifeStyleUIModel(list: List<LifestyleEntity>?): List<LifeStyleUIModel> {
        val lifeStyle = ArrayList<LifeStyleUIModel>()
        list?.forEach { model ->
            lifeStyle.add(
                LifeStyleUIModel(
                    model._id,
                    model.displayOrder,
                    model.lifestyle,
                    getAnswerLifeStyle(model.lifestyleAnswer),
                    model.lifestyleType,
                    model.cultureQuestionValue
                )
            )
        }
        return lifeStyle
    }

    private fun getAnswerLifeStyle(lifestyleAnswer: ArrayList<LifeStyleAnswer>): ArrayList<LifeStyleAnswerUIModel> {
        val lifeStyleUIAnswer = ArrayList<LifeStyleAnswerUIModel>()
        lifestyleAnswer.forEach {
            lifeStyleUIAnswer.add(
                LifeStyleAnswerUIModel(
                    name = it.name,
                    isAnswerDependent = it.isAnswerDependent,
                    cultureAnswerValue = it.cultureAnswerValue
                )
            )
        }
        return lifeStyleUIAnswer
    }

    private fun loadCurrentMedicationList(list: List<Any>) {
        binding.etOtherCurrentMedication.visibility = View.GONE
        var selectedMedicines: List<String>? = null
        viewModel.initialEncounterRequest.currentMedicationDetails?.medications?.let { medications ->
            selectedMedicines = medications.map { it.name }.toList()
            medications.find { it.name.startsWith(otherTypeText, true) }?.let {
                binding.etOtherCurrentMedication.setText(it.otherComplication)
            }
        }
        currentMedicationTagListCustomView.addChipItemList(list, selectedMedicines)
    }

    private fun loadLifeStyle(list: List<LifeStyleUIModel>?) {
        binding.llLifeStyleParentHolder.removeAllViews()
        val isTranslationEnabled = SecuredPreference.getIsTranslationEnabled()
        list?.forEach { style ->
            val lifeStyleBinding = LayoutLifeStyleBinding.inflate(LayoutInflater.from(context))
            if (isTranslationEnabled){
                lifeStyleBinding.tvTitle.text = style.cultureQuestionValue?:style.lifestyle
            }else{
                lifeStyleBinding.tvTitle.text = style.lifestyle
            }
            lifeStyleBinding.tvTitle.markMandatory()
            val selectedItem = viewModel.medicalReviewEditModel.initialMedicalReview?.lifestyle?.find { it.lifestyleId == style._id }
            selectedItem?.let { style.lifestyleAnswer.find { it.name == selectedItem.lifestyleAnswer } }
                ?.apply {
                    isSelected = true
                    comments = selectedItem.comments
                }
            val answerView = getAnswersView(style) { model, answerModel ->
                loadDependant(model, lifeStyleBinding, answerModel)
            }
            lifeStyleBinding.llLifeStyleHolder.addView(answerView)
            binding.llLifeStyleParentHolder.addView(lifeStyleBinding.root)
        }
    }

    private fun loadDependant(
        model: LifeStyleUIModel,
        lifeStyleBinding: LayoutLifeStyleBinding,
        answerModel: LifeStyleAnswerUIModel
    ) {
        if (answerModel.isAnswerDependent) {
            lifeStyleBinding.llDependentHolder.visibility = View.VISIBLE
            lifeStyleBinding.llDependentHolder.removeAllViews()
            lifeStyleBinding.llDependentHolder.addView(getDependView(model, answerModel))
        } else {
            lifeStyleBinding.llDependentHolder.visibility = View.GONE
        }
    }

    private fun getDependView(
        model: LifeStyleUIModel,
        answerModel: LifeStyleAnswerUIModel
    ): View {
        val binding = ChildViewLifeStyleBinding.inflate(LayoutInflater.from(context))
        when (model.lifestyleType) {
            DefinedParams.LIFE_STYLE_SMOKE -> {
                when (answerModel.name) {
                    Yes_Currently -> {
                        binding.tvChildTitle.text = getString(R.string.tobacoo_question_child_one)
                    }
                    Yes_Past -> {
                        binding.tvChildTitle.text =
                            getString(R.string.tobacoo_question_child_one_smoke)
                    }
                    else -> {
                        binding.tvChildTitle.text = getString(R.string.tobacoo_question_comment)
                    }
                }
            }
            DefinedParams.LIFE_STYLE_ALCOHOL -> {
                when (answerModel.name) {
                    Yes_Currently -> {
                        binding.tvChildTitle.text = getString(R.string.drinks_per_week)
                    }
                    Yes_Past -> {
                        binding.tvChildTitle.text =
                            getString(R.string.tobacoo_question_child_one_smoke)
                    }
                    else -> {
                        binding.tvChildTitle.text = getString(R.string.alcohol_question_comment)
                    }
                }
            }
            DefinedParams.LIFE_STYLE_NUT -> {
                binding.tvChildTitle.text = getString(R.string.comments_diet_nutrition)
            }
        }
        binding.tvChildTitle.markMandatory()
        if (!answerModel.comments.isNullOrBlank())
            binding.etChildUserInput.setText(answerModel.comments)
        binding.etChildUserInput.addTextChangedListener { childAnswer ->
            if (childAnswer.isNullOrBlank()) {
                answerModel.comments = null
            } else {
                answerModel.comments = childAnswer.toString()
            }
            addLifestyleAnswer(model, answerModel)
        }
        return binding.root
    }

    private fun addLifestyleAnswer(
        questionModel: LifeStyleUIModel,
        answerModel: LifeStyleAnswerUIModel
    ) {
        val model =
            viewModel.initialEncounterRequest.lifestyle?.find { it.lifestyleId == questionModel._id }
        if (model == null) {
            val listItem = InitialLifeStyle(
                answerModel.name,
                questionModel._id,
                isAnswerDependent = answerModel.isAnswerDependent,
                comments = answerModel.comments
            )
            viewModel.initialEncounterRequest.lifestyle?.add(listItem)
        } else {
            model.comments = answerModel.comments
        }
    }

    private fun getSelectedLifeStyleList(list: List<LifeStyleUIModel>): ArrayList<InitialLifeStyle> {
        val resultList = ArrayList<InitialLifeStyle>()
        list.forEach { model ->
            val selectedModel = model.lifestyleAnswer.filter { it.isSelected }
            if (selectedModel.isNotEmpty()) {
                resultList.add(
                    InitialLifeStyle(
                        selectedModel[0].name,
                        model._id,
                        isAnswerDependent = selectedModel[0].isAnswerDependent,
                        comments = selectedModel[0].comments
                    )
                )
            } else {
                resultList.add(InitialLifeStyle(lifestyleId = model._id))
            }
        }
        return resultList
    }

    private fun getAnswersView(
        style: LifeStyleUIModel,
        callback: ((model: LifeStyleUIModel, answerModel: LifeStyleAnswerUIModel) -> Unit?)? = null
    ): View {
        val view = LifeStyleCustomView(requireContext())
        view.tag = style._id
        view.addViewElements(style, callback)
        return view
    }

    private fun getDataList() {
        viewModel.getLifeStyleList()
    }

    private fun initializeView() {
        markMandatoryTitles()
        currentMedicationView()
        childFragmentManager.commit {
            setReorderingAllowed(true)
            add<ComorbiditiesAndComplicationsFragment>(
                binding.comorbidityContainer.id,
                tag = ComorbiditiesAndComplicationsFragment.TAG
            )
        }
        binding.tvYearOfDiabetesDiagnosisTitle.markMandatory()
        binding.tvYearOfHTNDiagnosisTitle.markMandatory()
        trackDiagnosisInformation()
        addTextChangeListener()
        addCurrentMedicationListener()
        loadDiabetesControlledType()
    }

    private fun currentMedicationView() {
        binding.etOtherCurrentMedication.touchObserver()
        binding.etCommentCurrentMedication.touchObserver()
        binding.etCommentAllergies.touchObserver()
        currentMedicationTagListCustomView =
            TagListCustomView(
                requireContext(),
                binding.currentMedicationTags, otherCallBack = { selectedName, isChecked ->
                    var currentMedicationObject =
                        viewModel.initialEncounterRequest.currentMedicationDetails
                    if (currentMedicationObject == null) {
                        viewModel.initialEncounterRequest.currentMedicationDetails =
                            InitialCurrentMedicationDetails()
                        currentMedicationObject =
                            viewModel.initialEncounterRequest.currentMedicationDetails
                    }
                    if (currentMedicationObject?.medications == null)
                        currentMedicationObject?.medications = ArrayList()
                    var item = viewModel.initialEncounterRequest.currentMedicationDetails?.medications?.find { it.name.equals(selectedName,true) }

                    if (isChecked && item == null) {
                        viewModel.currentMedicationResponse.value?.find {
                            it.medicationName.lowercase() == selectedName.lowercase()
                        }?.let { medications ->
                            item = CurrentMedicationRequest(
                                currentmedicationId = medications._id,
                                name = medications.medicationName,
                                type = medications.type
                            )
                            currentMedicationObject?.medications?.add(item!!)
                        }

                    } else if (!isChecked && item != null)
                        viewModel.initialEncounterRequest.currentMedicationDetails?.medications?.remove(
                            item
                        )

                    if (selectedName.startsWith(otherTypeText, ignoreCase = true)) {
                        if (isChecked)
                            binding.etOtherCurrentMedication.visibility = View.VISIBLE
                        else {
                            binding.etOtherCurrentMedication.visibility = View.GONE
                            binding.etOtherCurrentMedication.setText("")
                        }
                    }
                }
            )
    }

    private fun markMandatoryTitles() {
        binding.tvDiabetesRootTitle.markMandatory()
        binding.tvHypertensionLabel.markMandatory()
        binding.tvCurrentMedicationsTitle.markMandatory()
        binding.tvAllergiesToDrugsTitle.markMandatory()
        binding.tvDiabetesControlledTypeTitle.markMandatory()
        binding.tvDiabetesTypeTitle.markMandatory()
        binding.tvPregnancyLabel.markMandatory()
    }

    private fun loadDiabetesType(gender: String?) {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefaultIDLabel,
                DefinedParams.ID to DefaultID
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to MedicalReviewConstant.DiabetesType1,
                DefinedParams.ID to MedicalReviewConstant.DiabetesType1
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to MedicalReviewConstant.DiabetesType2,
                DefinedParams.ID to MedicalReviewConstant.DiabetesType2
            )
        )
        if (gender == DefinedParams.Female) {
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to DiabetesGestational,
                    DefinedParams.ID to DiabetesGestational
                )
            )
        }
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(dropDownList)
        binding.etDiabetesTypeUserInput.adapter = adapter
        binding.etDiabetesTypeUserInput.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        var diagnosis = viewModel.initialEncounterRequest.diagnosis
                        if (diagnosis == null) {
                            viewModel.initialEncounterRequest.diagnosis = InitialDiagnosis()
                            diagnosis = viewModel.initialEncounterRequest.diagnosis
                        }
                        val selectedId = it[DefinedParams.id] as String?
                        if (selectedId != "-1") {
                            diagnosis?.diabetesDiagnosis = selectedId
                        } else {
                            diagnosis?.diabetesDiagnosis = null
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }

            }
        if (binding.etDiabetesTypeUserInput.visibility == View.VISIBLE || viewModel.initialEncounterRequest.diagnosis?.diabetesDiagnosis != null) {
            viewModel.initialEncounterRequest.diagnosis?.diabetesDiagnosis?.let { diagnosisType ->
                val index =
                    dropDownList.indexOfFirst { (it[DefinedParams.id] as String?) == diagnosisType }
                binding.etDiabetesTypeUserInput.setSelection(index, true)
            }
        }
    }

    private fun loadDiabetesControlledType() {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefaultIDLabel,
                DefinedParams.ID to DefaultID
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DiabetesPreDiabetic,
                DefinedParams.ID to DiabetesPreDiabetic
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DiabetesWellControlled,
                DefinedParams.ID to DiabetesWellControlled
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DiabetesPoorControlled,
                DefinedParams.ID to DiabetesPoorControlled
            )
        )
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(dropDownList)
        binding.etDiabetesControlledUserInput.adapter = adapter
        binding.etDiabetesControlledUserInput.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    var diagnosis = viewModel.initialEncounterRequest.diagnosis
                    if (diagnosis == null) {
                        viewModel.initialEncounterRequest.diagnosis = InitialDiagnosis()
                        diagnosis = viewModel.initialEncounterRequest.diagnosis
                    }
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedId = it[DefinedParams.id] as String?
                        if (selectedId != "-1") {
                            diagnosis?.diabetesDiagControlledType = selectedId
                        } else {
                            diagnosis?.diabetesDiagControlledType = null
                        }
                        showOrHideDiabetesType(diagnosis?.diabetesDiagControlledType)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    private fun showOrHideDiabetesType(diabetesDiagnosis: String?) {
        if (diabetesDiagnosis == PreDiabetic || diabetesDiagnosis == "-1" || diabetesDiagnosis == null) {
            binding.tvDiabetesTypeTitle.visibility = View.GONE
            binding.etDiabetesTypeUserInput.visibility = View.GONE
            binding.etDiabetesTypeUserInput.setSelection(0, true)
        } else {
            binding.tvDiabetesTypeTitle.visibility = View.VISIBLE
            binding.etDiabetesTypeUserInput.visibility = View.VISIBLE
        }

    }

    private fun addOrUpdateOtherCurrentMedication(otherCurrentMedication: String?) {
        val medication =
            viewModel.initialEncounterRequest.currentMedicationDetails?.medications
        if (!medication.isNullOrEmpty()) {
            val otherModel = medication.find { it.name.startsWith(otherTypeText, true) }
            otherModel?.let {
                it.otherComplication = otherCurrentMedication
            }
        }
    }

    private fun addCurrentMedicationListener() {
        binding.rgCurrentMedications.setOnCheckedChangeListener { _, _ ->
            when (binding.rgCurrentMedications.checkedRadioButtonId) {
                R.id.rbCurrentMedicationYes -> {
                    addOrUpdateCurrentMedicationInformation(true, 1)
                }
                R.id.rbCurrentMedicationNo -> {
                    addOrUpdateCurrentMedicationInformation(false, 1)
                }
            }
        }
        binding.rgAllergiesToDrugs.setOnCheckedChangeListener { _, _ ->
            when (binding.rgAllergiesToDrugs.checkedRadioButtonId) {
                R.id.rbAllergiesToDrugsYes -> {
                    addOrUpdateCurrentMedicationInformation(true, 2)
                }
                R.id.rbAllergiesToDrugsNo -> {
                    addOrUpdateCurrentMedicationInformation(false, 2)

                }
            }
        }

        binding.etCommentCurrentMedication.addTextChangedListener { commentCurrentMedicaiton ->
            var currentMedication = viewModel.initialEncounterRequest.currentMedicationDetails
            if (currentMedication == null) {
                viewModel.initialEncounterRequest.currentMedicationDetails =
                    InitialCurrentMedicationDetails()
                currentMedication = viewModel.initialEncounterRequest.currentMedicationDetails
            }
            if (commentCurrentMedicaiton.isNullOrBlank()) {
                currentMedication?.adheringMedComment = null
            } else {
                currentMedication?.adheringMedComment = commentCurrentMedicaiton.toString()
            }
        }

        binding.etCommentAllergies.addTextChangedListener { commentAllergies ->
            var currentMedication = viewModel.initialEncounterRequest.currentMedicationDetails
            if (currentMedication == null) {
                viewModel.initialEncounterRequest.currentMedicationDetails =
                    InitialCurrentMedicationDetails()
                currentMedication = viewModel.initialEncounterRequest.currentMedicationDetails
            }
            if (commentAllergies.isNullOrBlank()) {
                currentMedication?.allergiesComment = null
            } else {
                currentMedication?.allergiesComment = commentAllergies.toString()
            }

        }

    }

    private fun addOrUpdateCurrentMedicationInformation(status: Boolean, type: Int) {
        var currentMedication = viewModel.initialEncounterRequest.currentMedicationDetails
        if (currentMedication == null) {
            viewModel.initialEncounterRequest.currentMedicationDetails =
                InitialCurrentMedicationDetails()
            currentMedication = viewModel.initialEncounterRequest.currentMedicationDetails
        }
        if (type == 1) {
            currentMedication?.isAdheringCurrentMed = status
            if (status) {
                binding.etCommentCurrentMedication.visibility = View.VISIBLE
            } else {
                binding.etCommentCurrentMedication.visibility = View.GONE
                binding.etCommentCurrentMedication.setText("")
            }
        } else {
            currentMedication?.isDrugAllergies = status
            if (status) {
                binding.etCommentAllergies.visibility = View.VISIBLE
            } else {
                binding.etCommentAllergies.visibility = View.GONE
                binding.etCommentAllergies.setText("")
            }
        }
    }

    private fun addTextChangeListener() {
        binding.etUserInputDiabetes.addTextChangedListener { editable ->
            if (editable.isNullOrBlank())
                addOrUpdateYearOfDiagnosis(null, 1)
            else
                addOrUpdateYearOfDiagnosis(editable.toString(), 1)
        }

        binding.etUserInputHTN.addTextChangedListener { editable ->
            if (editable.isNullOrBlank())
                addOrUpdateYearOfDiagnosis(null, 2)
            else
                addOrUpdateYearOfDiagnosis(editable.toString(), 2)
        }
        binding.rgPregnancy.setOnCheckedChangeListener { _, _ ->
            when (binding.rgPregnancy.checkedRadioButtonId) {
                R.id.rbPregnancy -> {
                    addOrUpdatePregnancyDetail(true)
                }
                R.id.rbPregnancyNa -> {
                    addOrUpdatePregnancyDetail(false)
                }
            }
        }

        binding.etOtherCurrentMedication.addTextChangedListener { otherCurrentMedication ->
            if (otherCurrentMedication.isNullOrBlank())
                addOrUpdateOtherCurrentMedication(null)
            else
                addOrUpdateOtherCurrentMedication(otherCurrentMedication.trim().toString())
        }
    }

    private fun addOrUpdatePregnancyDetail(status: Boolean) {
        viewModel.initialEncounterRequest.isPregnant = status
        binding.btnEditPregnancy.visibility = View.GONE
        if (status) {
            binding.btnEditPregnancy.visibility = View.VISIBLE
        }
    }

    private fun addOrUpdateYearOfDiagnosis(text: String?, value: Int) {
        var diagnosis = viewModel.initialEncounterRequest.diagnosis
        if (diagnosis == null) {
            viewModel.initialEncounterRequest.diagnosis = InitialDiagnosis()
            diagnosis = viewModel.initialEncounterRequest.diagnosis
        }
        if (value == 2) {
            diagnosis?.htnYearOfDiagnosis = text
        } else {
            diagnosis?.diabetesYearOfDiagnosis = text
        }
    }

    private fun trackDiagnosisInformation() {
        binding.rgDiabetes.setOnCheckedChangeListener { _, _ ->
            when (binding.rgDiabetes.checkedRadioButtonId) {
                R.id.rbDiabetesNa -> {
                    updateDiabetesDiagnosesInformation(false, DefinedParams.N_A)
                }
                R.id.rbDiabetesNewPatient -> {
                    updateDiabetesDiagnosesInformation(true, DefinedParams.NEW)
                }
                R.id.rbDiabetesKnownPatient -> {
                    updateDiabetesDiagnosesInformation(true, DefinedParams.KNOWN)
                }
            }
        }

        binding.rgHypertension.setOnCheckedChangeListener { _, _ ->
            when (binding.rgHypertension.checkedRadioButtonId) {
                R.id.rbHypertensionNa -> {
                    updateHyperTensionInformation(false, DefinedParams.N_A)
                }
                R.id.rbHypertensionNewPatient -> {
                    updateHyperTensionInformation(true, DefinedParams.NEW)
                }
                R.id.rbHypertensionKnownPatient -> {
                    updateHyperTensionInformation(true, DefinedParams.KNOWN)
                }
            }

        }
    }

    private fun updateHyperTensionInformation(status: Boolean, diabetesType: String) {
        var diagnosis = viewModel.initialEncounterRequest.diagnosis
        if (diagnosis == null) {
            viewModel.initialEncounterRequest.diagnosis = InitialDiagnosis()
            diagnosis = viewModel.initialEncounterRequest.diagnosis
        }
        diagnosis?.isHtnDiagnosis = status
        diagnosis?.htnPatientType = diabetesType
        showHideHyperTensionYearOfDiagnosis(diabetesType)
    }

    private fun showHideHyperTensionYearOfDiagnosis(diabetesType: String) {
        if (diabetesType == DefinedParams.KNOWN) {
            binding.llHTNYearDiagnosisHolder.visibility = View.VISIBLE
            showHyperTensionMedicationList()
        } else {
            binding.llHTNYearDiagnosisHolder.visibility = View.GONE
            binding.etUserInputHTN.setText("")
            hideHyperTensionMedication()
        }
    }

    private fun hideHyperTensionMedication() {
        removeMedicationList(DefinedParams.Compliance_Type_Hypertension)
        if (binding.rgDiabetes.checkedRadioButtonId == R.id.rbDiabetesKnownPatient) {
            viewModel.getCurrentMedicationList(DefinedParams.Compliance_Type_Diabetes)
        } else {
            binding.cardCurrentMedicationDetailsHolder.visibility = View.GONE
            clearCurrentMedications()
        }
    }

    private fun clearCurrentMedications() {
        binding.rgCurrentMedications.clearCheck()
        binding.rgAllergiesToDrugs.clearCheck()
        binding.etCommentCurrentMedication.text?.clear()
        binding.etCommentCurrentMedication.visibility = View.GONE
        binding.etCommentAllergies.text?.clear()
        binding.etCommentAllergies.visibility = View.GONE
        viewModel.initialEncounterRequest.currentMedicationDetails?.apply {
            isAdheringCurrentMed = null
            isDrugAllergies = null
            adheringMedComment = null
            allergiesComment = null
        }
    }

    private fun showHyperTensionMedicationList() {
        if (binding.cardCurrentMedicationDetailsHolder.visibility != View.VISIBLE) {
            binding.cardCurrentMedicationDetailsHolder.visibility = View.VISIBLE
        }
        if (binding.rgDiabetes.checkedRadioButtonId == R.id.rbDiabetesKnownPatient) {
            viewModel.getCurrentMedicationList()
        } else {
            viewModel.getCurrentMedicationList(DefinedParams.Compliance_Type_Hypertension)
        }
    }

    private fun showHideDiabetesYearOfDiagnoses(diabetesType: String) {
        if (diabetesType == DefinedParams.KNOWN) {
            binding.llDiabetesYearDiagnosisHolder.visibility = View.VISIBLE
            showDiabetesMedicationList()
        } else {
            binding.llDiabetesYearDiagnosisHolder.visibility = View.GONE
            binding.etUserInputDiabetes.setText("")
            binding.etDiabetesControlledUserInput.setSelection(0, true)
            binding.etDiabetesTypeUserInput.setSelection(0, true)
            hideDiabetesMedicationList()
        }
    }

    private fun hideDiabetesMedicationList() {
        removeMedicationList(DefinedParams.Compliance_Type_Diabetes)
        if (binding.rgHypertension.checkedRadioButtonId == R.id.rbHypertensionKnownPatient) {
            viewModel.getCurrentMedicationList(DefinedParams.Compliance_Type_Hypertension)
        } else {
            binding.cardCurrentMedicationDetailsHolder.visibility = View.GONE
            clearCurrentMedications()
        }
    }

    private fun removeMedicationList(type: String) {
        val removeList =
            viewModel.initialEncounterRequest.currentMedicationDetails?.medications?.filter { it.type == type }
        removeList?.let {
            viewModel.initialEncounterRequest.currentMedicationDetails?.medications?.removeAll(
                removeList
            )
        }

        if ((viewModel.initialEncounterRequest.diagnosis?.diabetesPatientType != DefinedParams.KNOWN) &&
            (viewModel.initialEncounterRequest.diagnosis?.htnPatientType != DefinedParams.KNOWN)
        ) {
            viewModel.initialEncounterRequest.currentMedicationDetails?.medications?.find {
                it.type.equals(
                    "Other",
                    ignoreCase = true
                )
            }?.let { otherMedication ->
                viewModel.initialEncounterRequest.currentMedicationDetails?.medications?.remove(
                    otherMedication
                )
                binding.etOtherCurrentMedication.setText("")
            }
        }
    }

    private fun showDiabetesMedicationList() {
        if (binding.cardCurrentMedicationDetailsHolder.visibility != View.VISIBLE) {
            binding.cardCurrentMedicationDetailsHolder.visibility = View.VISIBLE
        }
        if (binding.rgHypertension.checkedRadioButtonId == R.id.rbHypertensionKnownPatient) {
            viewModel.getCurrentMedicationList()
        } else {
            viewModel.getCurrentMedicationList(DefinedParams.Compliance_Type_Diabetes)
        }
    }

    private fun updateDiabetesDiagnosesInformation(status: Boolean, diabetesType: String) {
        var diagnosis = viewModel.initialEncounterRequest.diagnosis
        if (diagnosis == null) {
            viewModel.initialEncounterRequest.diagnosis = InitialDiagnosis()
            diagnosis = viewModel.initialEncounterRequest.diagnosis
        }
        diagnosis?.isDiabetesDiagnosis = status
        diagnosis?.diabetesPatientType = diabetesType
        showHideDiabetesYearOfDiagnoses(diabetesType)
    }

    private fun showErrorMessage(message: String, view: TextView) {
        view.visibility = View.VISIBLE
        view.text = message
    }

    private fun hideErrorMessage(view: TextView) {
        view.visibility = View.GONE
    }

    private fun validateValues(): Boolean {
        var isValid = true
        var errorView: TextView? = null
        var focusComorbidityContainer = false
        val initialEncounterRequest = viewModel.initialEncounterRequest

        validateConfirmDiagnosis().let {
            it.first?.let {
                isValid = it
            }
            it.second?.let { textView ->
                errorView = errorView ?: textView
            }
        }

        validateCurrentMedicationDetails().let {
            it.first?.let {
                isValid = it
            }
            it.second?.let { textView ->
                errorView = errorView ?: textView
            }
        }

        if (binding.cardPregnancyHolder.visibility == View.VISIBLE) {
            if (initialEncounterRequest.isPregnant != null)
                hideErrorMessage(binding.tvPregnancyErrorMessage)
            else {
                isValid = false
                if (errorView == null) {
                    errorView = binding.tvPregnancyLabel
                }
                showErrorMessage(
                    getString(R.string.default_user_input_error),
                    binding.tvPregnancyErrorMessage
                )
            }
        }

        childFragmentManager.findFragmentById(binding.comorbidityContainer.id)
            ?.let { fragment ->
                if (fragment is ComorbiditiesAndComplicationsFragment) {
                    fragment.validateInputs().let { validInput ->
                        isValid = validInput
                        if(errorView == null)
                            focusComorbidityContainer = !validInput
                    }
                }
            }

        viewModel.lifeStyleListUIModel?.let {
            viewModel.initialEncounterRequest.lifestyle = getSelectedLifeStyleList(it)
        }

        validateLifeStyle().let {
            it.first?.let {
                isValid = it
            }
            it.second?.let { textView ->
                if(errorView == null && !focusComorbidityContainer)
                    errorView = errorView ?: textView
            }
        }

        if(errorView != null || focusComorbidityContainer) {
            if (activity is MedicalReviewBaseActivity) {
                val scrollView = (activity as MedicalReviewBaseActivity).getScrollview()
                scrollToView(scrollView, errorView ?: binding.comorbidityContainer)
            }
        }
        return (errorView == null && !focusComorbidityContainer)
    }

    private fun validateLifeStyle(): Pair<Boolean?, TextView?> {
        var isValid: Boolean? = null
        var errorView: TextView? = null
        if (viewModel.initialEncounterRequest.lifestyle != null) {
            viewModel.initialEncounterRequest.lifestyle?.let {
                val unAnsweredList = it.filter { it.lifestyleAnswer == null }
                if (unAnsweredList.isNotEmpty()) {
                    isValid = false
                    errorView = errorView ?: binding.tvLifeStyleTitle
                    showErrorMessage(
                        getString(R.string.validation_message_lifestyle),
                        binding.tvErrorLifeStyle
                    )
                } else {
                    val unCommentedList =
                        it.filter { it.isAnswerDependent && it.comments == null }
                    if (unCommentedList.isNotEmpty()) {
                        isValid = false
                        errorView = errorView ?: binding.tvLifeStyleTitle
                        showErrorMessage(
                            getString(R.string.validation_message_lifestyle),
                            binding.tvErrorLifeStyle
                        )
                    } else
                        hideErrorMessage(binding.tvErrorLifeStyle)
                }
            }
        } else {
            isValid = false
            errorView = errorView ?: binding.tvLifeStyleTitle
            showErrorMessage(
                getString(R.string.validation_message_lifestyle),
                binding.tvErrorLifeStyle
            )
        }
        return Pair(isValid, errorView)
    }

    private fun validateCurrentMedicationDetails(): Pair<Boolean?, TextView?> {
        var isValid: Boolean? = null
        var errorView: TextView? = null
        if (binding.cardCurrentMedicationDetailsHolder.visibility == View.VISIBLE) {
            if (viewModel.initialEncounterRequest.currentMedicationDetails != null) {
                viewModel.initialEncounterRequest.currentMedicationDetails?.let { currentMedicationDetails ->
                    if (
                        (currentMedicationDetails.isAdheringCurrentMed == null) ||
                        (currentMedicationDetails.isAdheringCurrentMed == true && currentMedicationDetails.adheringMedComment == null)
                    ) {
                        isValid = false
                        errorView = errorView ?: binding.tvCurrentMedicationsTitle
                        showErrorMessage(
                            getString(R.string.default_user_input_error),
                            binding.tvErrorCurrentMedications
                        )
                    } else
                        hideErrorMessage(binding.tvErrorCurrentMedications)

                    if (validateDrugAllergies(currentMedicationDetails)) {
                        isValid = false
                        errorView = errorView ?: binding.tvAllergiesToDrugsTitle
                        showErrorMessage(
                            getString(R.string.default_user_input_error),
                            binding.tvErrorAllergiesToDrugs
                        )
                    } else
                        hideErrorMessage(binding.tvErrorAllergiesToDrugs)

                    validateMedicationList(currentMedicationDetails.medications).let {
                        it.first?.let { valid ->
                            isValid = valid
                        }
                        it.second?.let { textView ->
                            errorView = textView
                        }
                    }
                }
            } else {
                isValid = false
                errorView = errorView ?: binding.tvAllergiesToDrugsTitle
                showErrorMessage(
                    getString(R.string.default_user_input_error),
                    binding.tvErrorAllergiesToDrugs
                )
                showErrorMessage(
                    getString(R.string.default_user_input_error),
                    binding.tvErrorCurrentMedications
                )
            }
        }
        return Pair(isValid, errorView)
    }

    private fun validateDrugAllergies(currentMedicationDetails: InitialCurrentMedicationDetails): Boolean {
        return (currentMedicationDetails.isDrugAllergies == null) || (currentMedicationDetails.isDrugAllergies == true && currentMedicationDetails.allergiesComment == null)
    }

    private fun validateMedicationList(currentMedicationList: ArrayList<CurrentMedicationRequest>?): Pair<Boolean?, TextView?> {
        var isValid: Boolean? = null
        var errorView: TextView? = null
        if (!(currentMedicationList.isNullOrEmpty())) {
            val selectedOtherMedication =
                currentMedicationList.filter { it.name.startsWith(otherTypeText, true) }
            if (selectedOtherMedication.isNotEmpty()) {
                selectedOtherMedication[0].otherComplication?.let {
                    hideErrorMessage(binding.tvErrorOtherCurrentMedications)
                } ?: kotlin.run {
                    isValid = false
                    errorView = errorView ?: binding.tvCurrentMedicationDetailsTitle
                    showErrorMessage(
                        getString(R.string.default_user_input_error),
                        binding.tvErrorOtherCurrentMedications
                    )
                }
            } else
                hideErrorMessage(binding.tvErrorOtherCurrentMedications)
        } else {
            showErrorMessage(
                getString(R.string.default_user_input_error),
                binding.tvErrorOtherCurrentMedications
            )
            errorView = errorView ?: binding.tvCurrentMedicationDetailsTitle
            isValid = false
        }
        return Pair(isValid, errorView)
    }

    private fun validateConfirmDiagnosis(): Pair<Boolean?, TextView?> {
        var isValid: Boolean? = null
        var errorView: TextView? = null

        if (binding.cardDiagnosisHolder.visibility == View.GONE && binding.cardHypertensionHolder.visibility == View.GONE)
            return Pair(true, null)

        if (viewModel.initialEncounterRequest.diagnosis != null) {
            viewModel.initialEncounterRequest.diagnosis?.let { diagnosis ->
                if (binding.cardDiagnosisHolder.visibility == View.VISIBLE && validateDiagnosis(diagnosis)) {
                    isValid = false
                    errorView = errorView ?: binding.tvDiabetesTypeTitle
                    showErrorMessage(
                        getString(R.string.default_user_input_error),
                        binding.tvDiabetesErrorMessage
                    )
                } else
                    hideErrorMessage(binding.tvDiabetesErrorMessage)

                if (binding.cardHypertensionHolder.visibility == View.VISIBLE && (diagnosis.isHtnDiagnosis == null) ||
                    (diagnosis.htnPatientType == DefinedParams.KNOWN && (diagnosis.htnYearOfDiagnosis == null)) ||
                    (diagnosis.htnYearOfDiagnosis != null && !isYearInLimit(diagnosis.htnYearOfDiagnosis))
                ) {
                    isValid = false
                    errorView = errorView ?: binding.tvYearOfHTNDiagnosisTitle
                    showErrorMessage(
                        getString(R.string.default_user_input_error),
                        binding.tvHypertensionErrorMessage
                    )
                } else
                    hideErrorMessage(binding.tvHypertensionErrorMessage)
            }
        } else {
            errorView = errorView ?: binding.tvDiabetesTypeTitle
            isValid = false
            showErrorMessage(
                getString(R.string.default_user_input_error),
                binding.tvDiabetesErrorMessage
            )
            showErrorMessage(
                getString(R.string.default_user_input_error),
                binding.tvHypertensionErrorMessage
            )
        }
        return Pair(isValid, errorView)
    }

    private fun validateDiagnosis(diagnosis: InitialDiagnosis): Boolean {
        return (diagnosis.isDiabetesDiagnosis == null) ||
                (diagnosis.diabetesPatientType == DefinedParams.KNOWN
                        && (diagnosis.diabetesDiagControlledType == null ||
                        diagnosis.diabetesYearOfDiagnosis == null))
                || (diagnosis.diabetesDiagControlledType != null &&
                diagnosis.diabetesDiagControlledType != DiabetesPreDiabetic
                && diagnosis.diabetesDiagnosis == null
                )
                || (diagnosis.diabetesYearOfDiagnosis != null
                && !isYearInLimit(diagnosis.diabetesYearOfDiagnosis))
    }

    private fun isYearInLimit(inputYr: String?): Boolean {
        val cal = Calendar.getInstance()
        val calYr = cal.get(Calendar.YEAR)
        var age: Int? = null
        patientViewModel.patientDetailsResponse.value?.data?.age?.let {
            cal.add(Calendar.YEAR, -it.toInt())
            age = cal.get(Calendar.YEAR)
        }
        inputYr?.toInt()?.let {
            return (it <= calYr && it > (age ?: 0))
        }
        return false
    }

    private fun loadPatientInformation(data: PatientDetailsModel?) {
        data?.apply {
            viewModel.medicalReviewEditModel.patientTrackId = _id
            if (SecuredPreference.isPregnancyWorkFlowEnabled() && gender == DefinedParams.Female) {
                binding.cardPregnancyHolder.visibility = View.VISIBLE
                binding.btnEditPregnancy.text = getString(R.string.add_pregnancy_details)
                binding.btnEditPregnancy.visibility = View.GONE
                if (isPregnant) {
                    binding.rbPregnancy.isChecked = true
                    binding.btnEditPregnancy.visibility = View.VISIBLE
                } else {
                    binding.rbPregnancyNa.isChecked = true
                }
            } else {
                binding.cardPregnancyHolder.visibility = View.GONE
            }
            loadDiabetesType(gender)
            renderMentalHealthCardDetails(this)
            checkForConfirmDiagnosis(isHtnDiagnosis, isDiabetesDiagnosis)

            //Show Patient Edit Icon
            if (activity is MedicalReviewBaseActivity && (activity as MedicalReviewBaseActivity).isInitialFragmentVisible()) {
                val isPatientEnrolled = (data.patientId ?: 0) > 0
                (context as BaseActivity).showVerticalMoreIcon(
                    isPatientEnrolled,
                    callback = { view ->
                        (activity as MedicalReviewBaseActivity).onMoreIconClicked(view)
                    })
            }
        }

        if (binding.cardPregnancyHolder.visibility == View.VISIBLE)
            binding.cardMentalHealthHolder.visibility =
                if (SecuredPreference.isPHQ4Enabled()) View.VISIBLE else View.INVISIBLE
        else
            binding.cardMentalHealthHolder.visibility =
                if (SecuredPreference.isPHQ4Enabled()) View.VISIBLE else View.GONE
    }

    private fun checkForConfirmDiagnosis(isHTNDiagnosis: Boolean, isDiabetesDiagnosis: Boolean) {
        if (!isHTNDiagnosis) {
            binding.cardHypertensionHolder.visibility = View.VISIBLE
        } else {
            binding.cardHypertensionHolder.visibility = View.GONE
        }

        if (!isDiabetesDiagnosis) {
            binding.cardDiagnosisHolder.visibility = View.VISIBLE
        } else
            binding.cardDiagnosisHolder.visibility = View.GONE
    }

    private fun renderMentalHealthCardDetails(data: PatientDetailsModel) {
        if (SecuredPreference.isPHQ4Enabled()) {
            binding.phq4Holder.visibility = View.GONE
            binding.phq9Holder.visibility = View.GONE
            binding.gad7Holder.visibility = View.GONE
            when {
                data.isPhq9 && data.isGad7 -> {
                    binding.phq9Holder.visibility = View.VISIBLE
                    binding.gad7Holder.visibility = View.VISIBLE
                }
                data.isPhq9 -> binding.phq9Holder.visibility = View.VISIBLE
                data.isGad7 -> binding.gad7Holder.visibility = View.VISIBLE
                else -> binding.phq4Holder.visibility = View.VISIBLE
            }
            val phq9Score = CommonUtils.getMentalHealthScoreWithRisk(data, DefinedParams.PHQ9)
            if (phq9Score != getString(R.string.separator_hyphen)) {
                binding.tvPHQ9Score.visibility = View.VISIBLE
                binding.tvPHQ9Score.text = "- $phq9Score"
                binding.btnStartPHQ9Assessment.text = getString(R.string.view_detail)
            }
            val gad7Score = CommonUtils.getMentalHealthScoreWithRisk(data, DefinedParams.GAD7)
            if (gad7Score != getString(R.string.separator_hyphen)) {
                binding.tvGAD7Score.visibility = View.VISIBLE
                binding.tvGAD7Score.text = "- $gad7Score"
                binding.btnStartGAD7Assessment.text = getString(R.string.view_detail)
            }
        } else
            binding.phq4Holder.visibility = View.VISIBLE
    }

    override fun onClick(mView: View?) {
        when (mView?.id) {
            binding.btnStartPHQ9Assessment.id -> {
                val isViewOnly =
                    binding.btnStartPHQ9Assessment.text == getString(R.string.view_detail)
                showMentalHealthAssessmentDialog(DefinedParams.PHQ9, isViewOnly)
            }
            binding.btnStartGAD7Assessment.id -> {
                val isViewOnly =
                    binding.btnStartGAD7Assessment.text == getString(R.string.view_detail)
                showMentalHealthAssessmentDialog(DefinedParams.GAD7, isViewOnly)
            }
            binding.btnStartPhq4Assessment.id -> {
                showMentalHealthAssessmentDialog(DefinedParams.PHQ4, false)
            }
            binding.btnEditPregnancy.id -> {
                patientViewModel.allowDismiss = false
                PregnantDetailsDialogFragment.newInstance(this).show(
                    childFragmentManager,
                    PregnantDetailsDialogFragment.TAG
                )
            }
        }
    }

    private fun showMentalHealthAssessmentDialog(assessmentType: String, isViewOnly: Boolean) {
        MentalHealthAssessmentDialog.newInstance(assessmentType = assessmentType, isViewOnly).show(
            childFragmentManager,
            MentalHealthAssessmentDialog.TAG
        )
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

    override fun onSubmit(gestationalAge: String?) {
        gestationalAge?.let { age ->
            patientViewModel.pregnancyDetails.postValue(age)
        }
    }

    companion object {
        const val TAG = "InitialEncounterFragment"
    }
}