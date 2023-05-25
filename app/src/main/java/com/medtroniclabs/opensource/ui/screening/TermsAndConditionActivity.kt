package com.medtroniclabs.opensource.ui.screening

import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.hideKeyboard
import com.medtroniclabs.opensource.appextensions.markMandatory
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.common.ViewUtil.statusCheck
import com.medtroniclabs.opensource.databinding.ActivityTermsAndConditionBinding
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.BaseActivity
import com.medtroniclabs.opensource.ui.enrollment.EnrollmentFormBuilderActivity
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import com.medtroniclabs.opensource.ui.screening.viewmodel.TermsAndConditionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TermsAndConditionActivity : BaseActivity(), View.OnClickListener {

    lateinit var binding: ActivityTermsAndConditionBinding

    private val viewModel: TermsAndConditionViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityTermsAndConditionBinding.inflate(layoutInflater)
        setMainContentView(binding.root, true, getString(R.string.terms_condition), homeAndBackVisibility = Pair(true, null), callbackHome = {
            if (viewModel.isFromScreening) {
                startAsNewActivity(Intent(this, LandingActivity::class.java))
            } else {
                onHomeIconClicked()
            }
        })
        initializeView()
        attachObserver()
    }

    private fun attachObserver() {
        binding.etUserInitial.addTextChangedListener { patientInitial ->
            if (patientInitial.isNullOrBlank()) {
                viewModel.patientInitial.value = null
            } else {
                viewModel.patientInitial.value = patientInitial.trim().toString()
            }
        }
        viewModel.consentDoneLiveDate.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState.data?.let { url ->
                        loadRespectiveWebpage(url)
                    }
                }
                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }

        viewModel.patientInitial.observe(this) { initial ->
            if (viewModel.enrollmentConsent) {
                binding.btnAccept.isEnabled = !initial.isNullOrBlank()
            }
        }
    }

    private fun loadRespectiveWebpage(url: String) {
        binding.termsConditionWebView.loadDataWithBaseURL(null, url, "text/html", "utf-8", null)
    }

    private fun initializeView() {
        viewModel.isFromScreening =
            intent.getBooleanExtra(IntentConstants.IntentScreening, false)
        viewModel.enrollmentConsent =
            intent.getBooleanExtra(IntentConstants.IntentEnrollment, false)
        viewModel.isFromSummaryPage =
            intent.getBooleanExtra(IntentConstants.isFromSummaryPage, false)
        viewModel.isFromDirectEnrollment =
            intent.getBooleanExtra(IntentConstants.isFromDirectEnrollment, false)
        binding.tvTitle.markMandatory()
        if (viewModel.enrollmentConsent) {
            binding.tvTitle.visibility = View.VISIBLE
            binding.btnAccept.isEnabled = false
            binding.etUserInitial.visibility = View.VISIBLE
            binding.tvTermsAndConditionInfo.text =
                getString(R.string.terms_condition_info_enrollment)
        } else {
            binding.tvTitle.visibility = View.GONE
            binding.btnAccept.isEnabled = true
            binding.etUserInitial.visibility = View.GONE
            binding.tvTermsAndConditionInfo.text =
                getString(R.string.terms_condition_info_screening)
        }
        binding.termsConditionWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {

                request?.url?.let { emailUri ->
                    if (emailUri.toString().startsWith("mailto")) {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, emailUri))
                            return true
                        } catch (e: Exception) {
                            return false
                        }
                    } else {
                        showLoading()
                        view?.loadUrl(emailUri.toString())
                    }
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                showLoading()
            }


            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideLoading()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                hideLoading()
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                hideLoading()
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                hideLoading()
            }
        }
        binding.termsConditionWebView.settings.javaScriptEnabled = true
        viewModel.fetchConsentRawHTML()
        binding.btnAccept.safeClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnAccept -> {
                binding.btnAccept.isEnabled = false
                hideKeyboard(view)
                if (viewModel.enrollmentConsent) {
                    startActivity(
                        Intent(
                            this@TermsAndConditionActivity,
                            EnrollmentFormBuilderActivity::class.java
                        ).apply {
                            putExtra(DefinedParams.Patient_Id, intent.getLongExtra(DefinedParams.Patient_Id, -1L))
                            putExtra(IntentConstants.IntentPatientInitial, binding.etUserInitial.text?.toString())
                            putExtra(IntentConstants.isFromDirectEnrollment, viewModel.isFromDirectEnrollment)
                            putExtra(DefinedParams.Screening_Id,intent.getLongExtra(DefinedParams.Screening_Id, -1L))
                        }
                    )

                } else {
                    startActivity(
                        Intent(
                            this@TermsAndConditionActivity,
                            ScreeningFormBuilderActivity::class.java
                        )
                    )
                }
                binding.btnAccept.isEnabled = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        statusCheck(this)
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            backHandelFlow()
        }
    }

    private fun backHandelFlow() {
        if (viewModel.isFromSummaryPage) {
            startAsNewActivity(Intent(this@TermsAndConditionActivity, LandingActivity::class.java))
        }else {
            finish()
        }
    }
}