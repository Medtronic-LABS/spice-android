package com.medtroniclabs.opensource.ui.boarding

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.hideKeyboard
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.DeviceInformation.getDeviceDetails
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.common.Validator
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.databinding.ActivityLoginBinding
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.boarding.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setMainContentView(binding.root)
        initView()
        setListeners()
        attachObserver()
        checkDeviceDetails()
        disableCopyPaste()
    }


    private fun disableCopyPaste() {
        binding.password.isLongClickable = false
        binding.userName.isLongClickable = false
        binding.tlUserName.isLongClickable = false
        binding.passwordLayout.isLongClickable = false

        binding.password.customSelectionActionModeCallback =
            object : android.view.ActionMode.Callback {
                override fun onActionItemClicked(
                    p0: android.view.ActionMode?,
                    p1: MenuItem?
                ): Boolean {
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
        binding.userName.customSelectionActionModeCallback =
            object : android.view.ActionMode.Callback {
                override fun onActionItemClicked(
                    p0: android.view.ActionMode?,
                    p1: MenuItem?
                ): Boolean {
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

    private fun initView() {
        binding.tvUserNameLabel.markMandatory()
        binding.tvPasswordLabel.markMandatory()
    }

    private fun attachObserver() {
        viewModel.loginResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.errorCardMessage.visibility = View.GONE
                    showLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    binding.errorCardMessage.visibility = View.GONE
                    handleLoginSuccess()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        binding.errorCardMessage.visibility = View.VISIBLE
                        binding.tvErrorMessage.text = it
                    }
                }
            }
        }

        viewModel.noInternetResponse.observe(this) { internetCheck ->
            if (internetCheck) {
                showErrorDialogue(
                    getString(R.string.alert),
                    message = getString(R.string.offline_login_message),
                    isNegativeButtonNeed = true
                ) { buttonState ->
                    if (buttonState) {
                        handleOfflineLoginSuccess()
                    }
                }
            }
        }

        viewModel.appUpdateDiaglogue.observe(this) { message ->
            if (message != null && message.isNotBlank()) {
                broadCastIntent(message)
            }
        }
    }

    private fun broadCastIntent(errorMessage: String) {
        val intent = Intent(IntentConstants.ACTION_UPDATE_APP)
        intent.putExtra(IntentConstants.UPDATE_REQUIRED, true)
        intent.putExtra(IntentConstants.INTENT_MESSAGE, errorMessage)
        LocalBroadcastManager.getInstance(this@LoginActivity).sendBroadcast(intent)
    }

    private fun handleOfflineLoginSuccess() {
        SecuredPreference.putBoolean(
            SecuredPreference.EnvironmentKey.ISOFFLINELOGIN.name,
            true
        )
        startAsNewActivity(
            Intent(
                this@LoginActivity,
                ResourceLoadingScreen::class.java
            )
        )
    }

    private fun handleLoginSuccess() {
        startAsNewActivity(
            Intent(
                this@LoginActivity,
                ResourceLoadingScreen::class.java
            )
        )
    }

    /**
     * attach listener to view
     */
    private fun setListeners() {
        binding.btnLogin.safeClickListener(this)
        binding.tvForgotPassword.safeClickListener(this)
        binding.ivClose.safeClickListener(this)
    }

    /**
     * listener for view on click events
     * @param view respected view clicked
     */
    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnLogin -> {
                hideKeyboard(view)
                validateLoginInputs()
            }
            R.id.tvForgotPassword -> {
                startActivity(
                    Intent(
                        this@LoginActivity,
                        ForgotPasswordActivity::class.java
                    )
                )
            }

            R.id.ivClose -> {
                binding.errorCardMessage.visibility = View.GONE
            }
        }
    }

    /**
     * method to validate the user inputs
     * show appropriate message
     */
    private fun validateLoginInputs() {
        val userName = binding.userName.text
        val password = binding.password.text
        if (userName.isNullOrBlank()) {
            binding.tvUserEmailError.visibility = View.VISIBLE
            binding.tvUserEmailError.text = getString(R.string.email_cannot_be_empty)
        } else if (!Validator.isEmailValid(userName.toString())) {
            binding.tvUserEmailError.visibility = View.VISIBLE
            binding.tvUserEmailError.text = getString(R.string.email_invalid)
        } else if (password.isNullOrBlank()) {
            binding.tvUserEmailError.visibility = View.GONE
            binding.tvUserPasswordError.visibility = View.VISIBLE
            binding.tvUserPasswordError.text = getString(R.string.password_cannot_be_empty)
        } else {
            binding.tvUserEmailError.visibility = View.GONE
            binding.tvUserPasswordError.visibility = View.GONE
            viewModel.doLogin(
                this,
                userName.toString(),
                password.toString(),
                getDeviceDetails(this)
            )
        }
    }

}