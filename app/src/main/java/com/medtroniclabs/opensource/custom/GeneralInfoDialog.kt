package com.medtroniclabs.opensource.custom

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.databinding.DialogGeneralInfoBinding

class GeneralInfoDialog : DialogFragment(), View.OnClickListener {

    companion object {

        const val TAG = "GeneralInfoDialog"

        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_SUB_TITLE = "KEY_SUB_TITLE"
        private const val KEY_INFORMATION = "KEY_INFORMATION"

        fun newInstance(
            title: String,
            subTitle: String? = null,
            information: ArrayList<String>
        ): GeneralInfoDialog {

            val args = Bundle()
            args.putString(KEY_TITLE, title)
            subTitle?.let {
                args.putString(KEY_SUB_TITLE, subTitle)
            }
            args.putStringArrayList(KEY_INFORMATION, information)
            val fragment = GeneralInfoDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding: DialogGeneralInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogGeneralInfoBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    private fun setupView() {
        binding.clTitleCard.titleView.text = requireArguments().getString(KEY_TITLE)
        requireArguments().getString(KEY_SUB_TITLE)?.let {
            binding.tvSubTitle.visibility = View.VISIBLE
            binding.tvSubTitle.text = it
        }
        val information = requireArguments().getStringArrayList(KEY_INFORMATION)
        binding.rvDetails.layoutManager =
            LinearLayoutManager(view?.context, LinearLayoutManager.VERTICAL, false)
        binding.rvDetails.adapter = information?.let { GeneralInfoAdapter(it) }
        binding.clTitleCard.ivClose.safeClickListener(this)
        binding.btnOkay.safeClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            binding.btnOkay.id, binding.clTitleCard.ivClose.id -> dismiss()
        }
    }

}