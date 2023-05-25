package com.medtroniclabs.opensource.ui.medicalreview.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.touchObserver
import com.medtroniclabs.opensource.common.MedicalReviewConstant
import com.medtroniclabs.opensource.data.model.InitialComorbidities
import com.medtroniclabs.opensource.data.model.InitialComplications
import com.medtroniclabs.opensource.databinding.FragmentComorbiditiesAndComplicationsBinding
import com.medtroniclabs.opensource.db.tables.ComorbidityEntity
import com.medtroniclabs.opensource.db.tables.ComplicationEntity
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.TagListCustomView
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.MedicalReviewBaseViewModel

class ComorbiditiesAndComplicationsFragment : BaseFragment() {
    lateinit var binding: FragmentComorbiditiesAndComplicationsBinding
    private val viewModel: MedicalReviewBaseViewModel by activityViewModels()
    private lateinit var comorbidityTagListCustomView: TagListCustomView
    private lateinit var complicationTagListCustomView: TagListCustomView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentComorbiditiesAndComplicationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        attachObservers()
        getDataList()
    }

    private fun addListeners() {
        binding.etOtherComorbidity.addTextChangedListener { otherComorbidity ->
            if (otherComorbidity.isNullOrBlank())
                addOrUpdateOtherComorbidity(null)
            else
                addOrUpdateOtherComorbidity(otherComorbidity.trim().toString())
        }
        binding.etOtherComplications.addTextChangedListener { otherComplication ->
            if (otherComplication.isNullOrBlank())
                addOrUpdateOtherComplication(null)
            else
                addOrUpdateOtherComplication(otherComplication.trim().toString())
        }
    }

    private fun addOrUpdateOtherComplication(otherComplication: String?) {
        val complicationList = viewModel.initialEncounterRequest.complications
        if (!complicationList.isNullOrEmpty()) {
            val otherModel = complicationList.find { it.name.startsWith(MedicalReviewConstant.otherTypeText, true) }
            otherModel?.let {
                it.otherComplication = otherComplication
            }
        }
    }

    private fun addOrUpdateOtherComorbidity(otherComorbidity: String?) {
        val comorbidityList = viewModel.initialEncounterRequest.comorbidities
        if (!comorbidityList.isNullOrEmpty()) {
            val otherModel = comorbidityList.find { it.name.startsWith(MedicalReviewConstant.otherTypeText, true) }
            otherModel?.let {
                it.otherComorbidity = otherComorbidity
            }
        }
    }

    private fun getDataList() {
        viewModel.getComorbidityList()
        viewModel.getComplicationList()
    }

    private fun attachObservers() {
        viewModel.comorbidityListResponse.observe(viewLifecycleOwner) { list ->
            list?.let {
                loadComorbidityList(it)
            }
        }

        viewModel.complicationListResponse.observe(viewLifecycleOwner) { list ->
            list?.let {
                loadComplicationList(it)
            }
        }
    }

    private fun loadComorbidityList(list: List<Any>) {
        var selectedcomorbidities: List<String>? = null
        viewModel.initialEncounterRequest.comorbidities?.let { comorbidities ->
            selectedcomorbidities = comorbidities.map { it.name }.toList()
            comorbidities.find { it.name.startsWith(MedicalReviewConstant.otherTypeText, true) }?.let {
                binding.etOtherComorbidity.setText(it.otherComorbidity)
            }
        }
        comorbidityTagListCustomView.addChipItemList(list, selectedcomorbidities)
    }

    private fun initializeView() {
        binding.tvComplicationLbl.markMandatory()
        binding.tvComorbidityLbl.markMandatory()
        binding.etOtherComplications.touchObserver()
        binding.etOtherComorbidity.touchObserver()
        addListeners()
        comorbidityView()
        complicationView()
    }

    private fun complicationView() {
        complicationTagListCustomView =
            TagListCustomView(
                requireContext(),
                binding.llComplicationsRoot, otherCallBack = { selectedName, isChecked ->
                    if (viewModel.initialEncounterRequest.complications == null)
                        viewModel.initialEncounterRequest.complications = java.util.ArrayList()
                    var item =
                        viewModel.initialEncounterRequest.complications?.find { it.name.equals(selectedName, ignoreCase = true) }

                    if (isChecked && item == null) {
                        viewModel.complicationListResponse.value?.find { it.complication.lowercase() == selectedName.lowercase() }
                            ?.let { complications ->
                                item = InitialComplications(
                                    complications._id,
                                    name = complications.complication
                                )
                                viewModel.initialEncounterRequest.complications?.add(item!!)
                            }
                    } else if (!isChecked && item != null)
                        viewModel.initialEncounterRequest.complications?.remove(item)

                    if (selectedName.startsWith(MedicalReviewConstant.otherTypeText, ignoreCase = true)) {
                        if (isChecked)
                            binding.etOtherComplicationsHolder.visibility = View.VISIBLE
                        else {
                            binding.etOtherComplicationsHolder.visibility = View.GONE
                            binding.etOtherComplications.setText("")
                        }
                    }
                }
            )
    }

    companion object {
        const val TAG = "ComorbiditiesAndComplicationsFragment"
        @JvmStatic
        fun newInstance() =
            ComorbiditiesAndComplicationsFragment().apply {}
    }

    fun updateCommorbiditiesList() {
        val selectedList = comorbidityTagListCustomView.getSelectedTags()
        val updatedList = ArrayList<InitialComorbidities>()
        viewModel.initialEncounterRequest.comorbidities?.forEach { commorbidity ->
            selectedList.find { it is ComorbidityEntity && it._id == commorbidity.comorbidityId }
                ?.let {
                    updatedList.add(commorbidity)
                }
        }
        viewModel.initialEncounterRequest.comorbidities = updatedList
    }

    private fun comorbidityView() {
        comorbidityTagListCustomView =
            TagListCustomView(
                requireContext(),
                binding.llComorbidityRoot, otherCallBack = { selectedName, isChecked ->
                    if (viewModel.initialEncounterRequest.comorbidities == null)
                        viewModel.initialEncounterRequest.comorbidities = ArrayList()
                    var item =
                        viewModel.initialEncounterRequest.comorbidities?.find { it.name.equals(selectedName, ignoreCase = true) }

                    if (isChecked && item == null) {
                        viewModel.comorbidityListResponse.value?.find { it.comorbidity.lowercase() == selectedName.lowercase() }
                            ?.let { comorbidity ->
                                item = InitialComorbidities(
                                    comorbidity._id,
                                    name = comorbidity.comorbidity
                                )
                                viewModel.initialEncounterRequest.comorbidities?.add(item!!)
                            }
                    } else if (!isChecked && item != null)
                        viewModel.initialEncounterRequest.comorbidities?.remove(item)

                    if (selectedName.startsWith(MedicalReviewConstant.otherTypeText, ignoreCase = true)) {
                        if (isChecked) {
                            binding.etOtherComorbidityHolder.visibility = View.VISIBLE
                        } else {
                            binding.etOtherComorbidityHolder.visibility = View.GONE
                            binding.etOtherComorbidity.setText("")
                        }
                    }
                }
            )
    }

    fun updateComplications() {
        val selectedList = complicationTagListCustomView.getSelectedTags()
        val updateList = ArrayList<InitialComplications>()
        viewModel.initialEncounterRequest.complications?.forEach { complications ->
            selectedList.find {
                it is ComplicationEntity && it._id == complications.complicationId
            }
                ?.let {
                    updateList.add(complications)
                }
        }
        viewModel.initialEncounterRequest.complications = updateList
    }

    private fun loadComplicationList(list: List<Any>) {
        var selectedComplications: List<String>? = null
        viewModel.initialEncounterRequest.complications?.let { complications ->
            selectedComplications = complications.map { it.name }.toList()
            complications.find { it.name.startsWith(MedicalReviewConstant.otherTypeText, true) }?.let {
                binding.etOtherComplications.setText(it.otherComplication)
            }
        }
        complicationTagListCustomView.addChipItemList(list, selectedComplications)
    }

    private fun validateComplication(): Pair<Boolean?, TextView?> {
        var isValid: Boolean? = null
        var errorView: TextView? = null
        if (viewModel.initialEncounterRequest.complications != null) {
            viewModel.initialEncounterRequest.complications?.let {
                val selectedOtherComplication =
                    it.filter { it.name.startsWith(MedicalReviewConstant.otherTypeText, true) }
                if (selectedOtherComplication.isNotEmpty()) {
                    if (selectedOtherComplication[0].otherComplication == null) {
                        isValid = false
                        errorView = binding.tvComplicationsTitle
                        showErrorMessage(
                            getString(R.string.default_user_input_error),
                            binding.tvErrorOtherComplication
                        )
                    } else
                        hideErrorMessage(binding.tvErrorOtherComplication)
                } else
                    hideErrorMessage(binding.tvErrorOtherComplication)
            }
        } else
            hideErrorMessage(binding.tvErrorOtherComplication)
        return Pair(isValid, errorView)
    }

    private fun validateComorbidity(): Pair<Boolean?, TextView?> {
        var isValid: Boolean? = null
        var errorView: TextView? = null
        if (viewModel.initialEncounterRequest.comorbidities != null) {
            viewModel.initialEncounterRequest.comorbidities?.let {
                val selectedComorbidity =
                    it.filter { it.name.startsWith(MedicalReviewConstant.otherTypeText, true) }
                if (selectedComorbidity.isNotEmpty()) {
                    if (selectedComorbidity[0].otherComorbidity == null) {
                        isValid = false
                        errorView = binding.tvComorbidityTitle
                        showErrorMessage(
                            getString(R.string.default_user_input_error),
                            binding.tvErrorOtherComorbidity
                        )
                    } else
                        hideErrorMessage(binding.tvErrorOtherComorbidity)
                } else
                    hideErrorMessage(binding.tvErrorOtherComorbidity)
            }
        } else
            hideErrorMessage(binding.tvErrorOtherComorbidity)
        return Pair(isValid, errorView)
    }

    private fun showErrorMessage(message: String, view: TextView) {
        view.visibility = View.VISIBLE
        view.text = message
    }

    private fun hideErrorMessage(view: TextView) {
        view.visibility = View.GONE
    }

    fun validateInputs() : Boolean
    {
        var isValid = true
        var errorView: TextView? = null

        validateComorbidity().let {
            it.first?.let { first ->
                isValid = first
            }
            it.second?.let { textView ->
                errorView = textView
            }
        }

        validateComplication().let {
            it.first?.let { first ->
                if (isValid)
                    isValid = first
            }
            it.second?.let { textView ->
                errorView = errorView ?: textView
            }
        }

        return isValid
    }
}