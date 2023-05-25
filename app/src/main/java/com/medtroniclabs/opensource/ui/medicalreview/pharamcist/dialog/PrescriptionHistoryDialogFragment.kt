package com.medtroniclabs.opensource.ui.medicalreview.pharamcist.dialog

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.appextensions.parcelableArrayList
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.data.model.PrescriptionRefillHistoryResponse
import com.medtroniclabs.opensource.databinding.FragmentDialogPrescriptionHistoryBinding

class PrescriptionHistoryDialogFragment :
    DialogFragment(),
    View.OnClickListener {

    private lateinit var binding: FragmentDialogPrescriptionHistoryBinding
    private lateinit var prescriptionHistoryAdapter: PrescriptionHistoryAdapter
    private lateinit var prescriptionLists: ArrayList<PrescriptionRefillHistoryResponse>

    companion object {
        const val TAG = "PrescriptionSignatureDialogFragment"
        const val PRESCRIPTION_LIST = "PRESCRIPTION_LIST"
        fun newInstance(prescriptionLists: ArrayList<PrescriptionRefillHistoryResponse>): PrescriptionHistoryDialogFragment {
            val args = Bundle().apply {
                putParcelableArrayList(PRESCRIPTION_LIST, prescriptionLists)
            }
            return PrescriptionHistoryDialogFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDialogPrescriptionHistoryBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readArguments()
        initializeView()
        setListeners()
    }

    private fun readArguments() {
        arguments?.parcelableArrayList<PrescriptionRefillHistoryResponse>(PRESCRIPTION_LIST)?.let {
            prescriptionLists = it
        }
    }

    private fun setListeners() {
        binding.clTitleCard.ivClose.safeClickListener(this)
        binding.btnClose.safeClickListener(this)
    }

    private fun initializeView() {
        isCancelable = false

        if (prescriptionLists.size > 0) {
            prescriptionHistoryAdapter = PrescriptionHistoryAdapter(prescriptionLists)
            binding.rvMedicationHistory.layoutManager = LinearLayoutManager(requireContext())
            binding.rvMedicationHistory.adapter = prescriptionHistoryAdapter
            binding.tvNoRecord.visibility = View.GONE
            binding.rvMedicationHistory.visibility = View.VISIBLE
        }else{
            binding.tvNoRecord.visibility = View.VISIBLE
            binding.rvMedicationHistory.visibility = View.GONE
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
            binding.clTitleCard.ivClose.id -> dismiss()
            binding.btnClose.id -> dismiss()
        }
    }
}