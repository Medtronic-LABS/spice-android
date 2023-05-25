package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.databinding.LayoutDialogSymptomsBinding

class SymptomsDialog(val symptoms: String) : DialogFragment(), View.OnClickListener {

    private lateinit var binding: LayoutDialogSymptomsBinding

    companion object {
        const val TAG = "SymptomsDialog"

        fun newInstance(symptoms: String): SymptomsDialog {
            val fragment = SymptomsDialog(symptoms)
            fragment.arguments = Bundle().apply {}
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutDialogSymptomsBinding.inflate(inflater, container, false)
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
        initViews()
        clickListeners()
    }

    private fun initViews() {
        binding.titleCard.titleView.text = getString(R.string.symptoms)
        binding.tvSymptoms.text = symptoms
    }

    private fun clickListeners() {
        binding.titleCard.ivClose.safeClickListener(this)
    }

    override fun onClick(mView: View?) {
        when (mView?.id) {
            binding.titleCard.ivClose.id -> {
                dismiss()
            }
        }
    }
}