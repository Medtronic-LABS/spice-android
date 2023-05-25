package com.medtroniclabs.opensource.ui.screening

import android.os.Bundle
import android.view.*
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.customGetSerializable
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.appextensions.setError
import com.medtroniclabs.opensource.common.IntentConstants.CulturesList
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.CultureLocaleModel
import com.medtroniclabs.opensource.databinding.ActivityTranslationBinding
import com.medtroniclabs.opensource.db.tables.CulturesEntity
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.landing.LandingViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TranslationDialogueFragment() : DialogFragment(),
    View.OnClickListener {

    private var selectedCultureId: Long? = null
    lateinit var binding: ActivityTranslationBinding
    private val viewModel: LandingViewModel by activityViewModels()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    companion object {
        const val TAG = "TranslateDialog"
        fun newInstance(
            culturesList: ArrayList<CulturesEntity>
        ): TranslationDialogueFragment {
            val args = Bundle()
            args.putSerializable(CulturesList, culturesList)
            val fragment = TranslationDialogueFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityTranslationBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        initializeView()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.cultureUpdateResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.SUCCESS -> {
                    viewModel.cultureUpdateResponse.setError()
                    dismiss()
                    (activity as BaseActivity).showAlertWith(
                        message = getString(R.string.language_change_alert),
                        isNegativeButtonNeed = true,
                        negativeButtonName = getString(R.string.no),
                        positiveButtonName = getString(R.string.yes)
                    ) { callBack ->
                        if (callBack)
                            viewModel.userLogout(requireContext())
                    }
                }
                else -> {
                    //Nothing to invoke
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initializeView() {
        arguments?.let { args ->
            initializeDialogue(args, SecuredPreference.getCultureId())
        }
        binding.labelHeader.ivClose.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.btnConfirm.safeClickListener(this)
    }

    private fun initializeDialogue(arguments: Bundle, cultureSelected: Long) {
        val list = arguments.customGetSerializable(CulturesList) as ArrayList<CulturesEntity>?
        list?.let { initializeRadioGroup(it, cultureSelected) }
    }

    private fun initializeRadioGroup(langList: ArrayList<CulturesEntity>, cultureSelected: Long) {
        for (i in langList.indices) {
            val radioButton = RadioButton(requireContext())
            radioButton.text = langList[i].name
            radioButton.tag = langList[i].id
            radioButton.textSize = 16f
            radioButton.setPadding(16, 15, 0, 15)
            binding.radioGroup.addView(radioButton)

            if (langList[i].id == cultureSelected) {
                binding.radioGroup.check(radioButton.id)
            }
        }
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            selectedCultureId = group.findViewById<RadioButton>(checkedId).tag as Long
        }
    }

    private fun updateUserLocale(selectedCultureId: Long?) {
        selectedCultureId?.let { id ->
            viewModel.cultureLocaleUpdate(
                CultureLocaleModel(
                    SecuredPreference.getUserId(),
                    id
                )
            )
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.labelHeader.ivClose.id, binding.btnCancel.id -> {
                dismiss()
            }

            binding.btnConfirm.id -> {
                if (connectivityManager.isNetworkAvailable()) {
                    updateUserLocale(selectedCultureId)
                } else {
                    (activity as BaseActivity).showErrorDialogue(
                        getString(R.string.error),
                        getString(R.string.no_internet_error),
                        isNegativeButtonNeed = false,
                    ) {}
                }
            }
        }
    }
}