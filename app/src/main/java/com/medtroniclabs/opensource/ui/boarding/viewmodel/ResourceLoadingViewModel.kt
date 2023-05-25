package com.medtroniclabs.opensource.ui.boarding.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.MedicalReviewStaticResponse
import com.medtroniclabs.opensource.data.model.RiskClassificationModel
import com.medtroniclabs.opensource.db.tables.*
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.network.NetworkConstants
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.boarding.repo.OnBoardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import javax.inject.Inject


@HiltViewModel
class ResourceLoadingViewModel @Inject constructor(
    private val onBoardingRepo: OnBoardingRepository,
    private val connectivityManager: ConnectivityManager,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) :
    ViewModel() {

    private val translationComplete = MutableLiveData<Boolean>()

    val metaDataComplete = MutableLiveData<Resource<Boolean>>()
    val medicalReviewStaticDataResponse = MutableLiveData<Resource<MedicalReviewStaticResponse>>()
    val siteListLiveData = MutableLiveData<Resource<List<SiteEntity>>>()
    val siteStoredLiveData = MutableLiveData<Boolean>()

    init {
        getMetaDataInformation()
    }

    fun getMetaDataInformation() {
        viewModelScope.launch(dispatcherIO) {
            when {
                connectivityManager.isNetworkAvailable() -> {
                    metaDataComplete.postLoading()
                    try {
                        val response = onBoardingRepo.getMetaDataResponse(SecuredPreference.getCultureId())
                        if (response.isSuccessful && response.body()?.status == true) {
                            val responseBody = response.body()?.entity
                            onBoardingRepo.deleteConsentEntity()
                            onBoardingRepo.deleteFormEntity()
                            responseBody?.dosageFrequency?.let { frequency ->
                                onBoardingRepo.deleteFrequency()
                                onBoardingRepo.saveFrequency(frequency)
                            }
                            responseBody?.screening?.let { screening ->
                                screening.consentForm.let { consentHTMLRawString ->
                                    val consentEntity = ConsentEntity(
                                        formType = UIConstants.screeningUniqueID,
                                        consentHtmlRaw = consentHTMLRawString,
                                        userId = SecuredPreference.getUserId()
                                    )
                                    onBoardingRepo.saveConsentEntity(consentEntity)
                                }
                                screening.inputForm.let { screeningForm ->
                                    onBoardingRepo.saveFormEntity(
                                        FormEntity(
                                            formType = UIConstants.screeningUniqueID,
                                            formString = screeningForm,
                                            userId = SecuredPreference.getUserId()
                                        )
                                    )
                                }
                            }
                            responseBody?.enrollment?.let { enrollment ->
                                onBoardingRepo.saveConsentEntity(
                                    ConsentEntity(
                                        formType = UIConstants.enrollmentUniqueID,
                                        consentHtmlRaw = enrollment.consentForm,
                                        userId = SecuredPreference.getUserId()
                                    )
                                )
                                onBoardingRepo.saveFormEntity(
                                    FormEntity(
                                        formType = UIConstants.enrollmentUniqueID,
                                        formString = enrollment.inputForm,
                                        userId = SecuredPreference.getUserId()
                                    )
                                )
                            }
                            responseBody?.assessment?.let { assessment ->
                                onBoardingRepo.saveConsentEntity(
                                    ConsentEntity(
                                        formType = UIConstants.AssessmentUniqueID,
                                        consentHtmlRaw = assessment.consentForm,
                                        userId = SecuredPreference.getUserId()
                                    )
                                )
                                onBoardingRepo.saveFormEntity(
                                    FormEntity(
                                        formType = UIConstants.AssessmentUniqueID,
                                        formString = assessment.inputForm,
                                        userId = SecuredPreference.getUserId()
                                    )
                                )
                            }
                            onBoardingRepo.deleteRiskFactor()
                            responseBody?.cvdRiskAlgorithm?.let { cvdRiskAlgorithm ->
                                cvdRiskAlgorithm.nonLab?.let {
                                    val baseType: Type =
                                        object :
                                            TypeToken<ArrayList<RiskClassificationModel>>() {}.type
                                    val resultString = Gson().toJson(
                                        it,
                                        baseType
                                    )
                                    onBoardingRepo.insertRiskFactor(
                                        RiskFactorEntity(
                                            nonLabEntity = resultString,
                                            id = 1
                                        )
                                    )
                                }
                            }
                            onBoardingRepo.deleteSiteCache()
                            val accountSiteList = ArrayList<SiteEntity>()
                            responseBody?.operatingSites?.forEach { accountSiteResponse ->
                                accountSiteResponse.name?.let {
                                    accountSiteList.add(
                                        SiteEntity(
                                            id = accountSiteResponse._id,
                                            name = it,
                                            userSite = false,
                                            userId = SecuredPreference.getUserId(),
                                            tenantId = accountSiteResponse.tenantId
                                        )
                                    )
                                }
                            }
                            onBoardingRepo.saveSiteEntities(accountSiteList)
                            val list = ArrayList<SiteEntity>()
                            responseBody?.sites?.forEach { siteResponse ->
                                siteResponse.name?.let {
                                    if (siteResponse.roleName.isNotEmpty()
                                        && (siteResponse.roleDisplayName?.isNotEmpty() == true)
                                    ) {
                                        list.add(
                                            SiteEntity(
                                                id = siteResponse._id,
                                                name = it,
                                                userSite = true,
                                                role = siteResponse.roleName[0],
                                                roleName = siteResponse.roleDisplayName[0],
                                                userId = SecuredPreference.getUserId(),
                                                tenantId = siteResponse.tenantId,
                                                isDefault = siteResponse._id == responseBody.defaultSite._id,
                                                isQualipharmEnabledSite = siteResponse.isQualipharmEnabledSite
                                            )
                                        )
                                    }
                                }
                            }
                            onBoardingRepo.saveSiteEntities(list)
                            onBoardingRepo.deleteAllMenu()
                            val menuList: ArrayList<MenuEntity> = ArrayList()
                            responseBody?.menus?.forEach { menuObject ->
                                menuObject.menus.keys.forEach { key ->
                                    val menuTitle = menuObject.menus[key]
                                    val menuCultureValue = menuObject.cultureValues[key]
                                    menuTitle?.let {
                                        menuList.add(
                                            MenuEntity(
                                                id = 0,
                                                name = menuTitle,
                                                role = menuObject.roleName,
                                                userId = SecuredPreference.getUserId(),
                                                menuId = key,
                                                cultureValue = menuCultureValue
                                            )
                                        )
                                    }
                                }
                            }
                            onBoardingRepo.saveMenuList(menuList)
                            responseBody?.countries?.let {
                                onBoardingRepo.deleteCountryList()
                                onBoardingRepo.saveCountryList(it)
                            }
                            responseBody?.counties?.let {
                                onBoardingRepo.deleteCountyList()
                                onBoardingRepo.saveCountyList(it)
                            }
                            responseBody?.subCounties?.let {
                                onBoardingRepo.deleteSubCountyList()
                                onBoardingRepo.saveSubCountyList(it)
                            }
                            responseBody?.symptoms?.let {
                                onBoardingRepo.deleteSymptoms()
                                onBoardingRepo.saveSymptoms(it)
                            }
                            responseBody?.medicalCompliances?.let {
                                onBoardingRepo.deleteMedicalCompliance()
                                onBoardingRepo.saveMedicalCompliance(it)
                            }
                            responseBody?.mentalHealth?.let {
                                val mentalHealthList = ArrayList<MentalHealthEntity>()
                                it.forEach { listItem ->
                                    mentalHealthList.add(
                                        MentalHealthEntity(
                                            formType = listItem.type,
                                            formString = listItem.questions,
                                            userId = SecuredPreference.getUserId()
                                        )
                                    )
                                }
                                checkMentalHealth(mentalHealthList)
                            } ?: kotlin.run {
                                onBoardingRepo.deleteMHList()
                            }
                            responseBody?.programs?.let {
                                onBoardingRepo.deleteProgramList()
                                onBoardingRepo.saveProgramList(it)
                            }
                            onBoardingRepo.deleteDiagnosis()
                            responseBody?.diagnosis?.let {
                                onBoardingRepo.saveDiagnosis(it)
                            }
                            onBoardingRepo.deleteShortageReason()
                            responseBody?.reason?.let {
                                onBoardingRepo.saveShortageReason(it)
                            }
                            onBoardingRepo.deleteUnitList()
                            responseBody?.units?.let {
                                onBoardingRepo.saveUnitList(it)
                            }
                            SecuredPreference.saveClinicalWorkflow(responseBody?.clinicalWorkflow)
                            responseBody?.countries?.let { countriesList ->
                                updateCountries(countriesList)
                            }
                            responseBody?.account?.let { account ->
                                SecuredPreference.putLong(SecuredPreference.EnvironmentKey.ACCOUNT.name,account)
                            }
                            responseBody?.operatingUnit?.toString()?.let { operatingUnit ->
                                SecuredPreference.putString(SecuredPreference.EnvironmentKey.OPERATING_UNIT.name,operatingUnit)
                            }
                            responseBody?.nutritionLifestyle?.let { nutritionList ->
                                onBoardingRepo.saveNutritionLifeStyle(nutritionList)
                            }
                            responseBody?.customizedWorkflows?.let { customizedWorkflows ->
                                if (customizedWorkflows.isNotEmpty()) {
                                    val moduleString = Gson().toJson(customizedWorkflows)
                                    if (!(moduleString.isNullOrEmpty() || moduleString.isBlank()))
                                        onBoardingRepo.saveFormEntity(
                                            FormEntity(
                                                formType = DefinedParams.Workflow,
                                                formString = moduleString,
                                                userId = SecuredPreference.getUserId()
                                            )
                                        )
                                }
                            }
                            responseBody?.cultures?.let { cultureList ->
                                onBoardingRepo.deleteCulturesList()
                                onBoardingRepo.saveCulturesList(cultureList)
                                val currentLocaleId = SecuredPreference.getCultureId()
                                for (i in 0 until cultureList.size) {
                                    if(currentLocaleId <= 0)
                                    {
                                        if(cultureList[i].name.contains(DefinedParams.EN_Locale, ignoreCase = true))
                                        {
                                            SecuredPreference.setUserPreference(cultureList[i].id, cultureList[i].name, false)
                                            break
                                        }
                                    }
                                    else if (cultureList[i].id == currentLocaleId) {
                                        val isEnabled = CommonUtils.checkIfTranslationEnabled(cultureList[i].name)
                                        SecuredPreference.setUserPreference(cultureList[i].id, cultureList[i].name, isEnabled)
                                        break
                                    }
                                }
                            }
                            metaDataComplete.postSuccess()
                        } else {
                            metaDataComplete.postError()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        metaDataComplete.postError()
                    }
                }
                SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.ISOFFLINELOGIN.name) -> {
                    metaDataComplete.postSuccess()
                }
                else -> {
                    metaDataComplete.postError(NetworkConstants.NETWORK_ERROR)
                }
            }
        }
    }

    private fun updateCountries(countriesList: java.util.ArrayList<CountryEntity>) {
        if (countriesList.isNotEmpty()) {
            var measurementType = DefinedParams.Unit_Measurement_Metric_Type
            countriesList[0].unitMeasurement?.let {
                measurementType = it
            }
            SecuredPreference.putString(
                SecuredPreference.EnvironmentKey.MEASUREMENT_TYPE_KEY.name,
                measurementType
            )
        }
    }

    private suspend fun checkMentalHealth(mentalHealthList: java.util.ArrayList<MentalHealthEntity>) {
        if (mentalHealthList.isNotEmpty()) {
            onBoardingRepo.deleteMHList()
            onBoardingRepo.insertMHList(list = mentalHealthList)
        } else {
            onBoardingRepo.deleteMHList()
        }
    }

    fun getSiteList(userSite: Boolean) {
        viewModelScope.launch(dispatcherIO) {
            try {
                siteListLiveData.postLoading()
                val siteList = onBoardingRepo.getSiteList(userSite, SecuredPreference.getUserId())
                siteListLiveData.postSuccess(siteList)
            } catch (e: Exception) {
                siteListLiveData.postError()
            }
        }
    }

    fun storeSelectedEntity(siteEntity: SiteEntity) {
        viewModelScope.launch(dispatcherIO) {
            try {
                onBoardingRepo.storeSelectedSiteEntity(siteEntity)
                siteStoredLiveData.postValue(true)
            } catch (e: Exception) {
                siteStoredLiveData.postValue(false)
            }
        }
    }

    fun getMedicalReviewStaticData(cultureId: Long) {
        viewModelScope.launch(dispatcherIO) {
            when {
                connectivityManager.isNetworkAvailable() -> {
                    try {
                        val response = onBoardingRepo.getMedicalReviewStaticData(cultureId)
                        if (response.isSuccessful && response.body()?.status == true) {
                            val data = response.body()?.entity
                            data?.let { medicalReviewStaticData ->
                                onBoardingRepo.deleteComorbidity()
                                onBoardingRepo.saveComorbidity(medicalReviewStaticData.comorbidity)
                                onBoardingRepo.deleteCurrentMedication()
                                onBoardingRepo.saveCurrentMedication(medicalReviewStaticData.currentMedication)
                                onBoardingRepo.deleteComplication()
                                onBoardingRepo.saveComplication(medicalReviewStaticData.complications)
                                onBoardingRepo.deletePhysicalExamination()
                                onBoardingRepo.savePhysicalExamination(medicalReviewStaticData.physicalExamination)
                                onBoardingRepo.deleteCompliants()
                                onBoardingRepo.saveCompliants(medicalReviewStaticData.complaints)
                                onBoardingRepo.deleteLifeStyle()
                                onBoardingRepo.saveLifeStyle(medicalReviewStaticData.lifestyle)
                                onBoardingRepo.deleteTreatmentPlanData()
                                medicalReviewStaticData.treatmentPlanFormData?.let { list ->
                                    list.forms?.forEach { model ->
                                        list.options?.let {
                                            list.frequencyKey = model.frequencyKey
                                            list.labelName = model.labelName
                                            val gson = Gson()
                                            val jsonString = gson.toJson(list)
                                            onBoardingRepo.saveTreatmentPlan(
                                                TreatmentPlanEntity(
                                                    frequencyKey = model.frequencyKey!!,
                                                    value = jsonString,
                                                    userId = SecuredPreference.getUserId()
                                                )
                                            )
                                        }
                                    }
                                }
                                medicalReviewStaticDataResponse.postSuccess(data)
                            } ?: kotlin.run {
                                medicalReviewStaticDataResponse.postError()
                            }
                        } else {
                            medicalReviewStaticDataResponse.postError()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        medicalReviewStaticDataResponse.postError()
                    }
                }
                SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.ISOFFLINELOGIN.name) -> {
                    medicalReviewStaticDataResponse.postSuccess()
                }
                else -> {
                    medicalReviewStaticDataResponse.postError(NetworkConstants.NETWORK_ERROR)
                }
            }
        }
    }

}