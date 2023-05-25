package com.medtroniclabs.opensource.ui.screening.repo

import com.medtroniclabs.opensource.data.screening.SiteDetails
import com.medtroniclabs.opensource.data.ui.PatientBasicRequest
import com.medtroniclabs.opensource.db.local.RoomHelper
import com.medtroniclabs.opensource.db.tables.ScreeningEntity
import com.medtroniclabs.opensource.network.ApiHelper
import javax.inject.Inject

class ScreeningRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {


    suspend fun getFormBasedOnType(formType: String, userId: Long) =
        roomHelper.getFormEntity(formType, userId)

    suspend fun getFormBasedOnType(formTypeOne: String,formTypeTwo: String, userId: Long) =
        roomHelper.getFormEntity(formTypeOne,formTypeTwo,userId)

    suspend fun getConsentHtmlRawString(formType: String, userId: Long) =
        roomHelper.getConsentEntity(formType, userId)

    suspend fun savePatientScreeningInformation(screeningEntity: ScreeningEntity): Long =
        roomHelper.savePatientScreeningInformation(screeningEntity)

    suspend fun riskFactorListing() = roomHelper.getRiskFactorEntity()

    suspend fun saveChosenSiteDetail(siteDetail: SiteDetails) =
        roomHelper.saveChosenSiteDetail(siteDetail)

    suspend fun getScreenedPatientCount(startDate: Long, endDate: Long, userId: Long) =
        roomHelper.getScreenedPatientCount(startDate, endDate, userId)

    suspend fun getScreenedPatientReferredCount(startDate: Long, endDate: Long, userId: Long, isReferred: Boolean) =
        roomHelper.getScreenedPatientReferredCount(startDate, endDate, userId,isReferred)

    suspend fun getAccountSiteList(userId: Long) = roomHelper.getAccountSiteList(userId)

    suspend fun getAllScreeningRecords(uploadStatus: Boolean): List<ScreeningEntity>? =
        roomHelper.getAllScreeningRecords(uploadStatus)

    suspend fun deleteUploadedScreeningRecords(todayDateTimeInMilliSeconds: Long) =
        roomHelper.deleteUploadedScreeningRecords(todayDateTimeInMilliSeconds)

    suspend fun updateScreeningRecordById(id: Long, uploadStatus: Boolean) =
        roomHelper.updateScreeningRecordById(id, uploadStatus)

    suspend fun updateGeneralDetailsById(id: Long, generalDetails: String) =
        roomHelper.updateGeneralDetailsById(id, generalDetails)

    suspend fun getScreeningRecordById(id: Long): ScreeningEntity =
        roomHelper.getScreeningRecordById(id)


    suspend fun getPatientBasicDetail(request: PatientBasicRequest) =
        apiHelper.getPatientBasicDetail(request)

    suspend fun updatePatient(request: HashMap<String, Any>) = apiHelper.updatePatient(request)

}