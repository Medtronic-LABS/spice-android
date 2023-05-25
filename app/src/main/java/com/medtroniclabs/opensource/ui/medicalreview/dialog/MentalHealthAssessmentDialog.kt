package com.medtroniclabs.opensource.ui.medicalreview.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.databinding.FragmentDialogMentalHealthAssessmentBinding
import com.medtroniclabs.opensource.formgeneration.FormGenerator
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.definedproperties.ViewType
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.formgeneration.listener.FormEventListener
import com.medtroniclabs.opensource.formgeneration.model.FormLayout
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel

class MentalHealthAssessmentDialog : DialogFragment(),
    View.OnClickListener, FormEventListener {

    companion object {
        const val TAG = "MentalHealthAssessmentDialog"
        const val ASSESSMENT_TYPE = "ASSESSMENT_TYPE"
        const val IS_VIEW_ONLY = "IS_VIEW_ONLY"
        fun newInstance(assessmentType: String, isViewOnly: Boolean = false) : MentalHealthAssessmentDialog{
            val fragment = MentalHealthAssessmentDialog()
            fragment.arguments = Bundle().apply {
                putString(ASSESSMENT_TYPE, assessmentType)
                putBoolean(IS_VIEW_ONLY, isViewOnly)
            }
            return fragment
        }
    }


    private lateinit var mentalHealthAssessmentBinding: FragmentDialogMentalHealthAssessmentBinding

    private val viewModel: PatientDetailViewModel by activityViewModels()

    private lateinit var formGenerator: FormGenerator

    private lateinit var assessmentType: String
    private var isViewOnly: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mentalHealthAssessmentBinding =
            FragmentDialogMentalHealthAssessmentBinding.inflate(inflater, container, false)

        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return mentalHealthAssessmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readArguments()
        initializeView()
        initializeFormGenerator()
        setObserver()
    }

    private fun readArguments() {
        arguments?.let { args ->
            if(args.containsKey(ASSESSMENT_TYPE))
                assessmentType = args.getString(ASSESSMENT_TYPE, "")

            if(args.containsKey(IS_VIEW_ONLY))
                isViewOnly = args.getBoolean(IS_VIEW_ONLY, false)
        }
    }

    private fun initializeView() {
        mentalHealthAssessmentBinding.clTitleCard.ivClose.safeClickListener(this)
        mentalHealthAssessmentBinding.clTitleCard.titleView.text =
            "${getDisplayText(assessmentType)} Assessment"
    }

    private fun getDisplayText(assessmentType: String): String {
        return when (assessmentType) {
            DefinedParams.PHQ9 -> getString(R.string.phq9_display)
            DefinedParams.PHQ4 -> getString(R.string.phq4_display)
            DefinedParams.GAD7 -> getString(R.string.gad7_display)
            else -> assessmentType
        }
    }

    private fun initializeFormGenerator() {
        formGenerator =
            FormGenerator(
                requireContext(),
                mentalHealthAssessmentBinding.llAssessments,
                resultLauncher,
                this,
                mentalHealthAssessmentBinding.nslAssessments,
                SecuredPreference.getIsTranslationEnabled()
            )
        populateViews()
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let { resultIntent ->
                    formGenerator.onSpinnerActivityResult(resultIntent)
                }
            }
        }

    private fun setObserver() {
        viewModel.mentalHealthQuestions.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    if (isViewOnly) {
                        val request = HashMap<String, Any>()
                        request[DefinedParams.PatientTrackId] = viewModel.patientTrackId
                        request[DefinedParams.Type] = assessmentType
                        viewModel.getMentalHealthDetails(requireContext(),request)
                    } else {
                        formGenerator.loadMentalHealthQuestions(
                            viewModel.mentalHealthQuestions.value?.data,
                            mentalHealthEditList = null
                        )
                    }
                }
            }
        }

        viewModel.mentalHealthCreateResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState?.message?.let { message ->
                        (activity as BaseActivity).showErrorDialogue(getString(R.string.error),message, false){}
                        viewModel.mentalHealthCreateResponse.postError(null)
                    }
                }
                ResourceState.SUCCESS -> {
                    viewModel.mentalHealthCreateResponse.value = Resource(ResourceState.ERROR)
                    dismiss()
                }
            }
        }

        viewModel.mentalHealthDetails.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    loadQuestions(null)
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadQuestions(resourceState)
                }
            }
        }
    }

    private fun loadQuestions(resourceState: Resource<HashMap<String, Any>>?) {
        var editList: ArrayList<Map<String, Any>>? = null
        resourceState?.data?.let { map ->
            val key = CommonUtils.mentalHealthKey(assessmentType)
            if (map.containsKey(key))
                editList = map[key] as ArrayList<Map<String, Any>>
        }
        formGenerator.loadMentalHealthQuestions(
            viewModel.mentalHealthQuestions.value?.data,
            mentalHealthEditList = editList,
            isViewOnly = isViewOnly
        )
    }

    private fun populateViews() {
        val lists = ArrayList<FormLayout>()

        lists.add(
            FormLayout(
                ViewType.VIEW_TYPE_FORM_TEXTLABEL,
                id = DefinedParams.Health_Text,
                title = getString(R.string.mental_health_title),
                visibility = DefinedParams.VISIBLE,
                optionsList = null,
            )
        )

        lists.add(
            FormLayout(
                ViewType.VIEW_TYPE_METAL_HEALTH,
                id = getAssessmentId(),
                title = getString(R.string.mental_health),
                visibility = DefinedParams.VISIBLE,
                isMandatory = true,
                optionsList = null,
                localDataCache = assessmentType
            )
        )

        if (!isViewOnly) {
            lists.add(
                FormLayout(
                    ViewType.VIEW_TYPE_FORM_BUTTON,
                    id = DefinedParams.btnSubmit,
                    title = getString(R.string.fp_submit),
                    visibility = DefinedParams.VISIBLE,
                    optionsList = null,
                    action = DefinedParams.ACTION_FORM_SUBMISSION
                )
            )
        }
        formGenerator.populateViews(lists)
    }

    private fun getAssessmentId(): String {
        var assessmentId = ""
        when (assessmentType) {
            DefinedParams.PHQ4 -> assessmentId = DefinedParams.PHQ4_Mental_Health
            DefinedParams.PHQ9 -> assessmentId = DefinedParams.PHQ9_Mental_Health
            DefinedParams.GAD7 -> assessmentId = DefinedParams.GAD7_Mental_Health
        }
        return assessmentId
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivClose -> {
                this.dismiss()
            }
        }
    }

    fun showLoading() {
        mentalHealthAssessmentBinding.loadingProgress.visibility = View.VISIBLE
    }

    fun hideLoading() {
        mentalHealthAssessmentBinding.loadingProgress.visibility = View.GONE
    }

    override fun onFormSubmit(resultHashMap: HashMap<String, Any>, serverData: List<FormLayout?>?) {
        val resultMap = HashMap(resultHashMap)
        CommonUtils.calculatePHQScore(resultMap, type = assessmentType)
        SecuredPreference.getSelectedSiteEntity()?.tenantId?.let {
            resultMap[DefinedParams.TenantId] = it
        }
        resultMap[DefinedParams.PatientTrackId] = viewModel.patientTrackId
        val id = viewModel.mentalHealthDetails.value?.data?.let { detailsMap ->
            if (detailsMap.containsKey(DefinedParams.ID)) {
                val value = (detailsMap[DefinedParams.ID] as Double?)?.toLong()
                resultMap[DefinedParams.ID] = value
            }

            val key =
                if (assessmentType == DefinedParams.PHQ9) DefinedParams.PHQ9_Mental_Health else null
            if (key != null && detailsMap.containsKey(key)) {
                val map = detailsMap[key] as List<*>
                if (!map.isEmpty())
                    return@let key
            }

            return@let null
        }
        viewModel.createOrUpdateMentalHealth(
            requireContext(),
            resultMap,
            isUpdate = id != null
        )
    }

    override fun onBPInstructionClicked(
        title: String,
        informationList: ArrayList<String>,
        description: String?
    ) {
        /**
         * this method is not used
         */
    }

    override fun onRenderingComplete() {
        /**
         * this method is not used
         */
    }

    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        if (localDataCache is String) {
            when (localDataCache) {
                DefinedParams.PHQ4, DefinedParams.PHQ9, DefinedParams.GAD7 -> {
                    viewModel.fetchMentalHealthQuestions(id, localDataCache)
                }
            }
        }
    }

    override fun onFormError() {
        /**
         * this method is not used
         */
    }

    override fun onJsonMismatchError() {
        /**
         * this method is not used
         */
    }
}