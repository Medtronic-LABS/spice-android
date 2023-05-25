package com.medtroniclabs.opensource.ui.medicalreview

import android.os.Bundle
import android.view.ViewGroup
import com.google.android.flexbox.FlexboxLayout
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.databinding.ActivityTreatmentPlanBinding
import com.medtroniclabs.opensource.ui.BaseActivity

class TreatmentPlanActivity : BaseActivity() {

    lateinit var binding: ActivityTreatmentPlanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTreatmentPlanBinding.inflate(layoutInflater)
        setMainContentView(binding.root, true, getString(R.string.treatment_plan))
        initView()

    }

    private fun initView() {
        binding.MedicalReviewFrequency.root.layoutParams = getLayoutParams()
        binding.BPCheckFrequency.root.layoutParams = getLayoutParams()
        binding.BloodGlucoseFrequency.root.layoutParams = getLayoutParams()
        binding.HbA1cFrequency.root.layoutParams = getLayoutParams()
        binding.DiabetesFootFrequency.root.layoutParams = getLayoutParams()
        binding.AntenatalFrequency.root.layoutParams = getLayoutParams()

        binding.MedicalReviewFrequency.tvTitle.text = getString(R.string.medical_review_frequency)
        binding.BPCheckFrequency.tvTitle.text = getString(R.string.bp_check_frequency)
        binding.BloodGlucoseFrequency.tvTitle.text = getString(R.string.blood_glucose_frequency)
        binding.HbA1cFrequency.tvTitle.text = getString(R.string.hba1c_frequency)
        binding.DiabetesFootFrequency.tvTitle.text =
            getString(R.string.diabetic_foot_check_frequency)
        binding.AntenatalFrequency.tvTitle.text = getString(R.string.antenatal_frequency)
    }

    private fun getLayoutParams(): FlexboxLayout.LayoutParams {
        val lp = FlexboxLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.minWidth = resources.getDimension(R.dimen._360sdp).toInt()
        return lp
    }
}