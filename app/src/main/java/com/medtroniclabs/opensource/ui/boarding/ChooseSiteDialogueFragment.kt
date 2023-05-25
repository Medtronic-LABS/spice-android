package com.medtroniclabs.opensource.ui.boarding

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.customGetSerializable
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.IntentConstants.IntentSelectedSite
import com.medtroniclabs.opensource.common.IntentConstants.IntentSiteList
import com.medtroniclabs.opensource.databinding.FragmentChooseSiteBinding
import com.medtroniclabs.opensource.db.tables.SiteEntity
import com.medtroniclabs.opensource.ui.boarding.adapter.SiteAdapter
import com.medtroniclabs.opensource.ui.boarding.listener.SiteSelectionListener
import com.medtroniclabs.opensource.ui.customviews.DividerNCDItemDecoration

class ChooseSiteDialogueFragment() : DialogFragment(),
    View.OnClickListener {

    private var siteSelected: SiteEntity? = null
    var listener: SiteSelectionListener? = null
    lateinit var binding: FragmentChooseSiteBinding

    constructor(listener: SiteSelectionListener) : this() {
        this.listener = listener
    }

    companion object {
        const val TAG = "ChooseSiteDialog"
        fun newInstance(
            siteList: ArrayList<SiteEntity>,
            siteSelected: SiteEntity? = null,
            listener: SiteSelectionListener
        ): ChooseSiteDialogueFragment {
            val args = Bundle()
            args.putSerializable(IntentSiteList, siteList)
            args.putSerializable(IntentSelectedSite, siteSelected)
            val fragment = ChooseSiteDialogueFragment(listener)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChooseSiteBinding.inflate(inflater, container, false)

        binding.btnConfirm.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { arguments ->
            if (arguments.containsKey(IntentSiteList)) {
                if (arguments.containsKey(IntentSelectedSite)) {
                    siteSelected  = arguments.customGetSerializable(IntentSelectedSite) as SiteEntity?
                }
                initializeSiteList(arguments, siteSelected)
            }
        }
    }


    private fun initializeSiteList(arguments: Bundle, selectedSiteEntity: SiteEntity?) {
        val list = arguments.customGetSerializable(IntentSiteList) as ArrayList<SiteEntity>?
        list?.let { setAdapter(it, selectedSiteEntity) }
    }

    private fun setAdapter(siteList: ArrayList<SiteEntity>, selectedSiteEntity: SiteEntity?) {
        binding.rvSiteList.layoutManager = LinearLayoutManager(binding.root.context)
        binding.rvSiteList.addItemDecoration(
            DividerNCDItemDecoration(activity, R.drawable.divider)
        )
        binding.rvSiteList.adapter = SiteAdapter(siteList, selectedSiteEntity) { siteEntity ->
            enableConfirm(siteEntity)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnCancel -> {
                listener?.onCancelButtonSelected()
                dismiss()
            }
            R.id.btnConfirm -> {
                siteSelected?.let {
                    listener?.onSiteSelected(it)
                }
                dismiss()
            }
        }
    }

    private fun enableConfirm(siteEntity: SiteEntity) {
        siteSelected = siteEntity
        binding.btnConfirm.isEnabled = true
    }

}