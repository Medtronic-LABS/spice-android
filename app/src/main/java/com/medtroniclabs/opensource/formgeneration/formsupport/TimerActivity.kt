package com.medtroniclabs.opensource.formgeneration.formsupport

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.IntentConstants.INTENT_ID
import com.medtroniclabs.opensource.databinding.ActivityTimerBinding
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.formgeneration.formsupport.viewmodel.TimerScreenViewModel

class TimerActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityTimerBinding

    private val viewModel: TimerScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setMainContentView(binding.root)
        getIntentValues()
        initializeTimerScreenViews()
    }

    private fun getIntentValues() {
        viewModel.elementId = intent.getStringExtra(INTENT_ID)
    }

    private fun initializeTimerScreenViews() {
        binding.tvSkip.safeClickListener(this)
    }
    
    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.tvSkip -> {
                val intent = Intent()
                intent.putExtra(INTENT_ID, viewModel.elementId)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }


}