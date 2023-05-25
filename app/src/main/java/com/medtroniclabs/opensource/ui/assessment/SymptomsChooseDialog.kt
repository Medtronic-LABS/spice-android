package com.medtroniclabs.opensource.ui.assessment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.ui.SymptomModel
import com.medtroniclabs.opensource.databinding.LayoutChooseSymptomsBinding
import com.medtroniclabs.opensource.db.tables.SymptomEntity
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams

class SymptomsChooseDialog : DialogFragment(), View.OnClickListener {

    lateinit var binding: LayoutChooseSymptomsBinding

    private val viewModel: AssessmentViewModel by activityViewModels()

    companion object {
        const val TAG = "SymptomsChooseDialog"
        fun newInstance(): SymptomsChooseDialog {
            return SymptomsChooseDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutChooseSymptomsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView(view)
        viewModel.getSymptomList()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.symptomListResponse.observe(viewLifecycleOwner) {
            binding.rvSymptom.adapter = SymptomAdapter(getSymptomModelList(it),SecuredPreference.getIsTranslationEnabled())
        }
    }

    private fun getSymptomModelList(symptomList: List<SymptomEntity>?): ArrayList<SymptomModel> {
        val selectedSymptoms = viewModel.selectedSymptoms.value
        val tempList = ArrayList<SymptomModel>()
        symptomList?.forEach { symptom ->
            if (selectedSymptoms != null && selectedSymptoms.isNotEmpty()) {
                val model = selectedSymptoms.find { it._id == symptom._id }
                if (model != null) {
                    tempList.add(SymptomModel(symptom._id, symptom.symptom, true,symptom.type, cultureValue = symptom.cultureValue))
                } else {
                    tempList.add(SymptomModel(symptom._id, symptom.symptom, type = symptom.type, cultureValue = symptom.cultureValue))
                }
            } else {
                tempList.add(SymptomModel(symptom._id, symptom.symptom, type = symptom.type, cultureValue = symptom.cultureValue))
            }
        }

        val list = ArrayList<SymptomModel>()
        list.add(
            SymptomModel(
                (-1).toLong(),
                "",
                false,
                getString(R.string.hypertension),
                viewType = 1
            )
        )
        list.addAll(tempList.filter {
            it.type.equals(
                DefinedParams.Compliance_Type_Hypertension,
                true
            )
        })
        list.add(
            SymptomModel(
                (-1).toLong(),
                "",
                false,
                getString(R.string.diabetes),
                viewType = 1
            )
        )
        list.addAll(tempList.filter {
            it.type.equals(
                DefinedParams.Compliance_Type_Diabetes,
                true
            )
        })
        return list
    }


    private fun initializeView(view: View) {
        binding.rvSymptom.layoutManager = LinearLayoutManager(view.context)
        binding.btnCancel.safeClickListener(this)
        binding.btnOkay.safeClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    override fun onClick(view: View?) {
        when (view?.id) {

            R.id.btnCancel -> {
                dismiss()
            }

            R.id.btnOkay -> {
                val adapter = binding.rvSymptom.adapter
                if (adapter != null && adapter is SymptomAdapter) {
                    viewModel.selectedSymptoms.value = adapter.getSelectedSymptomList()
                }
                dismiss()
            }
        }
    }

}