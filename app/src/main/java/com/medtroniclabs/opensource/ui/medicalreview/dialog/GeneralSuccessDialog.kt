package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.databinding.DialogueReviewMedicationSuccessBinding
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.landing.LandingActivity

class GeneralSuccessDialog() :
    DialogFragment(), View.OnClickListener {

    private var callback: ((isPositiveResult: Boolean) -> Unit)? = null

    constructor(callback: (isPositiveResult: Boolean) -> Unit) : this() {
        this.callback = callback
    }
    companion object {
        const val TAG = "PrescriptionSuccessDialogue"

        private const val TITLE = "TITLE"
        private const val MESSAGE = "MESSAGE"
        private const val BUTTON_TEXT = "BUTTON_TEXT"
        private const val NAV_HOME_PAGE = "NAV_HOME_PAGE"

        fun newInstance(
            context: Context,
            title: String,
            message: String,
            doneButton: String = context.getString(R.string.done),
            needHomeNav: Boolean = true,
            callback: ((isPositiveResult: Boolean) -> Unit) = {}
        ): GeneralSuccessDialog {
            val args = Bundle()
            args.putString(TITLE, title)
            args.putString(MESSAGE, message)
            args.putString(BUTTON_TEXT, doneButton)
            args.putBoolean(NAV_HOME_PAGE, needHomeNav)
            val fragment = GeneralSuccessDialog(callback)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(callback: ((isPositiveResult: Boolean) -> Unit) = {}): GeneralSuccessDialog {
            return GeneralSuccessDialog(callback)
        }
    }

    private lateinit var binding: DialogueReviewMedicationSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogueReviewMedicationSuccessBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        setupView()
    }

    private fun setupView() {
        binding.labelHeader.titleView.text = requireArguments().getString(TITLE)
        binding.tvMessage.text = requireArguments().getString(MESSAGE)
        binding.btnDone.text = requireArguments().getString(BUTTON_TEXT)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initializeView() {
        isCancelable = false
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivClose,
            R.id.btnDone -> {
                dismiss()
                callback?.invoke(true)
                if (requireArguments().getBoolean(NAV_HOME_PAGE) && requireActivity() is BaseActivity) {
                    (requireActivity() as BaseActivity).startAsNewActivity(
                        Intent(
                            requireContext(),
                            LandingActivity::class.java
                        )
                    )
                }
            }
        }
    }
}