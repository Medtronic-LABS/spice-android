package com.medtroniclabs.opensource.formgeneration.listener

import com.medtroniclabs.opensource.formgeneration.model.FormLayout

interface FormEventListener {
    fun onFormSubmit(resultHashMap: HashMap<String, Any>, serverData: List<FormLayout?>? = null)
    fun onBPInstructionClicked(
        title: String,
        informationList: ArrayList<String>,
        description: String? = null
    )
    fun onRenderingComplete()
    fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long? = null)
    fun onFormError()
    fun onJsonMismatchError()
}