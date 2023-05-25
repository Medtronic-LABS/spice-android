package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.appextensions.setError
import com.medtroniclabs.opensource.appextensions.setSuccess
import com.medtroniclabs.opensource.common.CustomSpinnerAdapter
import com.medtroniclabs.opensource.common.MedicalReviewConstant.DefaultIDLabel
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.TreatmentPlanModel
import com.medtroniclabs.opensource.databinding.TreatmentPlanDialogBinding
import com.medtroniclabs.opensource.databinding.TreatmentPlanSpinnerBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import java.lang.reflect.Type

class TreatmentPlanDialog : DialogFragment(), View.OnClickListener {

    private lateinit var binding: TreatmentPlanDialogBinding
    private val viewModel: PatientDetailViewModel by activityViewModels()
    private val rootSuffix = "rootView"
    private val errorSuffix = "errorMessageView"
    private val titleSuffix = "titleTextView"
    private val validationIDs = ArrayList<String>()

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TreatmentPlanDialogBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        attachObservers()
        viewModel.getTreatmentPlanData(requireContext())
    }

    private fun loadSpinnerValues(prefillMap: HashMap<String, Any>?) {
        binding.gridView.removeAllViews()
        viewModel.treatmentPlanData.value?.data?.let { spinnerMap ->
            spinnerMap.forEach { map ->
                val type: Type = object : TypeToken<TreatmentPlanModel>() {}.type
                val treatmentPlanModel =
                    Gson().fromJson<TreatmentPlanModel>(map.value as String, type)
                createSpinner(
                    map.key,
                    treatmentPlanModel,
                    if (prefillMap?.containsKey(map.key) == true) prefillMap[map.key] as String? else null
                )
            }
        }
        viewModel.treatmentPlanData.setError()
        viewModel.treatmentPlanDetailsResponse.setError()
    }

    private fun attachObservers() {

        viewModel.treatmentPlanData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    viewModel.treatmentPlanDetails(requireContext())
                }
            }
        }

        viewModel.treatmentPlanDetailsResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadSpinnerValues(resourceState.data)
                }
            }
        }

        viewModel.updateTreatmentPlanResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        (activity as BaseActivity).showErrorDialogue(getString(R.string.error),message, false, ){}
                        viewModel.updateTreatmentPlanResponse.setError(null)
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    dismiss()
                    resourceState.data?.let { map ->
                        if (map.containsKey(DefinedParams.message)) {
                            val message = map[DefinedParams.message]
                            if (message is String) {
                                GeneralSuccessDialog.newInstance(
                                    requireContext(),
                                    getString(R.string.treatment_plan),
                                    message,
                                    needHomeNav = false
                                ) {
                                    viewModel.refreshMedicalReview.setSuccess()
                                }.show(parentFragmentManager, GeneralSuccessDialog.TAG)
                            }
                        }
                    }
                    viewModel.updateTreatmentPlanResponse.setError()
                }
            }
        }
    }

    private fun createSpinner(
        id: String,
        treatmentPlanModel: TreatmentPlanModel,
        defaultValue: String? = null
    ) {
        val spinnerBinding = TreatmentPlanSpinnerBinding.inflate(layoutInflater)
        spinnerBinding.root.tag = id + rootSuffix
        spinnerBinding.etUserInput.tag = id
        spinnerBinding.tvTitle.tag = id + titleSuffix
        spinnerBinding.tvErrorMessage.tag = id + errorSuffix
        spinnerBinding.tvTitle.text = treatmentPlanModel.labelName ?: ""
        if (treatmentPlanModel.labelName.equals(
                getString(R.string.bp_check_frequency),
                true
            ) || treatmentPlanModel.labelName.equals(
                getString(R.string.medical_review_frequency),
                true
            ) || treatmentPlanModel.labelName.equals(getString(R.string.bg_check_frequency), true)
        ) {
            spinnerBinding.tvTitle.markMandatory()
            treatmentPlanModel.frequencyKey?.let { key ->
                validationIDs.add(key)
            }
        }
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefaultIDLabel,
                DefinedParams.ID to "-1"
            )
        )
        val adapter = CustomSpinnerAdapter(requireContext())
        treatmentPlanModel.options?.let { list ->
            if (list.isNotEmpty()) {
                list.forEach { listItem ->
                    dropDownList.add(
                        hashMapOf<String, Any>(
                            DefinedParams.NAME to listItem,
                            DefinedParams.ID to dropDownList.size.toString()
                        )
                    )
                }
            }
        }
        adapter.setData(dropDownList)
        spinnerBinding.etUserInput.adapter = adapter
        spinnerBinding.etUserInput.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    getTreatmentPlanResult(adapter.getData(position = pos), id)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }

        defaultValue?.let { value ->
            val selectedMapIndex =
                dropDownList.indexOfFirst {
                    it.containsKey(DefinedParams.NAME) && (it[DefinedParams.NAME] as String).equals(
                        value,
                        ignoreCase = true
                    )
                }

            if (selectedMapIndex >= 0) {
                val selectedMap = dropDownList[selectedMapIndex]
                selectedMap.let {
                    if (selectedMap.isNotEmpty()) {
                        spinnerBinding.etUserInput.setSelection(selectedMapIndex, true)
                    }
                }
            }
        }
        binding.gridView.addView(spinnerBinding.root)
    }

    private fun getTreatmentPlanResult(selectedItem: Map<String, Any>?, id: String) {
        selectedItem?.let { item ->
            val selectedId = item[DefinedParams.id]
            if ((selectedId is String && selectedId == "-1")) {
                if (viewModel.treatmentPlanResultMap.containsKey(id)) {
                    viewModel.treatmentPlanResultMap.remove(id)
                }
            } else {
                viewModel.treatmentPlanResultMap[id] =
                    item[DefinedParams.NAME] as String
            }
        }
    }

    private fun setListeners() {
        binding.cancelButton.safeClickListener(this)
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.submitButton.safeClickListener(this)
    }

    override fun onClick(mView: View?) {
        when (mView?.id) {
            binding.labelHeader.ivClose.id -> dismiss()
            binding.cancelButton.id -> dismiss()
            binding.submitButton.id -> {
                saveTreatmentPlanInputs()
            }
        }
    }

    private fun saveTreatmentPlanInputs() {
        if (validateInputs()) {
            SecuredPreference.getSelectedSiteEntity()?.tenantId?.let {
                viewModel.treatmentPlanResultMap[DefinedParams.TenantId] = it
            }
            viewModel.treatmentPlanResultMap[DefinedParams.PatientTrackId] =
                viewModel.patientTrackId
            viewModel.treatmentPlanResultMap[DefinedParams.Patient_Visit_Id] =
                viewModel.patientVisitId ?: -1
            viewModel.treatmentPlanResultMap[DefinedParams.Is_Provisional] = false
            viewModel.treatmentPlanDetailsResponse.value?.data?.let { map ->
                if (map.containsKey(DefinedParams.ID)) {
                    viewModel.treatmentPlanResultMap[DefinedParams.ID] =
                        (map[DefinedParams.ID] as Double).toLong()
                }
            }
            viewModel.updateTreatmentPlan(requireContext())
        } else
            (activity as BaseActivity).showErrorDialogue(
                getString(R.string.alert),
                getString(R.string.please_check_your_inputs),
                false,
                ){}
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        if (viewModel.treatmentPlanResultMap.isEmpty()) {
            isValid = false
        } else {
            if ((!viewModel.treatmentPlanResultMap.containsKey(DefinedParams.BP_Check_Freq)) || (!viewModel.treatmentPlanResultMap.containsKey(
                    DefinedParams.Medical_Review_Freq
                )) || (!viewModel.treatmentPlanResultMap.containsKey(DefinedParams.BG_Check_Freq))
            ) {
                isValid = false
            }
        }
        validationIDs.forEach {
            if (!viewModel.treatmentPlanResultMap.containsKey(it))
                isValid = false
        }
        return isValid
    }

    fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
        binding.submitButton.visibility = View.INVISIBLE
        binding.cancelButton.visibility = View.INVISIBLE
    }

    fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
        binding.submitButton.visibility = View.VISIBLE
        binding.cancelButton.visibility = View.VISIBLE
    }

    companion object {
        const val TAG = "TreatmentPlanDialog"
    }
}