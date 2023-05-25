package com.medtroniclabs.opensource.ui.medicalreview

import android.content.Intent
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.data.model.PatientDetails
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.ui.AdvancedSearchActivity
import com.medtroniclabs.opensource.ui.medicalreview.labTechnician.LabTestListActivity

class SearchPatientPortraitActivity : AdvancedSearchActivity() {

    override fun initView() {
        patientListViewModel.spanCount = DefinedParams.span_count_2
    }

    override fun startNewReviewActivity(details: PatientDetails) {
        val intent = Intent(this, LabTestListActivity::class.java)
        intent.putExtra(IntentConstants.IntentPatientID, details.patientID)
        intent.putExtra(IntentConstants.IntentVisitId, details.visitID)
        startActivity(intent)
    }
}