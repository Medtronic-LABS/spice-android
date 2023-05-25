package com.medtroniclabs.opensource.ui.medicalreview

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.hideKeyboard
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.appextensions.setError
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.LabTestListResponse
import com.medtroniclabs.opensource.data.model.LabTestModel
import com.medtroniclabs.opensource.data.model.LabTestSearchResponse
import com.medtroniclabs.opensource.databinding.ActivityInvestigationBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.adapter.AutoCompleteAdapter
import com.medtroniclabs.opensource.ui.medicalreview.dialog.AddLabTestResultDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.GeneralSuccessDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.LabTestConfirmationDialog
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.LabTestViewModel

class LabTestCreateActivity : BaseActivity(), View.OnClickListener, LabTestInterface {

    private lateinit var binding: ActivityInvestigationBinding
    private lateinit var labTestCreateAdapter: LabTestCreateAdapter
    private val viewModel: LabTestViewModel by viewModels()
    private lateinit var myAdapter: AutoCompleteAdapter
    private var canSearch: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityInvestigationBinding.inflate(layoutInflater)
            setMainContentView(binding.root, isToolbarVisible = true, getString(R.string.investigation))
            onNewIntent(intent)
            initializeView()
            setListeners()
            attachObservers()
            viewModel.getLabTestList()
        } catch (e: Exception) {
            //Exception - Catch block
        }
    }

    private fun setListeners() {
        binding.llSearch.btnAdd.safeClickListener(this)
        binding.bottomSheet.btnNext.safeClickListener(this)
        binding.bottomSheet.btnBack.safeClickListener(this)

        binding.llSearch.etSearch.setOnItemClickListener { _, _, index, _ ->
            canSearch = false
            viewModel.selectedLabTest = null
            viewModel.searchResponse.value?.data?.let {
                if (it.size > 0)
                    viewModel.selectedLabTest = it[index]
            }
            binding.llSearch.btnAdd.isEnabled = true
        }

        binding.llSearch.etSearch.threshold = 2

        binding.llSearch.etSearch.addTextChangedListener(textWatcher)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getLongExtra(IntentConstants.IntentPatientID, -1)?.let {
            viewModel.patientTrackId = it
        }
        intent?.getLongExtra(IntentConstants.IntentVisitId, -1L)?.let {
            viewModel.patientVisitId = it
        }
    }

    private fun loadSearchDropDown(searchResult: ArrayList<LabTestSearchResponse>) {
        myAdapter.setData(searchResult)
        binding.llSearch.etSearch.setAdapter(myAdapter)
        if (searchResult.size > 0 && canSearch)
            binding.llSearch.etSearch.showDropDown()
    }


    private fun attachObservers() {
        viewModel.labTestListResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        showErrorDialogue(getString(R.string.error),message, false){}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                        loadLabTestList(it)
                    }
                }
            }
        }

        viewModel.referLabTestResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        showErrorDialogue(getString(R.string.error),it, false){}
                        viewModel.isToRefer.value = true
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                        finish()
                    }
                }
            }
        }

        viewModel.resultDetailsResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { data ->
                        loadResultDetails(data)
                    }
                }
            }
        }

        viewModel.labTestResultResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        showErrorDialogue(getString(R.string.error),message, false){}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                        newLabTestSuccess()
                    }
                }
            }
        }

        viewModel.searchResponse.observe(this) { resourceState ->
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
                        loadSearchDropDown(it)
                    }
                }
            }
        }

        viewModel.reviewResultResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    labTestCreateAdapter.updateReviewCommentsView()
                }
            }
        }

        viewModel.removeLabTestResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { result ->
                        removeLabTestSuccess(result)
                    }
                }
            }
        }

        viewModel.isToRefer.observe(this) { isToRefer ->
            binding.bottomSheet.btnNext.isEnabled = (isToRefer != null && isToRefer)
        }
    }

    private fun removeLabTestSuccess(result: HashMap<String, Any>) {
        val model = result[DefinedParams.Other] as LabTestModel
        val index = viewModel.labTestLists.indexOfFirst { model._id == it._id }
        if (index >= 0)
            viewModel.labTestLists.removeAt(index)
        enableOrDisableSubmit()
        viewModel.removeLabTestResponse.setError()
    }

    private fun newLabTestSuccess() {
        AddLabTestResultDialog.newInstance(viewModel.editModel!!) { status ->
            if(status) {
                viewModel.getLabTestList()
                GeneralSuccessDialog.newInstance(this, getString(R.string.lab_test), getString(
                    R.string.lab_test_saved_successfully), needHomeNav = false)
                    .show(supportFragmentManager, GeneralSuccessDialog.TAG)
            }
        }.show(
            supportFragmentManager,
            AddLabTestResultDialog.TAG)
    }

    private fun loadResultDetails(data: java.util.HashMap<String, Any>) {
        val comment =
            if (data.containsKey(DefinedParams.Comment)) data[DefinedParams.Comment] as String? else ""
        if (data.containsKey(DefinedParams.Patient_LabTest_Results)) {
            val resultsData =
                data[DefinedParams.Patient_LabTest_Results] as List<Map<String, Any>>
            labTestCreateAdapter.showResultDetails(resultsData, comment)
        }
    }

    private fun loadLabTestList(it: LabTestListResponse) {
        it.patientLabTest?.let { list ->
            viewModel.labTestLists = ArrayList(list)
            if (viewModel.labTestLists.size > 0) {
                showRecyclerView()
                labTestCreateAdapter = LabTestCreateAdapter(this)
                labTestCreateAdapter.submitData(viewModel.labTestLists)
                binding.rvTestList.adapter = labTestCreateAdapter
            }
        }
    }

    private fun enableOrDisableSubmit() {
        val labTest = viewModel.labTestLists.find { it._id == null }
        binding.bottomSheet.btnNext.isEnabled = labTest != null
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            /**
             * this method is not used
             */
        }

        override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {

            binding.llSearch.btnAdd.isEnabled = !text.isNullOrBlank()
            if (canSearch) {
                text?.toString()?.let {
                    if (it.isNotBlank() && it.length > 1) {
                        viewModel.searchLabTest(it)
                    }
                }
            } else
                canSearch = true
        }

        override fun afterTextChanged(p0: Editable?) {
            /**
             * this method is not used
             */
        }
    }

    private fun initializeView() {
        labTestCreateAdapter = LabTestCreateAdapter(this)
        myAdapter = AutoCompleteAdapter(this)
        binding.rvTestList.layoutManager = LinearLayoutManager(binding.rvTestList.context ?: this)
        binding.rvTestList.adapter = labTestCreateAdapter
        hideRecyclerView()
        binding.bottomSheet.btnNext.isEnabled = true
        binding.bottomSheet.btnNext.text = getString(R.string.fp_submit)
    }

    private fun fetchResults(model: LabTestModel) {
        viewModel.getResultDetails(model._id ?: -1L)
    }

    override fun onClick(mView: View?) {

        when (mView) {
            binding.llSearch.btnAdd -> {
                hideKeyboard(binding.llSearch.btnAdd)
                addPatientLabTest()
            }
            binding.bottomSheet.btnNext -> {
                referLabTest()
            }

            binding.bottomSheet.btnBack -> {
                finish()
            }
        }
    }

    private fun referLabTest() {
        val adapterList = ArrayList(labTestCreateAdapter.getData())
        val labTests = ArrayList<LabTestModel>()
        adapterList.forEach {
            if (it.referredDate == null) {
                if ((it.labTestId ?: 0) <= 0)
                    it.labTestId = -1
                labTests.add(it)
            }
        }
        if (labTests.size > 0) {
            viewModel.isToRefer.value = false
            viewModel.referLabTest(this,labTests)
        } else
            showErrorDialogue(getString(R.string.alert),getString(R.string.no_lab_test_to_refer),false){}
    }

    private fun addPatientLabTest() {
        binding.llSearch.btnAdd.isEnabled = false

        binding.llSearch.etSearch.text?.toString()?.let {
            if (it.isNotBlank()) {
                showRecyclerView()
                binding.llSearch.etSearch.setText("")
                val model = LabTestModel()
                model.isReviewed = false
                SecuredPreference.getUserDetails().let { user ->
                    model.referredBy = user._id
                    model.referredByDisplay = (user.firstName ?: "") + (user.lastName ?: "")
                }

                model.referDateDisplay =
                    DateUtils.getCurrentDateTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ)

                if (viewModel.selectedLabTest != null) {
                    model.labTestName = viewModel.selectedLabTest?.name
                    model.labTestId = viewModel.selectedLabTest?._id
                } else {
                    model.labTestName = it
                }
                viewModel.isToRefer.value = true
                viewModel.labTestLists.add(model)
                labTestCreateAdapter.submitData(viewModel.labTestLists)
                viewModel.selectedLabTest = null
            }
        }
    }

    private fun showRecyclerView() {
        binding.rvTestList.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE
    }

    private fun hideRecyclerView() {
        binding.rvTestList.visibility = View.GONE
        binding.tvNoData.visibility = View.VISIBLE
    }

    private fun onEditClicked(model: LabTestModel?) {
        if (model?.labTestId != null) {
            viewModel.editModel = model
            viewModel.getLabTestResults(this,model.labTestId!!, model.labTestName)
        }
    }

    override fun onItemSelected(model: LabTestModel, isRemove: Boolean, loadResult: Boolean) {
        if (loadResult)
            fetchResults(model)
        else if (isRemove)
            removeLabTest(model)
        else if (!isRemove)
            onEditClicked(model)
    }

    private fun removeLabTest(model: LabTestModel) {

        val adapterList = labTestCreateAdapter.getData()

        if (adapterList.isNullOrEmpty())
            hideRecyclerView()

        if (model._id != null) {
            val request = HashMap<String, Any>()
            request[DefinedParams.ID] = model._id
            SecuredPreference.getSelectedSiteEntity()?.tenantId?.let {
                request[DefinedParams.TenantId] = it
            }
            viewModel.removeLabTest(this,request, model)
        } else {
            val index =
                viewModel.labTestLists.indexOfFirst { it.labTestName == model.labTestName }
            if (index >= 0)
                viewModel.labTestLists.removeAt(index)
            enableOrDisableSubmit()
        }
    }

    override fun setButtonEnabled(isEnabled: Boolean) {
        viewModel.isToRefer.value = isEnabled
    }

    override fun reviewResults(model: LabTestModel) {
        val dialog = supportFragmentManager.findFragmentByTag(LabTestConfirmationDialog.TAG)

        if (dialog?.isVisible != true)
            LabTestConfirmationDialog.newInstance(model).show(
                supportFragmentManager,
                LabTestConfirmationDialog.TAG
            )
    }
}

interface LabTestInterface {
    fun onItemSelected(model: LabTestModel, isRemove: Boolean, loadResult: Boolean)
    fun setButtonEnabled(isEnabled: Boolean)
    fun reviewResults(model: LabTestModel)
}