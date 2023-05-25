package com.medtroniclabs.opensource.ui

import android.os.Bundle
import android.view.*
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.data.model.SortModel
import com.medtroniclabs.opensource.databinding.FragmentSortingDialogBinding
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.enrollment.viewmodel.PatientListViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SortingDialogFragment : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentSortingDialogBinding
    private val patientListViewModel: PatientListViewModel by activityViewModels()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    companion object {
        const val TAG = "SortingDialog"
        fun newInstance(): SortingDialogFragment {
            return SortingDialogFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSortingDialogBinding.inflate(inflater, container, false)
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
        prefillValues()
        setListeners()
    }

    private fun prefillValues() {
        patientListViewModel.sort?.let { sort ->
            binding.apply {
                rbRedRisk.isChecked = sort.isRedRisk != null
                rbLatestAssesment.isChecked = sort.isLatestAssessment != null
                rbMedReview.isChecked = sort.isMedicalReviewDueDate != null
                rbBP.isChecked = sort.isHighLowBp != null
                rbBG.isChecked = sort.isHighLowBg != null
                rbAssessmentDueDate.isChecked = sort.isAssessmentDueDate != null
            }
            binding.let {
                enableResetBtn(
                    it.rbRedRisk.isChecked || it.rbLatestAssesment.isChecked || it.rbMedReview.isChecked || it.rbBP.isChecked || it.rbBG.isChecked || it.rbAssessmentDueDate
                        .isChecked
                )
            }
        }
    }

    private fun setListeners() {
        binding.rgSortCondition.setOnCheckedChangeListener { _, checkedId ->
            if (connectivityManager.isNetworkAvailable()) {
                enableResetBtn(true)
                val radioButton =
                    if (checkedId > 0) binding.rgSortCondition.findViewById<RadioButton>(
                        checkedId
                    ) else null
                if (radioButton != null && radioButton.isChecked) {
                    applySort(
                        SortModel(
                            isRedRisk = value(checkedId, binding.rbRedRisk.id, true),
                            isLatestAssessment = value(
                                checkedId,
                                binding.rbLatestAssesment.id,
                                true
                            ),
                            isMedicalReviewDueDate = value(
                                checkedId,
                                binding.rbMedReview.id,
                                false
                            ),
                            isHighLowBp = value(checkedId, binding.rbBP.id, true),
                            isHighLowBg = value(checkedId, binding.rbBG.id, true),
                            isAssessmentDueDate = value(
                                checkedId,
                                binding.rbAssessmentDueDate.id, false
                            )
                        )
                    )
                }
            } else {
                dismiss()
                (activity as BaseActivity).showErrorDialogue(
                    getString(R.string.error),
                    getString(R.string.no_internet_error), false
                ) {}
            }
        }
        binding.btnReset.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
        binding.labelHeader.ivClose.safeClickListener(this)
    }

    private fun value(checkedId: Int, id: Int, isDec: Boolean): Boolean? {
        return if (checkedId == id) isDec else null
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnDone.id -> applySort(null)
            binding.labelHeader.ivClose.id -> if (binding.btnDone.isEnabled) applySort(null) else dismiss()
            binding.btnReset.id -> doReset()
        }
    }

    private fun applySort(sortModel: SortModel?) {
        patientListViewModel.sort = sortModel
        patientListViewModel.applySortFilter.postValue(true)
        dismiss()
    }

    private fun doReset() {
        binding.rgSortCondition.clearCheck()
        binding.btnDone.isEnabled = true
        enableResetBtn(false)
    }

    private fun enableResetBtn(isReset: Boolean) {
        binding.btnReset.isEnabled = isReset
    }
}