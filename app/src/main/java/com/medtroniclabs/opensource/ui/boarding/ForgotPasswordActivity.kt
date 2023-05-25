package com.medtroniclabs.opensource.ui.boarding

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.hideKeyboard
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.Validator
import com.medtroniclabs.opensource.databinding.ActivityForgotPasswordBinding
import com.medtroniclabs.opensource.network.NetworkConstants
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.boarding.viewmodel.ForgotPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setMainContentView(binding.root)
        initView()
        setListeners()
        attachObserver()
    }

    private fun initView() {
        binding.appCompatTextView2.markMandatory()

        binding.emailID.isLongClickable = false
        binding.emailID.customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
            override fun onActionItemClicked(p0: android.view.ActionMode?, p1: MenuItem?): Boolean {
                return false
            }

            override fun onCreateActionMode(p0: android.view.ActionMode?, p1: Menu?): Boolean {
                return false
            }

            override fun onPrepareActionMode(p0: android.view.ActionMode?, p1: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(p0: android.view.ActionMode?) {
                /**
                 * this method is not used
                 */
            }
        }
    }

    private fun attachObserver() {
        viewModel.forgotPasswordResponse.observe(this, { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { forgotPasswordResponse ->
                        showErrorDialogue(getString(R.string.success),forgotPasswordResponse.message) { buttonState ->
                            if (buttonState) {
                                startLoginActivity()
                            }
                        }
                    }
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    if (resourceState.message.equals(NetworkConstants.NETWORK_ERROR)) {
                        showErrorDialogue(getString(R.string.error),message = getString(R.string.no_internet_error)) {
                        }
                    }
                }
            }
        })
    }

    /**
     * method to attach listener to view
     */
    private fun setListeners() {
        binding.btnSubmit.safeClickListener(this)
        binding.btnBack2Login.safeClickListener(this)
    }

    /**
     * listener for view on click events
     * @param view respected view clicked
     */
    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnSubmit -> {
                hideKeyboard(view)
                validateForgotPasswordInputs()
            }
            R.id.btnBack2Login -> {
                startLoginActivity()
            }
        }
    }

    private fun startLoginActivity() {
        startAsNewActivity(
            Intent(
                this,
                LoginActivity::class.java
            )
        )
    }

    /**
     * method to validate the user inputs
     * show appropriate message
     */
    private fun validateForgotPasswordInputs() {
        val userName = binding.emailID.text
        if (userName.isNullOrBlank()) {
            binding.tvUserEmailError.visibility = View.VISIBLE
            binding.tvUserEmailError.text = getString(R.string.email_cannot_be_empty)
        } else if (!Validator.isEmailValid(userName.toString())) {
            binding.tvUserEmailError.visibility = View.VISIBLE
            binding.tvUserEmailError.text = getString(R.string.email_invalid)
        } else {
            binding.tvUserEmailError.visibility = View.GONE
            viewModel.resetPasswordFor(userName.toString())
        }
    }

}