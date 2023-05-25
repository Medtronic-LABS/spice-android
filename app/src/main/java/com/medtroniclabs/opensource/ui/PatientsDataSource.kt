package com.medtroniclabs.opensource.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.PatientListRespModel
import com.medtroniclabs.opensource.data.model.PatientsDataModel
import com.medtroniclabs.opensource.network.ApiHelper
import com.medtroniclabs.opensource.data.model.APIResponse
import com.medtroniclabs.opensource.ui.medicalreview.GetPatientsCount

private const val PAGE_INDEX = 0
const val LIST_LIMIT = 15

class PatientsDataSource(
    private val isPatientSearch: Boolean,
    private val isSiteBasedSearch: Boolean,
    private val searchModel: PatientsDataModel,
    private val apiHelper: ApiHelper,
    private val getPatientsCount: GetPatientsCount
) :
    PagingSource<Int, PatientListRespModel>() {

    private var loadedCount = 0
    private var totalCount = 0

    override fun getRefreshKey(state: PagingState<Int, PatientListRespModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PatientListRespModel> {
        val pageIndex = params.key ?: PAGE_INDEX
        return try {
            val response: APIResponse<ArrayList<PatientListRespModel>>
            if (isPatientSearch) {
                if (searchModel.searchId.isNullOrBlank()) {
                    //Advanced Search
                    val request = PatientsDataModel(
                        skip = loadedCount,
                        limit = LIST_LIMIT,
                        tenantId = SecuredPreference.getSelectedSiteEntity()?.tenantId,
                        firstName = searchModel.firstName,
                        lastName = searchModel.lastName,
                        phoneNumber = searchModel.phoneNumber,
                        operatingUnitId = searchModel.operatingUnitId,
                        isLabtestReferred = searchModel.isLabtestReferred,
                        isMedicationPrescribed = searchModel.isMedicationPrescribed,
                        patientSort = searchModel.patientSort,
                        patientFilter = searchModel.patientFilter
                    )
                    response =
                        if (isSiteBasedSearch) apiHelper.advancedSearchSite(request) else apiHelper.advancedSearchCountry(
                            request
                        )
                } else {
                    //National or Patient Id Search
                    response = apiHelper.searchPatientById(
                        PatientsDataModel(
                            skip = loadedCount,
                            limit = LIST_LIMIT,
                            tenantId = SecuredPreference.getSelectedSiteEntity()?.tenantId,
                            isSearchUserOrgPatient = isSiteBasedSearch,
                            searchId = searchModel.searchId,
                            operatingUnitId = searchModel.operatingUnitId,
                            isLabtestReferred = searchModel.isLabtestReferred,
                            isMedicationPrescribed = searchModel.isMedicationPrescribed,
                            patientSort = searchModel.patientSort,
                            patientFilter = searchModel.patientFilter
                        )
                    )
                }
            } else {
                //Patient List
                response = apiHelper.patientsList(
                    PatientsDataModel(
                        skip = loadedCount,
                        limit = LIST_LIMIT,
                        tenantId = SecuredPreference.getSelectedSiteEntity()?.tenantId,
                        operatingUnitId = searchModel.operatingUnitId,
                        isLabtestReferred = searchModel.isLabtestReferred,
                        isMedicationPrescribed = searchModel.isMedicationPrescribed,
                        patientSort = searchModel.patientSort,
                        patientFilter = searchModel.patientFilter
                    )
                )
            }
            val patientList: ArrayList<PatientListRespModel> = response.entityList ?: ArrayList()
            response.totalCount?.let { count ->
                totalCount = count
            }
            if (loadedCount == 0)
                getPatientsCount.patientsCount(totalCount.toString())
            loadedCount += patientList.size
            LoadResult.Page(
                data = patientList,
                prevKey = if (pageIndex > PAGE_INDEX) pageIndex - 1 else null,
                nextKey = if (loadedCount < totalCount) pageIndex + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}