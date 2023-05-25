package com.medtroniclabs.opensource.ui.medicalreview

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.hideKeyboard
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.CustomSpinnerAdapter
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.common.MedicalReviewConstant
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.MedicationSearchReqModel
import com.medtroniclabs.opensource.data.model.PatientPrescriptionModel
import com.medtroniclabs.opensource.data.model.PrescriptionModel
import com.medtroniclabs.opensource.data.model.UpdateMedicationModel
import com.medtroniclabs.opensource.databinding.ActivityPrescriptionCopyBinding
import com.medtroniclabs.opensource.databinding.LayoutMedicationEditBinding
import com.medtroniclabs.opensource.databinding.LayoutMedicationViewBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.adapter.PrescriptionAdapter
import com.medtroniclabs.opensource.ui.medicalreview.dialog.InstructionExpansionDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.MedicationHistoryDialogFragment
import com.medtroniclabs.opensource.ui.medicalreview.dialog.SignatureDialogFragment
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PrescriptionCopyViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrescriptionCopyActivity : BaseActivity(), View.OnClickListener, SignatureListener,
    MedicationListener {

    private lateinit var binding: ActivityPrescriptionCopyBinding

    private val prescriptionViewModel: PrescriptionCopyViewModel by viewModels()

    private val viewModel: PatientDetailViewModel by viewModels()

    private lateinit var prescriptionAdapter: PrescriptionAdapter
    private var canSearch: Boolean = true

    private var isFromSummaryPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrescriptionCopyBinding.inflate(layoutInflater)
        setMainContentView(binding.root, isToolbarVisible = true, getString(R.string.prescription))
        getIntentValues()
        initializeView()
        attachObserver()
        clickListener()
    }

    private fun getIntentValues() {
        prescriptionViewModel.patient_track_id = intent.getLongExtra(IntentConstants.IntentPatientID, -1)
        prescriptionViewModel.patient_visit_id =
            intent.getLongExtra(IntentConstants.IntentVisitId, -1L)
        prescriptionViewModel.tenant_id = SecuredPreference.getSelectedSiteEntity()?.tenantId
        isFromSummaryPage = intent.getBooleanExtra(IntentConstants.isFromSummaryPage, false)
    }

    private fun clickListener() {
        binding.btnAddMedicine.safeClickListener(this)
        binding.btnPrescribe.safeClickListener(this)
        binding.tvDiscontinuedMedication.safeClickListener(this)
        binding.btnBack.safeClickListener(this)
        binding.btnRenewAll.safeClickListener(this)
    }

    private fun attachObserver() {
        viewModel.medicationSearchResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    resourceState.data?.let {
                        generateSuggestions(it)
                    }
                }
                else -> {
                    //Invoked if response state is not success
                }
            }
        }

        prescriptionViewModel.prescriptionListLiveDate.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                        prescriptionViewModel.prescriptionUIModel = ArrayList()
                        prescriptionViewModel.prescriptionUIModel!!.addAll(it)
                    } ?: kotlin.run {
                        prescriptionViewModel.prescriptionUIModel = null
                    }
                    loadPrescriptionListData()
                }
            }
        }

        prescriptionViewModel.removePrescriptionLiveDate.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        showErrorDialogue(getString(R.string.error),message, false, ){}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    binding.llDiscontinuedMedication.visibility = View.GONE
                    binding.tvDiscontinuedMedication.text =
                        getString(R.string.view_discontinued_medication)
                    prescriptionViewModel.getPrescriptionList(
                        false
                    )
                }
            }
        }
        prescriptionViewModel.updatePrescriptionLiveDate.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        showErrorDialogue(getString(R.string.error),message, false, ){}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    updatePrescription()
                }
            }
        }
        prescriptionViewModel.frequencyList.observe(this) {
            prescriptionViewModel.getDosageUnitList()
        }

        prescriptionViewModel.unitList.observe(this) {
            prescriptionViewModel.getPrescriptionList(
                false
            )
        }

        prescriptionViewModel.disContinuedPrescriptionListLiveDate.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    resourceState.data?.let {
                        disContinuedResponse(it)
                    } ?: kotlin.run {
                        binding.llDiscontinuedMedication.visibility = View.GONE
                    }
                    hideLoading()
                }
            }
        }

        prescriptionViewModel.medicationHistoryLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.patientPrescription?.let {
                        MedicationHistoryDialogFragment.newInstance(it)
                            .show(supportFragmentManager, MedicationHistoryDialogFragment.TAG)
                    }
                }
            }
        }

        prescriptionViewModel.reloadInstruction.observe(this) {
            if (it) {
                loadPrescriptionListData()
            }
        }
    }

    private fun updatePrescription() {
        if (isFromSummaryPage) {
            setResult(Activity.RESULT_OK)
        }
        finish()
    }

    private fun disContinuedResponse(it: java.util.ArrayList<PrescriptionModel>) {
        if (it.size > 0) {
            val discontinuedMedicationAdapter =
                DiscontinuedMedicationAdapter(it, this)
            binding.rvDiscontinuedMedicationList.layoutManager =
                LinearLayoutManager(this)
            binding.rvDiscontinuedMedicationList.adapter =
                discontinuedMedicationAdapter
            showDMRecyclerView()
        } else {
            hideDMRecyclerView()
        }
    }

    private fun showDMRecyclerView() {
        binding.apply {
            llDiscontinuedMedication.visibility = View.VISIBLE
            rvDiscontinuedMedicationList.visibility = View.VISIBLE
            tvDMNoData.visibility = View.GONE
            tvDiscontinuedMedication.text = getString(R.string.hide_discontinued_medication)
        }
    }

    private fun hideDMRecyclerView() {
        binding.apply {
            rvDiscontinuedMedicationList.visibility = View.GONE
            llDiscontinuedMedication.visibility = View.VISIBLE
            tvDMNoData.visibility = View.VISIBLE
            tvDiscontinuedMedication.text = getString(R.string.hide_discontinued_medication)
        }
    }


    private fun loadPrescriptionListData() {
        prescriptionViewModel.prescriptionUIModel?.let {
            if (it.size > 0) {
                showRecyclerView()
                loadPrescriptionData(it)
            } else {
                hideRecyclerView()
            }
        }

        changeButtonVisibility()
    }

    private fun loadPrescriptionData(data: ArrayList<PrescriptionModel>) {
        binding.llMedicationList.removeAllViews()
        data.forEachIndexed { index, model ->
            if (model.prescribedSince.isNullOrBlank() || model.isEdit) {
                if (index == data.size - 1) {
                    loadMedicationEdit(model, true)
                } else {
                    loadMedicationEdit(model, false)
                }
            } else {
                if (index == data.size - 1) {
                    loadMedicationView(model, true)
                } else {
                    loadMedicationView(model, false)
                }
            }
        }
    }

    private fun loadMedicationEdit(model: PrescriptionModel, dividerStatus: Boolean) {
        val medicationEditBinding = LayoutMedicationEditBinding.inflate(layoutInflater)
        medicationEditBinding.tvMedicationName.text =
            if (model.medicationName.isNullOrBlank()) getString(R.string.separator_hyphen) else model.medicationName
        medicationEditBinding.etDosage.setText(model.enteredDosageUnitValue ?: "")
        if (model.dosage_form_name_entered.isNullOrBlank() || (model.medicationId ?: 0) <= 0)
            otherMedicationEdit(medicationEditBinding, model)
        else {
            medicationEditBinding.tvForm.visibility = View.VISIBLE
            medicationEditBinding.spinnerForm.visibility = View.GONE
            medicationEditBinding.tvForm.text = model.dosage_form_name_entered
        }
        model.filledPrescriptionDays?.let {
            medicationEditBinding.etPrescribedDays.setText(it.toString())
        } ?: kotlin.run {
            medicationEditBinding.etPrescribedDays.setText("")
        }

        medicationEditBinding.tvMedicineErrorMessage.tag =
            "${model.datetime}${model.medicationName}"

        medicationEditBinding.divider.visibility = dividerVisibility(dividerStatus)

        medicationEditBinding.etInstruction.text = model.instruction_entered ?: ""
        val dosageAdapter = CustomSpinnerAdapter(this)
        dosageAdapter.setData(getDosageUnit())
        medicationEditBinding.tvUnit.adapter = dosageAdapter
        medicationEditBinding.tvUnit.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val selectedItem = dosageAdapter.getData(position = p2)
                    selectedItem?.let {
                        model.dosage_unit_selected = it[DefinedParams.ID].toString().toLong()
                        model.dosage_unit_name_entered = it[DefinedParams.NAME] as String
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
        model.dosage_unit_name_entered?.let {
            medicationEditBinding.tvUnit.setSelection(getSpinnerPosition(dosageAdapter, it), true)
        } ?: kotlin.run {
            medicationEditBinding.tvUnit.setSelection(0, true)
        }
        val adapter = CustomSpinnerAdapter(this)
        adapter.setData(getFrequencyList())
        medicationEditBinding.spinnerFrequency.adapter = adapter
        medicationEditBinding.spinnerFrequency.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val selectedItem = adapter.getData(position = p2)
                    selectedItem?.let {
                        editSpinnerFrequency(selectedItem, medicationEditBinding, model)
                        model.dosage_frequency_name_entered = it[DefinedParams.NAME] as String
                        model.dosage_frequency_entered = it[DefinedParams.ID].toString().toLong()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }

        model.dosage_frequency_name_entered?.let {
            medicationEditBinding.spinnerFrequency.setSelection(
                getSpinnerPosition(adapter, it),
                true
            )
        } ?: kotlin.run {
            medicationEditBinding.spinnerFrequency.setSelection(0, true)
        }

        ivResetRemove(model.isEdit, medicationEditBinding)

        medicationEditBinding.etDosage.addTextChangedListener {
            checkValue(it)?.let { value ->
                model.enteredDosageUnitValue = value.toString()
            } ?: kotlin.run {
                model.enteredDosageUnitValue = null
            }
        }
        medicationEditBinding.etPrescribedDays.addTextChangedListener {
            checkValue(it)?.let { value ->
                model.filledPrescriptionDays = value.toString().toIntOrNull()
            } ?: kotlin.run {
                model.filledPrescriptionDays = null
            }
        }
        medicationEditBinding.etInstruction.addTextChangedListener {
            it?.let {
                model.instruction_entered = it.toString()
            } ?: kotlin.run {
                model.instruction_entered = null
            }
        }
        medicationEditBinding.ivRemoveMedication.safeClickListener {
            prescriptionViewModel.prescriptionUIModel?.remove(model)
            loadPrescriptionListData()
        }
        medicationEditBinding.ivResetMedication.safeClickListener {
            model.isEdit = false
            model.isEdited = false
            model.filledPrescriptionDays = model.prescribedDays
            model.enteredDosageUnitValue = model.dosageUnitValue
            model.dosage_form_name_entered = model.dosageFormName
            model.dosage_frequency_name_entered = model.dosageFrequencyName
            model.dosage_frequency_entered = model.dosageFrequencyId
            model.dosage_unit_selected = model.dosageUnitId
            model.dosage_unit_name_entered = model.dosageUnitName
            model.instruction_entered = model.instructionNote
            loadPrescriptionListData()
        }
        medicationEditBinding.etInstruction.safeClickListener {
            InstructionExpansionDialog.newInstance(model)
                .show(supportFragmentManager, InstructionExpansionDialog.TAG)
        }
        binding.llMedicationList.addView(medicationEditBinding.root)
    }

    private fun checkValue(it: Editable?): Editable? {
        return if (it.isNullOrBlank())
            null
        else
            it
    }

    private fun ivResetRemove(edit: Boolean, medicationEditBinding: LayoutMedicationEditBinding) {
        if (edit) {
            medicationEditBinding.ivResetMedication.visibility = View.VISIBLE
            medicationEditBinding.ivRemoveMedication.visibility = View.GONE
        } else {
            medicationEditBinding.ivResetMedication.visibility = View.GONE
            medicationEditBinding.ivRemoveMedication.visibility = View.VISIBLE
        }
    }

    private fun getSpinnerPosition(dosageAdapter: CustomSpinnerAdapter, it: String): Int {
        return if (it != getString(R.string.please_select) && dosageAdapter.getIndexOfItemByName(it) != -1)
            dosageAdapter.getIndexOfItemByName(it)
        else
            0
    }

    private fun editSpinnerFrequency(
        selectedItem: Map<String, Any>,
        medicationEditBinding: LayoutMedicationEditBinding,
        model: PrescriptionModel
    ) {
        selectedItem.let {
            if (!(model.dosage_frequency_name_entered != null && model.dosage_frequency_name_entered!!.isNotEmpty() && model.dosage_frequency_name_entered == (it[DefinedParams.NAME] as String))) {
                if (it.containsKey(DefinedParams.DESCRIPTION)) {
                    medicationEditBinding.etInstruction.text =
                        it[DefinedParams.DESCRIPTION] as String
                }
            } else {
                if (!model.instruction_entered.isNullOrBlank()) {
                    medicationEditBinding.etInstruction.text = model.instruction_entered
                } else {
                    medicationEditBinding.etInstruction.text =
                        it[DefinedParams.DESCRIPTION] as String
                }
            }
        }
    }

    private fun dividerVisibility(dividerStatus: Boolean): Int {
        return if (dividerStatus)
            View.GONE
        else
            View.VISIBLE
    }

    private fun otherMedicationEdit(
        medicationEditBinding: LayoutMedicationEditBinding,
        model: PrescriptionModel
    ) {
        medicationEditBinding.tvForm.visibility = View.GONE
        medicationEditBinding.spinnerForm.visibility = View.VISIBLE
        val spinnerFormAdapter = CustomSpinnerAdapter(this)
        spinnerFormAdapter.setData(getFormName())
        medicationEditBinding.spinnerForm.adapter = spinnerFormAdapter
        medicationEditBinding.spinnerForm.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val selectedItem = spinnerFormAdapter.getData(position = p2)
                    selectedItem?.let {
                        model.dosage_form_name_entered = it[DefinedParams.ID] as String
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
        model.dosage_form_name_entered?.let {
            if (it != getString(R.string.please_select) && spinnerFormAdapter.getIndexOfItemByName(
                    it
                ) != -1
            )
                medicationEditBinding.spinnerForm.setSelection(
                    spinnerFormAdapter.getIndexOfItemByName(
                        it
                    ), true
                )
        } ?: kotlin.run {
            medicationEditBinding.spinnerForm.setSelection(0, true)
        }
    }

    private fun getFormName(): ArrayList<Map<String, Any>> {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to MedicalReviewConstant.DefaultIDLabel,
                DefinedParams.ID to MedicalReviewConstant.DefaultID,
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.Tablet,
                DefinedParams.ID to DefinedParams.Tablet,
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.Liquid_Oral,
                DefinedParams.ID to DefinedParams.Liquid_Oral,
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.Injection_Injectable_Solution,
                DefinedParams.ID to DefinedParams.Injection_Injectable_Solution,
            )
        )
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.Capsule,
                DefinedParams.ID to DefinedParams.Capsule,
            )
        )

        return dropDownList
    }

    private fun getDosageUnit(): ArrayList<Map<String, Any>> {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to getString(R.string.please_select),
                DefinedParams.ID to "-1",
                DefinedParams.DESCRIPTION to ""
            )
        )
        prescriptionViewModel.unitList.value?.forEach {
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to it.unit,
                    DefinedParams.ID to it._id,
                )
            )
        }

        return dropDownList
    }

    private fun getFrequencyList(): ArrayList<Map<String, Any>> {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to getString(R.string.please_select),
                DefinedParams.ID to "-1",
                DefinedParams.DESCRIPTION to ""
            )
        )
        prescriptionViewModel.frequencyList.value?.forEach {
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to it.name,
                    DefinedParams.ID to it._id,
                    DefinedParams.DESCRIPTION to (it.description ?: "")
                )
            )
        }
        return dropDownList
    }

    private fun loadMedicationView(model: PrescriptionModel, dividerStatus: Boolean) {
        val medicationBinding = LayoutMedicationViewBinding.inflate(layoutInflater)
        medicationBinding.tvMedicationName.text =
            if (model.medicationName.isNullOrBlank()) getString(R.string.separator_hyphen) else model.medicationName
        medicationBinding.tvDosage.text =
            if (model.dosageUnitValue.isNullOrBlank()) getString(R.string.separator_hyphen) else model.dosageUnitValue
        medicationBinding.tvUnit.text =
            if (model.dosageUnitName.isNullOrBlank()) getString(R.string.separator_hyphen) else model.dosageUnitName
        medicationBinding.tvForm.text =
            if (model.dosageFormName.isNullOrBlank()) getString(R.string.separator_hyphen) else model.dosageFormName
        medicationBinding.tvFrequency.text =
            if (model.dosageFrequencyName.isNullOrBlank()) getString(R.string.separator_hyphen) else model.dosageFrequencyName

        var prescribedDays = ""
        model.prescribedDays?.let {
            prescribedDays += it.toString()
        }
        val remainingDays = model.prescriptionRemainingDays ?: 0
        val daysLeft = if (remainingDays > 1) " Days Left" else " Day Left"
        val completed = "Completed"
        val resultedString: String
        if (remainingDays > 0) {
            resultedString = "$prescribedDays - $remainingDays$daysLeft"
            medicationBinding.tvPrescribedDays.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.black
                )
            )
            medicationBinding.ivPrescriptionCompleted.visibility = View.GONE
        } else {
            resultedString = "$prescribedDays - $completed"
            medicationBinding.tvPrescribedDays.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.medium_high_risk_color
                )
            )
            medicationBinding.ivPrescriptionCompleted.visibility = View.VISIBLE
        }
        if (dividerStatus) {
            medicationBinding.divider.visibility = View.GONE
        } else {
            medicationBinding.divider.visibility = View.VISIBLE
        }
        medicationBinding.tvPrescribedDays.text = resultedString

        medicationBinding.tvInformation.text =
            model.instructionNote ?: ""

        var strPrescribedSince = getString(R.string.separator_hyphen)
        model.prescribedSince?.let { prescribedSince ->
            strPrescribedSince = DateUtils.convertDateTimeToDate(
                prescribedSince,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                DateUtils.DATE_FORMAT_ddMMMyyyy
            )
            medicationBinding.tvPrescribedSince.safeClickListener {
                model.id?.let { prescriptionID ->
                    prescriptionViewModel.getMedicationHistory(prescriptionID)
                }
            }
        } ?: kotlin.run {
            medicationBinding.tvPrescribedSince.text = getString(R.string.separator_hyphen)
        }
        val spannableString = SpannableString(strPrescribedSince)
        spannableString.setSpan(UnderlineSpan(), 0, strPrescribedSince.length, 0)
        medicationBinding.tvPrescribedSince.text = spannableString

        medicationBinding.ivEditMedication.safeClickListener {
            model.isEdit = true
            model.isEdited = true
            model.filledPrescriptionDays = null
            model.enteredDosageUnitValue = null
            model.dosage_form_name_entered = model.dosageFormName
            model.dosage_unit_name_entered = null
            model.instruction_entered = null
            model.dosage_unit_selected = null
            model.dosage_frequency_entered = null
            model.dosage_frequency_name_entered = null
            loadPrescriptionListData()
        }

        medicationBinding.ivDeleteMedication.safeClickListener {
            showAlertDialogWithComments(
                getString(R.string.confirmation), message = getString(R.string.delete_confirmation),
                getString(R.string.ok),
                true) { isPositiveResult, discontinuedReason ->
                if (isPositiveResult) {
                    model.id?.let {
                        prescriptionViewModel.removePrescription(this,it, discontinuedReason)
                    }
                }
            }
        }

        binding.llMedicationList.addView(medicationBinding.root)
    }

    private fun changeButtonVisibility() {
        if(prescriptionViewModel.prescriptionUIModel != null && prescriptionViewModel.prescriptionUIModel!!.size > 0) {
            val data = prescriptionViewModel.prescriptionUIModel?.firstOrNull { (it.id ?: 0) > 0 && !it.isEdit}
            binding.btnRenewAll.visibility = if(data == null) View.GONE else View.VISIBLE
        }
        else
            binding.btnRenewAll.visibility = View.GONE

    }

    private fun hideRecyclerView() {
        binding.apply {
            btnPrescribe.isEnabled = false
            llMedicationList.visibility = View.GONE
            tvNoData.visibility = View.VISIBLE
        }
    }

    private fun showRecyclerView() {
        binding.apply {
            btnPrescribe.isEnabled = true
            llMedicationList.visibility = View.VISIBLE
            tvNoData.visibility = View.GONE
        }
    }

    private fun generateSuggestions(searchResultList: ArrayList<PrescriptionModel>) {
        val searchResults = ArrayList<Pair<String, String>>()
        if (searchResultList.isNotEmpty()) {
            searchResultList.forEach {
                val search = "${it.medicationName}, ${it.brandName}, ${it.dosageFormName}"
                if (isValidSuggestion(search)) searchResults.add(
                    Pair(
                        search,
                        it.classificationName ?: ""
                    )
                )
            }
        }
        prescriptionAdapter.setData(searchResults)
        binding.etPrescriptionSearch.setAdapter(prescriptionAdapter)
        if (searchResults.size > 0 && canSearch)
            binding.etPrescriptionSearch.showDropDown()
    }

    private fun isValidSuggestion(search: String): Boolean {
        val searchStr = search.replace(", ", "", true)
        return searchStr.isNotEmpty()
    }

    private fun initializeView() {
        prescriptionAdapter = PrescriptionAdapter(this)
        binding.etPrescriptionSearch.apply {
            setOnItemClickListener { _, _, position, _ ->
                canSearch = false
                hideKeyboard(this)
                viewModel.selectedMedication = null
                viewModel.medicationSearchResponse.value?.data?.let {
                    if (it.size > 0) {
                        viewModel.selectedMedication = it[position]

                        val search = getSearchString()
                        binding.etPrescriptionSearch.setText(search)
                        binding.etPrescriptionSearch.setSelection(search.length)
                    }
                }
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    /**
                     * this method is not used
                     */
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    /**
                     * this method is not used
                     */
                }

                override fun afterTextChanged(p0: Editable?) {
                    binding.btnAddMedicine.isEnabled =
                        !binding.etPrescriptionSearch.text.isNullOrBlank()
                    if (canSearch) {
                        text?.toString()?.let {
                            if (it.isNotEmpty() && it.length > 1) {
                                SecuredPreference.getSelectedSiteEntity()?.let { site ->
                                    viewModel.searchMedication(this@PrescriptionCopyActivity, MedicationSearchReqModel(it,site.isQualipharmEnabledSite))
                                }
                            }
                        }
                    } else
                        canSearch = true
                }

            })
        }
        prescriptionViewModel.getFrequencyList()
    }

    private fun getSearchString(): String {
        var str = ""
        viewModel.selectedMedication?.let {
            val medicationName = it.medicationName ?: ""
            val brandName = it.brandName ?: ""
            val dosageName = it.dosageFormName ?: ""
            str = "$medicationName, $brandName, $dosageName"
        }
        return str
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnAddMedicine -> {
                binding.btnAddMedicine.isEnabled = false
                val prescription = binding.etPrescriptionSearch.text
                binding.etPrescriptionSearch.setText("")
                viewModel.selectedMedication?.let {
                    if (prescriptionViewModel.prescriptionUIModel == null) {
                        prescriptionViewModel.prescriptionUIModel = ArrayList()
                        val model = PrescriptionModel(
                            isDeleted = false,
                            medicationId = it.id,
                            dosageFrequencyId = it.dosageFrequencyId,
                            prescribedDays = it.prescribedDays,
                            medicationName = it.medicationName,
                            dosageUnitValue = it.dosageUnitValue,
                            dosageUnitName = it.dosageUnitName,
                            dosageFrequencyName = it.dosageFrequencyName,
                            instructionNote = it.instructionNote,
                            dosageFormName = it.dosageFormName,
                            dosageForm = it.dosageForm,
                            classificationName = it.classificationName,
                            brandName = it.brandName,
                            )
                        model.dosage_form_name_entered = it.dosageFormName
                        prescriptionViewModel.prescriptionUIModel!!.add(
                            model
                        )
                        viewModel.selectedMedication = null
                    } else {
                        val model = PrescriptionModel(
                            isDeleted = false,
                            medicationId = it.id,
                            dosageFrequencyId = it.dosageFrequencyId,
                            prescribedDays = it.prescribedDays,
                            medicationName = it.medicationName,
                            dosageUnitValue = it.dosageUnitValue,
                            dosageUnitName = it.dosageUnitName,
                            dosageFrequencyName = it.dosageFrequencyName,
                            instructionNote = it.instructionNote,
                            dosageFormName = it.dosageFormName,
                            dosageForm = it.dosageForm,
                            classificationName = it.classificationName,
                            brandName = it.brandName
                        )
                        model.dosage_form_name_entered = it.dosageFormName
                        prescriptionViewModel.prescriptionUIModel!!.add(
                            model
                        )
                        viewModel.selectedMedication = null
                    }
                    loadPrescriptionListData()
                } ?: kotlin.run {
                    if (!prescription.isNullOrBlank()) {
                        if (prescriptionViewModel.prescriptionUIModel == null) {
                            prescriptionViewModel.prescriptionUIModel = ArrayList()
                            prescriptionViewModel.prescriptionUIModel!!.add(
                                PrescriptionModel(
                                    isDeleted = false,
                                    medicationId =  -1L,
                                    dosageFrequencyId = null,
                                    prescribedDays = null,
                                    medicationName = prescription.toString(),
                                    dosageUnitValue = null,
                                    dosageUnitName = null,
                                    dosageFrequencyName = null,
                                    instructionNote = null,
                                    dosageFormName = null,
                                    dosageForm = null
                                )
                            )
                        } else {
                            prescriptionViewModel.prescriptionUIModel!!.add(
                                PrescriptionModel(
                                    isDeleted = false,
                                    medicationId =  -1L,
                                    dosageFrequencyId = null,
                                    prescribedDays = null,
                                    medicationName = prescription.toString(),
                                    dosageUnitValue = null,
                                    dosageUnitName = null,
                                    dosageFrequencyName = null,
                                    instructionNote = null,
                                    dosageFormName = null,
                                    dosageForm = null
                                )
                            )
                        }
                        loadPrescriptionListData()
                    }
                }
            }
            R.id.btnPrescribe -> {
                createOrUpdatePrescription()
            }
            R.id.tvDiscontinuedMedication -> {
                disContinuedOnClick()
            }
            R.id.btnBack -> {
                finish()
            }
            R.id.btnRenewAll -> {
                changeAllToEditMode()
            }
        }
    }

    private fun disContinuedOnClick() {
        if (binding.llDiscontinuedMedication.visibility == View.GONE) {
            prescriptionViewModel.getDiscountinuedPrescriptionList(true)
        } else {
            binding.tvDiscontinuedMedication.text =
                getString(R.string.view_discontinued_medication)
            binding.llDiscontinuedMedication.visibility = View.GONE
        }
    }

    private fun changeAllToEditMode() {
        val list = prescriptionViewModel.prescriptionUIModel?.filter { !it.isEdit }
        if (list != null && list.isNotEmpty()) {
            list.forEach {
                it.isEdit = true
                it.isEdited = true
                it.filledPrescriptionDays = null
                it.enteredDosageUnitValue = null
                it.dosage_form_name_entered = it.dosageFormName
                it.dosage_frequency_entered = null
                it.dosage_frequency_name_entered = null
                it.dosage_unit_name_entered = null
                it.instruction_entered = null
                it.dosage_unit_selected = null
            }
        }
        loadPrescriptionListData()
    }

    private fun createOrUpdatePrescription() {
        val list =
            prescriptionViewModel.prescriptionUIModel?.filter { it.isEdited || it.prescribedSince.isNullOrBlank() }
        list?.let { _ ->
            if (list.isNotEmpty()) {
                val errorList = ArrayList<String>()
                prescriptionViewModel.savePrescriptionList = ArrayList()
                list.forEachIndexed { _, it ->
                    var isValid: Boolean
                    val invalidList = ArrayList<String>()

                    prescriptionValidation(it).let {
                        isValid = it.first
                        invalidList.addAll(it.second)
                    }

                    errorList.addAll(invalidList)
                    if (isValid) {
                        invalidList.clear()
                        showHideMedicineErrorMessage(
                            it.datetime,
                            it.medicationName,
                            ArrayList(),
                            View.GONE
                        )
                        val finalModel = UpdateMedicationModel(
                            id = it.id,
                            medicationId = it.medicationId,
                            medicationName = it.medicationName,
                            dosageUnitName = it.dosage_unit_name_entered,
                            dosageUnitValue = it.enteredDosageUnitValue,
                            dosageFormName = it.dosage_form_name_entered,
                            dosageFrequencyName = it.dosage_frequency_name_entered,
                            dosageFrequencyId = it.dosage_frequency_entered,
                            prescribedDays = it.filledPrescriptionDays,
                            instructionNote = it.instruction_entered,
                            isDeleted = false,
                            dosageUnitId = it.dosage_unit_selected,
                            classificationName = it.classificationName,
                            brandName = it.brandName,
                        )
                        prescriptionViewModel.savePrescriptionList?.add(finalModel)
                    } else {
                        invalidPrescription(it, invalidList)
                        return@forEachIndexed
                    }
                }
                if (errorList.size <= 0) {
                    SignatureDialogFragment.newInstance(this)
                        .show(supportFragmentManager, SignatureDialogFragment.TAG)
                }
            } else
                showErrorDialogue(
                    getString(R.string.alert),
                    getString(R.string.no_new_medicines_prescribed),
                    false,
                    ){}
        } ?: kotlin.run {
            showErrorDialogue(
                getString(R.string.alert),
                getString(R.string.no_new_medicines_prescribed),
                false,
                ){}
        }
    }

    private fun invalidPrescription(
        it: PrescriptionModel,
        invalidList: java.util.ArrayList<String>
    ) {
        if (prescriptionViewModel.prescriptionUIModel != null && prescriptionViewModel.prescriptionUIModel!!.isNotEmpty()) {
            showHideMedicineErrorMessage(
                it.datetime,
                it.medicationName,
                invalidList,
                View.VISIBLE
            )
        }
        prescriptionViewModel.savePrescriptionList?.clear()
    }

    private fun prescriptionValidation(prescriptionModel: PrescriptionModel): Pair<Boolean, ArrayList<String>> {
        prescriptionModel.let { prescription ->
            var isValid = true
            val invalidList = ArrayList<String>()
            if (prescription.enteredDosageUnitValue.isNullOrBlank()) {
                isValid = false
                invalidList.add(getString(R.string.dosage))
            }
            prescription.dosage_unit_name_entered?.let {
                if (validateSpinnerValue(it)) {
                    isValid = false
                    invalidList.add(getString(R.string.unit))
                }
            } ?: kotlin.run {
                isValid = false
                invalidList.add(getString(R.string.unit))
            }
            prescription.dosage_form_name_entered?.let {
                if (it.isEmpty() || it == MedicalReviewConstant.DefaultID) {
                    isValid = false
                    invalidList.add(getString(R.string.form))
                }
            } ?: kotlin.run {
                isValid = false
                invalidList.add(getString(R.string.form))
            }
            prescription.dosage_frequency_name_entered?.let {
                if (validateSpinnerValue(it)) {
                    isValid = false
                    invalidList.add(getString(R.string.frequency))
                }
            } ?: kotlin.run {
                isValid = false
                invalidList.add(getString(R.string.frequency))
            }
            if (prescription.filledPrescriptionDays?.toString().isNullOrBlank()) {
                isValid = false
                invalidList.add(getString(R.string.prescribed_days))
            }
            return Pair(isValid, invalidList)
        }
    }

    private fun validateSpinnerValue(it: String): Boolean {
        return it.isEmpty() || it == getString(R.string.please_select)
    }

    private fun showHideMedicineErrorMessage(
        index: Long?,
        medicationName: String?,
        invalidList: ArrayList<String>,
        visiblity: Int
    ) {
        index?.let {
            getViewByTag("${index}${medicationName}")?.let { view ->
                if (view is TextView) {
                    view.visibility = visiblity
                    if (invalidList.isNotEmpty()) {
                        view.text = "${getString(R.string.please_enter_details_prefix)} ${
                            invalidList.joinToString(separator = ", ")
                        }"
                    }
                }
            }
        }
    }

    override fun applySignature(signature: Bitmap) {
        updatePrescriptions(signature)
    }

    private fun updatePrescriptions(signatureBitmap: Bitmap) {
        signatureBitmap.let { signature ->
            val request = PatientPrescriptionModel(
                patientTrackId = prescriptionViewModel.patient_track_id,
                patientVisitId = prescriptionViewModel.patient_visit_id,
                tenantId = prescriptionViewModel.tenant_id,
                prescriptionList = prescriptionViewModel.savePrescriptionList
            )
            prescriptionViewModel.createOrUpdatePrescription(
                this,
                signature,
                CommonUtils.getFilePath(prescriptionViewModel.patient_visit_id!!.toString(), context = this),
                request
            )
        }
    }

    override fun openMedicalHistory(prescriptionId: Long?) {
        prescriptionViewModel.getMedicationHistory(prescriptionId)
    }

    override fun updateView(isEmpty: Boolean) {
        /**
         * this method is not used
         */
    }

    override fun deleteMedication(pos: Int, prescriptionId: Long?) {
        /**
         * this method is not used
         */
    }

    private fun getViewByTag(tag: Any): View? {
        return binding.root.findViewWithTag(tag)
    }
}