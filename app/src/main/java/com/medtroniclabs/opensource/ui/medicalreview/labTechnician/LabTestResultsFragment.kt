package com.medtroniclabs.opensource.ui.medicalreview.labTechnician

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.data.model.LabTestListResponse
import com.medtroniclabs.opensource.data.model.LabTestModel
import com.medtroniclabs.opensource.databinding.FragmentLabtestResultsBinding
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.medicalreview.dialog.AddLabTestResultDialog
import com.medtroniclabs.opensource.ui.medicalreview.dialog.GeneralSuccessDialog
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.LabTestViewModel

class LabTestResultsFragment : BaseFragment(), LabResultInterface {

    lateinit var binding: FragmentLabtestResultsBinding

    private val viewModel: LabTestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLabtestResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.labTestListResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadLabTestList(resourceState.data)
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        (activity as BaseActivity).showErrorDialogue(getString(R.string.error),message, false){}
                    }
                }
            }
        }

        viewModel.labTestResultResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        (activity as BaseActivity).showErrorDialogue(getString(R.string.error),message, false){}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                        AddLabTestResultDialog.newInstance(viewModel.editModel!!, true) { status ->
                            if(status) {
                                viewModel.getLabTestList(true)
                                GeneralSuccessDialog.newInstance(requireContext(), getString(R.string.lab_test), getString(
                                    R.string.lab_test_saved_successfully), needHomeNav = false)
                                    .show(childFragmentManager, GeneralSuccessDialog.TAG)
                            }
                        }.show(
                            childFragmentManager,
                            AddLabTestResultDialog.TAG)
                    }
                }
            }
        }
    }

    private fun loadLabTestList(data: LabTestListResponse?) {
        binding.tvNoRecord.visibility = View.GONE
        data?.patientLabTest?.let { listData ->
            if (listData.isEmpty()) {
                binding.rvLabTestList.visibility = View.GONE
                binding.tvNoRecord.visibility = View.VISIBLE
            } else {
                binding.rvLabTestList.visibility = View.VISIBLE
                binding.rvLabTestList.adapter = LabTestResultsAdapter(listData, this)
            }
        }
    }

    private fun initView() {
        binding.rvLabTestList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLabTestList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun selectedLabResult(result: LabTestModel) {
        result.labTestId?.let { id ->
            viewModel.editModel = result
            viewModel.getLabTestResults(requireContext(),id, result.labTestName)
        }
    }
}