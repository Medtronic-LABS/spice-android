package com.medtroniclabs.opensource

import android.app.Application
import com.medtroniclabs.opensource.appextensions.isDebug
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.log.CrashReportingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SpiceBaseApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        initTimber()
        initPreference()
    }

    /**
     * method to print debug and release logs
     */
    private fun initTimber() {
        isDebug { debug ->
            if (debug)
                Timber.plant(Timber.DebugTree())
            else
                Timber.plant(CrashReportingTree())
        }
    }

    /**
     * method to initialize preference
     */
    private fun initPreference() {
        SecuredPreference
            .Builder()
            .build(packageName, applicationContext)
    }
}