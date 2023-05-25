package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.customGetParcelable
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.data.model.PrescriptionModel
import com.medtroniclabs.opensource.databinding.DialogInstructionExpansionBinding
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PrescriptionCopyViewModel

class InstructionExpansionDialog : DialogFragment(), View.OnClickListener {

    private var model: PrescriptionModel? = null
    private val prescriptionViewModel: PrescriptionCopyViewModel by activityViewModels()

    companion object {
        const val TAG = "InstructionExpansionDialog"
        const val KEY_MODEL = "KEY_MODEL"
        fun newInstance(model: PrescriptionModel): InstructionExpansionDialog {
            val fragment = InstructionExpansionDialog()
            fragment.arguments = Bundle().apply {
                putParcelable(KEY_MODEL, model)
            }
            return fragment
        }
    }

    private lateinit var binding: DialogInstructionExpansionBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogInstructionExpansionBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.attributes?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.CENTER
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.setDecorFitsSystemWindows(false)
        }
        binding.root.setOnApplyWindowInsetsListener { _, windowInsets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val imeHeight = windowInsets.getInsets(WindowInsets.Type.ime()).bottom
                binding.root.setPadding(0, 0, 0, imeHeight)
                windowInsets.getInsets(WindowInsets.Type.ime() or WindowInsets.Type.systemGestures())
            }
            windowInsets
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.attributes?.windowAnimations = R.style.dialogEnterExitAnimation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readArguments()
        initView()
    }

    private fun readArguments() {
        arguments?.let { args ->
            if(args.containsKey(KEY_MODEL))
                model = args.customGetParcelable(KEY_MODEL)
        }
    }

    private fun initView() {
        binding.btnDone.safeClickListener(this)
        binding.ivClose.safeClickListener(this)
        binding.etInstruction.setText(model?.instruction_entered ?: "")
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnDone.id -> {
                val instructionText = binding.etInstruction.text
                model?.instruction_entered = instructionText.toString()
                model?.isInstructionUpdated = true
                prescriptionViewModel.reloadInstruction.value = true
                dismiss()
            }
            binding.ivClose.id -> {
                dismiss()
            }
        }
    }


}