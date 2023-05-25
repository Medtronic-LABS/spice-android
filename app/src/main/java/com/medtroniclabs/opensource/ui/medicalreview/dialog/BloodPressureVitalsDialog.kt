package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.view.*
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.appextensions.setError
import com.medtroniclabs.opensource.appextensions.setSuccess
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.Validator
import com.medtroniclabs.opensource.common.ViewUtil
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.ui.BPModel
import com.medtroniclabs.opensource.databinding.FragmentBloodPressureVitalsBinding
import com.medtroniclabs.opensource.formgeneration.FormGenerator
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.formgeneration.model.FormLayout
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.AssessmentReadingViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BloodPressureVitalsDialog : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentBloodPressureVitalsBinding
    private val viewModel: AssessmentReadingViewModel by activityViewModels()
    private val bpVitalsViewModel: PatientDetailViewModel by activityViewModels()
    private var formGenerator: FormGenerator? = null
    @Inject
    lateinit var connectivityManager: ConnectivityManager

    companion object{
        val TAG = "BloodPressureVitalsDialog"
        fun newInstance():BloodPressureVitalsDialog{
            return BloodPressureVitalsDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBloodPressureVitalsBinding.inflate(inflater, container, false)
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
        isCancelable =false
        viewModel.fetchWorkFlow(UIConstants.AssessmentUniqueID)
        viewModel.getRiskEntityList()
        setupView()
        attachObservers()
        setClickListeners()
    }

    private fun attachObservers() {
        bpVitalsViewModel.assessmentPatientDetails.value?.data?.let {
            if (SecuredPreference.getUnitMeasurementType() == DefinedParams.Unit_Measurement_Metric_Type) {
                it.height?.let { heightValue ->
                    binding.etHeight.setText(CommonUtils.getDecimalFormatted(heightValue))
                }
            } else {
                it.height?.let { heightValue ->
                    binding.etFeet.setText(
                        CommonUtils.getDecimalFormatted(
                            CommonUtils.getFeetFromHeight(
                                heightValue
                            )
                        )
                    )
                    binding.etInches.setText(
                        CommonUtils.getDecimalFormatted(
                            CommonUtils.getInchesFromHeight(
                                heightValue
                            )
                        )
                    )
                }
            }
            it.weight?.let { weightValue ->
                binding.etWeight.setText(CommonUtils.getDecimalFormatted(weightValue))
            }
        }
        bpVitalsViewModel.createBPvitalResponse.observe(this) { resourceState ->
            when(resourceState.state){
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
                    bpVitalsViewModel.createBPvitalResponse.setError()
                    GeneralSuccessDialog.newInstance(requireContext(), getString(R.string.blood_pressure), getString(
                        R.string.blood_pressure_saved_successfully), needHomeNav = false)
                        .show(parentFragmentManager, GeneralSuccessDialog.TAG)
                    closeDialog()
                }
            }
        }

        viewModel.formResponseLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> {
                    hideLoading()
                    val formList = resourceState.data?.formLayout
                    formList?.let {
                        bpVitalsViewModel.height = it.find { formLayout ->
                            formLayout.id == DefinedParams.Height
                        }
                        bpVitalsViewModel.weight = it.find { formLayout ->
                            formLayout.id == DefinedParams.Weight
                        }
                        bpVitalsViewModel.bpLog = it.find { formLayout ->
                            formLayout.id == DefinedParams.BPLog_Details
                        }
                        populateFields()
                    }
                }
            }
        }
    }

    private fun closeDialog() {
        bpVitalsViewModel.refreshGraphDetails.setSuccess(data = true)
        dismiss()
    }

    private fun populateFields() {
        bpVitalsViewModel.height?.let { height ->
            binding.tvHeight.text = setTitle(height)
            binding.tvHeight.hint = setHint(height)
        }
        bpVitalsViewModel.weight?.let { weight->
            binding.tvWeight.text = setTitle(weight)
            binding.tvWeight.hint = setHint(weight)
        }
        binding.tvHeight.markMandatory()
        binding.tvWeight.markMandatory()
    }

    private fun setHint(formData: FormLayout): CharSequence? {
        if (SecuredPreference.getIsTranslationEnabled()) {
            return (formData.hintCulture ?: formData.hint)
        }
        return (formData.hint)
    }

    private fun setTitle(formData: FormLayout): CharSequence {
        if (SecuredPreference.getIsTranslationEnabled()) {
            return (formData.titleCulture ?: formData.title)
        }
        return (formData.title)
    }

    private fun setClickListeners() {
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.etAssessmentDate.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnAddReading.safeClickListener(this)
    }

    private fun setupView() {
        if (SecuredPreference.getUnitMeasurementType() == DefinedParams.Unit_Measurement_Metric_Type) {
            binding.imperialLayout.visibility = View.GONE
            binding.etHeight.visibility = View.VISIBLE
        } else {
            binding.imperialLayout.visibility = View.VISIBLE
            binding.etHeight.visibility = View.GONE
        }
        binding.tvAssessmentDate.markMandatory()
        binding.llBpReading.instructionsLayout.visibility = View.GONE
        binding.llBpReading.bpReadingThree.visibility = View.VISIBLE
        binding.llBpReading.apply {
            setMaxLength(etSystolicOne)
            setMaxLength(etDiastolicOne)
            setMaxLength(etPulseOne)
            setMaxLength(etSystolicTwo)
            setMaxLength(etDiastolicTwo)
            setMaxLength(etPulseTwo)
            setMaxLength(etSystolicThree)
            setMaxLength(etDiastolicThree)
            setMaxLength(etPulseThree)
            instructionsLayout.visibility = View.GONE
            tvSnoReadingTwo.isEnabled = false
            etSystolicTwo.isEnabled = false
            etDiastolicTwo.isEnabled = false
            etPulseTwo.isEnabled = false
            separatorRowTwo.isEnabled = false
            tvSnoReadingThree.isEnabled = false
            etSystolicThree.isEnabled = false
            etDiastolicThree.isEnabled = false
            etPulseThree.isEnabled = false
            separatorRowThree.isEnabled = false
        }

        val list = DefinedParams.getEmptyBPReading(3)
        bpVitalsViewModel.resultHashMap[DefinedParams.BPLog_Details] = list
        binding.llBpReading.etSystolicOne.addTextChangedListener {
            checkInputsAndEnableNextField(binding.llBpReading.tvSnoReading.text, list)
        }
        binding.llBpReading.etDiastolicOne.addTextChangedListener {
            checkInputsAndEnableNextField(binding.llBpReading.tvSnoReading.text, list)
        }
        binding.llBpReading.etPulseOne.addTextChangedListener {
            checkInputsAndEnableNextField(binding.llBpReading.tvSnoReading.text, list)
        }

        binding.llBpReading.etSystolicTwo.addTextChangedListener {
            checkInputsAndEnableNextField(binding.llBpReading.tvSnoReadingTwo.text, list)
        }
        binding.llBpReading.etDiastolicTwo.addTextChangedListener {
            checkInputsAndEnableNextField(binding.llBpReading.tvSnoReadingTwo.text, list)
        }
        binding.llBpReading.etPulseTwo.addTextChangedListener {
            checkInputsAndEnableNextField(binding.llBpReading.tvSnoReadingTwo.text, list)
        }
        binding.llBpReading.etSystolicThree.addTextChangedListener {
            checkInputsAndEnableNextField(binding.llBpReading.tvSnoReadingThree.text, list)
        }
        binding.llBpReading.etDiastolicThree.addTextChangedListener {
            checkInputsAndEnableNextField(binding.llBpReading.tvSnoReadingThree.text, list)
        }
        binding.llBpReading.etPulseThree.addTextChangedListener {
            checkInputsAndEnableNextField(binding.llBpReading.tvSnoReadingThree.text, list)
        }
    }

    private fun setMaxLength(appCompatEditText: AppCompatEditText) {
        appCompatEditText.filters = arrayOf(InputFilter.LengthFilter(3))
    }

    private fun checkInputsAndEnableNextField(
        text: CharSequence,
        list: ArrayList<BPModel>
    ) {
        val binding = binding.llBpReading
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

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }

    override fun onClick(mView: View?) {
        when(mView?.id) {
            binding.btnAddReading.id -> {
                validateInputs()
            }

            binding.btnCancel.id, binding.labelHeader.ivClose.id -> {
                dismiss()
            }

            binding.etAssessmentDate.id -> showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        var datePickerDialog: DatePickerDialog? = null
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.etAssessmentDate.text.isNullOrBlank())
            yearMonthDate = DateUtils.getYearMonthAndDate(binding.etAssessmentDate.text.toString())

        if (datePickerDialog == null) {
            datePickerDialog = ViewUtil.showDatePicker(
                context = requireContext(),
                maxDate = System.currentTimeMillis(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.etAssessmentDate.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_ddMMyyyy
                    )
                datePickerDialog = null
            }
        }
    }

    private fun validateInputs() {
        var isValid = true
        if(SecuredPreference.getUnitMeasurementType() == DefinedParams.Unit_Measurement_Metric_Type)
            isValid = validateHeightAndWeight(isValid, binding.etHeight, binding.tvHeightErrorMessage,
                bpVitalsViewModel.height, errorMessage = getString(R.string.validation_error,
                    getString(R.string.height_value)))
        else
        {
            binding.tvHeightErrorMessage.visibility = View.GONE
            isValid =
                validateHeightAndWeight(isValid, binding.etFeet, binding.tvHeightErrorMessage,
                    bpVitalsViewModel.height,getString(R.string.validation_error, getString(R.string.feet_value)))
            isValid = validateHeightAndWeight(
                isValid,
                binding.etInches,
                binding.tvHeightErrorMessage,
                bpVitalsViewModel.height,
                getString(R.string.validation_error, getString(R.string.inches_value))
            )

            if(isValid)
            {
                isValid = !CommonUtils.validateFeetAndInches(binding.etFeet.text.toString().toDouble(),
                    binding.etInches.text.toString().toDouble())
                if(!isValid)
                {
                    binding.tvHeightErrorMessage.visibility = View.VISIBLE
                    binding.tvHeightErrorMessage.text = getString(R.string.validation_error, getString(R.string.height_value))
                }
            }
        }
        isValid = validateHeightAndWeight(isValid, binding.etWeight, binding.tvWeightErrorMessage, bpVitalsViewModel.weight,
            errorMessage = getString(R.string.validation_error, getString(R.string.weight_value)))

        if (binding.etAssessmentDate.text.isNullOrBlank()) {
            isValid = false
            binding.tvAssessmentDateErrorMessage.visibility = View.VISIBLE
            binding.tvAssessmentDateErrorMessage.text = getString(R.string.valid_assessment_date)
        } else
            binding.tvAssessmentDateErrorMessage.visibility = View.GONE

        if(binding.llBpReading.etSystolicOne.text.isNullOrBlank() ||
                binding.llBpReading.etDiastolicOne.text.isNullOrBlank())
        {
            isValid = false
            binding.tvBpLogErrorMessage.text = bpVitalsViewModel.bpLog?.cultureErrorMessage ?: bpVitalsViewModel.bpLog?.errorMessage
            binding.tvBpLogErrorMessage.visibility = View.VISIBLE
        }else{
            val list = ArrayList<BPModel>()
            (bpVitalsViewModel.resultHashMap[DefinedParams.BPLog_Details] as ArrayList<BPModel>).forEach {
                if (it.diastolic != null || it.systolic != null ) {
                    list.add(it)
                }
            }
            bpVitalsViewModel.resultHashMap[DefinedParams.BPLog_Details] = list
            val validationBPResultModel =
                Validator.checkValidBPInput(requireContext(), list, bpVitalsViewModel.bpLog, SecuredPreference.getIsTranslationEnabled())
            if (validationBPResultModel.status) {
                bpVitalsViewModel.bpLog?.let { formGenerator?.calculateBPValues(formLayout = it, bpVitalsViewModel.resultHashMap) }
                binding.tvBpLogErrorMessage.visibility = View.GONE
            } else {
                isValid = false
                binding.tvBpLogErrorMessage.text = validationBPResultModel.message
                binding.tvBpLogErrorMessage.visibility = View.VISIBLE
            }
        }

        if (isValid) {
            processResultandProceed()
        }
    }

    private fun validateHeightAndWeight(
        validOrNot: Boolean,
        etInput: AppCompatEditText,
        tvErrorMessage: AppCompatTextView,
        formJson: FormLayout?,
        errorMessage: String? = null
    ):Boolean {
        var isValid= validOrNot
        if (etInput.text.isNullOrBlank()) {
            isValid = false
            tvErrorMessage.visibility = View.VISIBLE
            errorMessage?.let {
                tvErrorMessage.text = errorMessage
            }
        } else {
            isValid = setErrorView(
                etInput.text.toString().toDouble(),
                formJson?.minValue,
                formJson?.maxValue,
                tvErrorMessage,
                isValid
            )
        }
        return isValid
    }

    private fun setErrorView(
        value: Double,
        minValue: Double?,
        maxValue: Double?,
        tvErrorMessage: AppCompatTextView,
        validOrNot: Boolean,
    ): Boolean {
        var isValid = validOrNot
        if (minValue != null || maxValue != null) {
            if (minValue != null && maxValue != null) {
                if (value < minValue || value > maxValue) {
                    isValid = false
                    tvErrorMessage.visibility = View.VISIBLE
                    tvErrorMessage.text =
                        getString(
                            R.string.general_min_max_validation,
                            CommonUtils.getDecimalFormatted(minValue),
                            CommonUtils.getDecimalFormatted(maxValue)
                        )
                } else {
                    tvErrorMessage.visibility = View.GONE
                }
            } else if (minValue != null) {
                isValid = minMaxValueCheck(value, minValue, 0, isValid, tvErrorMessage)
            } else if (maxValue != null) {
                isValid = minMaxValueCheck(value, maxValue, 1, isValid, tvErrorMessage)
            }
        } else {
            tvErrorMessage.visibility = View.GONE
        }
        return isValid
    }

    private fun minMaxValueCheck(
        value: Double,
        minOrMaxValue: Double,
        minMaxFlg: Int,
        validOrNot: Boolean,
        tvErrorMessage: AppCompatTextView
    ):Boolean {
        var isValid = validOrNot
        if (minMaxFlg == 0)
        {
            if(value < minOrMaxValue)
            {
                isValid = false
                tvErrorMessage.visibility = View.VISIBLE
                tvErrorMessage.text =
                    getString(
                        R.string.general_min_validation,
                        CommonUtils.getDecimalFormatted(minOrMaxValue)
                    )
            }else {
                tvErrorMessage.visibility = View.GONE
            }
        }else{
            if(value > minOrMaxValue)
            {
                isValid = false
                tvErrorMessage.visibility = View.VISIBLE
                tvErrorMessage.text = getString(
                    R.string.general_max_validation,
                    CommonUtils.getDecimalFormatted(minOrMaxValue)
                )
            }else {
                tvErrorMessage.visibility = View.GONE
            }
        }
        return isValid
    }

    private fun processResultandProceed() {
        if (SecuredPreference.getUnitMeasurementType() == DefinedParams.Unit_Measurement_Metric_Type)
            bpVitalsViewModel.resultHashMap[DefinedParams.Height] = binding.etHeight.text.toString().toDouble()
        else
        {
            bpVitalsViewModel.resultHashMap[DefinedParams.Feet] = binding.etFeet.text.toString().toDouble()
            bpVitalsViewModel.resultHashMap[DefinedParams.Inches] = binding.etInches.text.toString().toDouble()
        }
        bpVitalsViewModel.resultHashMap[DefinedParams.Weight] = binding.etWeight.text.toString().toDouble()
        bpVitalsViewModel.resultHashMap[DefinedParams.UnitMeasurement] = SecuredPreference.getUnitMeasurementType()
        bpVitalsViewModel.resultHashMap[DefinedParams.PatientTrackId] = bpVitalsViewModel.patientTrackId
        bpVitalsViewModel.resultHashMap[DefinedParams.BPTakenOn] = DateUtils.getDateStringInFormat(
            binding.etAssessmentDate.text.toString(),
            DateUtils.DATE_ddMMyyyy,
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
        )
        bpVitalsViewModel.patientDetailsResponse.value?.data?.let { patientDetails ->
            patientDetails.age?.let {
                bpVitalsViewModel.resultHashMap[DefinedParams.Age] = it
            }
            patientDetails.gender?.let {
                bpVitalsViewModel.resultHashMap[DefinedParams.Gender] = it
            }
            patientDetails.isRegularSmoker?.let {
                bpVitalsViewModel.resultHashMap[DefinedParams.Is_Regular_Smoker] = it
            }
        }
        CommonUtils.calculateAverageBloodPressure(requireContext(), bpVitalsViewModel.resultHashMap)
        CommonUtils.calculateBMI(bpVitalsViewModel.resultHashMap, SecuredPreference.getUnitMeasurementType())
        CommonUtils.calculateCVDRiskFactor(
            bpVitalsViewModel.resultHashMap,
            viewModel.list,
            bpVitalsViewModel.resultHashMap[DefinedParams.Avg_Systolic] as Int?
        )
        if(connectivityManager.isNetworkAvailable())
        {
            bpVitalsViewModel.createBpLog(bpVitalsViewModel.resultHashMap)
        }else{
            (activity as BaseActivity).showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error), false
            ) {}
        }
    }
}