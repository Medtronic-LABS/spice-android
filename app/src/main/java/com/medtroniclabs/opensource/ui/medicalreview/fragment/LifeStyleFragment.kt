package com.medtroniclabs.opensource.ui.medicalreview.fragment

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.data.model.PatientLifeStyle
import com.medtroniclabs.opensource.data.model.PatientLifeStyleRequest
import com.medtroniclabs.opensource.databinding.FragmentLifeStyleBinding
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseFragment
import com.medtroniclabs.opensource.ui.medicalreview.LifeStyleConstants
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.LifeStyleViewModel
import com.medtroniclabs.opensource.ui.medicalreview.viewmodel.PatientDetailViewModel

class LifeStyleFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentLifeStyleBinding

    private var popupWindow: PopupWindow? = null

    private val viewModel: LifeStyleViewModel by activityViewModels()

    private val patientViewModel: PatientDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLifeStyleBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachObserver()
        binding.ivRefresh.safeClickListener(this)
        binding.tvDietNutrition.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.tvDietNutrition.safeClickListener(this)
        binding.tvSmoking.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.tvSmoking.safeClickListener(this)
        binding.tvAlcohol.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.tvAlcohol.safeClickListener(this)
    }

    private fun apiCalls(data: PatientDetailsModel?) {
        data?.let { patient ->
            SecuredPreference.getSelectedSiteEntity()?.let { siteEntity ->
                viewModel.getPatientLifeStyleDetails(
                    PatientLifeStyleRequest(
                        patient._id,
                        siteEntity.tenantId
                    )
                )
            }
        }
    }

    private fun attachObserver() {
        patientViewModel.patientDetailsResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.CenterProgress.visibility = View.VISIBLE
                }
                ResourceState.SUCCESS -> {
                    apiCalls(resourceState.data)
                }
                ResourceState.ERROR -> {
                    binding.CenterProgress.visibility = View.GONE
                }
            }
        }

        viewModel.patientLifeStyleResponse.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.CenterProgress.visibility = View.VISIBLE
                    binding.clLifeStyle.visibility = View.GONE
                    binding.ivRefresh.visibility = View.GONE
                }
                ResourceState.ERROR -> {
                    binding.CenterProgress.visibility = View.GONE
                    binding.clLifeStyle.visibility = View.GONE
                    binding.ivRefresh.visibility = View.VISIBLE
                }
                ResourceState.SUCCESS -> {
                    binding.ivRefresh.visibility = View.GONE
                    binding.CenterProgress.visibility = View.GONE
                    binding.clLifeStyle.visibility = View.VISIBLE
                    resourceState.data?.let {
                        loadLifeStyleData(it)
                    }
                }
            }
        }
    }

    private fun loadLifeStyleData(it: ArrayList<PatientLifeStyle>) {
        it.forEachIndexed { _, lifeStyle ->
            val answer: String =
                if (lifeStyle.lifestyleAnswer.isNullOrBlank()) getString(R.string.separator_hyphen) else lifeStyle.lifestyleAnswer
                    ?: ""

            binding.apply {
                when (lifeStyle.lifestyleType) {
                    LifeStyleConstants.SMOKING -> {
                        val s = getString(R.string.lifestyle_smoking).replace(
                            getString(R.string.separator_hyphen),
                            answer
                        )
                        tvSmoking.text = s
                    }
                    LifeStyleConstants.ALCOHOL -> {
                        val s = getString(R.string.lifestyle_alcohol).replace(
                            getString(R.string.separator_hyphen),
                            answer
                        )
                        tvAlcohol.text = s
                    }
                    LifeStyleConstants.DIET_NUTRITION -> {
                        val s = getString(R.string.lifestyle_diet_nutrition).replace(
                            getString(R.string.separator_hyphen),
                            answer
                        )
                        tvDietNutrition.text = s
                    }
                    LifeStyleConstants.PHYSICAL_ACTIVITY -> {
                        val s = getString(R.string.lifestyle_physical_activity).replace(
                            getString(R.string.separator_hyphen),
                            answer
                        )
                        tvPhysicalActivity.text = s
                    }
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivRefresh -> {
                patientViewModel.patientDetailsResponse.value?.data?.let { patient ->
                    SecuredPreference.getSelectedSiteEntity()?.let { siteEntity ->
                        viewModel.getPatientLifeStyleDetails(
                            PatientLifeStyleRequest(
                                patient._id,
                                siteEntity.tenantId
                            )
                        )
                    }
                }
            }
            binding.tvDietNutrition.id -> {
                showPopup(
                    binding.tvDietNutrition,
                    LifeStyleConstants.DIET_NUTRITION
                )
            }
            binding.tvSmoking.id -> {
                showPopup(binding.tvSmoking, LifeStyleConstants.SMOKING)
            }
            binding.tvAlcohol.id -> {
                showPopup(binding.tvAlcohol, LifeStyleConstants.ALCOHOL)
            }
        }
    }

    private fun showPopup(popUpView: View, type: String) {
        viewModel.patientLifeStyleResponse.value?.data?.let { list ->
            if (list.isNotEmpty()) {
                list.firstOrNull { it.lifestyleType == type }?.let {
                    val inflater =
                        requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val layout =
                        inflater.inflate(R.layout.layout_patient_lifestyle_details, null)
                    popupWindow = PopupWindow(requireContext())
                    popupWindow?.apply {
                        contentView = layout
                        width = LinearLayout.LayoutParams.WRAP_CONTENT
                        height = LinearLayout.LayoutParams.WRAP_CONTENT
                        isFocusable = true
                        isOutsideTouchable = true
                        setBackgroundDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.bg_popup_window
                            )
                        )
                    }
                    val tvPatientLifeStyleTitle =
                        layout.findViewById<TextView>(R.id.tvPatientLifestyleTitle)
                    val tvPatientLifestyleDesc =
                        layout.findViewById<TextView>(R.id.tvPatientLifestyleDesc)
                    tvPatientLifeStyleTitle.text =
                        getTitleText(type) ?: getString(R.string.separator_hyphen)
                    tvPatientLifestyleDesc.text = it.comments
                    val size = Size(
                        popupWindow?.contentView?.measuredWidth!!,
                        popupWindow?.contentView?.measuredHeight!!
                    )
                    val location = IntArray(2)
                    popUpView.getLocationOnScreen(location)
                    if (!it.comments.isNullOrBlank()) {
                        popupWindow?.showAtLocation(
                            popUpView,
                            Gravity.TOP or Gravity.START,
                            location[0] - -(size.width - popUpView.width) / 2,
                            location[1] - size.height
                        )
                    }
                }
            }
        }
    }

    private fun getTitleText(type: String): String? {
        when (type) {
            LifeStyleConstants.DIET_NUTRITION -> return getString(R.string.nutrition_comment_title)

            LifeStyleConstants.SMOKING -> return getString(R.string.smoking_comments)

            LifeStyleConstants.ALCOHOL -> return getString(R.string.alcohol_comments)
        }

        return null
    }
}