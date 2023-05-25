package com.medtroniclabs.opensource.ui.landing

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.databinding.DialogOfflineDataBinding

class OfflineDataDialog() : DialogFragment(), View.OnClickListener {

    constructor(callback: (startUpload: Boolean) -> Unit) : this() {
        this.callback = callback
    }

    private var callback: ((startUpload: Boolean) -> Unit)? = null
    private val viewModel: LandingViewModel by activityViewModels()
    companion object {
        const val TAG = "OfflineDataDialog"
        fun newInstance(callback: (startUpload: Boolean) -> Unit): OfflineDataDialog {
            return OfflineDataDialog(callback)
        }
    }

    private lateinit var binding: DialogOfflineDataBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogOfflineDataBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListeners()
        getOfflineDataCount()
    }

    private fun getOfflineDataCount() {
        viewModel.offlineScreenedData.value.let {
            updateViews(it)
        }
    }

    private fun updateViews(count: Long?) {
        if (count != null && count > 0) {
            binding.btnUpload.visibility = View.VISIBLE
            binding.btnOkay.visibility = View.GONE

            binding.tvMessage.text =
                if (count > 1) getString(
                    R.string.screened_patients,
                    count.toString()
                ) else getString(
                    R.string.screened_patient
                )
        } else {
            binding.btnUpload.visibility = View.GONE
            binding.btnOkay.visibility = View.VISIBLE

            binding.tvMessage.text = getString(R.string.no_screened_patients)
        }
    }

    private fun clickListeners() {
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnOkay.safeClickListener(this)
        binding.btnUpload.safeClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.labelHeader.ivClose.id, binding.btnCancel.id, binding.btnOkay.id -> {
                dismiss()
                callback?.invoke(false)
            }
            R.id.btnUpload -> {
                dismiss()
                callback?.invoke(true)
            }
        }
    }
}