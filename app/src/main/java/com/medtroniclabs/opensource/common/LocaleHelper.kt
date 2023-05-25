package com.medtroniclabs.opensource.common

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import java.util.*

class LocaleHelper {

    companion object {

        fun onAttach(ctx: Context): Context {
            return setLocale(ctx, DefinedParams.EN)
        }

        fun onAttach(context: Context, defaultLanguage: String): Context {
            return setLocale(context, defaultLanguage)
        }

        fun setLocale(context: Context, language: String): Context {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return updateResources(context, language)
            }
            return updateResourcesLegacy(context, language)
        }
        @TargetApi(Build.VERSION_CODES.N)
        fun updateResources(context: Context, language: String): Context {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val configuration = context.resources.configuration
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            return context.createConfigurationContext(configuration)
        }

        @SuppressWarnings("deprecation")
        fun updateResourcesLegacy(context: Context, language: String): Context {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val resources = context.resources
            val configuration = resources.configuration
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
            return context
        }
    }
}