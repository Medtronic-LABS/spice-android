package com.medtroniclabs.opensource.ui.medicalreview.pharamcist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.data.model.FillPrescriptionListResponse
import com.medtroniclabs.opensource.databinding.FragmentPrescriptionRefillBinding
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.medicalreview.pharamcist.viewmodel.PrescriptionRefillViewModel

class PrescriptionRefillFragment : BaseFragment() {

    lateinit var binding: FragmentPrescriptionRefillBinding

    private val viewModel: PrescriptionRefillViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrescriptionRefillBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.patientRefillMedicationList.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadPrescriptionRefillList(resourceState.data)
                }
                ResourceState.ERROR -> {
                    hideLoading()

                }
            }
        }
    }

    private fun loadPrescriptionRefillList(data: ArrayList<FillPrescriptionListResponse>?) {
        binding.tvNoRecord.visibility = View.GONE
        data?.let { list ->
            if (list.isEmpty()) {
                binding.rvPrescriptionRefillList.visibility = View.GONE
                binding.tvNoRecord.visibility = View.VISIBLE
            } else {
                binding.rvPrescriptionRefillList.visibility = View.VISIBLE
                binding.rvPrescriptionRefillList.adapter = PrescriptionRefillAdapter(list)
            }
        }
    }

    private fun initView() {
        binding.rvPrescriptionRefillList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPrescriptionRefillList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
    }

}