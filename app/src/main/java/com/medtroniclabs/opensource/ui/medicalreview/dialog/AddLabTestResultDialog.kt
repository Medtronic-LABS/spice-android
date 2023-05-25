package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.internal.LinkedTreeMap
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.*
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.ViewUtil.showDatePicker
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.LabTestModel
import com.medtroniclabs.opensource.databinding.AddLabTestResultBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.RoleConstant
import com.medtroniclabs.opensource.ui.medicalreview.adapter.LabTestResultsAdapter
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.LabTestViewModel

class AddLabTestResultDialog() : DialogFragment(),
    View.OnClickListener {

    constructor(
        callback: (isPositiveResult: Boolean) -> Unit
    ) : this() {
        this.callback = callback
    }

    companion object {
        const val TAG = "AddLabTestResultDialog"
        const val KEY_MODEL = "KEY_MODEL"
        const val KEY_IS_TECHNICIAN = "KEY_IS_TECHNICIAN"
        fun newInstance(
            labTestModel: LabTestModel,
            isFromTechnicianLogin: Boolean = false,
            callback: (isPositiveResult: Boolean) -> Unit
        ): AddLabTestResultDialog {
            val fragment = AddLabTestResultDialog(callback)
            fragment.arguments = Bundle().apply {
                putBoolean(KEY_IS_TECHNICIAN, isFromTechnicianLogin)
                putSerializable(KEY_MODEL, labTestModel)
            }
            return fragment
        }
    }
    private var isFromTechnicianLogin: Boolean = false
    private var callback: ((isPositiveResult: Boolean) -> Unit)? = null
    private lateinit var labTestModel: LabTestModel
    private lateinit var binding: AddLabTestResultBinding
    private lateinit var resultsAdapter: LabTestResultsAdapter
    private val viewModel: LabTestViewModel by activityViewModels()
    private var datePickerDialog: DatePickerDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddLabTestResultBinding.inflate(inflater, container, false)
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
        initializeViews()
        setListeners()
        setAdapterViews()
        attachObservers()
    }

    private fun readArguments() {
        arguments?.let { args ->
            if (args.containsKey(KEY_IS_TECHNICIAN))
                isFromTechnicianLogin = args.getBoolean(KEY_IS_TECHNICIAN)
            if (args.containsKey(KEY_MODEL)) {
                (args.customGetSerializable(KEY_MODEL) as LabTestModel?)?.let {
                    labTestModel = it
                }
            }
        }
    }

    private fun attachObservers() {
        viewModel.createResultResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        (activity as BaseActivity).showErrorDialogue(getString(R.string.error),message, false){}
                        viewModel.createResultResponse.postError(null)
                    }
                }
                ResourceState.SUCCESS -> {
                    viewModel.createResultResponse.setError()
                    dismiss()
                    callback?.invoke(true)
                }
            }
        }
    }

    fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }

    private fun setListeners() {
        binding.btnSave.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.titleCard.ivClose.safeClickListener(this)
        binding.tvTestedOn.safeClickListener(this)
    }

    private fun setAdapterViews() {
        val layoutManager = GridLayoutManager(requireContext(), 1, RecyclerView.VERTICAL, false)
        binding.rvResults.layoutManager = layoutManager
        binding.rvResults.adapter = resultsAdapter
    }

    private fun initializeViews() {
        resultsAdapter = LabTestResultsAdapter(labTestModel.labTestName)
        labTestModel.patientLabtestResults?.let {
            resultsAdapter.setData(it, viewModel.labTestUnitList)
        }
        binding.titleCard.titleView.text = labTestModel.labTestName ?: "-"
        binding.tvTestedOnLbl.markMandatory()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.attributes?.windowAnimations = R.style.dialogEnterExitAnimation
    }

    override fun onClick(mView: View?) {
        when (mView?.id) {
            binding.btnSave.id -> {
                validateInputs()
            }

            binding.btnCancel.id, binding.titleCard.ivClose.id -> {
                dismiss()
                callback?.invoke(false)
            }

            binding.tvTestedOn.id -> showDatePickerDialog()
        }
    }

    private fun validateInputs() {
        var isValid = true

        if (binding.tvTestedOn.text.isNullOrBlank()) {
            isValid = false
            binding.tvTestedOnError.visibility = View.VISIBLE
            binding.tvTestedOnError.text = getString(R.string.select_tested_on_date)
        } else
            binding.tvTestedOnError.visibility = View.GONE

        if (!resultsAdapter.validateInputs())
            isValid = false

        if (isValid)
            createRequest()
        else
            resultsAdapter.notifyDataSetChanged()
    }

    private fun createRequest() {
        val request = HashMap<String, Any>()
        val labResults = resultsAdapter.getResultsList()
        var isEmptyRanges = true
        labResults.forEach { resultMap ->
            //Entered Value
            val result = resultMap[DefinedParams.Result_Value]
            var enteredValue: Double? = null
            if (result is String)
                enteredValue = result.toDoubleOrNull()

            //Comparing the selected unit test range with the entered value
            val selectedUnit = resultMap[DefinedParams.Unit]
            val rangesList = resultMap[DefinedParams.Lab_Result_Range]
            if (rangesList is ArrayList<*>) {
                isEmptyRanges = rangesList.isEmpty()
                handleRangeList(resultMap, rangesList, selectedUnit, enteredValue)
            }
            request[DefinedParams.Is_Empty_Ranges] = isEmptyRanges

            //Removing the Lab Result Ranges list(As we have calculated the value for the is_abnormal key)
            resultMap.remove(DefinedParams.Lab_Result_Range)
        }
        request[DefinedParams.Patient_LabTest_Results] = labResults
        request[DefinedParams.Patient_LabTest_Id] = labTestModel._id ?: -1
        SecuredPreference.getSelectedSiteEntity()?.tenantId?.let {
            request[DefinedParams.TenantId] = it
        }
        request[DefinedParams.Tested_On] = DateUtils.convertDateTimeToDate(
            binding.tvTestedOn.text.toString(),
            DateUtils.DATE_FORMAT_ddMMMyyyy,
            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
            inUserTimeZone = true
        )
        request[DefinedParams.Comment] = binding.etComment.text?.toString() ?: ""
        SecuredPreference.getSelectedSiteEntity()?.let {
            it.role?.let { role ->
                request[DefinedParams.RoleName] = role
            }
            request[DefinedParams.Is_Reviewed] = (it.role == RoleConstant.PROVIDER || it.role == RoleConstant.PHYSICIAN_PRESCRIBER)
        }
        viewModel.createLabTestResult(requireContext(),request)
    }

    private fun handleRangeList(
        resultMap: HashMap<String, Any>,
        rangesList: ArrayList<*>,
        selectedUnit: Any?,
        enteredValue: Double?
    ) {
        rangesList.forEach range@{ range ->
            if (range is LinkedTreeMap<*, *>) {
                val unit = range[DefinedParams.Unit]
                if (unit is String && selectedUnit is String && unit == selectedUnit) {
                    validateRanges(resultMap, range, enteredValue)
                    return@range
                }
            }
        }
    }

    private fun validateRanges(
        resultMap: HashMap<String, Any>,
        range: LinkedTreeMap<*, *>,
        enteredValue: Double?
    ) {
        val min = range[DefinedParams.Minimum_Value] ?: 0.0
        val max = range[DefinedParams.Maximum_Value]
        max?.let { maximumRange ->
            if (min is Double && maximumRange is Double) {
                enteredValue?.let { value ->
                    val isNormal = value >= min && value <= maximumRange
                    resultMap[DefinedParams.Is_Abnormal] = !isNormal
                    resultMap[DefinedParams.Result_Status] =
                        if (isNormal) DefinedParams.Result_Negative else DefinedParams.Result_Positive
                }
            }
        }
        if (!resultMap.containsKey(DefinedParams.Is_Abnormal)) {
            resultMap[DefinedParams.Is_Abnormal] = false
            resultMap[DefinedParams.Result_Status] =
                DefinedParams.Result_Negative
        }
        val displayName = range[DefinedParams.Display_Name]
        if (displayName is String)
            resultMap[DefinedParams.Display_Name] = displayName
    }

    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.tvTestedOn.text.isNullOrBlank())
            yearMonthDate = DateUtils.getYearMonthAndDate(binding.tvTestedOn.text.toString())

        if (datePickerDialog == null) {
            datePickerDialog = showDatePicker(
                context = requireContext(),
                disableFutureDate = true,
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.tvTestedOn.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_FORMAT_ddMMMyyyy
                    )
                datePickerDialog = null
            }
        }
    }
}