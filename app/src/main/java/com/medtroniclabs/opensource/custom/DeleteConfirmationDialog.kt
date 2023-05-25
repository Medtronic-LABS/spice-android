package com.medtroniclabs.opensource.custom

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.fetchString
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.MedicalReviewConstant
import com.medtroniclabs.opensource.databinding.DeleteConfirmationDialogueBinding
import com.medtroniclabs.opensource.db.tables.ShortageReasonEntity
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.ui.TagListCustomView
import com.medtroniclabs.opensource.ui.medicalreview.pharamcist.viewmodel.PrescriptionRefillViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteConfirmationDialog() : DialogFragment(), View.OnClickListener {

    private lateinit var reasonListCustomView: TagListCustomView
    private val viewModel: PrescriptionRefillViewModel by activityViewModels()

    private var callback: ((isPositiveResult: Boolean,reason:String?,otherReason:String?) -> Unit)? = null
    var isNegativeButtonNeed: Boolean = false

    constructor(callback: (isPositiveResult: Boolean,reason:String?,otherReason:String?) -> Unit) : this() {
        this.callback = callback
    }

    companion object {

        const val TAG = "DeleteConfirmationDialog"

        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_MESSAGE = "KEY_MESSAGE"
        private const val KEY_OKAY_BUTTON = "KEY_OKAY_BUTTON"
        private const val KEY_CANCEL_BUTTON = "KEY_CANCEL_BUTTON"
        private const val IS_NEGATIVE_BUTTON_NEEDED = "IS_NEGATIVE_BUTTON_NEEDED"

        fun newInstance(
            title: String,
            message: String,
            callback: ((isPositiveResult: Boolean,reason:String?, otherReason:String?) -> Unit),
            context: Context,
            isNegativeButtonNeed: Boolean,
            okayButton: String = context.getString(R.string.ok),
            cancelButton: String = context.getString(R.string.cancel)
        ): DeleteConfirmationDialog {

            val args = Bundle()
            args.putString(KEY_TITLE, title)
            args.putString(KEY_MESSAGE, message)
            args.putString(KEY_OKAY_BUTTON, okayButton)
            args.putString(KEY_CANCEL_BUTTON, cancelButton)
            args.putBoolean(IS_NEGATIVE_BUTTON_NEEDED, isNegativeButtonNeed)
            val fragment = DeleteConfirmationDialog(callback)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding: DeleteConfirmationDialogueBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DeleteConfirmationDialogueBinding.inflate(inflater, container, false)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        readArguments()
        setupView()
        setupClickListeners()
        viewModel.getDeleteReasonList()
        attachObserver()
        initializeTagView()
    }

    private fun initializeTagView() {
        reasonListCustomView = TagListCustomView(
            requireContext(),
            binding.cgDeleteReason,
            otherCallBack = { selectedName, isChecked ->
                if (selectedName.startsWith(
                        MedicalReviewConstant.otherTypeText,
                        ignoreCase = true
                    )
                ) {
                    if (isChecked) {
                        binding.etOtherReason.visibility = View.VISIBLE
                        binding.tvReasonErrorMessage.visibility = View.GONE
                    }
                    else {
                        binding.etOtherReason.visibility = View.GONE
                        binding.tvReasonErrorMessage.visibility = View.GONE
                        binding.etOtherReason.setText("")
                    }
                }
            },
            otherSingleSelect = true
        )
    }

    private fun attachObserver() {
        viewModel.deleteReasonList.observe(viewLifecycleOwner) { list ->
            list?.let {
                loadDeleteReasonList(it)
            }
        }
    }

    private fun loadDeleteReasonList(list: List<ShortageReasonEntity>) {
        val deleteMap: HashMap<String, MutableList<String>> = HashMap()
        val deleteList: MutableList<String> = ArrayList()
        val translate = SecuredPreference.getIsTranslationEnabled()
        list.forEach {
            if (translate)
                deleteList.add(it.cultureValue ?: it.reason)
            else
                deleteList.add(it.reason)
        }
        deleteMap[DefinedParams.TYPE_DELETE] = deleteList.toMutableList()
        reasonListCustomView.addChipItemList(list, diagnosisMap = deleteMap)
    }

    private fun readArguments() {
        arguments?.getBoolean(IS_NEGATIVE_BUTTON_NEEDED)?.let {
            isNegativeButtonNeed = it
        }
    }

    private fun setupView() {
        binding.labelHeader.titleView.text = requireArguments().getString(KEY_TITLE)
        binding.tvDeleteMessage.text = requireArguments().getString(KEY_MESSAGE)
        binding.tvDeleteMessage.markMandatory()
        binding.btnOkay.text = requireArguments().getString(KEY_OKAY_BUTTON)
        binding.btnCancel.text = requireArguments().getString(KEY_CANCEL_BUTTON)
        binding.labelHeader.ivClose.visibility = View.GONE
        if (isNegativeButtonNeed){
            binding.btnCancel.visibility = View.VISIBLE
        } else {
            binding.btnCancel.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.btnOkay.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
    }

    private fun deleteArchive() {
        val chipValue = getChipValue(reasonListCustomView.getSelectedTags()[0])
        if(binding.etOtherReason.text.isNullOrEmpty())
            callback?.invoke(true, chipValue,null)
        else
            callback?.invoke(true, chipValue,binding.etOtherReason.fetchString())
        dismiss()
    }

    private fun getChipValue(any: Any): String {
        return if (any is ShortageReasonEntity)
            any.reason
        else
            any.toString()
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if(!reasonListCustomView.getSelectedTags().isNullOrEmpty())
        {
            val selectedReason = getChipValue(reasonListCustomView.getSelectedTags()[0])
            if(selectedReason == DefinedParams.Compliance_Type_Other)
            {
                if (binding.etOtherReason.text.isNullOrBlank())
                {
                    isValid = false
                    binding.tvReasonErrorMessage.text = getString(R.string.valid_reason)
                    binding.tvReasonErrorMessage.visibility = View.VISIBLE
                }else{
                    binding.tvReasonErrorMessage.visibility = View.GONE
                }
            }
        }else{
            binding.tvReasonErrorMessage.text = getString(R.string.reason_error)
            binding.tvReasonErrorMessage.visibility = View.VISIBLE
            isValid = false
        }

        return isValid
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onClick(mView: View?) {
        when(mView?.id)
        {
            binding.btnOkay.id -> {
                if (validateInputs()) {
                    deleteArchive()
                }
            }
            binding.btnCancel.id -> {
                dismiss()
            }
        }
    }

}
