package com.medtroniclabs.opensource.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.medtroniclabs.opensource.BuildConfig
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.safeClickListener
import com.medtroniclabs.opensource.common.IntentConstants
import com.medtroniclabs.opensource.common.LocaleHelper
import com.medtroniclabs.opensource.custom.CommentsAlertDialog
import com.medtroniclabs.opensource.custom.GeneralErrorDialog
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.databinding.ActivityBaseBinding
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.boarding.LoginActivity
import com.medtroniclabs.opensource.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.*
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.InvalidParameterSpecException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.math.abs
import kotlin.system.exitProcess


/**
 * Base activity for all activity
 */
@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBaseBinding

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    lateinit var sessionExpiredBroadcastReceiver: SessionExpiredBroadcastReceiver
    lateinit var updateAppReceiver: UpdateAppReceiver

    private var downX: Int = 0

    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionExpiredBroadcastReceiver = SessionExpiredBroadcastReceiver()
        updateAppReceiver = UpdateAppReceiver()
        setListener()
        if (CommonUtils.isProduction()) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        checkInAppUpdate()
    }

    private fun checkInAppUpdate() {
        if (connectivityManager.isNetworkAvailable()) {
            appUpdateManager = AppUpdateManagerFactory.create(this)

            val appUpdateInfoTask = appUpdateManager.appUpdateInfo

            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                        AppUpdateType.IMMEDIATE
                    )
                ) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        IntentConstants.APP_UPDATE_REQUEST_CODE
                    )
                }
            }
        }
    }

    private fun setListener() {
        binding.loadingProgress.safeClickListener {

        }
    }


    fun startAsNewActivity(intent: Intent) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    /**
     * method to load screen layout
     * @param view child layout root
     */
    fun setMainContentView(
        view: View,
        isToolbarVisible: Boolean = false,
        title: String? = null,
        //Pair(Home, Back)
        homeAndBackVisibility: Pair<Boolean?, Boolean?> = Pair(false, true),
        callback: (() -> Unit?)? = null,
        callbackHome: (() -> Unit?)? = null,
        homeIcon: Drawable? = null,
        callbackMore: ((view: View) -> Unit?)? = null,
    ) {
        if (isToolbarVisible) {
            binding.toolbar.visibility = View.VISIBLE
        } else {
            binding.toolbar.visibility = View.GONE
        }
        title?.let {
            binding.titleToolbar.text = it
        }

        binding.frameBaseLayout.addView(view)

        homeAndBackVisibility.first?.let {
            binding.ivHome.visibility = checkVisibility(it)
        }

        homeAndBackVisibility.second?.let {
            binding.ivBack.visibility = checkVisibility(it)
        }

        binding.ivBack.safeClickListener {
            if (callback != null)
                callback.invoke()
            else
                finish()
        }
        if (homeIcon != null) {
            binding.ivHome.setImageDrawable(homeIcon)
        }else {
           binding.ivHome.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_home_img))
        }
        binding.ivHome.safeClickListener {
            if (homeIcon == null){
                if (callbackHome == null) {
                    redirectToHome()
                } else {
                    callbackHome.invoke()
                }
            } else{
                callbackMore?.invoke(binding.ivHome)
            }
        }
    }

    private fun checkVisibility(isVisible: Boolean): Int {
        return if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    fun setTitle(title: String) {
        binding.titleToolbar.text = title
    }

    fun hideHomeButton(status: Boolean) {
        if (status) {
            binding.ivHome.visibility = View.INVISIBLE
        } else {
            binding.ivHome.visibility = View.VISIBLE

        }
    }

    fun setToolBarOptions(
        isToolbarVisible: Boolean = false,
        title: String? = null,
        centerTitle: Boolean? = true,
        isHomeVisible: Boolean = false,
        isBackVisible: Boolean = true,
        callback: (() -> Unit?)? = null,
        callbackHome: (() -> Unit?)? = null
    ) {
        if (isToolbarVisible) {
            binding.toolbar.visibility = View.VISIBLE
        } else {
            binding.toolbar.visibility = View.GONE
        }
        title?.let {
            binding.titleToolbar.text = it
        }

        binding.titleToolbar.gravity = Gravity.CENTER

        centerTitle?.let {
            if (!it)
                binding.titleToolbar.gravity = Gravity.START
        }

        if (isHomeVisible) {
            binding.ivHome.visibility = View.VISIBLE
        } else {
            binding.ivHome.visibility = View.INVISIBLE
        }

        if (isBackVisible) {
            binding.ivBack.visibility = View.VISIBLE
        } else {
            binding.ivBack.visibility = View.INVISIBLE
        }
        binding.ivBack.safeClickListener {

            if (callback != null)
                callback.invoke()
            else
                finish()
        }
        binding.ivHome.safeClickListener {
            callbackHome?.invoke()
        }
    }

    fun setRedRiskPatient(isRedRiskPatient: Boolean?) {
        binding.ivRedAlert.visibility = if (isRedRiskPatient == true) View.VISIBLE else View.GONE
        binding.tvRedAlert.visibility = if (isRedRiskPatient == true) View.VISIBLE else View.GONE
    }

    fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }

    /**
     * method to showAlert
     */
    fun showAlertWith(
        message: String,
        positiveButtonName: String = getString(R.string.ok),
        isNegativeButtonNeed: Boolean = false,
        negativeButtonName: String = getString(R.string.cancel),
        okayBtnEnable: Boolean = true,
        callback: ((isPositiveResult: Boolean) -> Unit)
    ) {
        val generalErrorDialog = GeneralErrorDialog.newInstance(
            getString(R.string.alert),
            message,
            callback,
            this,
            isNegativeButtonNeed,
            okayButton = positiveButtonName,
            cancelButton = negativeButtonName,
            okayBtnEnable = okayBtnEnable
        )
        generalErrorDialog.show(supportFragmentManager, GeneralErrorDialog.TAG)
    }

    fun showAlertDialogWithComments(
        title: String? = null,
        message: String,
        positiveButtonName: String = getString(R.string.ok),
        isNegativeButtonNeed: Boolean = false,
        negativeButtonName: String = getString(R.string.cancel),
        showComment: Boolean = true,
        errorMessage: String = getString(R.string.default_user_input_error),
        callback: ((isPositiveResult: Boolean, reason: String?) -> Unit)
    ) {
        val dialog = CommentsAlertDialog.newInstance(
            this,
            title,
            message,
            isNegativeButtonNeed,
            Pair(positiveButtonName, negativeButtonName),
            callback = callback,
            showComment = showComment,
            errorMessage = errorMessage
        )
        dialog.show(supportFragmentManager, CommentsAlertDialog.TAG)
    }


    fun showErrorDialogue(
        title: String = getString(R.string.error),
        message: String,
        isNegativeButtonNeed: Boolean? = null,
        positiveButtonName: String? = null,
        okayBtnEnable: Boolean? = null,
        callback: ((isPositiveResult: Boolean) -> Unit)
    ) {
        val generalErrorDialog =
            GeneralErrorDialog.newInstance(
                title,
                message,
                callback,
                this,
                isNegativeButtonNeed ?: false,
                okayButton = positiveButtonName ?: getString(R.string.ok),
                okayBtnEnable = okayBtnEnable ?: true
            )
        val errorFragment = supportFragmentManager.findFragmentByTag(GeneralErrorDialog.TAG)
        if (errorFragment == null)
            generalErrorDialog.show(supportFragmentManager, GeneralErrorDialog.TAG)
    }

    override fun onStart() {
        super.onStart()
        connectivityManager.registerConnectionObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterConnectionObserver(this)
    }

    fun hideBackButton() {
        binding.ivBack.visibility = View.INVISIBLE
    }

    fun showBackButton() {
        binding.ivBack.visibility = View.VISIBLE
    }

    fun onHomeIconClicked() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) {
            if (it) {
                startAsNewActivity(Intent(this, LandingActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            sessionExpiredBroadcastReceiver,
            IntentFilter(
                IntentConstants.ACTION_SESSION_EXPIRED
            )
        )
        LocalBroadcastManager.getInstance(this).registerReceiver(
            updateAppReceiver,
            IntentFilter(
                IntentConstants.ACTION_UPDATE_APP
            )
        )
        if (this::appUpdateManager.isInitialized) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        IntentConstants.APP_UPDATE_REQUEST_CODE
                    )
                }
            }
        }
    }

    /*
    https://issuetracker.google.com/issues/181785653
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentConstants.APP_UPDATE_REQUEST_CODE && resultCode == RESULT_CANCELED && resultCode != RESULT_OK) {
            finishAffinity()
            exitProcess(0)
        }
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            sessionExpiredBroadcastReceiver
        )
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            updateAppReceiver
        )
        super.onPause()
    }

    /**
     * Receiver for session expired broadcasts from [Retrofit API].
     */
    inner class SessionExpiredBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val sessionExpired = intent.getBooleanExtra(IntentConstants.NCD_SESSION, false)
            if (sessionExpired) {
                showErrorDialogue(
                    getString(R.string.alert),
                    getString(R.string.session_expired),
                    isNegativeButtonNeed = false
                ) { status ->
                    if (status) {
                        SecuredPreference.clear(this@BaseActivity)
                        val i = Intent(context, LoginActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(i)
                    }
                }
            }
        }
    }

    inner class UpdateAppReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val updateRequired = intent.getBooleanExtra(IntentConstants.UPDATE_REQUIRED, false)
            val errorMessage = intent.getStringExtra(IntentConstants.INTENT_MESSAGE) ?: getString(R.string.latest_update)
            if (updateRequired) {
                showErrorDialogue(
                    getString(R.string.alert),
                    errorMessage,
                    positiveButtonName = getString(R.string.open_play_store),
                    isNegativeButtonNeed = false,
                    okayBtnEnable = CommonUtils.isGooglePlayServiceInstalled(context) && CommonUtils.isProduction()
                ) { status ->
                    if (status) {
                        startActivity(Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(
                                "https://play.google.com/store/apps/details?id=" +
                                        if (CommonUtils.isProduction()) BuildConfig.APPLICATION_ID
                                        else "com.medtroniclabs.opensource"

                            )
                            setPackage("com.android.vending")
                        })
                    }
                }
            }
        }
    }

    private fun showDeviceError() {
        showErrorDialogue(
            getString(R.string.error),
            getString(R.string.emulator_not_supported),
            callback = {
                finish()
                exitProcess(0)
            })
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidParameterSpecException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        UnsupportedEncodingException::class
    )
    open fun encryptMsg(message: String, secret: SecretKey): String {
        val cipher = Cipher.getInstance(getString(R.string.encryption_scheme))
        cipher.init(Cipher.ENCRYPT_MODE, secret)
        val cipherText: ByteArray = cipher.doFinal(message.toByteArray(charset("UTF-8")))
        return Base64.encodeToString(cipherText, Base64.NO_WRAP)
    }

    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidParameterSpecException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        UnsupportedEncodingException::class
    )
    open fun decryptMsg(cipherText: String, secret: SecretKey): String {
        val cipher = Cipher.getInstance(getString(R.string.encryption_scheme))
        cipher.init(Cipher.DECRYPT_MODE, secret)
        val decode =
            Base64.decode(cipherText, Base64.NO_WRAP)
        return String(cipher.doFinal(decode), charset("UTF-8"))
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    open fun generateKey(key: String): SecretKey {
        return SecretKeySpec(key.toByteArray(), "AES")
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            downX = event.rawX.toInt()
        }
        if (event.action == MotionEvent.ACTION_UP) {
            val v = currentFocus
            if (v is EditText) {
                val x = event.rawX.toInt()
                val y = event.rawY.toInt()
                //Was it a scroll - If skip all
                if (abs(downX - x) > 5) {
                    return super.dispatchTouchEvent(event)
                }
                val reducePx = 2
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                //Bounding box is to big, reduce it just a little bit
                outRect.inset(reducePx, reducePx)
                touchEvent(v, x, y, reducePx, outRect)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun touchEvent(v: EditText, x: Int, y: Int, reducePx: Int, outRect: Rect) {
        if (!outRect.contains(x, y)) {
            v.clearFocus()
            var touchTargetIsEditText = false
            //Check if another editText has been touched
            for (vi in v.rootView.touchables) {
                if (vi is EditText) {
                    val clickedViewRect = Rect()
                    vi.getGlobalVisibleRect(clickedViewRect)
                    //Bounding box is to big, reduce it just a little bit
                    clickedViewRect.inset(reducePx, reducePx)
                    if (clickedViewRect.contains(x, y)) {
                        touchTargetIsEditText = true
                        break
                    }
                }
            }
            if (!touchTargetIsEditText) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
            }
        }
    }

    fun redirectToHome() {
        startAsNewActivity(Intent(this, LandingActivity::class.java))
    }

    fun showVerticalMoreIcon(canVisible: Boolean, callback: ((view: View) -> Unit?)? = null) {
        binding.ivMore.visibility = if (canVisible) View.VISIBLE else View.GONE
        binding.ivMore.safeClickListener {
            callback?.invoke(it)
        }
    }

    fun changeMoreIconVisibility(visibility : Int)
    {
        binding.ivMore.visibility = visibility
    }

    fun checkDeviceDetails() {
        if (CommonUtils.isProduction() && CommonUtils.isEmulator()) {
            showDeviceError()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        try {
            super.attachBaseContext(newBase?.let {
                LocaleHelper.onAttach(it, CommonUtils.localeCode)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}