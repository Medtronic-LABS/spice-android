package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.custom.signature.view.SignatureView
import com.medtroniclabs.opensource.databinding.FragmentDialogSignatureBinding
import com.medtroniclabs.opensource.ui.medicalreview.SignatureListener

class SignatureDialogFragment() : DialogFragment(),
    View.OnClickListener {

    constructor(signatureListener: SignatureListener) : this(){
        this.signatureListener = signatureListener
    }

    private var signatureListener: SignatureListener? = null
    private lateinit var binding: FragmentDialogSignatureBinding

    private var isSigned:Boolean = false

    companion object {
        const val TAG = "SignatureDialogFragment"
        fun newInstance(signatureListener: SignatureListener): SignatureDialogFragment {
            return SignatureDialogFragment(signatureListener)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDialogSignatureBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        setListeners()
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
    }

    private fun setListeners() {
        binding.btnClearSign.safeClickListener(this)
        binding.clTitleCard.ivClose.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnConfirm.safeClickListener(this)
        binding.signatureView.setOnSignedListener(signListener)
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnClearSign.id -> binding.signatureView.clear()
            binding.btnCancel.id -> dismiss()
            binding.clTitleCard.ivClose.id -> dismiss()
            binding.btnConfirm.id -> {
                if (isSigned()) {
                    signatureListener?.applySignature(binding.signatureView.getSignatureBitmap())
                    dismiss()
                }
            }
        }
    }

    private val signListener = object : SignatureView.OnSignedListener {
        override fun onStartSigning() {
            binding.tvErrorSignature.visibility = View.GONE
        }

        override fun onSigned() {
            isSigned = true
            binding.tvErrorSignature.visibility = View.GONE
        }

        override fun onClear() {
            isSigned = false
            binding.tvErrorSignature.visibility = View.GONE
        }
    }

    private fun isSigned(): Boolean {
        var signed = true
        if(!isSigned) {
            signed = false
            binding.tvErrorSignature.visibility = View.VISIBLE
        }
        return signed
    }
}