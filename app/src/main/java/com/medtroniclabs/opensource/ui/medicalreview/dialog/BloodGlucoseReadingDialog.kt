package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.appextensions.setError
import com.medtroniclabs.opensource.appextensions.setSuccess
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.ViewUtil
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.databinding.FragmentBloodGlucoseReadingBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.formgeneration.model.FormLayout
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.TagListCustomView
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.AssessmentReadingViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BloodGlucoseReadingDialog : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentBloodGlucoseReadingBinding
    private lateinit var tagListCustomView: TagListCustomView
    private var datePickerDialog: DatePickerDialog? = null
    private val viewModel: AssessmentReadingViewModel by activityViewModels()
    private val patientDetailViewModel: PatientDetailViewModel by activityViewModels()
    @Inject
    lateinit var connectivityManager: ConnectivityManager

    companion object{
        val TAG = "BloodGlucoseReadingDialog"
        fun newInstance(): BloodGlucoseReadingDialog {
            return BloodGlucoseReadingDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBloodGlucoseReadingBinding.inflate(inflater, container, false)
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
        initializeTagView()
    }

    private fun attachObservers() {

        patientDetailViewModel.createBloodGlucoseResponse.observe(this) { resourceState ->
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
                    patientDetailViewModel.createBloodGlucoseResponse.setError()
                    GeneralSuccessDialog.newInstance(
                        requireContext(), getString(R.string.blood_glucose), getString(
                            R.string.blood_glucose_saved_successfully
                        ), needHomeNav = false
                    )
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
                        patientDetailViewModel.bloodGlucose = it.find { formLayout ->
                            formLayout.id == DefinedParams.BloodGlucoseID
                        }
                        patientDetailViewModel.hbA1c = it.find { formLayout ->
                            formLayout.id == DefinedParams.Hemoglobin
                        }
                        populateFields()
                    }
                }
            }
        }
    }

    private fun closeDialog() {
        patientDetailViewModel.refreshGraphDetails.setSuccess(data = true)
        dismiss()
    }

    private fun populateFields() {
        patientDetailViewModel.bloodGlucose?.let { bloodGlucose->
            binding.tvBloodGlucoseTitle.text = setTitle(bloodGlucose)
            binding.etBloodGlucose.hint = setHint(bloodGlucose)
        }
        patientDetailViewModel.hbA1c?.let { hbA1c->
            binding.tvHbA1c.text = setTitle(hbA1c)
            binding.etHbA1c.hint = setHint(hbA1c)
        }
        binding.tvBloodGlucoseTitle.markMandatory()
    }

    private fun setHint(formData: FormLayout): CharSequence? {
        if (SecuredPreference.getIsTranslationEnabled()) {
            return (formData.hintCulture ?: formData.hint)
        }
        return (formData.hint)
    }

    private fun setTitle(formData: FormLayout): CharSequence {
        if (SecuredPreference.getIsTranslationEnabled()) {
            return (formData.titleCulture ?: formData.title) + " (${formData.unitMeasurement})"
        }
        return (formData.title) + " (${formData.unitMeasurement})"
    }


    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }

    private fun setClickListeners() {
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnAddReading.safeClickListener(this)
        binding.etAssessmentDate.safeClickListener(this)
    }

    private fun initializeTagView() {
        tagListCustomView = TagListCustomView(requireContext(), binding.cgSelectType) {}
        val bgChipList = mutableListOf(DefinedParams.FBS, DefinedParams.RBS)
        val hm: HashMap<String, MutableList<String>> = HashMap()
        hm[DefinedParams.BloodGlucoseID] = bgChipList
        tagListCustomView.addChipItemList(bgChipList, null, hm)
    }

    private fun setupView() {
        binding.tvAssessmentDate.markMandatory()
        binding.tvSelectType.markMandatory()
    }

    override fun onClick(mView: View?) {
        when (mView?.id) {
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

        if (binding.etBloodGlucose.text.isNullOrBlank()) {
            isValid = false
            binding.tvBloodGlucoseErrorMessage.visibility = View.VISIBLE
            binding.tvBloodGlucoseErrorMessage.text = getString(R.string.validation_error, getString(
                            R.string.glucose_value))
        } else {
            isValid = setErrorView(
                binding.etBloodGlucose.text.toString().toDouble(),
                patientDetailViewModel.bloodGlucose?.minValue,
                patientDetailViewModel.bloodGlucose?.maxValue,
                binding.tvBloodGlucoseErrorMessage,
                isValid
            )
        }

        if (!binding.etHbA1c.text.isNullOrBlank()) {
            isValid = setErrorView(
                binding.etHbA1c.text.toString().toDouble(),
                patientDetailViewModel.hbA1c?.minValue,
                patientDetailViewModel.hbA1c?.maxValue,
                binding.tvHbA1cErrorMessage,
                isValid
            )
        }

        if (binding.etAssessmentDate.text.isNullOrBlank()) {
            isValid = false
            binding.tvAssessmentDateErrorMessage.visibility = View.VISIBLE
            binding.tvAssessmentDateErrorMessage.text = getString(R.string.valid_assessment_date)
        } else
            binding.tvAssessmentDateErrorMessage.visibility = View.GONE

        if(tagListCustomView.getSelectedTags().isEmpty()){
            isValid = false
            binding.tvSelectTypeErrorMessage.visibility = View.VISIBLE
            binding.tvSelectTypeErrorMessage.text = getString(R.string.bg_select_type)
        }else{
            binding.tvSelectTypeErrorMessage.visibility = View.GONE
        }

        if (isValid) {
            processResultandProceed()
        }
    }

    private fun setErrorView(
        value: Double,
        minValue: Double?,
        maxValue: Double?,
        tvErrorMessage: AppCompatTextView,
        validOrNot: Boolean,
    ):Boolean {
        var isValid = validOrNot
        if (minValue != null || maxValue != null) {
            if(minValue != null && maxValue != null) {
                if (value < minValue || value > maxValue) {
                    isValid = false
                    tvErrorMessage.visibility = View.VISIBLE
                    tvErrorMessage.text =
                        getString(
                            R.string.general_min_max_validation,
                            CommonUtils.getDecimalFormatted(minValue),
                            CommonUtils.getDecimalFormatted(maxValue))
                }else {
                    tvErrorMessage.visibility = View.GONE
                }
            }else if(minValue !=null)
            {
                isValid = minMaxValueCheck(value, minValue, 0,isValid,tvErrorMessage)
            }else if(maxValue!=null){
                isValid = minMaxValueCheck(value, maxValue, 1,isValid,tvErrorMessage)
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
        patientDetailViewModel.bgResultHashMap.clear()
        patientDetailViewModel.bloodGlucose?.unitMeasurement?.let { bgUnit ->
            patientDetailViewModel.bgResultHashMap[DefinedParams.BloodGlucoseUnit] = bgUnit
        }
        patientDetailViewModel.hbA1c?.unitMeasurement?.let { hbA1Unit ->
            patientDetailViewModel.bgResultHashMap[DefinedParams.HBA1CUnit] = hbA1Unit
        }
        patientDetailViewModel.bgResultHashMap[DefinedParams.BloodGlucoseID] = binding.etBloodGlucose.text.toString().toDouble()
        patientDetailViewModel.bgResultHashMap[DefinedParams.Glucose_Value] = binding.etBloodGlucose.text.toString().toDouble()
        patientDetailViewModel.bgResultHashMap[DefinedParams.PatientTrackId] = patientDetailViewModel.patientTrackId
        patientDetailViewModel.bgResultHashMap[DefinedParams.Glucose_Type] = (tagListCustomView.getSelectedTags()[0] as String).lowercase()
        if(!binding.etHbA1c.text.isNullOrBlank())
        {
            patientDetailViewModel.bgResultHashMap[DefinedParams.Hemoglobin] = binding.etHbA1c.text.toString().toDouble()
        }
        patientDetailViewModel.bgResultHashMap[DefinedParams.Glucose_Date_Time] = DateUtils.getDateStringInFormat(
            binding.etAssessmentDate.text.toString(),
            DateUtils.DATE_ddMMyyyy,
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
        )

        patientDetailViewModel.bgResultHashMap[DefinedParams.BGTakenOn] = DateUtils.getDateStringInFormat(
            binding.etAssessmentDate.text.toString(),
            DateUtils.DATE_ddMMyyyy,
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
        )

        if(connectivityManager.isNetworkAvailable())
        {
            patientDetailViewModel.createGlucoseLog(patientDetailViewModel.bgResultHashMap)
        }else{
            (activity as BaseActivity).showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error), false
            ) {}
        }
    }
}