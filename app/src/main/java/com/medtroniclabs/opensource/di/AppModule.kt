package com.medtroniclabs.opensource.di

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.medtroniclabs.opensource.BuildConfig
import com.medtroniclabs.opensource.common.AppConstants
import com.medtroniclabs.opensource.common.IntentConstants.ACTION_SESSION_EXPIRED
import com.medtroniclabs.opensource.common.IntentConstants.NCD_SESSION
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.db.NCDMergerDatabase
import com.medtroniclabs.opensource.db.dao.LanguageDAO
import com.medtroniclabs.opensource.db.dao.MetaDataDAO
import com.medtroniclabs.opensource.db.dao.RiskFactorDAO
import com.medtroniclabs.opensource.db.dao.ScreeningDAO
import com.medtroniclabs.opensource.db.local.RoomHelper
import com.medtroniclabs.opensource.db.local.RoomHelperImpl
import com.medtroniclabs.opensource.network.ApiHelper
import com.medtroniclabs.opensource.network.ApiHelperImpl
import com.medtroniclabs.opensource.network.ApiService
import com.medtroniclabs.opensource.network.NetworkConstants
import com.medtroniclabs.opensource.network.NetworkConstants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideBaseUrl() = BASE_URL

    @Singleton
    @Provides
    fun provideOkHttpClient(@ApplicationContext context: Context) = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AppInterceptor(context))
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    } else {
        OkHttpClient.Builder()
            .addInterceptor(AppInterceptor(context))
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    class AppInterceptor(val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request: Request = chain.request()
            val requestBuilder = request.newBuilder()
                .header(
                    "Authorization",
                    SecuredPreference.getString(SecuredPreference.EnvironmentKey.TOKEN.toString())
                        ?: ""
                )
                .header("App-Version", getAppPackageInfo())
                .header("Client", AppConstants.SPICE_MOBILE)

            if(!request.url.toString().endsWith(NetworkConstants.DEVICE_DETAILS)
                && !request.url.toString().endsWith(NetworkConstants.LOGIN)
                && !request.url.toString().contains(NetworkConstants.FORGOT_PASSWORD))
            {
                SecuredPreference.getLoginTenantId()?.toString()?.let {
                    requestBuilder.header("TenantId", it)
                }
            }
            request = requestBuilder.build()
            val response = chain.proceed(request)
            Timber.i("HEADERS ->\n${request.headers}")
            if (SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.ISLOGGEDIN.name) && !response.isSuccessful && response.code == 401) {
                redirectLogin(context)
            }
            return response
        }
    }

    private fun redirectLogin(context: Context) {
        val intent = Intent(ACTION_SESSION_EXPIRED)
        intent.putExtra(NCD_SESSION, true)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    @Singleton
    @Provides
    fun providesRetrofit(okHttpClient: OkHttpClient, baseUrl: String): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()

    @Singleton
    @Provides
    fun providesUserApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }


    @Singleton
    @Provides
    fun provideApiHelper(apiHelper: ApiHelperImpl): ApiHelper {
        return apiHelper
    }

    @Singleton
    @Provides
    fun provideNcdDatabase(@ApplicationContext context: Context): NCDMergerDatabase {
        return NCDMergerDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideLanguageDao(db: NCDMergerDatabase): LanguageDAO {
        return db.languageDao()
    }

    @Singleton
    @Provides
    fun provideScreeningDao(db: NCDMergerDatabase): ScreeningDAO {
        return db.screeningDao()
    }

    @Singleton
    @Provides
    fun provideRiskFactorDao(db: NCDMergerDatabase): RiskFactorDAO {
        return db.riskFactorDao()
    }

    @Singleton
    @Provides
    fun provideMetaDataDao(db: NCDMergerDatabase): MetaDataDAO {
        return db.metaDataDao()
    }

    @Singleton
    @Provides
    fun provideRoomHelper(roomHelper: RoomHelperImpl): RoomHelper {
        return roomHelper
    }

    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    private fun getAppPackageInfo(): String {
        return BuildConfig.VERSION_NAME
    }

}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher