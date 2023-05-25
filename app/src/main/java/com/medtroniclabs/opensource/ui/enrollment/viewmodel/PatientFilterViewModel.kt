package com.medtroniclabs.opensource.ui.enrollment.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.ui.enrollment.repo.PatientFilterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PatientFilterViewModel @Inject constructor(private val patientFilterRepo: PatientFilterRepository) :
    ViewModel() {
    var wardList = MutableLiveData<Resource<ArrayList<Map<String, String>>>>()
    var diagnosisList = MutableLiveData<Resource<ArrayList<Map<String, String>>>>()

    fun getWardList() {
        wardList.postSuccess(patientFilterRepo.getWardsList())
    }

    fun getDiagnosisList() {
        diagnosisList.postSuccess(patientFilterRepo.getDiagnosisList())
    }

}