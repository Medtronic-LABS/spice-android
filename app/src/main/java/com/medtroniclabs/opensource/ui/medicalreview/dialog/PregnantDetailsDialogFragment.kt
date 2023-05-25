package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.ViewUtil
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.data.model.PatientPregnancyModel
import com.medtroniclabs.opensource.data.model.PregnancyCreateRequest
import com.medtroniclabs.opensource.databinding.FragmentPregnantDetailsDialogBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.PregnancySubmitListener
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import java.util.*

class PregnantDetailsDialogFragment() :
    DialogFragment(), View.OnClickListener {

    constructor(pregnancySubmitListener: PregnancySubmitListener) : this() {
        this.pregnancySubmitListener = pregnancySubmitListener
    }

    private lateinit var binding: FragmentPregnantDetailsDialogBinding
    private var pregnancySubmitListener: PregnancySubmitListener? = null
    private val viewModel: PatientDetailViewModel by activityViewModels()
    private var resultHashMap = HashMap<String, Any>()
    private var patientDetails: PatientDetailsModel? = null
    private var lastMenstrualDate: Date? = null
    private var estimatedDeliveryDate: Date? = null
    private var gestationalAge: String? = null

    companion object {
        const val TAG = "PregnantDetails"
        fun newInstance(pregnancySubmitListener: PregnancySubmitListener): PregnantDetailsDialogFragment {
            val fragment = PregnantDetailsDialogFragment(pregnancySubmitListener)
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
        binding = FragmentPregnantDetailsDialogBinding.inflate(inflater, container, false)

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
        initViews()
        attachListeners()
        setListeners()
        apiCalls()
    }

    private fun initViews() {
        binding.apply {
            tvTitleDiagnosis.markMandatory()
            tvPatientOnTreatmentTitle.markMandatory()
            tvTemperatureTitle.text =
                if (SecuredPreference.getUnitMeasurementType() == DefinedParams.Unit_Measurement_Imperial_Type) getString(
                    R.string.temperature_in_fahrenheit
                ) else getString(R.string.temperature_in_celsius)
        }
    }

    private fun apiCalls() {
        patientDetails?.let {
            viewModel.getPatientPregnancyDetails(requireContext(),PatientPregnancyModel(it._id, patientPregnancyId = viewModel.patientPregnancyId))
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnClose, binding.labelHeader.ivClose.id, R.id.btnCancel -> {
                dismiss()
            }
            R.id.btnConfirm -> {
                savePregnancyDetails()
            }
            R.id.etLastMensturalDate -> {
                val yearMonthDate =
                    DateUtils.getYearMonthAndDate(binding.etLastMensturalDate.text.toString())
                openDatePicker(
                    yearMonthDate.first,
                    yearMonthDate.second,
                    yearMonthDate.third,
                    disableFutureDate = true
                ) { parsedDate ->
                    lastMenstrualDate = parsedDate
                    val sdf = DateUtils.parseDateWithTimeZone(DateUtils.DATE_ddMMyyyy)
                    binding.etLastMensturalDate.text = sdf.format(parsedDate)
                    calculateGestationalAge()
                }
            }

            R.id.etEstimatedDelivery -> {
                val yearMonthDate =
                    DateUtils.getYearMonthAndDate(binding.etEstimatedDelivery.text.toString())
                openDatePicker(
                    yearMonthDate.first,
                    yearMonthDate.second,
                    yearMonthDate.third,
                    disableFutureDate = false
                ) { parsedDate ->
                    estimatedDeliveryDate = parsedDate
                    val sdf = DateUtils.parseDateWithTimeZone(DateUtils.DATE_ddMMyyyy)
                    binding.etEstimatedDelivery.text = sdf.format(parsedDate)
                    calculateGestationalAge()
                }
            }
            R.id.etTimeOfDiagnosis -> {
                val yearMonthDate =
                    DateUtils.getYearMonthAndDate(binding.etTimeOfDiagnosis.text.toString())
                openDatePicker(
                    yearMonthDate.first,
                    yearMonthDate.second,
                    yearMonthDate.third
                ) { parsedDate ->
                    val sdf = DateUtils.parseDateWithTimeZone(DateUtils.DATE_ddMMyyyy)
                    binding.etTimeOfDiagnosis.text = sdf.format(parsedDate)
                }
            }
        }
    }

    private fun attachListeners() {
        viewModel.patientPregnancyDetailResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    populateData(resourceState.data)
                }
            }
        }
        viewModel.pregnancyCreateResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    (activity as BaseActivity).showErrorDialogue(
                        getString(R.string.error),
                        if (resourceState.message.isNullOrBlank()) getString(R.string.error) else resourceState.message,
                        false,
                        ){}
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { _ ->
                        if (viewModel.allowDismiss) {
                            pregnancySubmitListener?.onSubmit(gestationalAge)
                            dismiss()
                        }
                    }
                }
            }
        }

        viewModel.patientDetailsResponse.value?.data?.let {
            patientDetails = it
        }
    }

    private fun populateData(data: PregnancyCreateRequest?) {
        data?.let {
            viewModel.patientPregnancyId = it.id
            binding.apply {
                etGravida.setText(it.gravida?.toString() ?: "")
                etParity.setText(it.parity?.toString() ?: "")
                etTemperature.setText(it.temperature?.toString() ?: "")
                etNoOfFetuses.setText(it.pregnancyFetusesNumber?.toString() ?: "")
                it.lastMenstrualPeriodDate?.let { lastMenstrualPeriodDate ->
                    lastMenstrualDate =
                        DateUtils.parseDateWithTimeZone(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
                            .parse(lastMenstrualPeriodDate)
                    etLastMensturalDate.text = DateUtils.parseTextInUserTimeZone(
                        lastMenstrualPeriodDate,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DateUtils.DATE_ddMMyyyy
                    )
                }
                it.estimatedDeliveryDate?.let { estDeliveryDate ->
                    estimatedDeliveryDate =
                        DateUtils.parseDateWithTimeZone(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
                            .parse(estDeliveryDate)
                    etEstimatedDelivery.text = DateUtils.parseTextInUserTimeZone(
                        estDeliveryDate,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DateUtils.DATE_ddMMyyyy
                    )
                }
                calculateGestationalAge()
                it.diagnosis?.let { diagnosis ->
                    rbNone.isChecked = false
                    rbEclampsia.isChecked = false
                    rbPreEclampsia.isChecked = false
                    rbGestationalDiabetes.isChecked = false
                    setDiagnosis(diagnosis)
                }
                it.isOnTreatment?.let { isOnTreatment ->
                    rbYes.isChecked = isOnTreatment
                    rbNo.isChecked = !isOnTreatment
                }
                etTimeOfDiagnosis.text =
                    if (it.diagnosisTime == null) "" else DateUtils.parseTextInUserTimeZone(
                        it.diagnosisTime,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DateUtils.DATE_ddMMyyyy
                    )
            }
        }
    }

    private fun setDiagnosis(diagnosis: ArrayList<String>) {
        binding.apply {
            if (diagnosis.isNotEmpty()) {
                if (diagnosis.contains(getString(R.string.none))) {
                    rbNone.isChecked = true
                    showTreatmentDetails(false)
                } else {
                    setRb(diagnosis)
                    showTreatmentDetails(true)
                }
            }
        }
    }

    private fun setRb(diagnosis: ArrayList<String>) {
        binding.apply {
            if (diagnosis.contains(getString(R.string.eclampsia))) {
                rbEclampsia.isChecked = true
            }
            if (diagnosis.contains(getString(R.string.pre_eclampsia))) {
                rbPreEclampsia.isChecked = true
            }
            if (diagnosis.contains(getString(R.string.gestational_diabetes))) {
                rbGestationalDiabetes.isChecked = true
            }
        }
    }

    private fun setListeners() {
        binding.etLastMensturalDate.safeClickListener(this)
        binding.etEstimatedDelivery.safeClickListener(this)
        binding.etTimeOfDiagnosis.safeClickListener(this)
        binding.rbNone.setOnCheckedChangeListener { _, isChecked ->
            rbNoneListener(isChecked)
        }
        binding.rbEclampsia.setOnCheckedChangeListener { _, isChecked ->
            rbEclampsiaListener(isChecked)
        }
        binding.rbPreEclampsia.setOnCheckedChangeListener { _, isChecked ->
            rbPreEclampsiaListener(isChecked)
        }
        binding.rbGestationalDiabetes.setOnCheckedChangeListener { _, isChecked ->
            rbGestationalDiabetesListener(isChecked)
        }
        binding.rgPatientOnTreatment.setOnCheckedChangeListener { _, buttonID ->
            when (buttonID) {
                R.id.rbNo -> {
                    binding.etTimeOfDiagnosis.text = ""
                    resultHashMap.remove(DefinedParams.diagnosis_time)
                    resultHashMap[DefinedParams.is_on_treatment] = DefinedParams.NegativeValue
                    showDiagnosisDetails(false)
                }
                R.id.rbYes -> {
                    binding.etTimeOfDiagnosis.text = ""
                    resultHashMap.remove(DefinedParams.diagnosis_time)
                    resultHashMap[DefinedParams.is_on_treatment] = DefinedParams.PositiveValue
                    showDiagnosisDetails(true)
                }
            }
        }
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnConfirm.safeClickListener(this)
    }

    private fun rbGestationalDiabetesListener(isChecked: Boolean) {
        if (isChecked) {
            binding.rbNone.isChecked = false
            if (binding.llPatientOnTreatment.visibility == View.GONE) {
                showTreatmentDetails(true)
                resultHashMap.remove(DefinedParams.is_on_treatment)
            }
        } else {
            val list = selectedDiagnosis()
            if (list.isEmpty()) {
                showTreatmentDetails(false)
                resultHashMap.remove(DefinedParams.is_on_treatment)
            }
        }
    }

    private fun rbPreEclampsiaListener(isChecked: Boolean) {
        if (isChecked) {
            binding.rbNone.isChecked = false
            if (binding.llPatientOnTreatment.visibility == View.GONE) {
                showTreatmentDetails(true)
                resultHashMap.remove(DefinedParams.is_on_treatment)
            }
        } else {
            val list = selectedDiagnosis()
            if (list.isEmpty()) {
                showTreatmentDetails(false)
                resultHashMap.remove(DefinedParams.is_on_treatment)
            }
        }
    }

    private fun rbEclampsiaListener(isChecked: Boolean) {
        if (isChecked) {
            binding.rbNone.isChecked = false
            if (binding.llPatientOnTreatment.visibility == View.GONE) {
                showTreatmentDetails(true)
                resultHashMap.remove(DefinedParams.is_on_treatment)
            }
        } else {
            val list = selectedDiagnosis()
            if (list.isEmpty()) {
                showTreatmentDetails(false)
                resultHashMap.remove(DefinedParams.is_on_treatment)
            }
        }
    }

    private fun rbNoneListener(isChecked: Boolean) {
        if (isChecked) {
            showTreatmentDetails(false)
            resultHashMap.remove(DefinedParams.is_on_treatment)
            binding.rbEclampsia.isChecked = false
            binding.rbPreEclampsia.isChecked = false
            binding.rbGestationalDiabetes.isChecked = false
        }
    }

    private fun openDatePicker(
        year: Int? = null,
        month: Int? = null,
        date: Int? = null,
        disableFutureDate: Boolean = true,
        callBack: (parsedDate: Date) -> Unit
    ) {
        val date:Triple<Int?,Int?,Int?> = Triple(year,month,date)
        ViewUtil.showDatePicker(
            context = requireContext(),
            disableFutureDate = disableFutureDate,
            date = date
        ) { _, mYear, mMonth, mDayOfMonth ->
            val stringDate = "$mDayOfMonth-$mMonth-$mYear"
            val sdf = DateUtils.parseDateWithTimeZone(DateUtils.DATE_FORMAT_ddMMyyyy)
            val parsedDate = sdf.parse(stringDate)
            parsedDate?.let {
                callBack(it)
            }
        }
    }

    private fun calculateGestationalAge() {
        CommonUtils.calculateGestationalAge(lastMenstrualDate, estimatedDeliveryDate)
            ?.let { weeks ->
                binding.tvGestationalAge.text = getString(R.string.gestational_age_weeks, " $weeks")
                gestationalAge = weeks
            } ?: kotlin.run {
            gestationalAge = "-"
            binding.tvGestationalAge.text = getString(R.string.gestational_age)
        }
    }


    private fun showTreatmentDetails(hasToBeShown: Boolean) {
        if (hasToBeShown) {
            binding.llPatientOnTreatment.visibility = View.VISIBLE
        } else {
            binding.llPatientOnTreatment.visibility = View.GONE
        }
        binding.rgPatientOnTreatment.clearCheck()
        showDiagnosisDetails(false)
    }

    private fun showDiagnosisDetails(hasToBeShown: Boolean) {
        if (hasToBeShown) {
            binding.llTimeOfDiagnosis.visibility = View.VISIBLE
            binding.etTimeOfDiagnosis.text = ""
        } else {
            binding.llTimeOfDiagnosis.visibility = View.GONE
            binding.etTimeOfDiagnosis.text = ""
        }
    }

    private fun savePregnancyDetails() {
        if (validateInputs()) {
            getInputFieldsValues()
            val lastMenstrualDateString = binding.etLastMensturalDate.text.toString()
            if (lastMenstrualDateString.isNotEmpty()) {
                resultHashMap[DefinedParams.last_menstrual_period_date] =
                    DateUtils.parseTextInUserTimeZone(
                        lastMenstrualDateString,
                        DateUtils.DATE_ddMMyyyy, DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    )
            }
            val estimatedDelivery = binding.etEstimatedDelivery.text.toString()
            if (estimatedDelivery.isNotEmpty()) {
                resultHashMap[DefinedParams.estimated_delivery_date] =
                    DateUtils.parseTextInUserTimeZone(
                        estimatedDelivery,
                        DateUtils.DATE_ddMMyyyy, DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    )
            }
            val timeOfDiagnosis = binding.etTimeOfDiagnosis.text.toString()
            if (timeOfDiagnosis.isNotEmpty()) {
                resultHashMap[DefinedParams.diagnosis_time] = DateUtils.parseTextInUserTimeZone(
                    timeOfDiagnosis,
                    DateUtils.DATE_ddMMyyyy, DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                )
            } else {
                resultHashMap.remove(DefinedParams.diagnosis_time)
            }
            patientDetails?.let { patientDetails ->
                resultHashMap[DefinedParams.PatientTrackId] = patientDetails._id
                SecuredPreference.getSelectedSiteEntity()?.let { siteEntity ->
                    resultHashMap[DefinedParams.TenantId] = siteEntity.tenantId
                }
            }
            var isFromUpdate = false
            viewModel.patientPregnancyId?.let {
                isFromUpdate = true
                resultHashMap[DefinedParams.patient_pregnancy_id] = it
            }
            viewModel.allowDismiss = true
            resultHashMap[DefinedParams.UnitMeasurement] = SecuredPreference.getUnitMeasurementType()
            viewModel.createPregnancy(requireContext(),resultHashMap, isFromUpdate)
        }
    }

    private fun getInputFieldsValues() {
        binding.etGravida.text?.let {
            if (it.isNotEmpty()) {
                resultHashMap[DefinedParams.gravida] = it.toString()
            }
        }
        binding.etParity.text?.let {
            if (it.isNotEmpty()) {
                resultHashMap[DefinedParams.parity] = it.toString()
            }
        }
        binding.etTemperature.text?.let {
            if (it.isNotEmpty()) {
                resultHashMap[DefinedParams.Temperature] = it.toString()
                resultHashMap[DefinedParams.UnitMeasurement] =
                    SecuredPreference.getUnitMeasurementType()
            }
        }
        binding.etNoOfFetuses.text?.let {
            if (it.isNotEmpty()) {
                resultHashMap[DefinedParams.pregnancy_fetuses_number] = it.toString()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValidInput = true
        binding.tvErrorDiagnosis.visibility = View.GONE
        binding.tvErrorPatientOnTreatment.visibility = View.GONE
        binding.tvErrorLastMensturalDate.visibility = View.GONE
        val list = selectedDiagnosis()
        if (list.isNotEmpty()) {
            resultHashMap[DefinedParams.diagnosis] = list
        } else {
            binding.tvErrorDiagnosis.visibility = View.VISIBLE
            binding.tvErrorDiagnosis.text =
                requireContext().getString(R.string.default_user_input_error)
            isValidInput = false
        }
        if (resultHashMap.isNullOrEmpty()) {
            binding.tvErrorDiagnosis.visibility = View.VISIBLE
            binding.tvErrorDiagnosis.text =
                requireContext().getString(R.string.default_user_input_error)
            isValidInput = false
        } else {
            if (!list.contains(getString(R.string.none)) && !resultHashMap.containsKey(DefinedParams.is_on_treatment)) {
                    binding.tvErrorPatientOnTreatment.text =
                        requireContext().getString(R.string.default_user_input_error)
                    binding.tvErrorPatientOnTreatment.visibility = View.VISIBLE
                    isValidInput = false
            }
        }
        return isValidInput
    }

    private fun selectedDiagnosis(): ArrayList<String> {
        var list = ArrayList<String>()
        if (binding.rbNone.isChecked) {
            list.add(getString(R.string.none))
        } else {
            if (binding.rbEclampsia.isChecked) {
                list.add(getString(R.string.eclampsia))
            }
            if (binding.rbPreEclampsia.isChecked) {
                list.add(getString(R.string.pre_eclampsia))
            }
            if (binding.rbGestationalDiabetes.isChecked) {
                list.add(getString(R.string.gestational_diabetes))
            }
        }
        return list
    }

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }

}