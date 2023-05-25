package com.medtroniclabs.opensource.ui.medicalreview.pharamcist

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.databinding.QualipharmDialogueLayoutBinding
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import com.medtroniclabs.opensource.ui.medicalreview.pharamcist.viewmodel.PrescriptionRefillViewModel

class QualifyPharmStockAuditResponseDialogue : DialogFragment(), View.OnClickListener {

    private lateinit var binding: QualipharmDialogueLayoutBinding

    private val viewModel: PrescriptionRefillViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = QualipharmDialogueLayoutBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        viewModel.shortageReasonMap?.let { map ->
            binding.rvMedicationReason.layoutManager = LinearLayoutManager(view.context)
            binding.rvMedicationReason.adapter = QualiPharmAdapter(map)
        }
        binding.titleCard.ivClose.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivClose,R.id.btnDone -> {
                dialog?.dismiss()
                if (requireActivity() is BaseActivity) {
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


    companion object {
        const val TAG = "QualifyPharmStockAuditResponseDialogue"
        fun newInstance(): QualifyPharmStockAuditResponseDialogue {
            return QualifyPharmStockAuditResponseDialogue()
        }
    }
}