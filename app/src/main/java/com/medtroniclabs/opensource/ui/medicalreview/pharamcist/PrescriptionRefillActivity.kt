package com.medtroniclabs.opensource.ui.medicalreview.pharamcist

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.FillPrescriptionRequest
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.data.model.PatientPrescriptionModel
import com.medtroniclabs.opensource.databinding.ActivityPrescriptionRefillBinding
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.dialog.GeneralSuccessDialog
import com.medtroniclabs.opensource.ui.medicalreview.pharamcist.dialog.PrescriptionHistoryDialogFragment
import com.medtroniclabs.opensource.ui.medicalreview.pharamcist.viewmodel.PrescriptionRefillViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrescriptionRefillActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding: ActivityPrescriptionRefillBinding

    private val viewModel: PrescriptionRefillViewModel by viewModels()

    private val patientViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrescriptionRefillBinding.inflate(layoutInflater)
        setMainContentView(binding.root, true, homeAndBackVisibility = Pair(true,null))
        initView()
        attachObserver()
        viewModel.patient_track_id?.let { patientTrackerId ->
            SecuredPreference.getSelectedSiteEntity()?.let {
                viewModel.tenant_id = it.tenantId
                viewModel.getPatientRefillMedicationList(
                    FillPrescriptionRequest(
                        patientTrackerId,
                        it.tenantId
                    )
                )
            }
        }
    }

    private fun attachObserver() {
        viewModel.fillPrescriptionUpdateRequest.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        showErrorDialogue(getString(R.string.error),it){}
                    }
                }
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { fillPrescriptionResponse ->
                        fillPrescriptionResponse.data?.let { list ->
                            viewModel.shortageReasonMap = list
                            viewModel.shortageReasonMap?.let {
                                QualifyPharmStockAuditResponseDialogue.newInstance()
                                    .show(
                                        supportFragmentManager,
                                        QualifyPharmStockAuditResponseDialogue.TAG
                                    )
                            }
                        }
                    } ?: kotlin.run {
                        GeneralSuccessDialog.newInstance(
                            this,
                            getString(R.string.prescription),
                            getString(R.string.prescription_dispensed_successfully)
                        )
                            .show(supportFragmentManager, GeneralSuccessDialog.TAG)
                    }
                }
            }
        }

        patientViewModel.patientDetailsResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> showLoading()
                ResourceState.ERROR -> hideLoading()
                ResourceState.SUCCESS -> {
                    loadPatientInfo(resourceState.data)
                }
            }
        }

        viewModel.patientRefillMedicationList.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                        if (it.size > 0) {
                            binding.btnDone.visibility = View.VISIBLE
                        } else {
                            binding.btnDone.visibility = View.GONE
                        }
                    } ?: kotlin.run {
                        binding.btnDone.visibility = View.GONE
                    }
                }
                ResourceState.ERROR -> {
                    hideLoading()

                }
            }
        }

        viewModel.patientRefillHistoryList.observe(this){resourceState ->
            when(resourceState.state){
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        showErrorDialogue(getString(R.string.error),message){}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let {
                        PrescriptionHistoryDialogFragment.newInstance(it)
                            .show(supportFragmentManager, PrescriptionHistoryDialogFragment.TAG)
                    }
                }
            }
        }
    }

    private fun loadPatientInfo(data: PatientDetailsModel?) {
        data?.let {
            binding.tvProgramId.text = it.programId?.toString() ?: "-"
            binding.tvNationalId.text = it.nationalId ?: "-"
            data.firstName?.let {
                val text = StringConverter.appendTexts(firstText = it, data.lastName)
                setTitle(
                    StringConverter.appendTexts(
                        firstText = text,
                        data.age?.toInt().toString(),
                        data.gender,
                        separator = "-"
                    )
                )
            }
            data.prescriberDetails?.let { prescriberDetails ->
                val name = StringBuffer("")
                prescriberDetails.firstName?.let { firstName ->
                    name.append(firstName)
                }
                prescriberDetails.lastName?.let { lastName ->
                    name.append(" $lastName")
                }
                binding.tvPrescriberName.text =
                    name.ifBlank { getString(R.string.separator_hyphen) }
                prescriberDetails.phoneNumber?.let { prescriberNumber ->
                    binding.tvPrescriberNumber.text = prescriberNumber
                }
                prescriberDetails.lastRefilDate?.let { lastRefillDate ->
                    DateUtils.convertDateTimeToDate(lastRefillDate, DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ, DateUtils.DATE_FORMAT_ddMMMyyyy).let { formattedDate ->
                        val spannableString = SpannableString(formattedDate)
                        spannableString.setSpan(UnderlineSpan(), 0, formattedDate.length, 0)
                        binding.tvLastRefillDate.text = spannableString
                        binding.tvLastRefillDate.setTextAppearance(R.style.MR_Field_Style)
                        binding.tvLastRefillDate.setTextColor(this.getColor(R.color.cobalt_blue))
                        binding.tvLastRefillDate.safeClickListener {
                            viewModel.getPrescriptionRefillHistory(
                                this,
                                PatientPrescriptionModel(
                                    patientTrackId = viewModel.patient_track_id,
                                    tenantId = viewModel.tenant_id,
                                    lastRefillVisitId = viewModel.last_refill_visit_id
                                )
                            )
                        }
                    }
                }
                prescriberDetails.lastRefillVisitId?.let { lastRefillID ->
                    viewModel.last_refill_visit_id = lastRefillID
                }
            }
        }
    }

    private fun initView() {
        viewModel.patient_track_id = intent.getLongExtra(IntentConstants.IntentPatientID, -1L)
        viewModel.patient_visit_id = intent.getLongExtra(IntentConstants.IntentVisitId, -1L)
        patientViewModel.patientId = viewModel.patient_track_id
        replaceFragmentInId<PrescriptionRefillFragment>(binding.prescriptionRefillFragment.id)
        binding.btnDone.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        patientViewModel.getPatientDetails(this,false, true)
    }


    private inline fun <reified fragment : Fragment> replaceFragmentInId(
        id: Int? = null,
        bundle: Bundle? = null,
        tag: String? = null
    ) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<fragment>(id ?: binding.prescriptionRefillFragment.id, args = bundle, tag = tag)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnDone -> {
                validateCountDifference()
            }
            R.id.btnCancel -> {
                finish()
            }
        }
    }

    private fun validateCountDifference() {
        viewModel.patientRefillMedicationList.value?.data?.let { list ->
            val differedQuantityList =
                list.filter { it.remainingPrescriptionDays != it.prescriptionFilledDays }
            if (differedQuantityList.isNotEmpty()) {
                QuantityDifferenceDialogue.newInstance()
                    .show(supportFragmentManager, QuantityDifferenceDialogue.TAG)
            } else {
                val filledValues =
                    list.filter { it.prescriptionFilledDays != null && it.prescriptionFilledDays != 0 }
                if (filledValues.isNotEmpty()) {
                    viewModel.patient_visit_id?.let { patientVisitId ->
                        viewModel.patient_track_id?.let { patientId ->
                            SecuredPreference.getSelectedSiteEntity()?.let {
                                viewModel.fillPrescriptionUpdate(
                                    this,
                                    patientId,
                                    it.tenantId,
                                    patientVisitId,
                                    viewModel.getFillPrescriptionList()
                                )
                            }
                        }
                    }
                } else {
                    showErrorDialogue(getString(R.string.error),getString(R.string.days_are_required),){}
                }
            }
        }
    }

}