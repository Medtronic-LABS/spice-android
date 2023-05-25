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
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.DateUtils.DATE_FORMAT_ddMMMyyyy
import com.medtroniclabs.opensource.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.data.model.PatientHistoryRequest
import com.medtroniclabs.opensource.data.model.PatientLabTestHistoryResponse
import com.medtroniclabs.opensource.data.model.VisitDateModel
import com.medtroniclabs.opensource.databinding.FragmentInvestigationBinding
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.medicalreview.adapter.BulletPointsAdapter
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.MedicalReviewPatientHistoryViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel

class InvestigationFragment : BaseFragment(), View.OnClickListener, DateSelectionListener {

    private lateinit var binding: FragmentInvestigationBinding
    private val patientViewModel: PatientDetailViewModel by activityViewModels()
    val viewModel: MedicalReviewPatientHistoryViewModel by activityViewModels()
    var listPopupWindow: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInvestigationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapterViews()
        attachObserver()
        binding.llActions.ivReload.safeClickListener(this)
        binding.llActions.ivNext.safeClickListener(this)
        binding.llActions.ivPrevious.safeClickListener(this)
        binding.ivRefresh.safeClickListener(this)
    }

    private fun setAdapterViews() {
        val layoutManager = LinearLayoutManager(context)
        binding.rvInvestigation.layoutManager = layoutManager
        binding.rvInvestigation.adapter = BulletPointsAdapter(ArrayList())
    }

    private fun attachObserver() {
        patientViewModel.patientDetailsResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.CenterProgress.visibility = View.VISIBLE
                }
                ResourceState.SUCCESS -> {
                    getMedicalReview(resourceState.data)
                }
                ResourceState.ERROR -> {
                    binding.CenterProgress.visibility = View.GONE
                }
            }
        }
        viewModel.patientLabTestHistoryResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.ivRefresh.visibility = View.GONE
                    binding.CenterProgress.visibility = View.VISIBLE
                    binding.cardInvestigationHistory.visibility = View.GONE
                }
                ResourceState.SUCCESS -> {
                    binding.CenterProgress.visibility = View.GONE
                    binding.cardInvestigationHistory.visibility = View.VISIBLE
                    binding.ivRefresh.visibility = View.GONE
                    loadInvestigationData(resourceState.data, true)
                }
                ResourceState.ERROR -> {
                    binding.CenterProgress.visibility = View.GONE
                    binding.cardInvestigationHistory.visibility = View.GONE
                    binding.ivRefresh.visibility = View.VISIBLE
                }
            }
        }
        viewModel.patientLabTestHistoryResponseByID.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.ivRefresh.visibility = View.GONE
                    binding.CenterProgress.visibility = View.VISIBLE
                    binding.cardInvestigationHistory.visibility = View.GONE
                }
                ResourceState.SUCCESS -> {
                    binding.ivRefresh.visibility = View.GONE
                    binding.CenterProgress.visibility = View.GONE
                    binding.cardInvestigationHistory.visibility = View.VISIBLE
                    loadInvestigationData(resourceState.data, false)
                }
                ResourceState.ERROR -> {
                    binding.CenterProgress.visibility = View.GONE
                    binding.cardInvestigationHistory.visibility = View.GONE
                    binding.ivRefresh.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadInvestigationData(data: PatientLabTestHistoryResponse?, updateDate: Boolean) {
        data?.apply {
            if (patientLabTest.size > 0) {
                binding.tvEmptyInformation.visibility = View.GONE
                binding.rvInvestigation.adapter = BulletPointsAdapter(patientLabTest)
                binding.tvDate.text = DateUtils.convertDateTimeToDate(
                    patientLabTest[0].referredDate,
                    DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DATE_FORMAT_ddMMMyyyy
                )
                viewModel.SelectedPatientId = patientLabTest[0].patientVisitId
                binding.llActions.ivPrevious.isEnabled = checkForPreviousItem() != -1
                binding.llActions.ivNext.isEnabled = checkNextItem() != -1
                if (updateDate) {
                    loadDatesMenu(patientLabtestDates)
                } else {
                    val view =
                        listPopupWindow?.contentView?.findViewById<RecyclerView>(R.id.rvDateList)
                    if (view != null && view.adapter is DateListAdapter) {
                        viewModel.patientLabTestHistoryResponse.value?.data?.let {
                            view.adapter = DateListAdapter(
                                it.patientLabtestDates,
                                viewModel.SelectedPatientId,
                                this@InvestigationFragment
                            )
                        }
                    }
                }
            } else {
                binding.cardInvestigationHistory.visibility = View.GONE
                binding.tvEmptyInformation.visibility = View.VISIBLE
            }
        } ?: kotlin.run {
            binding.cardInvestigationHistory.visibility = View.GONE
            binding.tvEmptyInformation.visibility = View.VISIBLE
        }
    }

    private fun loadDatesMenu(patientLabtestDates: ArrayList<VisitDateModel>) {
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
        val adapter = DateListAdapter(patientLabtestDates, viewModel.SelectedPatientId, this)
        recyclerView.adapter = adapter
        listPopupWindow = PopupWindow(
            view,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun getMedicalReview(data: PatientDetailsModel?) {
        data?.let { patient ->
            SecuredPreference.getSelectedSiteEntity()?.let {
                val request = PatientHistoryRequest(true, patient._id, it.tenantId)
                viewModel.getPatientLabTestHistory(request)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivReload -> {
                listPopupWindow?.isOutsideTouchable = true
                listPopupWindow?.isFocusable = true
                listPopupWindow?.showAsDropDown(binding.llActions.ivReload)
            }
            R.id.ivPrevious -> {
                getPreviousItemToCurrent()
            }
            R.id.ivNext -> {
                getNextItemToCurrent()
            }
            R.id.ivRefresh -> {
                patientViewModel.patientDetailsResponse.value?.data?.let { patient ->
                    SecuredPreference.getSelectedSiteEntity()?.let {
                        if (viewModel.SelectedPatientId == null) {
                            val request = PatientHistoryRequest(true, patient._id, it.tenantId)
                            viewModel.getPatientLabTestHistory(request)
                        } else {
                            val request = PatientHistoryRequest(
                                false,
                                patient._id,
                                it.tenantId,
                                patientVisitId = viewModel.SelectedPatientId
                            )
                            viewModel.getPatientLabTestHistoryById(request)
                        }
                    }
                }
            }
        }
    }

    private fun getNextItemToCurrent() {
        val selectedIndex = checkNextItem()
        viewModel.patientLabTestHistoryResponse.value?.data?.apply {
            if (selectedIndex != -1) {
                SecuredPreference.getSelectedSiteEntity()?.let { site ->
                    patientViewModel.patientDetailsResponse.value?.data?.let { patientInfo ->
                        val request = PatientHistoryRequest(
                            false,
                            patientInfo._id,
                            site.tenantId,
                            patientVisitId = patientLabtestDates[selectedIndex]._id
                        )
                        viewModel.getPatientLabTestHistoryById(request)
                    }
                }
            }
        }
    }

    private fun getPreviousItemToCurrent() {
        val selectedIndex = checkForPreviousItem()
        viewModel.patientLabTestHistoryResponse.value?.data?.apply {
            if (selectedIndex != -1) {
                SecuredPreference.getSelectedSiteEntity()?.let { site ->
                    patientViewModel.patientDetailsResponse.value?.data?.let { patientInfo ->
                        val request = PatientHistoryRequest(
                            false,
                            patientInfo._id,
                            site.tenantId,
                            patientVisitId = patientLabtestDates[selectedIndex]._id
                        )
                        viewModel.getPatientLabTestHistoryById(request)
                    }
                }
            }
        }
    }

    private fun checkForPreviousItem(): Int {
        var selectedIndex = -1
        viewModel.patientLabTestHistoryResponse.value?.data?.apply {
            patientLabtestDates?.forEachIndexed { index, labTestDateModel ->
                if (labTestDateModel._id == viewModel.SelectedPatientId) {
                    selectedIndex = index - 1
                }
            }
        }
        return selectedIndex
    }

    private fun checkNextItem(): Int {
        var selectedIndex = -1
        viewModel.patientLabTestHistoryResponse.value?.data?.apply {
            patientLabtestDates.forEachIndexed { index, labTestDateModel ->
                if (labTestDateModel._id == viewModel.SelectedPatientId && index + 1 < patientLabtestDates.size) {
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
                viewModel.getPatientLabTestHistoryById(request)
            }
        }
        listPopupWindow?.dismiss()
    }
}