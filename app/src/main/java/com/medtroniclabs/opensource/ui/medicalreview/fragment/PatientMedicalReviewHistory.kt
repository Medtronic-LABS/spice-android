package com.medtroniclabs.opensource.ui.medicalreview.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.databinding.MedicalReviewHistoryPatinetFragmentBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.ui.medicalreview.InvestigationFragment
import com.medtroniclabs.opensource.ui.medicalreview.MedicalReviewHistoryFragment
import com.medtroniclabs.opensource.ui.medicalreview.PrescriptionFragment

class PatientMedicalReviewHistory : Fragment() {

    lateinit var binding: MedicalReviewHistoryPatinetFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MedicalReviewHistoryPatinetFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

        val bundle = Bundle().apply {
            putString(AssessmentHistoryFragment.TAG, AssessmentHistoryFragment.BP_TAG)
        }
        replaceFragmentInId<AssessmentHistoryFragment>(
            binding.patientBPHistory.id,
            bundle
        )
        val bgBundle = Bundle().apply {
            putString(AssessmentHistoryFragment.TAG, AssessmentHistoryFragment.BG_TAG)
        }
        replaceFragmentInId<AssessmentHistoryFragment>(
            binding.patientBGHistory.id,
            bgBundle

        )
        val origin = if (arguments?.containsKey(DefinedParams.Origin) == true) arguments?.getString(
            DefinedParams.Origin
        ) else null
        replaceFragmentInId<MedicalReviewHistoryFragment>(binding.patientClinicalReviewHistory.id, bundle = Bundle().apply {
            putBoolean(IntentConstants.isMedicalReviewSummary, true)
            putString(DefinedParams.Origin, origin)
        })
        replaceFragmentInId<PrescriptionFragment>(binding.patientPrescriptionHistory.id)
        replaceFragmentInId<InvestigationFragment>(binding.patientInvestigationHistory.id)
        replaceFragmentInId<LifeStyleFragment>(binding.patientLifeStyleHistory.id)
    }

    private inline fun <reified fragment : Fragment> replaceFragmentInId(
        id: Int? = null,
        bundle: Bundle? = null,
        tag: String? = null
    ) {
        childFragmentManager.commit {
            setReorderingAllowed(true)
            replace<fragment>(
                id ?: binding.patientClinicalReviewHistory.id,
                args = bundle,
                tag = tag
            )
        }
    }

}