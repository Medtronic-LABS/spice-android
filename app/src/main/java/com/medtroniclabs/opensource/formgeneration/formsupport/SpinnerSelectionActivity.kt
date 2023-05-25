package com.medtroniclabs.opensource.formgeneration.formsupport

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.customSerializableExtra
import com.medtroniclabs.opensource.common.IntentConstants.INTENT_ID
import com.medtroniclabs.opensource.common.IntentConstants.INTENT_SPINNER_ITEMS
import com.medtroniclabs.opensource.common.IntentConstants.INTENT_SPINNER_LABEL
import com.medtroniclabs.opensource.common.IntentConstants.INTENT_SPINNER_SELECTED_ITEM
import com.medtroniclabs.opensource.databinding.ActivitySpinnerSelectionBinding
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.formgeneration.formsupport.adapter.SpinnerSelectionAdapter
import com.medtroniclabs.opensource.formgeneration.formsupport.listener.SpinnerListener
import com.medtroniclabs.opensource.formgeneration.formsupport.viewmodel.SpinnerSelectionViewmodel


class SpinnerSelectionActivity : BaseActivity(), SpinnerListener {

    private lateinit var binding: ActivitySpinnerSelectionBinding

    private val viewModel: SpinnerSelectionViewmodel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpinnerSelectionBinding.inflate(layoutInflater)
        getIntentValues()
        setMainContentView(binding.root, true, viewModel.spinnerTitle?:getString(R.string.choose_option))
    }

    override fun onResume() {
        super.onResume()
        initializeLoadValues()
    }

    private fun initializeLoadValues() {
        binding.rvSpinner.layoutManager = LinearLayoutManager(binding.rvSpinner.context)
        binding.rvSpinner.itemAnimator = DefaultItemAnimator()
        binding.rvSpinner.adapter = SpinnerSelectionAdapter(viewModel.itemList, this,viewModel.spinnerSelectedItemId)
    }

    private fun getIntentValues() {
        viewModel.spinnerElementId = intent.getStringExtra(INTENT_ID)
        (intent.customSerializableExtra(INTENT_SPINNER_ITEMS) as ArrayList<Map<String, Any>>?)?.let { itemList ->
            viewModel.itemList = itemList
        }
        viewModel.spinnerSelectedItemId = intent.getStringExtra(INTENT_SPINNER_SELECTED_ITEM)
        viewModel.spinnerTitle = intent.getStringExtra(INTENT_SPINNER_LABEL)
    }

    override fun onSpinnerItemSelected(item: Map<String, Any>) {
        val intent = Intent()
        val list = ArrayList<Map<String, Any>>()
        list.add(item)
        intent.putExtra(INTENT_SPINNER_SELECTED_ITEM, list)
        intent.putExtra(INTENT_ID, viewModel.spinnerElementId)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


}