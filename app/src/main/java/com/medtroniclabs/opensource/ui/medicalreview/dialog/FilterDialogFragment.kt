package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.CompoundButton
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.FilterEnum
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.FilterModel
import com.medtroniclabs.opensource.databinding.FilterDialogBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.RoleConstant
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.enrollment.viewmodel.PatientListViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FilterDialogFragment : DialogFragment(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {

    private lateinit var binding: FilterDialogBinding
    private val patientListViewModel: PatientListViewModel by activityViewModels()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    companion object {
        const val TAG = "FilterDialogFragment"
        fun newInstance(origin: String): FilterDialogFragment {
            val args = Bundle()
            args.putString(DefinedParams.Origin, origin)
            val fragment = FilterDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FilterDialogBinding.inflate(inflater, container, false)
        isCancelable = false
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        prefillValues()
        setListeners()
    }

    private fun prefillValues() {
        patientListViewModel.filter?.let { filter ->
            //Medical Review Date
            filter.medicalReviewDate?.lowercase()?.let { medicalReviewDate ->
                when (medicalReviewDate) {
                    FilterEnum.TODAY.title.lowercase() -> binding.todayChip.isChecked = true
                    FilterEnum.TOMORROW.title.lowercase() -> binding.tomorrowChip.isChecked = true
                }
            }

            //Red Risk
            if (filter.isRedRiskPatient == true)
                binding.redRiskChip.isChecked = true

            //Enrollment
            filter.patientStatus?.lowercase()?.let { patientStatus ->
                when (patientStatus) {
                    FilterEnum.ENROLLED.toString().lowercase() -> binding.enrolledChip.isChecked =
                        true
                    FilterEnum.NOT_ENROLLED.toString()
                        .lowercase() -> binding.notEnrolledChip.isChecked =
                        true
                }
            }

            //CVD Risk
            when (filter.cvdRiskLevel?.lowercase()) {
                FilterEnum.HIGH.title.lowercase() -> binding.highCVDChip.isChecked = true
                FilterEnum.MEDIUM.title.lowercase() -> binding.mediumCVDChip.isChecked = true
                FilterEnum.LOW.title.lowercase() -> binding.lowCVDChip.isChecked = true
            }

            //Assessment Date
            filter.assessmentDate?.lowercase().let { assessmentDate ->
                when (assessmentDate) {
                    FilterEnum.TODAY.title.lowercase() -> binding.assessmentTodayChip.isChecked =
                        true
                    FilterEnum.TOMORROW.title.lowercase() -> binding.assessmentTomorrowChip.isChecked =
                        true
                }
            }

            filter.labTestReferredDate?.lowercase()?.let { date ->
                when (date) {
                    FilterEnum.TODAY.title.lowercase() -> binding.todayLabTestChip.isChecked = true
                    FilterEnum.YESTERDAY.title.lowercase() -> binding.yesterDayLabTestChip.isChecked =
                        true
                }
            }

            filter.medicationPrescribedDate?.lowercase()?.let { date ->
                when (date) {
                    FilterEnum.TODAY.title.lowercase() -> binding.todayLabTestChip.isChecked = true
                    FilterEnum.YESTERDAY.title.lowercase() -> binding.yesterDayLabTestChip.isChecked =
                        true
                }
            }
        }
        enableResetOption()
    }

    private fun setListeners() {
        //Medical Review Date
        binding.todayChip.setOnCheckedChangeListener(this)
        binding.tomorrowChip.setOnCheckedChangeListener(this)

        //Red Risk
        binding.redRiskChip.setOnCheckedChangeListener(this)

        //Enrollment
        binding.enrolledChip.setOnCheckedChangeListener(this)
        binding.notEnrolledChip.setOnCheckedChangeListener(this)

        //CVD Risk
        binding.highCVDChip.setOnCheckedChangeListener(this)
        binding.mediumCVDChip.setOnCheckedChangeListener(this)
        binding.lowCVDChip.setOnCheckedChangeListener(this)

        //Assessment Date
        binding.assessmentTodayChip.setOnCheckedChangeListener(this)
        binding.assessmentTomorrowChip.setOnCheckedChangeListener(this)

        binding.btnReset.safeClickListener(this)
        binding.btnApply.safeClickListener(this)
        binding.titleCard.ivClose.safeClickListener(this)
        binding.todayLabTestChip.setOnCheckedChangeListener(this)
        binding.yesterDayLabTestChip.setOnCheckedChangeListener(this)
    }

    override fun onClick(mView: View?) {
        when (mView?.id) {
            binding.btnApply.id -> applyFilter()
            binding.btnReset.id -> doReset()
            binding.titleCard.ivClose.id -> if (patientListViewModel.isFilterReset) {
                patientListViewModel.isFilterReset = false
                applyFilter()
            } else dismiss()
        }
    }

    private fun applyFilter() {
        if (connectivityManager.isNetworkAvailable()) {//Medical Review Date
            val medicalReviewDate: String? = when {
                binding.todayChip.isChecked -> FilterEnum.TODAY.title
                binding.tomorrowChip.isChecked -> FilterEnum.TOMORROW.title
                else -> null
            }

            //Red Risk
            val isRedRiskPatient = if (binding.redRiskChip.isChecked) true else null

            //Enrollment
            val patientStatus: String? = when {
                binding.enrolledChip.isChecked -> FilterEnum.ENROLLED.title
                binding.notEnrolledChip.isChecked -> FilterEnum.NOT_ENROLLED.title
                else -> null
            }

            //CVD Risk
            val cvdRiskLevel: String? = when {
                binding.highCVDChip.isChecked -> FilterEnum.HIGH.title
                binding.mediumCVDChip.isChecked -> FilterEnum.MEDIUM.title
                binding.lowCVDChip.isChecked -> FilterEnum.LOW.title
                else -> null
            }

            //Assessment Date
            val assessmentDate: String? = when {
                binding.assessmentTodayChip.isChecked -> FilterEnum.TODAY.title
                binding.assessmentTomorrowChip.isChecked -> FilterEnum.TOMORROW.title
                else -> null
            }

            val medicationPrescribedDate : String? = when {
                binding.todayLabTestChip.isChecked -> FilterEnum.TODAY.title
                binding.yesterDayLabTestChip.isChecked -> FilterEnum.YESTERDAY.title
                else -> null
            }

            val labTestReferredDate : String? = when {
                binding.todayLabTestChip.isChecked -> FilterEnum.TODAY.title
                binding.yesterDayLabTestChip.isChecked -> FilterEnum.YESTERDAY.title
                else -> null
            }

            val role = SecuredPreference.getSelectedSiteEntity()?.role

            patientListViewModel.filter = FilterModel(
                medicalReviewDate = medicalReviewDate,
                isRedRiskPatient = isRedRiskPatient,
                patientStatus = patientStatus,
                cvdRiskLevel = cvdRiskLevel,
                assessmentDate = assessmentDate,
                medicationPrescribedDate = if (role == RoleConstant.PHARMACIST) medicationPrescribedDate else null,
                labTestReferredDate = if (role == RoleConstant.LAB_TECHNICIAN) labTestReferredDate else null
            )
            patientListViewModel.applySortFilter.postValue(true)
            dismiss()
        } else {
            (activity as BaseActivity).showErrorDialogue(
                getString(R.string.error),
                getString(R.string.no_internet_error), false
            ) {}
        }
    }

    private fun doReset() {
        binding.medicalReviewChipGroup.clearCheck()
        binding.riskChipGroup.clearCheck()
        binding.enrollChipGroup.clearCheck()
        binding.cvdChipGroup.clearCheck()
        binding.assessmentChipGroup.clearCheck()
        binding.LabTestChipGroup.clearCheck()

        patientListViewModel.isFilterReset = true
    }

    private fun initializeViews() {
        val origin = arguments?.getString(DefinedParams.Origin)

        if (origin == UIConstants.myPatientsUniqueID) {
            binding.clfilterPatient.visibility = View.VISIBLE
            binding.clfilterPrescriptionInvestigation.visibility = View.GONE
        } else {
            binding.clfilterPrescriptionInvestigation.visibility = View.VISIBLE
            binding.clfilterPatient.visibility = View.INVISIBLE
        }

        //Medical Review Date
        initializeChipView(binding.todayChip)
        initializeChipView(binding.tomorrowChip)

        //Red Risk
        initializeChipView(binding.redRiskChip)

        //Enrollment
        initializeChipView(binding.enrolledChip)
        initializeChipView(binding.notEnrolledChip)

        //CVD Risk
        initializeChipView(binding.highCVDChip)
        initializeChipView(binding.mediumCVDChip)
        initializeChipView(binding.lowCVDChip)

        //Assessment Date
        initializeChipView(binding.assessmentTodayChip)
        initializeChipView(binding.assessmentTomorrowChip)

        initializeChipView(binding.todayLabTestChip)
        initializeChipView(binding.yesterDayLabTestChip)
    }

    private fun initializeChipView(chip: Chip) {
        requireContext().let { context ->
            chip.chipBackgroundColor =
                getColorStateList(
                    context.getColor(R.color.medium_blue),
                    context.getColor(R.color.white)
                )
            chip.setChipBackgroundColorResource(R.color.diagnosis_confirmation_selector)
            chip.chipStrokeWidth = 3f
            chip.setTextColor(
                getColorStateList(
                    context.getColor(R.color.white),
                    context.getColor(R.color.navy_blue)
                )
            )
            chip.chipStrokeColor = getColorStateList(
                context.getColor(R.color.medium_blue),
                context.getColor(R.color.mild_gray)
            )
        }
    }

    private fun getColorStateList(
        selectedColor: Int,
        unSelectedColor: Int
    ): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_selected),
            intArrayOf(-android.R.attr.state_selected),
        )
        val colors = intArrayOf(
            selectedColor,
            unSelectedColor,
            selectedColor,
            unSelectedColor
        )
        return ColorStateList(states, colors)
    }

    override fun onCheckedChanged(button: CompoundButton?, isChecked: Boolean) {
        when (button?.id) {
            binding.todayChip.id -> changeView(binding.todayChip, isChecked)
            binding.redRiskChip.id -> changeView(binding.redRiskChip, isChecked)
            binding.tomorrowChip.id -> changeView(binding.tomorrowChip, isChecked)
            binding.enrolledChip.id -> changeView(binding.enrolledChip, isChecked)
            binding.notEnrolledChip.id -> changeView(binding.notEnrolledChip, isChecked)
            binding.highCVDChip.id -> changeView(binding.highCVDChip, isChecked)
            binding.mediumCVDChip.id -> changeView(binding.mediumCVDChip, isChecked)
            binding.lowCVDChip.id -> changeView(binding.lowCVDChip, isChecked)
            binding.assessmentTodayChip.id -> changeView(binding.assessmentTodayChip, isChecked)
            binding.assessmentTomorrowChip.id -> changeView(
                binding.assessmentTomorrowChip,
                isChecked
            )
            binding.yesterDayLabTestChip.id -> changeView(binding.yesterDayLabTestChip, isChecked)
            binding.todayLabTestChip.id -> changeView(binding.todayLabTestChip, isChecked)
        }
    }

    private fun changeView(chipView: Chip, isChecked: Boolean) {
        if (isChecked) {
            chipView.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_bold)
            chipView.chipStrokeWidth = 0f
        } else {
            chipView.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
            chipView.chipStrokeWidth = 3f
        }
        enableResetOption()
    }

    private fun enableResetOption() {
        binding.btnReset.isEnabled = binding.medicalReviewChipGroup.checkedChipId > 0 ||
                binding.riskChipGroup.checkedChipId > 0 ||
                binding.enrollChipGroup.checkedChipId > 0 ||
                binding.cvdChipGroup.checkedChipId > 0 ||
                binding.assessmentChipGroup.checkedChipId > 0 ||
                binding.LabTestChipGroup.checkedChipId > 0
    }
}