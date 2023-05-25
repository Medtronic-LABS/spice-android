package com.medtroniclabs.opensource.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.PatientTransfer
import com.medtroniclabs.opensource.data.model.PatientTransferNotificationCountRequest
import com.medtroniclabs.opensource.databinding.PatientDetailDialogueBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.landing.LandingViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class PatientDetailDialogue : DialogFragment() {

    private lateinit var binding: PatientDetailDialogueBinding

    private val viewModel: LandingViewModel by viewModels()

    companion object{
        val TAG = "PatientDetailDialogue"
        fun newInstance(patientID: Long): PatientDetailDialogue {
            val bundle = Bundle()
            bundle.putLong(DefinedParams.ID,patientID)
             val fragment =  PatientDetailDialogue()
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PatientDetailDialogueBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setGravity(Gravity.CENTER)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        viewModel.transferPatientViewId = arguments?.getLong(DefinedParams.ID, -1)
        initializeView()
    }

    private fun initializeView() {

        binding.tvGenderAge.text = "${getString(R.string.gender)}/${getString(R.string.dob_age)}"

        binding.ivClose.safeClickListener {
            dismiss()
        }

        SecuredPreference.getSelectedSiteEntity()?.let {
            viewModel.getPatientListTransfer(PatientTransferNotificationCountRequest(it.id))
        }
         viewModel.patientListResponse.observe(viewLifecycleOwner) { resourceState ->

            when (resourceState.state) {
                ResourceState.LOADING -> {
                 binding.CenterProgress.visibility = View.VISIBLE
                 binding.cardHolder.visibility = View.GONE
                }
                ResourceState.SUCCESS -> {
                    resourceState.data?.let { data ->
                        loadPatientList(data.incomingPatientList)
                    }
                    binding.CenterProgress.visibility = View.GONE
                    binding.cardHolder.visibility = View.VISIBLE
                }
                ResourceState.ERROR -> {
                    binding.CenterProgress.visibility = View.GONE
                    binding.cardHolder.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadPatientList(data: ArrayList<PatientTransfer>) {
        val model = data.filter { it.id == viewModel.transferPatientViewId }
        if (model.isNotEmpty()) {
            val patientModel = model[0]
            patientModel.patient.firstName.let { firstName ->
                var patientName = "$firstName "
                patientModel.patient.lastName.let {
                    patientName += it
                }
                binding.tvDialogTitle.text = patientName
            }

            patientModel.patient.gender.let {gender ->
                patientModel.patient.age.let {age->
                    binding.tvGenderAgeValue.text = "${gender}/${age}"
                }
            }
            patientModel.patient.phoneNumber.let {
                binding.tvMobileNumberValue.text = it
            }
            patientModel.patient.nationalId.let {
                binding.tvNationalIdValue.text = it
            }
            patientModel.patient.programId.toString().let {
                binding.tvProgramIdValue.text = it
            }
            patientModel.patient.enrollmentAt.let {
                binding.tvEnrollDateValue.text =  DateUtils.convertDateTimeToDate(
                    it,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DateUtils.DATE_ddMMyyyy
                )
            }
            patientModel.oldSite?.apply {
                binding.tvCurrentFacilityValue.text = name?:"-"
            }

            patientModel.patient.pregnancyDetails?.apply {
                var menstrualDate: Date? = null
                lastMenstrualPeriodDate?.let {
                    menstrualDate = SimpleDateFormat(
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        Locale.ENGLISH
                    ).parse(it)
                }
                var deliveryDate: Date? = null
                estimatedDeliveryDate?.let {
                    deliveryDate = SimpleDateFormat(
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        Locale.ENGLISH
                    ).parse(it)
                }
                /*CommonUtils.calculateGestationalAge(menstrualDate, deliveryDate)?.let {
                    binding.tvGestationalWeekValue.text = it
                }?:kotlin.run {
                    binding.tvGestationalWeekValue.text = getString(R.string.separator_hyphen)
                }*/
            }

            if(!patientModel.patient.confirmDiagnosis.isNullOrEmpty()){
                binding.tvDiagnosisValue.text = getListAsText(patientModel.patient.confirmDiagnosis)
            }else if(!patientModel.patient.provisionalDiagnosis.isNullOrEmpty()){
                binding.tvDiagnosisValue.text = "${getListAsText(patientModel.patient.provisionalDiagnosis)} ${getString(R.string.provisional_text)}"
            }else{
                binding.tvDiagnosisValue.text = getString(R.string.separator_hyphen)
            }

            if(!patientModel.patient.cvdRiskScore.isNullOrEmpty() && !patientModel.patient.cvdRiskLevel.isNullOrEmpty())
                binding.tvCVDRiskValue.text = "${CommonUtils.getDecimalFormatted(patientModel.patient.cvdRiskScore)}% - ${patientModel.patient.cvdRiskLevel}"
            else
                binding.tvCVDRiskValue.text = getString(R.string.separator_hyphen)
        }
    }

    private fun getListAsText(list: ArrayList<String>?): String {
        val resultStringBuilder = StringBuilder()
        list?.let { list ->
            if (list.isNotEmpty()) {
                list.forEachIndexed { index, generalModel ->
                    resultStringBuilder.append(generalModel)
                    if (index != list.size - 1) {
                        resultStringBuilder.append(getString(R.string.comma_symbol))
                    }
                }
            } else {
                resultStringBuilder.append(getString(R.string.separator_hyphen))
            }
        }
        return resultStringBuilder.toString()
    }

}