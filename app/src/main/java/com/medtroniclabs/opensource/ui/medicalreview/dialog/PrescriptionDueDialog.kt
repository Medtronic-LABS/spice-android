package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.databinding.DialogPrescriptionDueBinding
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.MedicalReviewBaseViewModel

class PrescriptionDueDialog() : DialogFragment(), View.OnClickListener {

    private val viewModel: MedicalReviewBaseViewModel by activityViewModels()

    constructor(listener: PrescriptionDueListener) : this() {
        this.listener = listener
    }
    private var listener: PrescriptionDueListener? = null
    companion object {
        const val TAG = "PrescriptionDueDialog"
        private const val KEY_MESSAGE = "KEY_MESSAGE"
        private const val KEY_DIAGNOSIS = "KEY_DIAGNOSIS"
        fun newInstance(
            message: String,
            listener: PrescriptionDueListener,
            diagnosis: Boolean = false
        ): PrescriptionDueDialog {
            val args = Bundle()
            args.putString(KEY_MESSAGE, message)
            args.putBoolean(KEY_DIAGNOSIS,diagnosis)
            val fragment =  PrescriptionDueDialog(listener)
            fragment.arguments = args
            return fragment
        }
    }
    private lateinit var binding: DialogPrescriptionDueBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPrescriptionDueBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        setupView()
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.btnYes.safeClickListener(this)
        binding.btnNo.safeClickListener(this)
        binding.btnConfirmDiagnosis.safeClickListener(this)
    }

    private fun setupView() {
        binding.tvContent.text = requireArguments().getString(KEY_MESSAGE)
        if (requireArguments().getBoolean(KEY_DIAGNOSIS,false)){
            binding.btnYes.visibility = View.GONE
            binding.btnNo.visibility = View.GONE
            binding.btnConfirmDiagnosis.visibility = View.VISIBLE
        }else {
            binding.btnConfirmDiagnosis.visibility = View.GONE
            binding.btnYes.visibility = View.VISIBLE
            binding.btnNo.visibility = View.VISIBLE
        }
    }

    /**
     * listener for view on click events
     * @param view respected view clicked
     */
    override fun onClick(view: View) {
        when (view.id) {
            binding.labelHeader.ivClose.id, R.id.btnNo -> {
                dismiss()
            }
            R.id.btnYes -> {
                dismiss()
                listener?.submitMedicalReview()
            }
            R.id.btnConfirmDiagnosis -> {
                dismiss()
                listener?.performConfirmDiagnosis()
            }
        }
    }
}

interface PrescriptionDueListener {
    fun submitMedicalReview()
    fun performConfirmDiagnosis()
}