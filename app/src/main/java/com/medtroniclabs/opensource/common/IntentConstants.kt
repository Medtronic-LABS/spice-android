package com.medtroniclabs.opensource.common

import android.provider.ContactsContract

object IntentConstants {
    const val IntentSiteList = "site_list"
    const val IntentSelectedSite = "selected_site"
    const val IntentEnrollment = "enrollment_workflow"
    const val isFromDirectEnrollment = "isFromDirectEnrollment"
    const val IntentSiteChangeValue = "siteChangeValue"
    const val IntentStatsFromSummary = "statsFromSummary"
    const val IntentPatientID = "selectedPatientID"
    const val IntentVisitId = "patientVisitId"
    const val IntentPatientInitial = "intentPatientInitial"
    const val INTENT_SPINNER_ITEMS = "intent_spinner_items"
    const val INTENT_SPINNER_SELECTED_ITEM = "intent_spinner_selected_item"
    const val INTENT_ID = "intent_id"
    const val INTENT_SPINNER_LABEL = "intent_spinner_label"
    const val ACTION_SESSION_EXPIRED =
        "${ContactsContract.Directory.PACKAGE_NAME}.action.NCD_SESSION_EXPIRED"
    const val ACTION_UPDATE_APP =
        "${ContactsContract.Directory.PACKAGE_NAME}.action.NCD_UPDATE_APP"
    const val NCD_SESSION = "ncd_session"
    const val INTENT_FORM_ID = "intent_form_id"
    const val IntentPatientDetails = "selectedPatientDetails"
    const val ShowContinuousMedicalReview = "showContinuousMedicalReview"
    const val isFromCMR = "is_from_cmr"
    const val isFromSummaryPage = "is_from_summary_page"
    const val graphDetails = "history_graph_details"
    const val isMedicalReviewSummary = "is_medical_review_summary"
    const val IntentScreening = "screening_workflow"

    // crashlytics related customKeys
    const val UserName = "User name"
    const val Country = "Country"
    const val TenantID = "Tenant ID"
    const val UserID = "User ID"

    const val APP_UPDATE_REQUEST_CODE = 258
    const val UPDATE_REQUIRED = "update_required"
    const val INTENT_MESSAGE = "inten_message"

    const val CulturesList = "cutures_list"
}