package com.medtroniclabs.opensource.ui.medicalreview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.data.model.PatientHistoryRequest
import com.medtroniclabs.opensource.data.model.PatientPrescriptionHistoryResponse
import com.medtroniclabs.opensource.data.model.VisitDateModel
import com.medtroniclabs.opensource.databinding.FragmentPrescriptionBinding
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.MedicalReviewPatientHistoryViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel

class PrescriptionFragment : BaseFragment(), DateSelectionListener, View.OnClickListener {

    private lateinit var binding: FragmentPrescriptionBinding
    private val patientViewModel: PatientDetailViewModel by activityViewModels()
    val viewModel: MedicalReviewPatientHistoryViewModel by activityViewModels()
    private var listPopupWindow: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapterViews()
        attachObserver()
        binding.llPresActions.ivReload.safeClickListener(this)
        binding.llPresActions.ivNext.safeClickListener(this)
        binding.llPresActions.ivPrevious.safeClickListener(this)
        binding.ivRefresh.safeClickListener(this)
    }

    private fun setAdapterViews() {
        val layoutManager = FlexboxLayoutManager(context)
        binding.rvPrescription.layoutManager = layoutManager
    }

    private fun attachObserver() {
        patientViewModel.patientDetailsResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.CenterProgress.visibility = View.VISIBLE
                }
                ResourceState.SUCCESS -> {
                    getPrescriptionHistory(resourceState.data)
                }
                ResourceState.ERROR -> {
                    binding.CenterProgress.visibility = View.GONE
                }
            }
        }

        viewModel.patientPrescriptionHistoryResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.ivRefresh.visibility = View.GONE
                    binding.CenterProgress.visibility = View.VISIBLE
                    binding.cardPrescriptionHistory.visibility = View.GONE
                }
                ResourceState.SUCCESS -> {
                    binding.ivRefresh.visibility = View.GONE
                    binding.CenterProgress.visibility = View.GONE
                    binding.cardPrescriptionHistory.visibility = View.VISIBLE
                    loadPatientPrescription(resourceState.data, true)
                }
                ResourceState.ERROR -> {
                    binding.ivRefresh.visibility = View.VISIBLE
                    binding.CenterProgress.visibility = View.GONE
                    binding.cardPrescriptionHistory.visibility = View.GONE
                }
            }
        }
        viewModel.patientPrescriptionHistoryResponseBYID.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.ivRefresh.visibility = View.GONE
                    binding.CenterProgress.visibility = View.VISIBLE
                    binding.cardPrescriptionHistory.visibility = View.GONE
                }
                ResourceState.SUCCESS -> {
                    binding.ivRefresh.visibility = View.GONE
                    binding.CenterProgress.visibility = View.GONE
                    binding.cardPrescriptionHistory.visibility = View.VISIBLE
                    loadPatientPrescription(resourceState.data, false)
                }
                ResourceState.ERROR -> {
                    binding.ivRefresh.visibility = View.VISIBLE
                    binding.CenterProgress.visibility = View.GONE
                    binding.cardPrescriptionHistory.visibility = View.GONE
                }
            }
        }
    }

    private fun loadPatientPrescription(
        data: PatientPrescriptionHistoryResponse?,
        updateDate: Boolean
    ) {
        data?.let { prescription ->
            if (prescription.patientPrescription.isNotEmpty()) {
                binding.tvEmptyInformation.visibility = View.GONE
                binding.cardPrescriptionHistory.visibility = View.VISIBLE
                binding.rvPrescription.adapter =
                    BulletPointPrescriptionAdapter(prescription.patientPrescription)
                binding.tvDate.text = DateUtils.convertDateTimeToDate(
                    prescription.patientPrescription[0].createdAt,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DateUtils.DATE_FORMAT_ddMMMyyyy
                )
                viewModel.SelectedPatientPrescription =
                    prescription.patientPrescription[0].patientVisitId
                binding.llPresActions.ivPrevious.isEnabled = checkForPreviousItem() != -1
                binding.llPresActions.ivNext.isEnabled = checkNextItem() != -1
                if (updateDate) {
                    loadDatesMenu(prescription.prescriptionHistoryDates)
                } else {
                    val view =
                        listPopupWindow?.contentView?.findViewById<RecyclerView>(R.id.rvDateList)
                    if (view != null && view.adapter is DateListAdapter) {
                        viewModel.patientPrescriptionHistoryResponse.value?.data?.let {
                            view.adapter = DateListAdapter(
                                it.prescriptionHistoryDates,
                                viewModel.SelectedPatientPrescription,
                                this@PrescriptionFragment
                            )
                        }
                    }
                }
            } else {
                binding.tvEmptyInformation.visibility = View.VISIBLE
                binding.cardPrescriptionHistory.visibility = View.GONE
            }
        } ?: kotlin.run {
            binding.tvEmptyInformation.visibility = View.VISIBLE
            binding.cardPrescriptionHistory.visibility = View.GONE
        }
    }

    private fun loadDatesMenu(prescriptionHistoryDates: ArrayList<VisitDateModel>) {
        val inflater =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_popup_window, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvDateList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )
        val adapter =
            DateListAdapter(prescriptionHistoryDates, viewModel.SelectedPatientPrescription, this)
        recyclerView.adapter = adapter
        listPopupWindow = PopupWindow(
            view,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun getPrescriptionHistory(data: PatientDetailsModel?) {
        data?.let { patient ->
            SecuredPreference.getSelectedSiteEntity()?.let {
                val request = PatientHistoryRequest(true, patient._id, it.tenantId)
                viewModel.getPatientPrescriptionHistory(request)
            }
        }
    }

    private fun checkForPreviousItem(): Int {
        var selectedIndex = -1
        viewModel.patientPrescriptionHistoryResponse.value?.data?.apply {
            prescriptionHistoryDates.forEachIndexed { index, labTestDateModel ->
                if (labTestDateModel._id == viewModel.SelectedPatientPrescription) {
                    selectedIndex = index - 1
                }
            }
        }
        return selectedIndex
    }

    private fun checkNextItem(): Int {
        var selectedIndex = -1
        viewModel.patientPrescriptionHistoryResponse.value?.data?.apply {
            prescriptionHistoryDates.forEachIndexed { index, labTestDateModel ->
                if (labTestDateModel._id == viewModel.SelectedPatientPrescription && index + 1 < prescriptionHistoryDates.size) {
                    selectedIndex = index + 1
                }
            }
        }
        return selectedIndex
    }

    override fun onDateSelected(_id: Long) {
        SecuredPreference.getSelectedSiteEntity()?.let { site ->
            patientViewModel.patientDetailsResponse.value?.data?.let { patientInfo ->
                val request = PatientHistoryRequest(
                    false,
                    patientInfo._id,
                    site.tenantId,
                    patientVisitId = _id
                )
                viewModel.getPatientPrescriptionHistoryById(request)
            }
        }
        listPopupWindow?.dismiss()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.llPresActions.ivReload.id -> {
                listPopupWindow?.isOutsideTouchable = true
                listPopupWindow?.isFocusable = true
                listPopupWindow?.showAsDropDown(binding.llPresActions.ivReload)
            }
            binding.llPresActions.ivPrevious.id -> {
                getPreviousItemToCurrent()
            }
            binding.llPresActions.ivNext.id -> {
                getNextItemToCurrent()
            }
            binding.ivRefresh.id -> {
                patientViewModel.patientDetailsResponse.value?.data?.let { patient ->
                    SecuredPreference.getSelectedSiteEntity()?.let {
                        if (viewModel.SelectedPatientPrescription == null) {
                            val request = PatientHistoryRequest(true, patient._id, it.tenantId)
                            viewModel.getPatientPrescriptionHistory(request)
                        } else {
                            val request = PatientHistoryRequest(
                                false,
                                patient._id,
                                it.tenantId,
                                patientVisitId = viewModel.SelectedPatientPrescription
                            )
                            viewModel.getPatientPrescriptionHistoryById(request)
                        }
                    }
                }
            }
        }
    }

    private fun getPreviousItemToCurrent() {
        val selectedIndex = checkForPreviousItem()
        viewModel.patientPrescriptionHistoryResponse.value?.data?.apply {
            if (selectedIndex != -1) {
                SecuredPreference.getSelectedSiteEntity()?.let { site ->
                    patientViewModel.patientDetailsResponse.value?.data?.let { patientInfo ->
                        val request = PatientHistoryRequest(
                            false,
                            patientInfo._id,
                            site.tenantId,
                            patientVisitId = prescriptionHistoryDates[selectedIndex]._id
                        )
                        viewModel.getPatientPrescriptionHistoryById(request)
                    }
                }
            }
        }
    }

    private fun getNextItemToCurrent() {
        val selectedIndex = checkNextItem()
        viewModel.patientPrescriptionHistoryResponse.value?.data?.apply {
            if (selectedIndex != -1) {
                SecuredPreference.getSelectedSiteEntity()?.let { site ->
                    patientViewModel.patientDetailsResponse.value?.data?.let { patientInfo ->
                        val request = PatientHistoryRequest(
                            false,
                            patientInfo._id,
                            site.tenantId,
                            patientVisitId = prescriptionHistoryDates[selectedIndex]._id
                        )
                        viewModel.getPatientPrescriptionHistoryById(request)
                    }
                }
            }
        }
    }

}