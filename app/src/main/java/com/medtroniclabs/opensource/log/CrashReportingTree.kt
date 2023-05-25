package com.medtroniclabs.opensource.log
import android.util.Log
import timber.log.Timber

class CrashReportingTree: Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when (priority){
            Log.VERBOSE-> return
            Log.DEBUG->return
            Log.WARN-> {t?.let{/*crashlytics.logWarning(it)*/}}
            Log.ERROR->{t?.let{/*crashlytics.logError(it)*/}}
            else-> return
        }
    }
}