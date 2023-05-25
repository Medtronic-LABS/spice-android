package com.medtroniclabs.opensource.formgeneration.formsupport.viewmodel

import androidx.lifecycle.ViewModel

class SpinnerSelectionViewmodel : ViewModel() {
    var itemList: ArrayList<Map<String,Any>> = ArrayList()
    var spinnerElementId:String? =null
    var spinnerSelectedItemId:String? =null
    var spinnerTitle:String? = null

}