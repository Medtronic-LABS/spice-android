package com.medtroniclabs.opensource.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DateUtils
import com.medtroniclabs.opensource.databinding.ActivityNutritionReferralBinding

class NutritionReferralActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityNutritionReferralBinding

    private lateinit var nutritionReferralAdapter: NutritionReferralAdapter
    lateinit var tagListCustomView: TagListCustomView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNutritionReferralBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root, true, getString(R.string.nutrition_referral),
            homeAndBackVisibility = Pair(false,true)
        )
        setAdapterViews()
        listeners()

        showNoReferralMessage(true)
    }

    private fun setAdapterViews() {

        tagListCustomView = TagListCustomView(this, binding.cgAssessment!!) { isEmpty ->
            enableRefer(isEmpty)
        }
        // tagListCustomView.addChipItemList(diagnosisList)

        val referralListLayoutManager = LinearLayoutManager(this)
        binding.rvReferrals?.layoutManager = referralListLayoutManager
        nutritionReferralAdapter = NutritionReferralAdapter(arrayListOf()) { isNoReferral ->
            showNoReferralMessage(isNoReferral)
        }
        binding.rvReferrals?.adapter = nutritionReferralAdapter
    }

    private fun listeners() {
        binding.btnRefer?.safeClickListener(this)
    }


    private fun showNoReferralMessage(isNoReferral: Boolean) {
        if (isNoReferral) {
            binding.rvReferrals?.visibility = View.GONE
            binding.tvNoAssessmentLabel?.visibility = View.VISIBLE
        } else {
            binding.rvReferrals?.visibility = View.VISIBLE
            binding.tvNoAssessmentLabel?.visibility = View.GONE
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnRefer -> {
                nutritionReferralAdapter.addReferral(
                    NutritionReferralModel(
                        ArrayList<String>(),
                        DateUtils.convertMilliSecondsToDate(
                            System.currentTimeMillis(),
                            "dd MMM, yyyy"
                        ),
                        getString(R.string.separator_hyphen),
                        getString(R.string.not_yet_done),
                        getString(R.string.separator_hyphen)
                    )
                )
                binding.btnRefer?.isEnabled = false
                showNoReferralMessage(false)
                tagListCustomView.clearSelection()
            }
        }
    }

    private fun enableRefer(isEmptyCheck: Boolean) {
        binding.btnRefer?.isEnabled = !isEmptyCheck
    }
}
