package com.medtroniclabs.opensource.ui

import androidx.fragment.app.Fragment
import com.medtroniclabs.opensource.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseFragment : Fragment() {

    fun showLoading() {
        (requireActivity() as BaseActivity).showLoading()
    }

    fun hideLoading() {
        (requireActivity() as BaseActivity).hideLoading()
    }

    fun showToolbar(
        title: String? = null,
        isBackVisible: Boolean = false,
        isToolbarVisible: Boolean = false,
        centerTitle: Boolean? = true
    ) {
        (requireActivity() as BaseActivity).setToolBarOptions(
            isToolbarVisible = isToolbarVisible,
            title = title,
            centerTitle = centerTitle,
            isBackVisible = isBackVisible
        )
    }

    fun setTitle(title: String) {
        (requireActivity() as BaseActivity).setTitle(title = title)
    }

    fun hideHomeIcon(status: Boolean) {
        (requireActivity() as BaseActivity).hideHomeButton(status)
    }

    fun showAlertWith(
        message: String,
        isPositiveButtonNeed: Boolean = false,
        positiveButtonName: String = getString(R.string.ok),
        isNegativeButtonNeed: Boolean = false,
        negativeButtonName: String = getString(R.string.cancel),
        okayBtnEnable: Boolean = true,
        callback: ((isPositiveResult: Boolean) -> Unit)
    ) {
        (requireActivity() as BaseActivity).showAlertWith(
            message,
            positiveButtonName = positiveButtonName,
            isNegativeButtonNeed = isNegativeButtonNeed,
            negativeButtonName = negativeButtonName,
            okayBtnEnable = okayBtnEnable,
            callback
        )
    }
}