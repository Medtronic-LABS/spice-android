package com.medtroniclabs.opensource.custom

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.databinding.DialogGeneralBinding

class GeneralErrorDialog() : DialogFragment() {

    private var callback: ((isPositiveResult: Boolean) -> Unit)? = null
    var isNegativeButtonNeed: Boolean = false

    constructor(callback: (isPositiveResult: Boolean) -> Unit) : this() {
        this.callback = callback
    }

    companion object {

        const val TAG = "GeneralErrorDialog"

        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_MESSAGE = "KEY_MESSAGE"
        private const val KEY_OKAY_BUTTON = "KEY_OKAY_BUTTON"
        private const val KEY_CANCEL_BUTTON = "KEY_CANCEL_BUTTON"
        private const val IS_NEGATIVE_BUTTON_NEEDED = "IS_NEGATIVE_BUTTON_NEEDED"
        private const val KEY_OKAY_ENABLE = "KEY_OKAY_ENABLE"

        fun newInstance(
            title: String,
            message: String,
            callback: ((isPositiveResult: Boolean) -> Unit),
            context: Context,
            isNegativeButtonNeed: Boolean,
            okayButton: String = context.getString(R.string.ok),
            cancelButton: String = context.getString(R.string.cancel),
            okayBtnEnable: Boolean
        ): GeneralErrorDialog {

            val args = Bundle()
            args.putString(KEY_TITLE, title)
            args.putString(KEY_MESSAGE, message)
            args.putString(KEY_OKAY_BUTTON, okayButton)
            args.putString(KEY_CANCEL_BUTTON, cancelButton)
            args.putBoolean(IS_NEGATIVE_BUTTON_NEEDED, isNegativeButtonNeed)
            args.putBoolean(KEY_OKAY_ENABLE, okayBtnEnable)
            val fragment = GeneralErrorDialog(callback)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding: DialogGeneralBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogGeneralBinding.inflate(inflater, container, false)

        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        readArguments()
        setupView()
        setupClickListeners()
    }

    private fun readArguments() {
        arguments?.getBoolean(IS_NEGATIVE_BUTTON_NEEDED)?.let {
            isNegativeButtonNeed = it
        }
    }

    private fun setupView() {
        binding.labelHeader.titleView.text = requireArguments().getString(KEY_TITLE)
        binding.tvErrorMessage.text = requireArguments().getString(KEY_MESSAGE)
        binding.btnOkay.text = requireArguments().getString(KEY_OKAY_BUTTON)
        binding.btnCancel.text = requireArguments().getString(KEY_CANCEL_BUTTON)
        binding.labelHeader.ivClose.visibility = View.GONE
        if (isNegativeButtonNeed){
            binding.btnCancel.visibility = View.VISIBLE
        } else {
            binding.btnCancel.visibility = View.GONE
        }
        binding.btnOkay.isEnabled = requireArguments().getBoolean(KEY_OKAY_ENABLE)
    }

    private fun setupClickListeners() {
        binding.btnOkay.safeClickListener {
            dismiss()
            callback?.invoke(true)
        }
        binding.btnCancel.safeClickListener {
            dismiss()
            callback?.invoke(false)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

}
