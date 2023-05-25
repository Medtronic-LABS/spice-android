package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.parcelableArrayList
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.data.model.PatientPrescription
import com.medtroniclabs.opensource.databinding.FragmentDialogMedicalHistoryBinding
import com.medtroniclabs.opensource.ui.medicalreview.MedicationHistoryAdapter

class MedicationHistoryDialogFragment() :
    DialogFragment(),
    View.OnClickListener {

    private lateinit var binding: FragmentDialogMedicalHistoryBinding
    private lateinit var medicationHistoryAdapter: MedicationHistoryAdapter
    private lateinit var prescriptionLists: ArrayList<PatientPrescription>

    companion object {
        const val TAG = "SignatureDialogFragment"
        const val KEY_LIST = "KEY_LIST"
        fun newInstance(prescriptionLists: ArrayList<PatientPrescription>): MedicationHistoryDialogFragment {
            val fragment = MedicationHistoryDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelableArrayList(KEY_LIST, prescriptionLists)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDialogMedicalHistoryBinding.inflate(inflater, container, false)
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
        arguments?.let { args ->
            if (args.containsKey(KEY_LIST))
                prescriptionLists = args.parcelableArrayList(KEY_LIST) ?: ArrayList()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initializeView() {
        isCancelable = false

        if (prescriptionLists.size > 0) {
            medicationHistoryAdapter = MedicationHistoryAdapter(prescriptionLists)
            binding.rvMedicationHistory.layoutManager = LinearLayoutManager(requireContext())
            binding.rvMedicationHistory.adapter = medicationHistoryAdapter
            binding.tvNoRecord.visibility = View.GONE
            binding.rvMedicationHistory.visibility = View.VISIBLE
            val titleCard = getString(R.string.medication_history) + " - " + prescriptionLists[0].medicationName
            binding.clTitleCard.titleView.text = titleCard
        } else {
            binding.tvNoRecord.visibility = View.VISIBLE
            binding.rvMedicationHistory.visibility = View.GONE
        }
    }

    private fun setListeners() {
        binding.clTitleCard.ivClose.safeClickListener(this)
        binding.btnClose.safeClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.clTitleCard.ivClose.id -> dismiss()
            binding.btnClose.id -> dismiss()
        }
    }
}