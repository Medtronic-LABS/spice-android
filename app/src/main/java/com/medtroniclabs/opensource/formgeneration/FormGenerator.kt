package com.medtroniclabs.opensource.formgeneration

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Point
import android.text.*
import android.text.InputFilter.AllCaps
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.*
import com.medtroniclabs.opensource.common.*
import com.medtroniclabs.opensource.common.IntentConstants.INTENT_ID
import com.medtroniclabs.opensource.common.IntentConstants.INTENT_SPINNER_SELECTED_ITEM
import com.medtroniclabs.opensource.common.MedicalReviewConstant.DefaultIDLabel
import com.medtroniclabs.opensource.common.ViewUtil.showDatePicker
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.account.workflow.AccountWorkflow
import com.medtroniclabs.opensource.data.model.LocalSpinnerResponse
import com.medtroniclabs.opensource.data.ui.BPModel
import com.medtroniclabs.opensource.data.ui.MentalHealthOption
import com.medtroniclabs.opensource.databinding.*
import com.medtroniclabs.opensource.db.tables.*
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.ACTION_FORM_SUBMISSION
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.GONE
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.Gender
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.GlucoseId
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.INVISIBLE
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.SSP14
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.SSP16
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.VISIBLE
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_FORM_AGE
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_FORM_BP
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_FORM_BUTTON
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_FORM_CARD_FAMILY
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_FORM_CHEKBOX
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_FORM_DATEPICKER
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_FORM_EDITTEXT
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_FORM_RADIOGROUP
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_FORM_SPINNER
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_FORM_TAG
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_FORM_TEXTLABEL
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_HEIGHT
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_INSTRUCTION
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_METAL_HEALTH
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_SCALE_INDICATOR
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType.VIEW_TYPE_TIME
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.formgeneration.formsupport.adapter.BPResultAdapter
import com.medtroniclabs.opensource.formgeneration.formsupport.adapter.MentalHealthAdapter
import com.medtroniclabs.opensource.formgeneration.listener.FormEventListener
import com.medtroniclabs.opensource.formgeneration.model.ConditionalModel
import com.medtroniclabs.opensource.formgeneration.model.FormLayout
import com.medtroniclabs.opensource.formgeneration.model.FormResponseRootModel
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.ConditionModelConfig
import com.medtroniclabs.opensource.ui.indicator.interfaces.OnSeekChangeListener
import com.medtroniclabs.opensource.ui.indicator.views.SeekParams
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import com.medtroniclabs.opensource.ui.views.IndicatorSeekBar
import com.robertlevonyan.views.chip.Chip
import com.robertlevonyan.views.chip.OnCloseClickListener
import java.util.*
import kotlin.math.roundToInt


class FormGenerator(
    var context: Context,
    private val parentLayout: LinearLayout,
    private val resultLauncher: ActivityResultLauncher<Intent>,
    private val listener: FormEventListener,
    var scrollView: NestedScrollView? = null,
    val translate:Boolean = false
) : ContextWrapper(context) {

    private val rootSuffix = "rootView"

    private val errorSuffix = "errorMessageView"

    private val titleSuffix = "titleTextView"
    private val actionSuffix = "actionView"

    private val innerRootSuffix = "innerRootView"

    private val innerEntrySuffix = "innerEntryView"

    private val diastolicSuffix = "DiastolicSuffix"
    private val systolicSuffix = "SystolicSuffix"
    private val pulseSuffix = "PulseSuffix"

    private val tvKey = "summaryKey"
    private val tvValue = "summaryValue"
    private val rootSummary = "summaryRoot"


    private val resultHashMap = HashMap<String, Any>()

    private var serverData: List<FormLayout>? = null
    private var mentalHealthEditList: ArrayList<Map<String, Any>>? = null
    private var systolicAverageSummary: Int? = null
    private var diastolicAverageSummary: Int? = null
    private var phQ4Score: Int? = null
    private var fbsBloodGlucose: Double? = null
    private var rbsBloodGlucose: Double? = null
    private var focusNeeded: View? = null
    private var mentalHealthQuestions: ArrayList<MentalHealthOption>? = null

    private var selectedIndex: Int? = null

    private var EDITSCREEN: Boolean? = null

    fun populateViews(
        serverData: List<FormLayout>,
        mentalHealthEditList: ArrayList<Map<String, Any>>? = null
    ) {
        this.serverData = serverData
        this.mentalHealthEditList = mentalHealthEditList?.let {
            ArrayList(mentalHealthEditList)
        }
        parentLayout.removeAllViews()
        serverData.forEach { serverViewModel ->
            when (serverViewModel.viewType) {
                VIEW_TYPE_FORM_EDITTEXT -> createEditText(serverViewModel)
                VIEW_TYPE_FORM_CARD_FAMILY -> createCardViewFamily(serverViewModel)
                VIEW_TYPE_FORM_RADIOGROUP -> createRadioGroup(serverViewModel)
                VIEW_TYPE_FORM_DATEPICKER -> createDatePicker(serverViewModel)
                VIEW_TYPE_FORM_SPINNER -> createCustomSpinner(serverViewModel)
                VIEW_TYPE_FORM_BP -> createBPView(serverViewModel)
                VIEW_TYPE_FORM_TEXTLABEL -> createTextLabel(serverViewModel)
                VIEW_TYPE_FORM_AGE -> createAgeView(serverViewModel)
                VIEW_TYPE_FORM_CHEKBOX -> createCheckBoxView(serverViewModel)
                VIEW_TYPE_FORM_TAG -> createTagView(serverViewModel)
                VIEW_TYPE_TIME -> createTimeView(serverViewModel)
                VIEW_TYPE_METAL_HEALTH -> createMentalHealthView(serverViewModel)
                VIEW_TYPE_FORM_BUTTON -> createFormSubmitButton(serverViewModel)
                VIEW_TYPE_INSTRUCTION -> createInstructionView(serverViewModel)
                VIEW_TYPE_HEIGHT -> createHeightView(serverViewModel)
                VIEW_TYPE_SCALE_INDICATOR -> createScaleIndicatorView(serverViewModel)
            }
        }
    }


    fun populateEditableViews(
        serverData: List<FormLayout>,
        mentalHealthEditList: ArrayList<Map<String, Any>>? = null
    ) {
        this.serverData = serverData.filter { it.viewType != VIEW_TYPE_FORM_CARD_FAMILY && it.isEditable }
        this.mentalHealthEditList = mentalHealthEditList?.let {
            ArrayList(mentalHealthEditList)
        }
        EDITSCREEN = true
        parentLayout.removeAllViews()
        addEditableCards(serverData)
        this.serverData?.forEach { serverViewModel ->
                when (serverViewModel.viewType) {
                    VIEW_TYPE_FORM_EDITTEXT -> createEditText(serverViewModel)
                    VIEW_TYPE_FORM_RADIOGROUP -> createRadioGroup(serverViewModel)
                    VIEW_TYPE_FORM_DATEPICKER -> createDatePicker(serverViewModel)
                    VIEW_TYPE_FORM_SPINNER -> createCustomSpinner(serverViewModel)
                    VIEW_TYPE_FORM_BP -> createBPView(serverViewModel)
                    VIEW_TYPE_FORM_TEXTLABEL -> createTextLabel(serverViewModel)
                    VIEW_TYPE_FORM_AGE -> createAgeView(serverViewModel)
                    VIEW_TYPE_FORM_CHEKBOX -> createCheckBoxView(serverViewModel)
                    VIEW_TYPE_FORM_TAG -> createTagView(serverViewModel)
                    VIEW_TYPE_TIME -> createTimeView(serverViewModel)
                    VIEW_TYPE_METAL_HEALTH -> createMentalHealthView(serverViewModel)
                    VIEW_TYPE_FORM_BUTTON -> createFormSubmitButton(serverViewModel)
                    VIEW_TYPE_INSTRUCTION -> createInstructionView(serverViewModel)
                    VIEW_TYPE_HEIGHT -> createHeightView(serverViewModel)
                    VIEW_TYPE_SCALE_INDICATOR -> createScaleIndicatorView(serverViewModel)
                }
            }
    }

    private fun addEditableCards(serverData: List<FormLayout>) {
        serverData.forEach { data ->
            if (data.isEditable) {
                val list = serverData.find { it.id == data.family }
                if (list != null) {
                    createCardViewFamily(list)
                }
            }
        }
    }

    private fun createInstructionView(serverViewModel: FormLayout) {
        serverViewModel.apply {
            var instructionsList = instructions
            if(translate && !instructionsCulture.isNullOrEmpty())
                instructionsList = instructionsCulture

            if (!instructionsList.isNullOrEmpty()) {
                val binding = InstructionLayoutBinding.inflate(LayoutInflater.from(context))
                binding.root.tag = id + rootSuffix
                binding.tvTitle.tag = id
                if (translate){
                    binding.tvTitle.text = (titleCulture?:title)
                }else{
                    binding.tvTitle.text = title
                }
                binding.clInstructionRoot.safeClickListener {
                    val content = if(translate) instructionsCulture else instructions
                    content?.let {
                        listener.onBPInstructionClicked(
                            if (translate) (titleCulture ?: title) else title, it
                        )
                    }
                }
                getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                    parentLayout.addView(binding.root)
                }
                setViewVisibility(visibility, binding.root)
                setViewEnableDisable(isEnabled, binding.root)
            }
        }
    }


    private fun loadOtherData() {
        if (selectedIndex != null) {
            serverData?.forEachIndexed { index, serverViewModel ->
                if (index > selectedIndex!!) {
                    when (serverViewModel.viewType) {
                        VIEW_TYPE_FORM_EDITTEXT -> createEditText(serverViewModel)
                        VIEW_TYPE_FORM_CARD_FAMILY -> createCardViewFamily(serverViewModel)
                        VIEW_TYPE_FORM_RADIOGROUP -> createRadioGroup(serverViewModel)
                        VIEW_TYPE_FORM_DATEPICKER -> createDatePicker(serverViewModel)
                        VIEW_TYPE_FORM_SPINNER -> createCustomSpinner(serverViewModel)
                        VIEW_TYPE_FORM_BP -> createBPView(serverViewModel)
                        VIEW_TYPE_FORM_TEXTLABEL -> createTextLabel(serverViewModel)
                        VIEW_TYPE_FORM_AGE -> createAgeView(serverViewModel)
                        VIEW_TYPE_FORM_CHEKBOX -> createCheckBoxView(serverViewModel)
                        VIEW_TYPE_FORM_TAG -> createTagView(serverViewModel)
                        VIEW_TYPE_TIME -> createTimeView(serverViewModel)
                        VIEW_TYPE_METAL_HEALTH -> createMentalHealthView(serverViewModel)
                        VIEW_TYPE_FORM_BUTTON -> createFormSubmitButton(serverViewModel)
                        VIEW_TYPE_INSTRUCTION -> createInstructionView(serverViewModel)
                    }
                }
            }
        }
    }


    fun resetOtherView() {
        if (selectedIndex != null) {
            serverData?.forEachIndexed { index, formLayout ->
                if (index > selectedIndex!!) {
                    if (formLayout.family != null) {
                        getFamilyView(formLayout.family)?.removeView(getViewByTag(formLayout.id + rootSuffix))
                    } else {
                        parentLayout.removeView(getViewByTag(formLayout.id + rootSuffix))
                    }
                } else if (formLayout.isNeedAction) {
                    getViewByTag(formLayout.id + errorSuffix)?.let {
                        (it as TextView).apply {
                            text = ""
                            visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun createMentalHealthView(serverViewModel: FormLayout) {
        val binding = MentalHealthLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.rvMentalHealth.tag = id
            binding.rvMentalHealth.layoutManager = LinearLayoutManager(context)
            optionsList?.let {
                binding.rvMentalHealth.adapter = it.let {
                    MentalHealthAdapter(
                        getUIModel(it),
                        id,
                        editList = mentalHealthEditList,
                        translate = SecuredPreference.getIsTranslationEnabled()
                    ) { question, questionId, option, id, answerId, displayOrder ->
                        processMentalHealthResult(
                            id,
                            question,
                            questionId,
                            option,
                            answerId,
                            displayOrder
                        )
                    }
                }
            }

            localDataCache?.let {
                listener.loadLocalCache(id, it)
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private fun processMentalHealthResult(
        id: String,
        question: String,
        questionId: Long,
        option: String,
        answerId: Long,
        displayOrder: Long
    ) {
        if (resultHashMap.containsKey(id)) {
            val resultMap = resultHashMap[id] as HashMap<String, Any>
            if (resultMap.containsKey(question)) {
                val subMap = resultMap[question] as HashMap<String, Any>
                subMap[DefinedParams.Answer] = option
                subMap[DefinedParams.Question_Id] = questionId
                subMap[DefinedParams.Answer_Id] = answerId
                subMap[DefinedParams.Display_Order] = displayOrder
                resultMap[question] = subMap
            } else {
                val subMap = HashMap<String, Any>()
                subMap[DefinedParams.Answer] = option
                subMap[DefinedParams.Question_Id] = questionId
                subMap[DefinedParams.Answer_Id] = answerId
                subMap[DefinedParams.Display_Order] = displayOrder
                resultMap[question] = subMap
            }
        } else {
            val map = HashMap<String, Map<String, Any>>()
            val subMap = HashMap<String, Any>()
            subMap[DefinedParams.Answer] = option
            subMap[DefinedParams.Question_Id] = questionId
            subMap[DefinedParams.Answer_Id] = answerId
            subMap[DefinedParams.Display_Order] = displayOrder
            map[question] = subMap
            resultHashMap[id] = map
        }
    }

    private fun getUIModel(optionList: ArrayList<Map<String, Any>>): ArrayList<MentalHealthOption> {
        val optionListUI = ArrayList<MentalHealthOption>()
        optionList.forEach {
            optionListUI.add(MentalHealthOption(map = it))
        }
        return optionListUI
    }

    private fun createTimeView(serverViewModel: FormLayout) {
        val binding = TimeViewLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.tvTitle.tag = id + titleSuffix
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.etHour.tag = id
            binding.etMinute.tag = binding.etMinute.id
            binding.amBtn.tag = binding.amBtn.id
            binding.pmBtn.tag = binding.pmBtn.id
            binding.timeRadioGroup.tag = binding.timeRadioGroup.id
            binding.radioGrpDate.tag = binding.radioGrpDate.id
            binding.rbToday.tag = binding.rbToday.id
            binding.rbYesterday.tag = binding.rbYesterday.id

            if (translate){
                binding.tvTitle.text = titleCulture?:title
            }else{
                binding.tvTitle.text = title
            }

            if (isMandatory) {
                binding.tvTitle.markMandatory()
            }
            binding.etHour.addTextChangedListener {
                storeTimeValue(DefinedParams.Hour, it?.toString(), id)
            }
            binding.etMinute.addTextChangedListener {
                storeTimeValue(DefinedParams.Minute, it?.toString(), id)
            }
            binding.radioGrpDate.setOnCheckedChangeListener { _, checkedId ->
                rgDate(checkedId, serverViewModel, binding)
            }
            binding.timeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                rgTime(checkedId, serverViewModel, binding)
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root, true)
            setViewEnableDisable(isEnabled, binding.root, true)
        }
    }

    private fun rgTime(
        checkedId: Int,
        serverViewModel: FormLayout,
        binding: TimeViewLayoutBinding
    ) {
        if (checkedId <= 0) {
            storeTimeValue(
                DefinedParams.AM_PM,
                null,
                serverViewModel.id
            )
            changeRadioButtonStyle(binding.amBtn, false)
            changeRadioButtonStyle(binding.pmBtn, false)
        } else {
            storeTimeValue(
                DefinedParams.AM_PM,
                if (checkedId == binding.amBtn.id) DefinedParams.AM else DefinedParams.PM,
                serverViewModel.id
            )
            changeRadioButtonStyle(binding.amBtn, checkedId == binding.amBtn.id)
            changeRadioButtonStyle(binding.pmBtn, checkedId == binding.pmBtn.id)
        }
    }

    private fun rgDate(
        checkedId: Int,
        serverViewModel: FormLayout,
        binding: TimeViewLayoutBinding
    ) {
        if (checkedId <= 0) {
            storeTimeValue(
                DefinedParams.Last_Meal_Date,
                null,
                serverViewModel.id
            )
            changeRadioButtonStyle(binding.rbToday, false)
            changeRadioButtonStyle(binding.rbYesterday, false)
        } else {
            storeTimeValue(
                DefinedParams.Last_Meal_Date,
                if (checkedId == binding.rbToday.id) DefinedParams.Today else DefinedParams.Yesterday,
                serverViewModel.id
            )
            changeRadioButtonStyle(binding.rbToday, checkedId == binding.rbToday.id)
            changeRadioButtonStyle(binding.rbYesterday, checkedId == binding.rbYesterday.id)
        }
    }

    private fun storeTimeValue(key: String, value: String?, id: String) {
        if (resultHashMap.containsKey(id)) {
            if(resultHashMap[id] is Map<*,*>) {
                if (!value.isNullOrBlank()) {
                    (resultHashMap[id] as HashMap<String, String>)[key] = value
                } else {
                    (resultHashMap[id] as HashMap<String, String>).remove(key)
                }
            }
            else if(resultHashMap[id] is String)
            {
                resultHashMap.remove(id)
                storeTimeValue(key, value, id)
            }
        } else {
            if (!value.isNullOrBlank()) {
                val map = HashMap<String, String>()
                map[key] = value
                resultHashMap[id] = map
            }
        }
    }

    private fun createBPView(serverViewModel: FormLayout) {
        val binding = BpReadingLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.tvInstructionBloodPressure.tag = id + titleSuffix
            "${0}-${diastolicSuffix}".also { binding.etDiastolicOne.tag = it }
            "${0}-${systolicSuffix}".also { binding.etSystolicOne.tag = it }
            "${0}-${pulseSuffix}".also { binding.etPulseOne.tag = it }
            "${1}-${diastolicSuffix}".also { binding.etDiastolicTwo.tag = it }
            "${1}-${systolicSuffix}".also { binding.etSystolicTwo.tag = it }
            "${1}-${pulseSuffix}".also { binding.etPulseTwo.tag = it }
            "${2}-${diastolicSuffix}".also { binding.etDiastolicThree.tag = it }
            "${2}-${systolicSuffix}".also { binding.etSystolicThree.tag = it }
            "${2}-${pulseSuffix}".also { binding.etPulseThree.tag = it }
            binding.tvSnoReadingTwo.isEnabled = false
            binding.etSystolicTwo.isEnabled = false
            binding.etDiastolicTwo.isEnabled = false
            binding.etPulseTwo.isEnabled = false
            binding.separatorRowTwo.isEnabled = false
            binding.tvSnoReadingThree.isEnabled = false
            binding.etSystolicThree.isEnabled = false
            binding.etDiastolicThree.isEnabled = false
            binding.etPulseThree.isEnabled = false
            binding.separatorRowThree.isEnabled = false
            val list = DefinedParams.getEmptyBPReading(totalCount ?: 2)
            resultHashMap[id] = list
            binding.instructionsLayout.safeClickListener {
                var instructionsList = instructions
                if(translate && !instructionsCulture.isNullOrEmpty())
                    instructionsList = instructionsCulture
                instructionsList?.let {
                    listener.onBPInstructionClicked(
                        if (translate) (titleCulture ?: title) else title,
                        it,
                        getString(R.string.bp_measure)
                    )
                }
            }
            if (list.size > 2) {
                binding.bpReadingThree.visibility = View.VISIBLE
            } else {
                binding.bpReadingThree.visibility = View.GONE
            }

            val inputFilter = arrayListOf<InputFilter>()
            maxLength?.let {
                inputFilter.add(InputFilter.LengthFilter(it))
            }
            if (inputFilter.isNotEmpty()) {
                binding.etSystolicOne.filters = inputFilter.toTypedArray()
                binding.etDiastolicOne.filters = inputFilter.toTypedArray()
                binding.etPulseOne.filters = inputFilter.toTypedArray()
                binding.etSystolicTwo.filters = inputFilter.toTypedArray()
                binding.etDiastolicTwo.filters = inputFilter.toTypedArray()
                binding.etPulseTwo.filters = inputFilter.toTypedArray()
                binding.etSystolicThree.filters = inputFilter.toTypedArray()
                binding.etDiastolicThree.filters = inputFilter.toTypedArray()
                binding.etPulseThree.filters = inputFilter.toTypedArray()
            }

            binding.etSystolicOne.addTextChangedListener {
                checkInputsAndEnableNextField(binding.tvSnoReading.text, binding, list)
            }
            binding.etDiastolicOne.addTextChangedListener {
                checkInputsAndEnableNextField(binding.tvSnoReading.text, binding, list)
            }
            binding.etPulseOne.addTextChangedListener {
                checkInputsAndEnableNextField(binding.tvSnoReading.text, binding, list)
            }

            binding.etSystolicTwo.addTextChangedListener {
                checkInputsAndEnableNextField(binding.tvSnoReadingTwo.text, binding, list)
            }
            binding.etDiastolicTwo.addTextChangedListener {
                checkInputsAndEnableNextField(binding.tvSnoReadingTwo.text, binding, list)
            }
            binding.etPulseTwo.addTextChangedListener {
                checkInputsAndEnableNextField(binding.tvSnoReadingTwo.text, binding, list)
            }
            binding.etSystolicThree.addTextChangedListener {
                checkInputsAndEnableNextField(binding.tvSnoReadingThree.text, binding, list)
            }
            binding.etDiastolicThree.addTextChangedListener {
                checkInputsAndEnableNextField(binding.tvSnoReadingThree.text, binding, list)
            }
            binding.etPulseThree.addTextChangedListener {
                checkInputsAndEnableNextField(binding.tvSnoReadingThree.text, binding, list)
            }

            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            // setViewEnableDisable(isEnabled, binding.root)
        }
    }

    fun checkInputsAndEnableNextField(
        text: CharSequence,
        binding: BpReadingLayoutBinding,
        list: ArrayList<BPModel>
    ) {
      if (text == getString(R.string.sno_1)) {
            val systolicReadingOne = binding.etSystolicOne.text?.toString()
            val diastolicReadingOne = binding.etDiastolicOne.text?.toString()
            val pulseReadingOne = binding.etPulseOne.text?.toString()
            if (list.size > 0) {
                val model = list[0]
                model.systolic = systolicReadingOne?.toDoubleOrNull()
                model.diastolic = diastolicReadingOne?.toDoubleOrNull()
                model.pulse = pulseReadingOne?.toDoubleOrNull()
            }
            if (!systolicReadingOne.isNullOrBlank() && !diastolicReadingOne.isNullOrBlank()) {
                binding.tvSnoReadingTwo.isEnabled = true
                binding.etSystolicTwo.isEnabled = true
                binding.etDiastolicTwo.isEnabled = true
                binding.etPulseTwo.isEnabled = true
                binding.separatorRowTwo.isEnabled = true
            }
        } else if (text == getString(R.string.sno_2)) {
            val systolicReading = binding.etSystolicTwo.text?.toString()
            val diastolicReading = binding.etDiastolicTwo.text?.toString()
            val pulseReading = binding.etPulseTwo.text?.toString()
            if (list.size > 1) {
                val model = list[1]
                model.systolic = systolicReading?.toDoubleOrNull()
                model.diastolic = diastolicReading?.toDoubleOrNull()
                model.pulse = pulseReading?.toDoubleOrNull()
            }
            if (!systolicReading.isNullOrBlank() && !diastolicReading.isNullOrBlank()) {
                binding.tvSnoReadingThree.isEnabled = true
                binding.etSystolicThree.isEnabled = true
                binding.etDiastolicThree.isEnabled = true
                binding.etPulseThree.isEnabled = true
                binding.separatorRowThree.isEnabled = true
            }
        } else if (text == getString(R.string.sno_3)) {
            val systolicReading = binding.etSystolicThree.text?.toString()
            val diastolicReading = binding.etDiastolicThree.text?.toString()
            val pulseReading = binding.etPulseThree.text?.toString()
            if (list.size > 2) {
                val model = list[2]
                model.systolic = systolicReading?.toDoubleOrNull()
                model.diastolic = diastolicReading?.toDoubleOrNull()
                model.pulse = pulseReading?.toDoubleOrNull()
            }
        }
    }

    private fun createTagView(serverViewModel: FormLayout) {
        val binding = TagviewLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            if (translate){
                binding.tvTitle.text = titleCulture?:title
            }else{
                binding.tvTitle.text = title
            }
            binding.tvTitle.tag = id + titleSuffix
            binding.ivAdd.safeClickListener {
                val userInput = binding.etUserInput.text
                if (userInput.isNullOrBlank()) {
                    return@safeClickListener
                } else {
                    if (resultHashMap.containsKey(id)) {
                        val list = resultHashMap[id] as ArrayList<String>
                        if (!list.contains(userInput.toString())) {
                            list.add(userInput.toString())
                            createChipViews(binding.tagViewGroup, id)
                        }
                    } else {
                        val list = ArrayList<String>()
                        list.add(userInput.toString())
                        resultHashMap[id] = list
                        createChipViews(binding.tagViewGroup, id)
                    }
                }
                binding.etUserInput.setText("")
                context.hideKeyboard(binding.etUserInput)
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            // setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private fun createChipViews(tagViewGroup: ChipGroup, id: String) {
        tagViewGroup.removeAllViews()
        val list = resultHashMap[id] as ArrayList<*>
        list.forEach { label ->
            if (label is String) {
                val chip = Chip(context)
                chip.setText(label)
                chip.chipBackgroundColor = ContextCompat.getColor(context, R.color.cobalt_blue)
                chip.chipTextColor = ContextCompat.getColor(context, R.color.white)
                chip.closable = true
                chip.textSizeSsp = SSP16
                chip.chipCloseColor = ContextCompat.getColor(context, R.color.white)
                chip.onCloseClickListener = OnCloseClickListener {
                    if (resultHashMap.containsKey(id)) {
                        resultHashMap[id] = list.filter { it != label }
                        createChipViews(tagViewGroup, id)
                    }
                }
                tagViewGroup.addView(chip)
            }
        }
    }

    private fun createFormSubmitButton(serverViewModel: FormLayout) {
        val binding = ButtonLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.actionButton.tag = id
            if (translate){
                binding.actionButton.text = titleCulture?:title
            }else{
                binding.actionButton.text = title
            }
            binding.actionButton.safeClickListener {
                when (action) {
                    ACTION_FORM_SUBMISSION -> {
                        formSubmitAction(binding.actionButton)
                    }
                }
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
        listener.onRenderingComplete()
    }

    fun formSubmitAction(view: View) {
        if (validateInputs()) {
            hideKeyboard(view)
            listener.onFormSubmit(resultHashMap, serverData = serverData)
        } else {
            focusNeeded?.let { focusNeeded ->
                scrollView?.let { scrollView ->
                    scrollToView(scrollView, focusNeeded)
                }
            }
            listener.onFormError()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        focusNeeded = null
        serverData?.forEach { serverViewModel ->
            serverViewModel.apply {
                if ((isMandatory && !resultHashMap.containsKey(id)
                            && isViewVisible(id) && isViewEnabled(id, viewType) )
                            ||
                            (isMandatory && resultHashMap.containsKey(id)
                                    && resultHashMap[id] is String && (resultHashMap[id] as String).isEmpty())
                ) {
                    isValid = false
                    requestFocusView(serverViewModel)
                } else {
                    when (serverViewModel.viewType) {
                        VIEW_TYPE_FORM_BP -> {
                            val list = resultHashMap[id] as ArrayList<BPModel>
                            val validationBPResultModel =
                                Validator.checkValidBPInput(context, list, serverViewModel, translate)
                            if (validationBPResultModel.status) {
                                calculateBPValues(formLayout = serverViewModel, resultHashMap)
                                hideValidationField(serverViewModel)
                            } else {
                                isValid = false
                                requestFocusView(serverViewModel, validationBPResultModel.message)
                            }
                        }
                        VIEW_TYPE_TIME -> {
                            if (!resultHashMap.containsKey(DefinedParams.GlucoseLog)) {
                                if (isViewEnabled(id)) {
                                    if (resultHashMap.containsKey(DefinedParams.BloodGlucoseID)) {
                                        if (resultHashMap.containsKey(id)) {
                                            if(resultHashMap[id] is HashMap<*, *>){
                                                val map = resultHashMap[id] as HashMap<*, *>
                                                if (map.containsKey(DefinedParams.AM_PM) && map.containsKey(
                                                        DefinedParams.Minute
                                                    ) && map.containsKey(DefinedParams.Hour)
                                                ) {
                                                    if ((map[DefinedParams.Hour] is String && ((map[DefinedParams.Hour] as String).toInt() > DefinedParams.MaxHourLimit || (map[DefinedParams.Hour] as String).toInt() == 0)) ||
                                                        (map[DefinedParams.Minute] is String && (map[DefinedParams.Minute] as String).toInt() > DefinedParams.MaxMinutesLimit)
                                                    ) {
                                                        isValid = false
                                                        requestFocusView(serverViewModel)
                                                    } else {
                                                        if (!map.containsKey(DefinedParams.Last_Meal_Date)) {
                                                            isValid = false
                                                            requestFocusView(serverViewModel)
                                                        } else {
                                                            if (id == DefinedParams.lastMealTime) {
                                                                CommonUtils.processLastMealTime(
                                                                    resultHashMap,
                                                                    isFromValidation = true
                                                                )?.let {
                                                                    val firstDate = Date(it)
                                                                    val secondDate =
                                                                        Date(System.currentTimeMillis())
                                                                    when {
                                                                        firstDate.after(secondDate) -> {
                                                                            isValid = false
                                                                            requestFocusView(
                                                                                serverViewModel
                                                                            )
                                                                        }
                                                                        (firstDate.before(secondDate) || firstDate == secondDate) -> {
                                                                            hideValidationField(
                                                                                serverViewModel
                                                                            )
                                                                        }
                                                                    }
                                                                } ?: kotlin.run {
                                                                    hideValidationField(
                                                                        serverViewModel
                                                                    )
                                                                }
                                                            } else {
                                                                hideValidationField(serverViewModel)
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    isValid = false
                                                    requestFocusView(serverViewModel)
                                                }
                                            }
                                            else hideValidationField(serverViewModel)
                                        } else {
                                            isValid = false
                                            requestFocusView(serverViewModel)
                                        }
                                    } else {
                                        isValid = false
                                        if (focusNeeded == null) {
                                            focusNeeded =
                                                showValidationMessageBasedOnId(DefinedParams.BloodGlucoseID)
                                        } else {
                                            showValidationMessageBasedOnId(DefinedParams.BloodGlucoseID)
                                        }
                                    }
                                } else {
                                    resetTimeViewResultMap()
                                    hideValidationField(serverViewModel)
                                }
                            } else {
                                hideValidationField(serverViewModel)
                            }
                        }
                        VIEW_TYPE_METAL_HEALTH -> {
                            if (checkValidMentalHealth(this, id)) {
                                hideValidationField(serverViewModel)
                            } else {
                                isValid = false
                                requestFocusView(serverViewModel)
                            }
                        }
                        VIEW_TYPE_FORM_AGE -> {
                            if (SecuredPreference.getUnitMeasurementType() == DefinedParams.Unit_Measurement_Imperial_Type) {
                                if (resultHashMap.containsKey(DefinedParams.Age) && resultHashMap.containsKey(
                                        DefinedParams.DateOfBirth
                                    )
                                ) {
                                    val age = resultHashMap[DefinedParams.Age] as Double?
                                    if (age != null && age >= DefinedParams.MinimumAge && age <= DefinedParams.MaximumAge) {
                                        hideValidationField(serverViewModel)
                                    } else {
                                        isValid = false
                                        requestFocusView(
                                            serverViewModel, getString(
                                                R.string.general_min_max_validation,
                                                DefinedParams.MinimumAge.toString(),
                                                DefinedParams.MaximumAge.toString()
                                            )
                                        )
                                    }
                                } else {
                                    isValid = false
                                    requestFocusView(serverViewModel)
                                }
                            } else {
                                if (resultHashMap.containsKey(DefinedParams.Age)) {
                                    val age = resultHashMap[DefinedParams.Age] as Double?
                                    if (age != null && age >= DefinedParams.MinimumAge && age <= DefinedParams.MaximumAge) {
                                        hideValidationField(serverViewModel)
                                    } else {
                                        isValid = false
                                        requestFocusView(
                                            serverViewModel, getString(
                                                R.string.general_min_max_validation,
                                                DefinedParams.MinimumAge.toString(),
                                                DefinedParams.MaximumAge.toString()
                                            )
                                        )
                                    }
                                } else {
                                    isValid = false
                                    requestFocusView(serverViewModel)
                                }
                            }
                        }
                        VIEW_TYPE_HEIGHT -> {
                            if (SecuredPreference.getUnitMeasurementType() == DefinedParams.Unit_Measurement_Imperial_Type) {
                                if (resultHashMap.containsKey(DefinedParams.Height)) {
                                    if (resultHashMap[DefinedParams.Height] is Map<*, *>) {
                                        val heightMap =
                                            resultHashMap[DefinedParams.Height] as HashMap<String, Any>
                                        if (heightMap.containsKey(DefinedParams.Feet) && heightMap.containsKey(
                                                DefinedParams.Inches
                                            )
                                        ) {
                                            val feet = heightMap[DefinedParams.Feet] as Double
                                            val inches = heightMap[DefinedParams.Inches] as Double
                                            if (CommonUtils.validateFeetAndInches(feet, inches)) {
                                                isValid = false
                                                requestFocusView(
                                                    serverViewModel
                                                )
                                            } else {
                                                hideValidationField(serverViewModel)
                                            }
                                        } else {
                                            isValid = false
                                            requestFocusView(
                                                serverViewModel
                                            )
                                        }
                                    } else {
                                        isValid = false
                                        requestFocusView(
                                            serverViewModel,
                                            context.getString(R.string.check_form_attribute)
                                        )
                                        listener.onJsonMismatchError()
                                    }
                                } else {
                                    hideValidationField(serverViewModel)
                                }
                            } else {
                                if (resultHashMap.containsKey(DefinedParams.Height)) {
                                    val height = resultHashMap[DefinedParams.Height]
                                    if (height is Map<*, *>) {
                                        isValid = false
                                        requestFocusView(
                                            serverViewModel,
                                            context.getString(R.string.check_form_attribute)
                                        )
                                        listener.onJsonMismatchError()
                                    } else {
                                        hideValidationField(serverViewModel)
                                    }
                                }
                            }
                        }
                        else -> {
                            serverViewModel.apply {
                                if (resultHashMap.containsKey(id)) {
                                    val actualValue = resultHashMap[id]
                                    if (EDITSCREEN == true &&actualValue is String && actualValue.isEmpty()&&!isMandatory){
                                        hideValidationField(serverViewModel)
                                    }else if( id == DefinedParams.htnYearOfDiagnosis || id == DefinedParams.diabetesYearOfDiagnosis){
                                        if (actualValue is Number ){
                                            val cal = Calendar.getInstance()
                                            val calYr = cal.get(Calendar.YEAR)
                                            val actualValueString = CommonUtils.getDecimalFormatted(actualValue)
                                            if (actualValue.toDouble() > calYr || actualValueString.length != contentLength) {
                                                isValid = false
                                                requestFocusView(
                                                    serverViewModel
                                                )
                                            } else {
                                                hideValidationField(serverViewModel)
                                            }
                                        }else if (actualValue is String) {
                                            val cal = Calendar.getInstance()
                                            val calYr = cal.get(Calendar.YEAR)
                                            val actualValueString = CommonUtils.getDecimalFormatted(actualValue)
                                            val actualDoubleValue = actualValue.toDoubleOrNull()
                                            if (actualDoubleValue == null || actualDoubleValue > calYr || actualValueString.length != contentLength) {
                                                isValid = false
                                                requestFocusView(
                                                    serverViewModel
                                                )
                                            } else {
                                                hideValidationField(serverViewModel)
                                            }
                                        }
                                    } else {
                                        if (minLength != null && viewType == VIEW_TYPE_FORM_EDITTEXT
                                            && actualValue != null && actualValue is String
                                            && actualValue.length < minLength!!
                                        ) {
                                            isValid = false
                                            requestFocusView(
                                                serverViewModel,
                                                getString(
                                                    R.string.min_char_length_validation,
                                                    minLength!!.toString()
                                                )
                                            )
                                        } else if (maxValue != null || minValue != null) {
                                            if (maxValue != null && minValue != null) {
                                                if (actualValue is String) {
                                                    actualValue.toDoubleOrNull()?.let { value ->
                                                        if (value < minValue!! || value > maxValue!!) {
                                                            isValid = false
                                                            requestFocusView(
                                                                serverViewModel,
                                                                getString(
                                                                    R.string.general_min_max_validation,
                                                                    CommonUtils.getDecimalFormatted(minValue!!),
                                                                    CommonUtils.getDecimalFormatted(maxValue!!)
                                                                )
                                                            )
                                                        } else {
                                                            hideValidationField(serverViewModel)
                                                        }
                                                    }
                                                } else if (actualValue is Number) {
                                                    actualValue.toDouble().let { value ->
                                                        if (value < minValue!! || value > maxValue!!) {
                                                            isValid = false
                                                            requestFocusView(
                                                                serverViewModel,
                                                                getString(
                                                                    R.string.general_min_max_validation,
                                                                    CommonUtils.getDecimalFormatted(minValue!!),
                                                                    CommonUtils.getDecimalFormatted(maxValue!!)
                                                                )
                                                            )
                                                        } else {
                                                            hideValidationField(serverViewModel)
                                                        }
                                                    }
                                                } else {
                                                    hideValidationField(serverViewModel)
                                                }
                                            } else if (minValue != null) {
                                                if (actualValue is String) {
                                                    actualValue.toDoubleOrNull()?.let { value ->
                                                        if (value < minValue!!) {
                                                            isValid = false
                                                            requestFocusView(
                                                                serverViewModel,
                                                                getString(
                                                                    R.string.general_min_validation,
                                                                    CommonUtils.getDecimalFormatted(minValue!!)
                                                                )
                                                            )
                                                        } else {
                                                            hideValidationField(serverViewModel)
                                                        }
                                                    }
                                                } else if (actualValue is Number) {
                                                    actualValue.toDouble().let { value ->
                                                        if (value < minValue!!) {
                                                            isValid = false
                                                            requestFocusView(
                                                                serverViewModel,
                                                                getString(
                                                                    R.string.general_min_validation,
                                                                    CommonUtils.getDecimalFormatted(minValue!!)
                                                                )
                                                            )
                                                        } else {
                                                            hideValidationField(serverViewModel)
                                                        }
                                                    }
                                                } else {
                                                    hideValidationField(serverViewModel)
                                                }
                                            } else if (maxValue != null) {
                                                if (actualValue is String) {
                                                    actualValue.toDoubleOrNull()?.let { value ->
                                                        if (value > maxValue!!.toDouble()) {
                                                            isValid = false
                                                            requestFocusView(
                                                                serverViewModel,
                                                                getString(
                                                                    R.string.general_max_validation,
                                                                    CommonUtils.getDecimalFormatted(maxValue!!)
                                                                )
                                                            )
                                                        } else {
                                                            hideValidationField(serverViewModel)
                                                        }
                                                    }
                                                } else if (actualValue is Number) {
                                                    actualValue.toDouble().let { value ->
                                                        if (value > maxValue!!.toDouble()) {
                                                            isValid = false
                                                            requestFocusView(
                                                                serverViewModel,
                                                                getString(
                                                                    R.string.general_max_validation,
                                                                    CommonUtils.getDecimalFormatted(maxValue!!)
                                                                )
                                                            )
                                                        } else {
                                                            hideValidationField(serverViewModel)
                                                        }
                                                    }
                                                } else {
                                                    hideValidationField(serverViewModel)
                                                }
                                            }
                                        } else if (contentLength != null) {
                                            if (actualValue is Number) {
                                                val actualValueString = CommonUtils.getDecimalFormatted(actualValue)
                                                if (contentLength == actualValueString.length) {
                                                    hideValidationField(serverViewModel)
                                                } else {
                                                    isValid = false
                                                    requestFocusView(serverViewModel)
                                                }
                                            } else {
                                                val actualValueString = actualValue.toString()
                                                if (contentLength == actualValueString.length) {
                                                    hideValidationField(serverViewModel)
                                                } else {
                                                    isValid = false
                                                    requestFocusView(serverViewModel)
                                                }
                                            }
                                        } else {
                                            hideValidationField(serverViewModel)
                                        }
                                    }
                                } else {
                                    hideValidationField(serverViewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
        return isValid
    }

    private fun resetTimeViewResultMap() {
        if (resultHashMap.containsKey(DefinedParams.lastMealTime)) {
            resultHashMap.remove(DefinedParams.lastMealTime)
        }
    }

    private fun requestFocusView(serverViewModel: FormLayout, message: String? = null) {
        if (focusNeeded == null) {
            focusNeeded = showValidationMessage(serverViewModel, message)
        } else {
            showValidationMessage(serverViewModel, message)
        }
    }

    private fun checkValidMentalHealth(formLayout: FormLayout, id: String): Boolean {

        formLayout.optionsList?.forEach { option ->
            val isMandatory =
                (option[DefinedParams.isMandatory] as Boolean?) ?: false
            if (isMandatory) {
                if (resultHashMap.containsKey(id)) {
                    val map = resultHashMap[id] as HashMap<String, String>
                    val question = (option[DefinedParams.NAME] as String?) ?: ""
                    if (!map.containsKey(question)) {
                        return false
                    }
                } else {
                    return false
                }
            }
        }

        if (mentalHealthQuestions == null)
            listener.loadLocalCache(id, localDataCache = DefinedParams.Fetch_MH_Questions)

        mentalHealthQuestions(id)?.let {
            return it
        }

        return true
    }

    private fun mentalHealthQuestions(id: String): Boolean? {
        mentalHealthQuestions?.forEach { option ->
            val isMandatory =
                (option.map[DefinedParams.Mandatory] as Boolean?) ?: false
            if (isMandatory) {
                if (resultHashMap.containsKey(id)) {
                    val map = resultHashMap[id] as HashMap<String, String>
                    val question = (option.map[DefinedParams.Questions] as String?) ?: ""
                    if (!map.containsKey(question)) {
                        return false
                    }
                } else {
                    return false
                }
            }
        }
        return null
    }

    private fun isViewEnabled(id: String, viewType: String? = null): Boolean {
        return when(viewType) {
            VIEW_TYPE_HEIGHT -> {
                val feetView = getViewByTag(id + DefinedParams.Feet)
                val inchesView = getViewByTag(id + DefinedParams.Inches)
                (feetView != null && feetView.isEnabled && inchesView != null && inchesView.isEnabled)
            }
            else -> {
                val view = getViewByTag(id)
                view != null && view.isEnabled
            }
        }
    }

    private fun isViewVisible(id: String): Boolean {
        val view = getViewByTag(id + rootSuffix)
        return view != null && view.visibility == View.VISIBLE
    }

    private fun hideValidationField(serverViewModel: FormLayout) {
        serverViewModel.apply {
            val view = getViewByTag(serverViewModel.id + errorSuffix)
            if (view != null && view is TextView && view.visibility == View.VISIBLE) {
                view.visibility = View.GONE
            }
        }
    }

    private fun showValidationMessage(serverViewModel: FormLayout, message: String? = null): View? {
        serverViewModel.apply {
            val view = getViewByTag(serverViewModel.id + errorSuffix)
            if (view is TextView) {
                view.visibility = View.VISIBLE
                if (message != null) {
                    view.text = message
                } else {
                    view.text = getErrorMessageFromJSON(errorMessage, cultureErrorMessage)
                }
                return getViewByTag(serverViewModel.id + titleSuffix)
            }
        }
        return null
    }

    private fun getErrorMessageFromJSON(
        errorMessage: String?,
        cultureErrorMessage: String?
    ): String {

        return if (translate && !(cultureErrorMessage.isNullOrEmpty() || cultureErrorMessage.isBlank()))
            cultureErrorMessage
        else if (!(errorMessage.isNullOrEmpty() || errorMessage.isBlank()))
            errorMessage
        else getString(R.string.default_user_input_error)
    }

    private fun showValidationMessageBasedOnId(id: String, message: String? = null): View? {
        val view = getViewByTag(id + errorSuffix)
        if (view is TextView) {
            view.visibility = View.VISIBLE
            if (message != null) {
                view.text = message
            } else {
                view.text = getString(R.string.default_user_input_error)
            }
            return getViewByTag(id + titleSuffix)
        }
        return null
    }

    private fun createCheckBoxView(serverViewModel: FormLayout) {
        val binding = CheckboxLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.checkBoxRoot.tag = id
            binding.tvTitle.tag = id + titleSuffix
            binding.tvErrorMessage.tag = id + errorSuffix
            if (translate){
                binding.tvTitle.text = titleCulture?:title
            }else{
                binding.tvTitle.text = title
            }
            optionsList?.forEachIndexed { index, map ->
                val name = map[DefinedParams.NAME]
                if (name != null && name is String) {
                    val checkBox = CheckBox(binding.root.context)
                    val layoutParamsCompat = LinearLayout.LayoutParams(
                        RadioGroup.LayoutParams.WRAP_CONTENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT
                    )
                    layoutParamsCompat.setMargins(0, 30.dp, 0, 30.dp)
                    checkBox.layoutParams = layoutParamsCompat
                    checkBox.text = name
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-android.R.attr.state_enabled),
                            intArrayOf(android.R.attr.state_enabled)
                        ), intArrayOf(
                            ContextCompat.getColor(context, R.color.purple),  // disabled
                            ContextCompat.getColor(context, R.color.purple) // enabled
                        )
                    )
                    checkBox.buttonTintList = colorStateList
                    checkBox.invalidate()
                    checkBox.setTextColor(ContextCompat.getColor(context, R.color.navy_blue))
                    checkBox.textSizeSsp = SSP14
                    checkBox.id = index
                    checkBox.setOnCheckedChangeListener { _, boolean ->
                        if (boolean) {
                            if (resultHashMap.containsKey(id)) {
                                val list = resultHashMap[id] as ArrayList<Map<String, Any>>
                                val selectedMap =
                                    list.find { it[DefinedParams.ID] == map[DefinedParams.ID] }
                                if (selectedMap == null) {
                                    list.add(map)
                                }
                            } else {
                                val list = ArrayList<Map<String, Any>>()
                                list.add(map)
                                resultHashMap[id] = list
                            }
                        } else {
                            if (resultHashMap.containsKey(id)) {
                                val list = resultHashMap[id] as ArrayList<Map<String, Any>>
                                val selectedMap =
                                    list.find { it[DefinedParams.ID] == map[DefinedParams.ID] }
                                if (selectedMap != null) {
                                    list.remove(map)
                                }
                                if (list.isEmpty()) {
                                    resultHashMap.remove(id)
                                }
                            }
                        }
                    }
                    binding.checkBoxRoot.addView(checkBox)
                }
            }
            orientation?.let {
                binding.checkBoxRoot.orientation = it
            }

            if (isMandatory)
                binding.tvTitle.markMandatory()

            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private fun createHeightView(serverViewModel: FormLayout) {
        serverViewModel.apply {
            val binding = HeightFeetInchesLayoutBinding.inflate(LayoutInflater.from(context))
            binding.root.tag = id + rootSuffix
            binding.etFeet.tag = id + DefinedParams.Feet
            binding.etInches.tag = id + DefinedParams.Inches
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.tvHeight.tag = id + titleSuffix
            binding.tvHeight.text = title
            if (isMandatory) {
                binding.tvHeight.markMandatory()
            }
            binding.etFeet.addTextChangedListener {
                if (it.isNullOrBlank()) {
                    removeHeightInformation(DefinedParams.Feet, id)
                } else {
                    saveHeightInformation(it.toString(), DefinedParams.Feet, id)
                }
            }
            binding.etInches.addTextChangedListener {
                if (it.isNullOrBlank()) {
                    removeHeightInformation(DefinedParams.Inches, id)
                } else {
                    saveHeightInformation(it.toString(), DefinedParams.Inches, id)
                }
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private fun createScaleIndicatorView(serverViewModel: FormLayout) {
        serverViewModel.apply {
            val binding = ScaleIndicatorLayoutBinding.inflate(LayoutInflater.from(context))
            binding.root.tag = id + rootSuffix
            binding.indicatorSeekBar.tag = id
            binding.tvIsb.text = title
            binding.tvErrorMessage.tag = id + errorSuffix

            if (isMandatory) {
                binding.tvIsb.markMandatory()
            }

            val start: Float = (serverViewModel.startValue ?: 0).toFloat()
            val end: Float = (serverViewModel.endValue ?: 0).toFloat()
            val interval: Float = (serverViewModel.interval ?: 0).toFloat()

            binding.indicatorSeekBar.apply {
                if (start >= 0 && end > 0 && interval > 0) {
                    val dots = ((end - start).div(interval) + 1).roundToInt()
                    if (dots <= 50) {
                        min = start
                        max = end
                        tickCount = dots
                    }
                    isAboveUpperLimit = serverViewModel.isAboveUpperLimit
                }
            }

            binding.indicatorSeekBar.onSeekChangeListener = object : OnSeekChangeListener {
                override fun onSeeking(seekParams: SeekParams?) {
                    val progress = seekParams?.progress?.toDouble() ?: 0.0
                    resultHashMap[id] = progress
                    if (progress.toFloat() == start) {
                        resultHashMap.remove(id)
                    }
                }

                override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
                    /**
                     * this is default override method
                     */
                }

                override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                    /**
                     * this is default override method
                     */
                }
            }

            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private fun removeHeightInformation(key: String, id: String) {
        if (resultHashMap.containsKey(id)) {
            val map = resultHashMap[id]
            if (map is HashMap<*, *>) {
                map.remove(key)
                if (map.isEmpty()) {
                    resultHashMap.remove(id)
                }
            }
        }
    }

    private fun saveHeightInformation(value: String, key: String, id: String) {
        if (resultHashMap.containsKey(id)) {
            val map = resultHashMap[id] as HashMap<String, Any>
            map[key] = value.toDouble()
        } else {
            val map = HashMap<String, Any>()
            map[key] = value.toDouble()
            resultHashMap[id] = map
        }
    }

    var isDOBNeedToChange: Boolean = true
    private fun createAgeView(serverViewModel: FormLayout) {
        val binding = AgeDobLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.etDateOfBirth.tag = DefinedParams.DateOfBirth
            binding.etAgeInYears.inputType = 2
            binding.etAgeInYears.tag = id
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.tvDateOfBirth.tag = id + titleSuffix
            binding.etDateOfBirth.safeClickListener {
                val yearMonthDate =
                    DateUtils.getYearMonthAndDate(binding.etDateOfBirth.text.toString())
                openDatePicker(
                    serverViewModel,
                    binding.etDateOfBirth,
                    binding.etAgeInYears,
                    yearMonthDate.first,
                    yearMonthDate.second,
                    yearMonthDate.third
                )
            }

            binding.etAgeInYears.addTextChangedListener { age ->
                if (isDOBNeedToChange) {
                    binding.etDateOfBirth.text = ""
                    removeDOB()
                }
                isDOBNeedToChange = true
                if (age.isNullOrBlank()) {
                    removeMapValue()
                    setConditionalVisibility(serverViewModel, null)
                } else {
                    addOrUpdateAgeValue(age.toString())
                    setConditionalVisibility(serverViewModel, age.toString())
                }
            }

            if (isMandatory) {
                binding.tvAge.markMandatory()
                if (SecuredPreference.getUnitMeasurementType() == DefinedParams.Unit_Measurement_Imperial_Type)
                    binding.tvDateOfBirth.markMandatory()
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private fun createTextLabel(serverViewModel: FormLayout) {
        val binding = TextLabelLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.tvTitle.tag = id
            if (translate){
                binding.tvTitle.text = titleCulture?:title
            }else{
                binding.tvTitle.text = title
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private fun removeMapValue() {
        if (resultHashMap.containsKey(DefinedParams.Age)) {
            resultHashMap.remove(DefinedParams.Age)
        }
    }

    private fun addOrUpdateAgeValue(age: String) {
        resultHashMap[DefinedParams.Age] = if (age.isNotEmpty()) age.toDouble() else 0
    }

    private fun addOrUpdateDOB(dateOfBirth: String) {
        resultHashMap[DefinedParams.DateOfBirth] = dateOfBirth
    }


    private fun removeDOB() {
        if (resultHashMap.containsKey(DefinedParams.DateOfBirth)) {
            resultHashMap.remove(DefinedParams.DateOfBirth)
        }
    }

    private fun createCustomSpinner(serverViewModel: FormLayout) {
        val binding = CustomSpinnerBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.etUserInput.tag = id
            binding.tvTitle.tag = id + titleSuffix
            binding.tvErrorMessage.tag = id + errorSuffix
            if (translate){
                binding.tvTitle.text = titleCulture?:title
            }else{
                binding.tvTitle.text = title
            }
            val dropDownList = ArrayList<Map<String, Any>>()
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to DefaultIDLabel,
                    DefinedParams.ID to "-1"
                )
            )
            if (isMandatory) {
                binding.tvTitle.markMandatory()
            }
            val adapter = CustomSpinnerAdapter(context,translate)
            optionsList?.let { list ->
                if (list.isNotEmpty()) {
                    dropDownList.addAll(list)
                }
            }
            adapter.setData(dropDownList)
            binding.etUserInput.adapter = adapter
            binding.etUserInput.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        adapterView: AdapterView<*>?,
                        view: View?,
                        pos: Int,
                        itemId: Long
                    ) {
                        handleSelectedItem(
                            adapter.getData(position = pos),
                            id,
                            dependentID,
                            serverViewModel
                        )
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        /**
                         * usage of this method is not required
                         */
                    }

                }

            defaultValue?.let { value ->
                val selectedMapIndex =
                    dropDownList.indexOfFirst { it.containsKey(DefinedParams.id) && it[DefinedParams.id] != null && it[DefinedParams.id] == value }

                if (selectedMapIndex > 0) {
                    val selectedMap = dropDownList[selectedMapIndex]
                    selectedMap.let { map ->
                        if (map.isNotEmpty()) {
                            binding.etUserInput.setSelection(selectedMapIndex, true)
                            resultHashMap[id] = value
                        }
                    }
                }
            }

            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
            localDataCache?.let { localCache ->
                listener.loadLocalCache(id, localCache)
            }
        }
    }

    private fun handleSelectedItem(
        selectedItem: Map<String, Any>?,
        id: String,
        dependentID: String?,
        serverViewModel: FormLayout
    ) {
        selectedItem?.let {
            val selectedId = it[DefinedParams.id]
            if ((selectedId is String && selectedId == "-1")) {
                if (resultHashMap.containsKey(id)) {
                    handleId(id)
                    dependentID?.let { deptId ->
                        resetDependantSpinnerView(deptId)
                    }
                } else {
                    if (EDITSCREEN == true)
                        resultHashMap[id] = ""
                }
            } else {
                resultHashMap[id] =
                    it[DefinedParams.id] as Any
                dependentID?.let { deptId ->
                    resetDependantSpinnerView(deptId)
                    listener.loadLocalCache(
                        deptId,
                        deptId,
                        it[DefinedParams.id] as Long
                    )
                }
            }
            if (selectedId is String)
                setConditionalVisibility(serverViewModel, selectedId)
        }
    }

    private fun handleId(id: String) {
        if (EDITSCREEN == true) {
            resultHashMap[id] = ""
        } else {
            resultHashMap.remove(id)
        }
    }

    private fun createDatePicker(serverViewModel: FormLayout) {
        val binding = DatepickerLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.etUserInput.tag = id
            binding.tvErrorMessage.tag = id + errorSuffix
            if (translate){
                binding.tvTitle.text = titleCulture?:title
            }else{
                binding.tvTitle.text = title
            }
            binding.tvTitle.tag = id + titleSuffix
            binding.etUserInput.safeClickListener {
                openDatePicker(serverViewModel, binding.etUserInput)
            }

            if (isMandatory) {
                binding.tvTitle.markMandatory()
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }

            binding.etUserInput.addTextChangedListener { editable ->
                if (editable.isNullOrBlank()) {
                    resultHashMap.remove(id)
                    setConditionalVisibility(serverViewModel, null)
                } else {
                    resultHashMap[id] = editable.toString()
                    setConditionalVisibility(serverViewModel, editable.toString())
                }
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }


    private fun createRadioGroup(serverViewModel: FormLayout) {
        val binding = RadioGroupLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.rgGroup.tag = id
            binding.tvTitle.tag = id + titleSuffix
            binding.tvErrorMessage.tag = id + errorSuffix
            if (translate){
                binding.tvTitle.text = titleCulture?:title
            }else{
                binding.tvTitle.text = title
            }
            optionsList?.forEachIndexed { index, map ->
                val name = map[DefinedParams.NAME]
                if (name != null && name is String) {
                    val radioButton = RadioButton(binding.rgGroup.context)
                    radioButton.id = index
                    radioButton.tag = map[DefinedParams.id]
                    radioButton.setPadding(20.dp, 0, 20.dp, 0)
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-android.R.attr.state_enabled),
                            intArrayOf(-android.R.attr.state_checked),
                            intArrayOf(android.R.attr.state_checked)
                        ), intArrayOf(
                            ContextCompat.getColor(context, R.color.navy_blue_20_alpha),  // disabled
                            ContextCompat.getColor(context, R.color.purple),  // disabled
                            ContextCompat.getColor(context, R.color.purple) // enabled
                        )
                    )
                    val textColorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-android.R.attr.state_enabled),
                            intArrayOf(-android.R.attr.state_checked),
                            intArrayOf(android.R.attr.state_checked)
                        ), intArrayOf(
                            ContextCompat.getColor(context, R.color.navy_blue_20_alpha),  // disabled
                            ContextCompat.getColor(context, R.color.navy_blue), // enabled
                            ContextCompat.getColor(context,R.color.purple)
                        )
                    )
                    radioButton.setTextColor(textColorStateList)
                    radioButton.buttonTintList = colorStateList
                    radioButton.invalidate()
                    radioButton.textSizeSsp = SSP16
                    if (translate){
                        val translatedName = map[DefinedParams.cultureValue]
                        if (translatedName is String?){
                            radioButton.text = translatedName?:name
                        }else {
                            radioButton.text = name
                        }
                    }else{
                        radioButton.text = name
                    }
                    val optionVisibility  = map[DefinedParams.VISIBILITY]
                    if (optionVisibility is String){
                        setViewVisibility(optionVisibility,radioButton)
                    }
                    radioButton.layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.WRAP_CONTENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    radioButton.typeface = ResourcesCompat.getFont(context, R.font.inter_regular)
                    binding.rgGroup.addView(radioButton)
                }
            }
            orientation?.let {
                binding.rgGroup.orientation = it
            } ?: kotlin.run {
                binding.rgGroup.orientation = LinearLayout.HORIZONTAL
            }

            if (isMandatory)
                binding.tvTitle.markMandatory()

            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }

            binding.rgGroup.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId >= 0) {
                    optionsList?.let {
                        val map = it[checkedId]
                        resultHashMap[id] = map[DefinedParams.id] as Any
                        setConditionalVisibility(
                            serverViewModel,
                            it[checkedId][DefinedParams.NAME] as String? ?: ""
                        )
                    }
                }
                changeRadioGroupTypeFace(checkedId, binding.rgGroup)
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }
    }

    private fun changeRadioGroupTypeFace(radioButtonId: Int, rootView: RadioGroup) {

        rootView.let {
            rootView.children.iterator().forEach {
                val child = it as RadioButton
                if (child.id == radioButtonId) {
                    child.typeface = ResourcesCompat.getFont(this, R.font.inter_bold)
                } else {
                    child.typeface = ResourcesCompat.getFont(this, R.font.inter_regular)
                }
            }
        }
    }

    private fun createEditText(serverViewModel: FormLayout) {
        val binding = EdittextLayoutBinding.inflate(LayoutInflater.from(context))

        serverViewModel.apply {
            binding.root.tag = id + rootSuffix
            binding.tvTitle.tag = id + titleSuffix
            binding.etUserInput.tag = id
            binding.tvErrorMessage.tag = id + errorSuffix
            binding.tvNationalIdAction.visibility = View.GONE
            binding.tvKey.tag = id + tvKey
            binding.tvValue.tag = id + tvValue
            binding.bgLastMeal.tag = id + rootSummary

            if (serverViewModel.id == DefinedParams.Phone_Number) {
                SecuredPreference.getCountryCode()?.let {
                    binding.llCountryCode.visibility = View.VISIBLE
                    val text = "+${it}"
                    binding.tvCountryCode.text = text
                }
            }

            unitMeasurement?.let {
                it.also { resultHashMap["$id${DefinedParams.unitMeasurement_KEY}"] = it }
            }

            if (isNeedAction) {
                when (id) {
                    DefinedParams.National_Id -> {
                        binding.tvNationalIdAction.visibility = View.VISIBLE
                        val clickableSpan = object : ClickableSpan() {
                            override fun onClick(mView: View) {
                                generateNationalId()
                            }

                            override fun updateDrawState(ds: TextPaint) {
                                super.updateDrawState(ds)
                                ds.isUnderlineText = false
                            }
                        }
                        binding.tvNationalIdAction.text = getSpannableString(clickableSpan)
                        binding.tvNationalIdAction.movementMethod = LinkMovementMethod.getInstance()
                    }
                }
            }
            if (title.isNotEmpty() && !translate) {
                var titleText = title
                unitMeasurement?.let { measurementType ->
                    "$titleText (${measurementType})".also { text -> titleText = text }
                }
                binding.tvTitle.text = titleText
            }else if (titleCulture!= null && titleCulture!!.isNotBlank() && translate) {
                var titleText = titleCulture
                unitMeasurement?.let { measurementType ->
                    "$titleText (${measurementType})".also { text -> titleText = text }
                }
                binding.tvTitle.text = titleText
            }else {
                var titleText = title
                unitMeasurement?.let { measurementType ->
                    "$titleText (${measurementType})".also { text -> titleText = text }
                }
                binding.tvTitle.text = titleText
            }
            maxLines?.let { binding.etUserInput.setLines(it) }
                ?: binding.etUserInput.setSingleLine()

            if (isMandatory) {
                binding.tvTitle.markMandatory()
            }

            defaultValue?.let {
                binding.etUserInput.setText(it)
                resultHashMap[id] = it
            }

            hint?.let {
                if (translate){
                    binding.etUserInput.hint = hintCulture?:it
                }else{
                    binding.etUserInput.hint = it
                }
            }

            isEnabled?.let {
                binding.etUserInput.isEnabled = it
            }

            val inputFilter = arrayListOf<InputFilter>()
            maxLength?.let {
                inputFilter.add(InputFilter.LengthFilter(it))
            }

            contentLength?.let {
                inputFilter.add(InputFilter.LengthFilter(it))
            }

            if(id == DefinedParams.National_Id)
                inputFilter.add(AllCaps())

            if (inputFilter.isNotEmpty()) {
                try {
                    binding.etUserInput.filters = inputFilter.toTypedArray()
                } catch (_: Exception) {
                    //Exception - Catch block
                }
            }

            inputType?.let {
                when (it) {
                    InputType.TYPE_CLASS_PHONE, InputType.TYPE_CLASS_NUMBER -> binding.etUserInput.inputType =
                        InputType.TYPE_CLASS_NUMBER
                    InputType.TYPE_NUMBER_FLAG_DECIMAL -> binding.etUserInput.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                    else -> {
                        binding.etUserInput.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                    }
                }
            }
            getFamilyView(family)?.addView(binding.root) ?: kotlin.run {
                parentLayout.addView(binding.root)
            }

            binding.etUserInput.addTextChangedListener { editTable ->
                when {
                    id.contains(
                        DefinedParams.Phone_Number,
                        ignoreCase = true
                    ) && !Validator.isValidMobileNumber(editTable.toString()) -> {
                        resultHashMap.remove(id)
                        setConditionalVisibility(serverViewModel, null)
                    }
                    editTable.isNullOrBlank() -> {
                        if (EDITSCREEN == true){
                            if (id.contains(DefinedParams.Height, ignoreCase = true) || id.contains(
                                    DefinedParams.Weight,
                                    ignoreCase = true
                                ) || (inputType != null && (inputType == InputType.TYPE_CLASS_NUMBER ||
                                        inputType == InputType.TYPE_NUMBER_FLAG_DECIMAL))
                            ) {
                                resultHashMap.remove(id)
                            } else {
                                resultHashMap[id] = ""
                            }
                        } else {
                            resultHashMap.remove(id)
                            if(id == DefinedParams.BloodGlucoseID)
                            {
                                if(resultHashMap.containsKey(DefinedParams.Glucose_Type))
                                    resultHashMap.remove(DefinedParams.Glucose_Type)
                            }
                        }
                        setConditionalVisibility(serverViewModel, null)
                    }
                    else -> {
                        if (id.equals(DefinedParams.BloodGlucoseID, true)) {
                            getViewByTag(DefinedParams.lastMealTime + rootSuffix)?.visibility =
                                View.VISIBLE
                            getViewByTag(DefinedParams.BloodGlucoseID + rootSummary)?.visibility =
                                View.GONE
                            if (resultHashMap.containsKey(DefinedParams.GlucoseLog)) {
                                resultHashMap.remove(DefinedParams.GlucoseLog)
                            }
                        }
                        if (id.contains(DefinedParams.Height, ignoreCase = true) || id.contains(
                                DefinedParams.Weight,
                                ignoreCase = true
                            ) || (inputType != null && (inputType == InputType.TYPE_CLASS_NUMBER ||
                                    inputType == InputType.TYPE_NUMBER_FLAG_DECIMAL))
                        ) {
                            val resultValue = editTable.trim().toString().toDoubleOrNull()
                            resultValue?.let {
                                resultHashMap[id] = resultValue
                            }
                        } else
                            resultHashMap[id] = editTable.trim().toString()
                        setConditionalVisibility(serverViewModel, editTable.trim().toString())
                    }
                }
            }
            setViewVisibility(visibility, binding.root)
            setViewEnableDisable(isEnabled, binding.root)
        }

    }

    private fun getSpannableString(clickableSpan: ClickableSpan): CharSequence {
        val spannableString =
            SpannableString(context.getString(R.string.don_t_have_id_generate_id))
        val index = spannableString.indexOf("?")

        spannableString.setSpan(
            clickableSpan,
            if (index >= 0) index + 1 else 0,
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }

    fun generateNationalId() {
        var nationalId = ""
        var errorVisibility = View.GONE

        firstName()?.let {
            nationalId = it
        }
        focusNeeded = null
        firstNameError(nationalId)

        lastName(nationalId).let {
            it.first?.let { nId ->
                nationalId = nId
            }
            it.second?.let { errVisibility ->
                errorVisibility = errVisibility
            }
        }
        lastNameError(errorVisibility)

        phoneNumber(nationalId).let {
            it.first?.let { nId ->
                nationalId = nId
            }
            it.second?.let { errVisibility ->
                errorVisibility = errVisibility
            }
        }
        phoneNumberError(errorVisibility)

        if (nationalId.isNotEmpty()) {
            getViewByTag(DefinedParams.National_Id)?.let { editText ->
                if (editText is AppCompatEditText) {
                    editText.setText(nationalId.uppercase())
                    editText.isEnabled = false
                }
            }
        } else {
            focusNeeded?.let { focusNeeded ->
                scrollView?.let { scrollView ->
                    scrollToView(scrollView, focusNeeded)
                }
            }
        }
    }

    private fun phoneNumberError(errorVisibility: Int) {
        getViewByTag(DefinedParams.Phone_Number + errorSuffix)?.let { tvError ->
            (tvError as TextView).apply {
                text = getString(R.string.default_user_input_error)
                visibility = errorVisibility
                if (errorVisibility == View.VISIBLE && focusNeeded == null)
                    focusNeeded = getViewByTag(DefinedParams.Phone_Number + titleSuffix) ?: this
            }
        }
    }

    private fun phoneNumber(nationalId: String): Pair<String?, Int?> {
        var nId = nationalId
        var errorVisibility: Int? = null
        try {
            getViewByTag(DefinedParams.Phone_Number)?.let { editText ->
                if (editText is AppCompatEditText && !editText.text.isNullOrBlank()) {
                    errorVisibility = View.GONE
                    if (nId.isNotEmpty()) {
                        val input = editText.text!!.trim().replace("\\s".toRegex(), "")
                        nId = if (input.length > AppConstants.National_Id_Phone_Length) {
                            val phnStartIndex = input.length - AppConstants.National_Id_Phone_Length

                            "$nId${
                                input.substring(phnStartIndex)
                            }"
                        } else "$nId$input"
                    }
                } else {
                    nId = ""
                    errorVisibility = View.VISIBLE
                }
            }
        } catch (_: Exception) {
            //Exception - Catch block
        }
        return Pair(nId, errorVisibility)
    }

    private fun lastNameError(errorVisibility: Int) {
        getViewByTag(DefinedParams.Last_Name + errorSuffix)?.let { tvError ->
            (tvError as TextView).apply {
                text = getString(R.string.default_user_input_error)
                visibility = errorVisibility
                if (errorVisibility == View.VISIBLE && focusNeeded == null)
                    focusNeeded = getViewByTag(DefinedParams.Last_Name + titleSuffix) ?: this
            }
        }
    }

    private fun lastName(nationalId: String): Pair<String?, Int?> {
        var nId = nationalId
        var errorVisibility: Int? = null
        getViewByTag(DefinedParams.Last_Name)?.let { editText ->
            if (editText is AppCompatEditText && !editText.text.isNullOrBlank()) {
                errorVisibility = View.GONE
                if (nId.isNotEmpty()) {
                    val input = editText.text!!.trim().replace("\\s".toRegex(), "")
                    nId = if (input.length >= AppConstants.National_Id_Char_Length)
                        "$nId${
                            input.substring(
                                0,
                                AppConstants.National_Id_Char_Length
                            )
                        }"
                    else "$nId$input"
                }
            } else {
                nId = ""
                errorVisibility = View.VISIBLE
            }
        }
        return Pair(nId, errorVisibility)
    }

    private fun firstNameError(nationalId: String) {
        getViewByTag(DefinedParams.First_Name + errorSuffix)?.let { tvError ->
            (tvError as TextView).apply {
                text = getString(R.string.default_user_input_error)
                if (nationalId.isEmpty()) {
                    visibility = View.VISIBLE
                    focusNeeded = getViewByTag(DefinedParams.First_Name + titleSuffix) ?: this
                } else visibility = View.GONE
            }
        }
    }

    private fun firstName(): String? {
        var nationalId: String? = null
        getViewByTag(DefinedParams.First_Name)?.let { editText ->
            if (editText is AppCompatEditText && !editText.text.isNullOrBlank()) {
                val input = editText.text!!.trim().replace("\\s".toRegex(), "")
                nationalId = if (input.length >= AppConstants.National_Id_Char_Length)
                    input.substring(0, AppConstants.National_Id_Char_Length)
                else input
            }
        }
        return nationalId
    }

    private fun setConditionalVisibility(model: FormLayout, actualValue: String?) {
        val conditionList = model.condition
        if (conditionList == null || conditionList.isEmpty())
            return

        conditionList.forEach { conditionalModel ->
            conditionalModel.apply {
                if (targetId != null && targetOption != null){
                    val targetedView = parentLayout.findViewWithTag<View>(targetOption)
                    if (visibility != null) {
                        checkConditionBasedRendering(
                            conditionalModel,
                            ConditionModelConfig.VISIBILITY,
                            actualValue,
                            targetedView,
                            targetOption
                        )
                    } else if (enabled != null) {
                        checkConditionBasedRendering(
                            conditionalModel,
                            ConditionModelConfig.ENABLED,
                            actualValue,
                            targetedView,
                            targetOption
                        )
                    }
                }else if (targetId != null) {
                    val targetedView = parentLayout.findViewWithTag<View>(targetId + rootSuffix)
                    if (visibility != null) {
                        checkConditionBasedRendering(
                            conditionalModel,
                            ConditionModelConfig.VISIBILITY,
                            actualValue,
                            targetedView
                        )
                    } else if (enabled != null) {
                        checkConditionBasedRendering(
                            conditionalModel,
                            ConditionModelConfig.ENABLED,
                            actualValue,
                            targetedView
                        )
                    }
                }else {
                     return@apply
                }
            }
        }
    }

    private fun checkConditionBasedRendering(
        conditionalModel: ConditionalModel,
        config: ConditionModelConfig,
        actualValue: String?,
        targetedView: View?,
        targetOption: String? = null
    ) {
        conditionalModel.apply {
            targetedView ?: return@apply
            if (!eq.isNullOrBlank()) {
                if (eq == actualValue)
                    handleConfig(true, config, visibility, enabled, targetedView)
                else
                    handleTargetConfig(serverData, targetId, targetedView, config,targetOption)
            } else if (lengthGreaterThan != null) {
                if (actualValue != null && actualValue.length > lengthGreaterThan!!)
                    handleConfig(false, config, visibility, enabled, targetedView)
                else
                    handleTargetConfig(serverData, targetId, targetedView, config, targetOption)
            }else if (!eqList.isNullOrEmpty()){
                if (eqList!!.contains(actualValue))
                    handleConfig(true, config, visibility, enabled, targetedView)
                else
                    handleTargetConfig(serverData, targetId, targetedView, config, targetOption)
            }
        }
    }

    private fun handleTargetConfig(
        serverData: List<FormLayout>?,
        targetId: String?,
        targetedView: View,
        config: ConditionModelConfig,
        targetOption: String?
    ) {
        val targetModel = serverData?.find { it.id == targetId }
        if (config == ConditionModelConfig.VISIBILITY) {
            var visibility: String? = null
            if (targetOption != null){
                targetModel?.optionsList?.forEach { map ->
                    if (map[DefinedParams.NAME] == targetOption){
                        val value  = map[DefinedParams.VISIBILITY]
                        if ( value is String?){
                            visibility = value
                        }
                    }
                }
                 if (visibility == GONE && targetId != null){
                     val view = getViewByTag(targetId)
                     if (view is RadioGroup && targetedView.isEnabled){
                         view.clearCheck()
                         resultHashMap.remove(targetId)
                     }
                 }
                setViewVisibility(visibility, targetedView, false)
            } else {
                visibility = targetModel?.visibility
                setViewVisibility(visibility, targetedView, true)
            }
        } else {
            var enabled: Boolean? = null
            if (targetOption != null){
                targetModel?.optionsList?.forEach { map ->
                    if (map[DefinedParams.NAME] == targetOption){
                        val value  = map[DefinedParams.isEnabled]
                        if ( value is Boolean?){
                            enabled = value
                        }
                    }
                }
                setViewEnableDisable(enabled, targetedView, false)
            } else {
                enabled = targetModel?.isEnabled
                setViewEnableDisable(enabled, targetedView, true)
            }
        }
    }

    private fun handleConfig(
        resetValue: Boolean,
        config: ConditionModelConfig,
        visibility: String?,
        enabled: Boolean?,
        targetedView: View
    ) {
        if (config == ConditionModelConfig.VISIBILITY)
            setViewVisibility(visibility, targetedView, resetValue)
        else
            setViewEnableDisable(enabled, targetedView, resetValue)
    }

    private fun setViewEnableDisable(
        enabled: Boolean?,
        rootLyt: View,
        resetValue: Boolean = false
    ) {
        recursiveLoopChildren(rootLyt as ViewGroup, { view ->
            view?.apply {
                if (resetValue) {
                    resetChildFormViewComponents(view)
                } else {
                    resetSpecificChildViews(view)
                }
                if (enabled == null) {
                    this.isEnabled = true
                    return@apply
                }
                this.isEnabled = enabled
            }
        }) { viewGroup ->
            if (resetValue) {
                resetChildFormViewGroupComponents(viewGroup)
            }
            viewGroup?.forEach { view ->
                view.apply {
                    if (enabled == null) {
                        this.isEnabled = true
                        return@apply
                    }
                    this.isEnabled = enabled
                }
            }
        }
    }

    private fun resetSpecificChildViews(view: View?) {
        view?.apply {
            val model = serverData?.find { it.id == tag }
            when (model?.viewType) {
                VIEW_TYPE_TIME -> {
                    if (!view.isEnabled) {
                        getViewByTag(R.id.radioGrpDate)?.let {
                            resetRadioGroup(it, model)
                        }
                    }
                }
            }
        }
    }

    private fun setViewVisibility(visibility: String?, root: View, resetValue: Boolean = false) {

        if (resetValue && visibility != null && visibility != VISIBLE) {
            resetChildViews(root)
        }

        when (visibility) {
            VISIBLE -> {
                root.visibility = View.VISIBLE
            }
            INVISIBLE -> {
                root.visibility = View.INVISIBLE
            }
            GONE -> {
                root.visibility = View.GONE
            }
            else -> {
                root.visibility = View.VISIBLE
            }
        }
    }

    private fun createCardViewFamily(serverViewModel: FormLayout) {
        val binding = CardLayoutBinding.inflate(LayoutInflater.from(context))
        serverViewModel.apply {
            setViewVisibility(visibility, binding.root)
            binding.root.tag = id + rootSuffix
            binding.llFamilyRoot.tag = id
            if (translate){
                binding.cardTitle.text = titleCulture?:title
            }else{
                binding.cardTitle.text = title
            }
            if (parentLayout.findViewWithTag<LinearLayout>(id) == null) {
                parentLayout.addView(binding.root)
            }
            /*binding.ivExpand.safeClickListener {
                if (binding.llFamilyRoot.visibility == View.GONE) {
                    binding.ivExpand.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_minimize
                        )
                    )
                    binding.llFamilyRoot.visibility = View.VISIBLE
                    val face = ResourcesCompat.getFont(context, R.font.dmsans_medium)
                    binding.cardTitle.typeface = face
                    binding.cardTitle.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.cobalt_blue
                        )
                    )
                } else {
                    binding.llFamilyRoot.visibility = View.GONE
                    val face = ResourcesCompat.getFont(context, R.font.dmsans_regular)
                    binding.cardTitle.typeface = face
                    binding.ivExpand.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_expand
                        )
                    )
                    binding.cardTitle.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.navy_blue
                        )
                    )
                }
            }*/
        }

    }

    private fun getFamilyView(family: String?): LinearLayout? {
        family ?: return null
        return parentLayout.findViewWithTag(family)
    }

    private fun openDatePicker(
        serverViewModel: FormLayout,
        userInput: AppCompatTextView,
        ageView: AppCompatEditText? = null,
        year: Int? = null,
        month: Int? = null,
        date: Int? = null
    ) {
        val date: Triple<Int?, Int?, Int?> = Triple(year, month, date)
        serverViewModel.run {
            showDatePicker(
                context = context,
                disableFutureDate = disableFutureDate ?: false,
                minDate = minDate,
                maxDate = maxDate,
                date = date
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                val parsedDate = DateUtils.getDatePatternDDMMYYYY().parse(stringDate)
                parsedDate?.let {
                    isDOBNeedToChange = false
                    userInput.text = DateUtils.getDateDDMMYYYY().format(it)
                    ageView?.setText(DateUtils.getAge(stringDate))
                    addOrUpdateDOB(
                        DateUtils.getDateString(
                            parsedDate.time
                        )
                    )
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    fun onSpinnerActivityResult(resultIntent: Intent) {
        if (resultIntent.hasExtra(INTENT_SPINNER_SELECTED_ITEM) && resultIntent.hasExtra(
                INTENT_ID
            )
        )
            spinnerSelectedItem(resultIntent)
        else if (resultIntent.hasExtra(INTENT_ID)) {
            val elementId = resultIntent.getStringExtra(INTENT_ID)
            elementId?.let { it ->
                val view = parentLayout.findViewWithTag<View>(it)
                if (view is RecyclerView) {
                    val resultHolderView = getViewByTag(elementId + innerRootSuffix)
                    resultHolderView?.visibility = View.VISIBLE
                    if (resultHashMap.containsKey(elementId)) {
                        val list = resultHashMap[elementId] as ArrayList<Map<String, Double>>
                        view.layoutManager = LinearLayoutManager(context)
                        view.itemAnimator = DefaultItemAnimator()
                        view.adapter = BPResultAdapter(list)
                        val entryHolderView = getViewByTag(elementId + innerEntrySuffix)
                        entryHolderView?.visibility = holderVisibility(list.size)
                    }
                }
            }
        }
    }

    private fun holderVisibility(size: Int): Int {
        return if(size == 3) View.GONE else View.VISIBLE
    }

    private fun spinnerSelectedItem(resultIntent: Intent) {
        val spinnerId = resultIntent.getStringExtra(INTENT_ID)
        spinnerId?.let {
            try {
                var selectedItem: ArrayList<Map<String,Any>> = ArrayList()
                (resultIntent.customSerializableExtra(INTENT_SPINNER_SELECTED_ITEM) as ArrayList<Map<String, Any>>?)?.let { selectedSpinnerItem ->
                    selectedItem = selectedSpinnerItem
                }
                val spinnerTextView = parentLayout.findViewWithTag<AppCompatTextView>(spinnerId)
                if (selectedItem.isNotEmpty()) {
                    resultHashMap[spinnerId] = selectedItem[0][DefinedParams.id] as Any
                    spinnerTextView.text = selectedItem[0][DefinedParams.NAME] as String? ?: ""
                    val serverViewModel = serverData?.find { it.id == spinnerId }
                    serverViewModel?.let {
                        setConditionalVisibility(
                            serverViewModel,
                            selectedItem[0][DefinedParams.NAME] as String?
                        )
                    } ?: return
                } else {
                    val serverViewModel = serverData?.find { it.id == spinnerId }
                    serverViewModel?.let {
                        setConditionalVisibility(serverViewModel, null)
                    } ?: return
                }
            } catch (e: Exception) {
                return
            }
        }
    }

    private fun recursiveLoopChildren(
        parent: ViewGroup,
        viewItem: (view: View?) -> Unit,
        viewGroupItem: (viewGroup: ViewGroup?) -> Unit
    ) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child is ViewGroup) {
                recursiveLoopChildren(child, {
                    viewItem.invoke(it)
                }) { viewGroup ->
                    viewGroupItem.invoke(viewGroup)
                }
                if (child.tag != null) {
                    viewGroupItem.invoke(child)
                }

            } else {
                if (child != null && child.tag != null) {
                    // DO SOMETHING WITH VIEW
                    viewItem.invoke(child)
                }
            }
        }
    }


    private fun resetChildViews(rootLyt: View) {
        recursiveLoopChildren(rootLyt as ViewGroup, { view ->
            resetChildFormViewComponents(view)
        }) { viewGroup ->
            resetChildFormViewGroupComponents(viewGroup)
        }
    }

    private fun resetChildFormViewGroupComponents(viewGroup: ViewGroup?) {
        viewGroup?.apply {
            val model = serverData?.find { it.id == tag }
            when (model?.viewType) {
                VIEW_TYPE_FORM_SPINNER -> resetSpinner(this)
                VIEW_TYPE_FORM_RADIOGROUP -> resetRadioGroup(this, model)
                //  VIEW_TYPE_FORM_CHECKBOX -> resetCheckbox(this, model)
            }
        }
    }

    private fun resetChildFormViewComponents(view: View?) {
        view?.apply {
            val model = serverData?.find { it.id == tag }
            when (model?.viewType) {
                VIEW_TYPE_FORM_DATEPICKER,
                VIEW_TYPE_FORM_SPINNER,
                VIEW_TYPE_FORM_EDITTEXT -> resetEditTextDatePicker(this, model)
                VIEW_TYPE_TIME -> resetTimeView(this, model)
                VIEW_TYPE_FORM_AGE -> resetAgeView(this, model)
            }
        }
    }

    private fun resetAgeView(view: View, model: FormLayout) {
        resetEditTextDatePicker(view, model)
        getViewByTag(R.id.etAgeInYears)?.let {
            resetEditTextDatePicker(it, model)
        }
    }

    private fun resetTimeView(view: View, model: FormLayout) {
        resetEditTextDatePicker(view, model)
        getViewByTag(R.id.etMinute)?.let {
            resetEditTextDatePicker(it, model)
        }
        getViewByTag(R.id.timeRadioGroup)?.let {
            resetRadioGroup(it, model)
        }
        getViewByTag(R.id.radioGrpDate)?.let {
            resetRadioGroup(it, model)
        }
    }


    private fun resetEditTextDatePicker(view: View, model: FormLayout) {
        if (view is EditText) {
            model.defaultValue?.let {
                view.setText(it)
            } ?: kotlin.run {
                view.text.clear()
            }
        } else if (view is TextView) {
            model.defaultValue?.let {
                view.text = it
            } ?: kotlin.run {
                view.text = ""
            }
        }
    }

    private fun resetSpinner(spinnerView: View) {
        if (spinnerView is Spinner) {
            spinnerView.setSelection(0, true)
        }
    }

    private fun resetRadioGroup(rgGroup: View, model: FormLayout, button: Int? = null) {
        if (rgGroup is RadioGroup) {
            val default = model.defaultValue
            if (default != null || button != null) {
                val count = rgGroup.childCount
                for (i in 0..count) {
                    resetRg(rgGroup, default, button)
                }
            } else {
                rgGroup.clearCheck()
                resultHashMap.remove(model.id)
            }
        }
    }

    private fun resetRg(rgGroup: RadioGroup, default: String?, button: Int?) {
        rgGroup.forEach {
            if (it is RadioButton) {
                when {
                    default != null && it.tag == default -> it.isChecked = true
                    button != null && it.id == button -> it.isChecked = true
                }
            }
        }
    }

    private fun getViewByTag(tag: Any): View? {
        return parentLayout.findViewWithTag(tag)
    }

    fun getResultHashMap(): HashMap<String, Any> {
        return resultHashMap
    }

    fun calculateBPValues(formLayout: FormLayout, resultMap: Map<String, Any>) {
        formLayout.apply {
            var systolic = 0.0
            var diastolic = 0.0
            if (resultMap.containsKey(id)) {
                val actualMapList = resultMap[id]
                if (actualMapList is ArrayList<*>) {
                    var systolicEntries = 0
                    var diastolicEntries = 0
                    actualMapList.forEach { map ->
                        if (map is BPModel) {
                            map.systolic?.let {
                                systolic += it
                                systolicEntries++
                            }
                            map.diastolic?.let {
                                diastolic += it
                                diastolicEntries++
                            }
                        } else {
                            validateMap(map, DefinedParams.Systolic)?.let {
                                systolic += it
                                systolicEntries++
                            }
                            validateMap(map, DefinedParams.Diastolic)?.let {
                                diastolic += it
                                diastolicEntries++
                            }
                        }
                    }
                    if (actualMapList.size > 0 && systolicEntries > 0 && diastolicEntries > 0) {
                        val systolicAverage = (systolic / systolicEntries).roundToInt()
                        val diastolicAverage = (diastolic / diastolicEntries).roundToInt()
                        systolicAverageSummary = systolicAverage
                        diastolicAverageSummary = diastolicAverage
                    }
                }
            }
        }
    }

    private fun validateMap(map: Any?, value: String): Double? {
        return if (map is Map<*, *> && map.containsKey(value)) map[value] as Double else null
    }

    fun getSystolicAverage(): Int? {
        return systolicAverageSummary
    }

    fun getDiastolicAverage(): Int? {
        return diastolicAverageSummary
    }

    fun setPhQ4Score(phQ4Score: Int) {
        this.phQ4Score = phQ4Score
    }

    fun getPhQ4Score(): Int? {
        return phQ4Score
    }

    fun setFbsBloodGlucose(glucose: Double) {
        fbsBloodGlucose = glucose
    }

    fun setRbsBloodGlucose(glucose: Double) {
        rbsBloodGlucose = glucose
    }

    fun getFbsBloodGlucose(): Double {
        return fbsBloodGlucose ?: 0.0
    }

    fun getRbsBloodGlucose(): Double {
        return rbsBloodGlucose ?: 0.0
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

    fun onValidationCompleted(
        screeningDetails: HashMap<*, *>? = null,
        isSuccess: Boolean = true,
        errorText: String? = null,
        isAssessmentRequired: Boolean
    ) {
        if (!isSuccess) {
            showValidationError(errorText)
            return
        }
        loadOtherData()
        autoPopulateFields(screeningDetails)
        changeButtonTitle(isAssessmentRequired)
    }

    fun changeButtonTitle(isAssessmentRequired: Boolean) {
        val view = parentLayout.findViewWithTag<AppCompatButton>(DefinedParams.btnSubmit)
        view?.text =
            if (isAssessmentRequired) getString(R.string.next) else getString(R.string.fp_submit)
    }


    private fun autoPopulateFields(screeningDetails: HashMap<*, *>?) {
        if (screeningDetails != null) {
                screeningDetails.keys.forEach { key ->
                    if (key is String) {
                        getViewByTag(key)?.let { view ->
                            val value = screeningDetails[key]
                            if (view is AppCompatEditText && !key.equals(DefinedParams.lastMealTime,ignoreCase = true)) {
                                if (value is String) {
                                    view.setText(checkNameFieldValues(value,key))
                                } else if (value is Number) {
                                    view.setText(CommonUtils.getDecimalFormatted(value))
                                }
                            } else if (view is RadioGroup) {
                                view.children.iterator().forEach childLoop@{ radioBtn ->
                                    radioBtn as RadioButton
                                    if (value is String) {
                                        if (radioBtn.text.toString().equals(value, ignoreCase = true)) {
                                            radioBtn.isChecked = true
                                            return@childLoop
                                        } else if (radioBtn.tag != null && radioBtn.tag.toString()
                                                .equals(value, ignoreCase = true)
                                        ) {
                                            radioBtn.isChecked = true
                                            return@childLoop
                                        }
                                    } else if (value is Boolean && ((radioBtn.text.toString()
                                            .equals(DefinedParams.Yes, ignoreCase = true) && value)
                                                || (radioBtn.text.toString()
                                            .equals(DefinedParams.No, true) && !value))) {
                                        radioBtn.isChecked = true
                                        return@childLoop

                                    }
                                }
                            } else if (view is Spinner) {
                                val adapter = view.adapter
                                if (adapter != null && adapter is CustomSpinnerAdapter) {
                                    if(value is Long) {
                                        val selectedIndex = adapter.getIndexOfItem(value)
                                        if (selectedIndex != -1) {
                                            view.setSelection(selectedIndex, true)
                                        }
                                    }
                                    else if(value is String)
                                    {
                                        val selectedIndex = adapter.getIndexOfItemByName(value)
                                        if(selectedIndex!= -1)
                                            view.setSelection(selectedIndex, true)
                                    }
                                }
                            }
                        }
                    }
                }

                screeningDetails.let{
                    if(it.containsKey(DefinedParams.InsuranceStatus) &&
                        it.containsKey(DefinedParams.InsuranceType))
                    {
                        if (it[DefinedParams.InsuranceStatus] == "true" &&
                            it[DefinedParams.InsuranceType] == DefinedParams.Compliance_Type_Other) {
                            getViewByTag(DefinedParams.OtherInsurance)?.let { view ->
                                val value =
                                    it[DefinedParams.OtherInsurance]
                                if (view is AppCompatEditText) {
                                    if (value is String) {
                                        view.setText(value)
                                    } else {
                                        view.setText("")
                                    }
                                }
                            }
                        }
                    }
                }

                screeningDetails.let {
                    if (it.containsKey(DefinedParams.DateOfBirth)) {
                        getViewByTag(DefinedParams.DateOfBirth)?.let { view ->
                            view as AppCompatTextView
                            view.text = DateUtils.convertDateTimeToDate(
                                it[DefinedParams.DateOfBirth] as String?,
                                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                                DateUtils.DATE_ddMMyyyy
                            )
                        }
                        val dateOfBirth = it[DefinedParams.DateOfBirth]
                        if (dateOfBirth is String) {
                            resultHashMap[DefinedParams.DateOfBirth] = dateOfBirth
                        }
                    }
                    if (screeningDetails.containsKey(DefinedParams.BPLog_Details)) {
                        if (screeningDetails.containsKey(DefinedParams.bp_log_id)) {
                            val bpLogId = screeningDetails[DefinedParams.bp_log_id]
                            (bpLogId as Double?)?.toLong()?.let { logId ->
                                resultHashMap[DefinedParams.bp_log_id] = logId
                            }
                        }
                        val bpLogDetail = screeningDetails[DefinedParams.BPLog_Details]
                        if (bpLogDetail is ArrayList<*>) {
                            bpLogDetail.forEachIndexed { index, map ->
                                if (map is Map<*, *>) {
                                    if (map.containsKey(DefinedParams.Diastolic)) {
                                        val view = getViewByTag("${index}-${diastolicSuffix}")
                                        if (view != null && view is EditText) {
                                            map[DefinedParams.Diastolic].toString().toDoubleOrNull()
                                                ?.let { diastolic ->
                                                    view.setText(CommonUtils.getDecimalFormatted(diastolic) ?: "")
                                                } ?: kotlin.run {
                                                view.setText("")
                                            }
                                        }
                                    }
                                    if (map.containsKey(DefinedParams.Systolic)) {
                                        val view = getViewByTag("${index}-${systolicSuffix}")
                                        if (view != null && view is EditText) {
                                            map[DefinedParams.Systolic].toString().toDoubleOrNull()
                                                ?.let {
                                                    view.setText(CommonUtils.getDecimalFormatted(it))
                                                } ?: kotlin.run {
                                                view.setText("")
                                            }
                                        }
                                    }
                                    if (map.containsKey(DefinedParams.Pulse)) {
                                        val view = getViewByTag("${index}-${pulseSuffix}")
                                        if (view != null && view is EditText) {
                                            map[DefinedParams.Pulse].toString().toDoubleOrNull()
                                                ?.let {
                                                    view.setText(
                                                        CommonUtils.getDecimalFormatted(it)
                                                    )
                                                } ?: kotlin.run {
                                                view.setText("")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (screeningDetails.containsKey(DefinedParams.GlucoseLog)) {
                        screeningDetails[DefinedParams.GlucoseLog]?.let { details ->
                            if (details is Map<*, *>) {
                                val map = HashMap(details)
                                if (map.containsKey(GlucoseId)) {
                                    val glucoseId = map[GlucoseId]
                                    if (glucoseId is String || glucoseId is Long) {
                                        resultHashMap[GlucoseId] = glucoseId
                                    }
                                }
                                if (map.containsKey(DefinedParams.glucose_unit)) {
                                    resultHashMap[DefinedParams.glucose_unit] = map[DefinedParams.glucose_unit] as String
                                }
                                if (map.containsKey(DefinedParams.Glucose_Value)) {
                                    val view = getViewByTag(DefinedParams.BloodGlucoseID)
                                    if (view != null && view is EditText) {
                                        map[DefinedParams.Glucose_Value].toString().toDoubleOrNull()?.let { glucoseVal ->
                                            view.setText(CommonUtils.getDecimalFormatted(glucoseVal))
                                        }
                                    }
                                    getViewByTag(DefinedParams.lastMealTime + rootSuffix)?.visibility =
                                        View.GONE
                                    getViewByTag(DefinedParams.BloodGlucoseID + rootSummary)?.visibility =
                                        View.VISIBLE
                                    getViewByTag(DefinedParams.BloodGlucoseID + tvKey)?.let {
                                        if (it is TextView) {
                                            it.text = getString(R.string.time_last_meal)
                                        }
                                    }
                                    getViewByTag(DefinedParams.BloodGlucoseID + tvValue)?.let {
                                        if (it is TextView) {
                                            val mealTime = map[DefinedParams.lastMealTime]
                                            if (mealTime is String) {
                                                it.text = DateUtils.convertDateTimeToDate(
                                                    mealTime,
                                                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                                                    DateUtils.DATE_FORMAT_ddMMyyHHmmss
                                                )
                                            }
                                        }
                                    }
                                    if (map.containsKey(DefinedParams.Hemoglobin)) {
                                        map[DefinedParams.Hemoglobin]?.let { value ->
                                            getViewByTag(DefinedParams.Hemoglobin)?.let { view ->
                                                if (view is AppCompatEditText) {
                                                    if (value is String) {
                                                        view.setText(value)
                                                    } else if (value is Number) {
                                                        view.setText(CommonUtils.getDecimalFormatted(value))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    resultHashMap[DefinedParams.lastMealTime] = map[DefinedParams.lastMealTime] as String? ?: ""
                                    (map[DefinedParams.GlucoseLogId] as Double?)?.toLong()
                                        ?.let { logId ->
                                            resultHashMap[DefinedParams.GlucoseLogId] = logId
                                        }
                                    resultHashMap[DefinedParams.Glucose_Type] = map[DefinedParams.Glucose_Type] as String? ?: ""
                                }
                            }
                        }
                    }

                    screeningDetails[DefinedParams.PatientTrackId].let {trackId ->
                        if (trackId is String) {
                            resultHashMap[DefinedParams.PatientTrackId] = trackId
                        }
                    }
                    if (screeningDetails.containsKey(DefinedParams.diagnosis)) {
                        val diagnosis  = screeningDetails[DefinedParams.diagnosis]
                        if (diagnosis is Map<*,*> ){
                            val diabetesPatientType = diagnosis[DefinedParams.DiabetesPatientType]
                            if (diabetesPatientType is String && diabetesPatientType.isNotEmpty()) {
                                selectRadioButton(diabetesPatientType,diabetesPatientType,DefinedParams.DiabetesPatientType)
                            }
                            val diabetesYearOfDiagnosis = diagnosis[DefinedParams.diabetesYearOfDiagnosis]
                            getViewByTag(DefinedParams.diabetesYearOfDiagnosis)?.let { yearOfDiagnosis ->
                                if (yearOfDiagnosis is AppCompatEditText) {
                                    if (diabetesYearOfDiagnosis is String && diabetesYearOfDiagnosis.isNotEmpty()) {
                                        yearOfDiagnosis.setText(diabetesYearOfDiagnosis)
                                        yearOfDiagnosis.isEnabled = false
                                        yearOfDiagnosis.setTextColor(ContextCompat.getColor(context,R.color.navy_blue_20_alpha))
                                    } else if (diabetesYearOfDiagnosis is Number) {
                                        yearOfDiagnosis.setText(CommonUtils.getDecimalFormatted(diabetesYearOfDiagnosis))
                                        yearOfDiagnosis.isEnabled = false
                                        yearOfDiagnosis.setTextColor(ContextCompat.getColor(context,R.color.navy_blue_20_alpha))
                                    }else {
                                        yearOfDiagnosis.setTextColor(ContextCompat.getColor(context,R.color.secondary_black))
                                    }
                                }
                            }

                            val diagnosisDiabetes = diagnosis[DefinedParams.diagnosisDiabetes]
                            if (diagnosisDiabetes is String && diagnosisDiabetes.isNotEmpty()) {
                                selectRadioButton(diabetesPatientType, diagnosisDiabetes,DefinedParams.diabetesDiagnosis)
                            }else {
                                if (diabetesPatientType == DefinedParams.KnownPatient) {
                                    val diabetesDiagnosisControlledType = diagnosis[DefinedParams.diabetesDiagControlledType]
                                    if (diabetesDiagnosisControlledType is String && diabetesDiagnosisControlledType.isNotEmpty() && diabetesDiagnosisControlledType == DefinedParams.PreDiabetic ){
                                        selectRadioButton(diabetesPatientType,DefinedParams.PreDiabetic,DefinedParams.diabetesDiagnosis)
                                    } else {
                                        val diabetesType = diagnosis[DefinedParams.diabetesDiagnosis]
                                        if (diabetesType is String && diabetesType.isNotEmpty()){
                                            when (diabetesType) {
                                                DefinedParams.typeOne -> {
                                                    selectRadioButton(diabetesPatientType,DefinedParams.dmtOne,DefinedParams.diabetesDiagnosis)
                                                }
                                                DefinedParams.typeTwo -> {
                                                    selectRadioButton(diabetesPatientType,DefinedParams.dmtTwo,DefinedParams.diabetesDiagnosis)
                                                }
                                                DefinedParams.GestationalDiabetes -> {
                                                    selectRadioButton(diabetesPatientType,DefinedParams.GestationalDiabetes,DefinedParams.diabetesDiagnosis)
                                                    val genderView = getViewByTag(Gender)
                                                    genderView?.let { view ->
                                                        setViewEnableDisable(false,view,false)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            val htnPatientType = diagnosis[DefinedParams.HTNPatientType]
                            if (htnPatientType is String && htnPatientType.isNotEmpty()) {
                                selectRadioButton(htnPatientType,htnPatientType,DefinedParams.HTNPatientType)
                            }
                            val htnYearOfDiagnosis = diagnosis[DefinedParams.htnYearOfDiagnosis]
                            getViewByTag(DefinedParams.htnYearOfDiagnosis)?.let { yearOfDiagnosis ->
                                if (yearOfDiagnosis is AppCompatEditText) {
                                    if (htnYearOfDiagnosis is String) {
                                        yearOfDiagnosis.setText(htnYearOfDiagnosis)
                                        yearOfDiagnosis.isEnabled = false
                                        yearOfDiagnosis.setTextColor(ContextCompat.getColor(context,R.color.navy_blue_20_alpha))
                                    } else if (htnYearOfDiagnosis is Number) {
                                        yearOfDiagnosis.setText(CommonUtils.getDecimalFormatted(htnYearOfDiagnosis))
                                        yearOfDiagnosis.isEnabled = false
                                        yearOfDiagnosis.setTextColor(ContextCompat.getColor(context,R.color.navy_blue_20_alpha))
                                    }else {
                                        yearOfDiagnosis.setTextColor(ContextCompat.getColor(context,R.color.secondary_black))
                                    }
                                }
                            }

                            val diagnosisHypertension = diagnosis[DefinedParams.diagnosisHypertension]
                            if (diagnosisHypertension is String && diagnosisHypertension.isNotEmpty()) {
                                selectRadioButton(htnPatientType,diagnosisHypertension,DefinedParams.htnDiagnosis)
                            } else {
                                if (htnPatientType == DefinedParams.KnownPatient){
                                    selectRadioButton(htnPatientType,DefinedParams.Hypertension,DefinedParams.htnDiagnosis)
                                }
                            }
                        }
                    }
                }
        }
        showFormSubmitButton(DefinedParams.btnSubmit)
    }

    private fun checkNameFieldValues(value: String, key: String): String {
        return when (key) {
            DefinedParams.First_Name, DefinedParams.Last_Name, DefinedParams.Middle_Name -> {
                CommonUtils.capitalize(value.lowercase())
            }

            else -> value
        }
    }


    private fun showValidationError(errorText: String?) {

        errorText?.let {
            if (errorText.startsWith(
                    getString(R.string.patient_already_enrolled),
                    ignoreCase = true
                )
            ) {
                (context as BaseActivity).showErrorDialogue(
                    getString(R.string.error),
                    message = it
                ) {
                    if (it) {
                        (context as BaseActivity).startAsNewActivity(
                            Intent(
                                context,
                                LandingActivity::class.java
                            )
                        )
                    }
                }
                return
            }
        }

        resultHashMap.clear()

        val editTextView = getViewByTag(DefinedParams.National_Id)

        val nationalId =
            editTextView?.let { (it as AppCompatEditText).text.toString() } ?: ""

        resetChildViews(parentLayout)
        errorText?.let {
            editTextView?.let { (it as AppCompatEditText).setText(nationalId) }

            getViewByTag(DefinedParams.National_Id + errorSuffix)?.let { tvError ->
                (tvError as TextView).apply {
                    text = errorText
                    visibility = View.VISIBLE
                }
            }
        }
        showFormSubmitButton(DefinedParams.btnSubmit)
    }

    private fun changeRadioButtonStyle(button: RadioButton, isChecked: Boolean) {
        if (isChecked) {
            button.typeface = ResourcesCompat.getFont(context, R.font.inter_bold)
        } else {
            button.typeface = ResourcesCompat.getFont(context, R.font.inter_regular)
        }
    }

    fun showFormSubmitButton(tag: String) {
        val view = parentLayout.findViewWithTag<AppCompatButton>(tag)
        view?.visibility = View.VISIBLE
    }

    fun hideFormSubmitButton(tag: String) {
        val view = parentLayout.findViewWithTag<AppCompatButton>(tag)
        view?.visibility = View.GONE
    }

    fun setSpinnerData(data: LocalSpinnerResponse) {
        val view = getViewByTag(data.tag)
        if (view is AppCompatSpinner && view.adapter is CustomSpinnerAdapter) {
            val mapList = ArrayList<Map<String, Any>>()
            mapList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to DefaultIDLabel,
                    DefinedParams.ID to "-1"
                )
            )
            if (data.response is List<*>) {
                data.response.forEach { properties ->
                    val map = HashMap<String, Any>()
                    mapsIdName(properties, map)
                    mapList.add(map)
                }
                (view.adapter as CustomSpinnerAdapter).setData(mapList)
                if (data.response.size > 0 && data.response[0] is CountryEntity) {
                    SecuredPreference.getCountryID().let {
                        val selectedIndex =
                            (view.adapter as CustomSpinnerAdapter).getIndexOfItem(it)
                        if (selectedIndex != -1) {
                            view.setSelection(selectedIndex, true)
                            view.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun mapsIdName(properties: Any?, map: HashMap<String, Any>) {
        when (properties) {
            is CountryEntity -> {
                map[DefinedParams.id] = properties._id
                map[DefinedParams.NAME] = properties.name
            }
            is CountyEntity -> {
                map[DefinedParams.id] = properties._id
                map[DefinedParams.NAME] = properties.name
            }
            is SubCountyEntity -> {
                map[DefinedParams.id] = properties._id
                map[DefinedParams.NAME] = properties.name
            }
            is ProgramEntity -> {
                map[DefinedParams.id] = properties._id
                map[DefinedParams.NAME] = properties.name
            }
        }
    }

    private fun resetDependantSpinnerView(dependentID: String) {
        val view = getViewByTag(dependentID)
        if (view is AppCompatSpinner && view.adapter is CustomSpinnerAdapter) {
            val dropDownList = ArrayList<Map<String, Any>>()
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to DefaultIDLabel,
                    DefinedParams.ID to "-1"
                )
            )
            (view.adapter as CustomSpinnerAdapter).setData(dropDownList)
            view.setSelection(0, true)
        }
    }

    fun saveMentalHealthQuestions(questions: ArrayList<MentalHealthOption>) {
        mentalHealthQuestions = questions
    }


    fun fetchMHQuestions(questions: LocalSpinnerResponse?) {
        questions?.let {
            questions.response as MentalHealthEntity
            val map = questions.response.formString?.let {
                StringConverter.convertStringToListOfMap(it)
            }
            map?.let { list ->
                saveMentalHealthQuestions(questions = getUIModel(list))
            }
        }
    }

    fun loadMentalHealthQuestions(
        questions: LocalSpinnerResponse?,
        mentalHealthEditList: ArrayList<Map<String, Any>>? = null,
        isViewOnly: Boolean = false
    ) {
        this.mentalHealthEditList = mentalHealthEditList?.let {
            ArrayList(mentalHealthEditList)
        }

        questions?.let { response ->
            questions.response as MentalHealthEntity
            val map = questions.response.formString?.let {
                StringConverter.convertStringToListOfMap(it)
            }
            map?.let { list ->
                val model = getUIModel(list)
                saveMentalHealthQuestions(questions = model)
                getViewByTag(response.tag)?.let {
                    if (it is RecyclerView) {
                        it.adapter = MentalHealthAdapter(
                            model,
                            response.tag,
                            isViewOnly = isViewOnly,
                            editList = this.mentalHealthEditList,
                            translate = SecuredPreference.getIsTranslationEnabled()
                        ) { question, questionId, option, id, answerId, displayOrder ->
                            processMentalHealthResult(
                                id,
                                question,
                                questionId,
                                option,
                                answerId,
                                displayOrder
                            )
                        }
                    }
                }
            }
        }
    }

    fun showHeightWeight(height: Double?, weight: Double?) {
        if (SecuredPreference.getUnitMeasurementType() == DefinedParams.Unit_Measurement_Imperial_Type)
            showImperialHeight(height)
        else
            showMetricHeight(height)

        getViewByTag(DefinedParams.Weight)?.let { view ->
            weight?.let { weight ->
                if (view is EditText) {
                    view.setText(CommonUtils.getDecimalFormatted(weight))
                }
            }
        }
    }

    private fun showMetricHeight(height: Double?) {
        getViewByTag(DefinedParams.Height)?.let { view ->
            height?.let { height ->
                if (view is EditText) {
                    val formatedText = CommonUtils.getDecimalFormatted(height)
                    view.setText(formatedText)
                    view.isEnabled = formatedText.isEmpty()
                }
            }
        }
    }

    private fun showImperialHeight(height: Double?) {
        height?.let { heightValue ->
            val feet: Int = CommonUtils.getFeetFromHeight(heightValue)
            val inches: Int = CommonUtils.getInchesFromHeight(heightValue)
            getViewByTag(DefinedParams.Height + DefinedParams.Feet)?.let { view ->
                if (view is EditText) {
                    val feetText = CommonUtils.getDecimalFormatted(feet)
                    view.setText(feetText)
                    view.isEnabled = feetText.isEmpty()
                }
            }

            getViewByTag(DefinedParams.Height + DefinedParams.Inches)?.let { view ->
                if (view is EditText) {
                    val inchText = CommonUtils.getDecimalFormatted(inches)
                    view.setText(inchText)
                    view.isEnabled = inchText.isEmpty()
                }
            }
        }
    }

    fun combineAccountWorkflows(
        list: ArrayList<Pair<String, String>>,
        type: String
    ): ArrayList<FormLayout> {
        val serverCombinedData: ArrayList<FormLayout> = ArrayList()
        val screeningList = list.filter { it.first == type }
        val gson = Gson()
        screeningList.forEach {
            val formModel = gson.fromJson(it.second, FormResponseRootModel::class.java)
            serverCombinedData.addAll(formModel.formLayout)
        }
        val buttonLayout = serverCombinedData.removeLast()
        val workflowList = list.filter { it.first != type }
        val itemType = object : TypeToken<List<AccountWorkflow>>() {}.type
        workflowList.forEach { pair ->
            val itemList = gson.fromJson<List<AccountWorkflow>>(pair.second, itemType)
            val filteredList = itemList.filter {
                it.viewScreens != null && it.viewScreens.contains(
                    type.replaceFirstChar(Char::titlecase)
                )
            }
            filteredList.forEach { listItem ->
                if (!listItem.formInput.isNullOrBlank()) {
                    val rootModel =
                        gson.fromJson(listItem.formInput, FormResponseRootModel::class.java)
                    rootModel.formLayout.filter { it.viewType == VIEW_TYPE_FORM_CARD_FAMILY }
                        .forEach { cardItem ->
                            cardItem.customWorkflowId = (listItem.id).toString()
                        }
                    serverCombinedData.addAll(rootModel.formLayout)
                }
            }
        }
        serverCombinedData.add(buttonLayout)
        populateViews(serverCombinedData)

        return serverCombinedData
    }

    fun selectRadioButton(patientType: Any?, patientDiagnosis:String,viewType:String) {
        getViewByTag(viewType)?.let { diagnosisDiabetesView ->
            if (diagnosisDiabetesView is ViewGroup) {
                diagnosisDiabetesView.children.iterator().forEach childLoop@{ radioBtn ->
                    radioBtn as RadioButton
                    if (radioBtn.text.toString().equals(patientDiagnosis, true)) {
                        radioBtn.isChecked = true
                    } else if (radioBtn.tag != null && radioBtn.tag.toString()
                            .equals(patientDiagnosis, ignoreCase = true)
                    ) {
                        radioBtn.isChecked = true
                    }
                    if (patientType == DefinedParams.KnownPatient || patientType == DefinedParams.NewPatient) {
                        radioBtn.isEnabled = false
                    }
                }
            }
        }
    }


}