package com.medtroniclabs.opensource.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.ui.boarding.LoginActivity
import com.medtroniclabs.opensource.ui.landing.LandingActivity


class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CommonUtils.localeCode = CommonUtils.parseUserLocale()
        intent =
            if (SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.ISLOGGEDIN.name)
                && SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.ISMETALOADED.name)) {
                Intent(this, LandingActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
        startActivity(intent)
        finish()
    }
}