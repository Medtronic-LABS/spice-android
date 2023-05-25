package com.medtroniclabs.opensource.custom

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.opensource.data.model.CulturePreference
import com.medtroniclabs.opensource.data.model.LoginResponse
import com.medtroniclabs.opensource.data.model.WorkflowModel
import com.medtroniclabs.opensource.data.screening.SiteDetails
import com.medtroniclabs.opensource.db.tables.SiteEntity
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import timber.log.Timber
import java.lang.reflect.Type


object SecuredPreference {

    private const val DEFAULT_SUFFIX = "_preferences"

    private var mPrefs: SharedPreferences? = null

    private fun initPrefs(context: Context, prefsName: String) {
        val mainKeyAlias = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        mPrefs = EncryptedSharedPreferences.create(
            context,
            prefsName,
            mainKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Returns the underlying SharedPreference instance
     *
     * @return an instance of the SharedPreference
     * @throws RuntimeException if SharedPreference instance has not been instantiated yet.
     */
    private val preferences: SharedPreferences
        get() {
            if (mPrefs != null) {
                return mPrefs!!
            }
            throw RuntimeException(
                "Prefs class not correctly instantiated. Please call Builder.setContext().build() in the Application class onCreate."
            )
        }

    /**
     * @return Returns a map containing a list of pairs key/value representing
     * the preferences.
     * @see android.content.SharedPreferences.getAll
     */
    val all: Map<String, *>
        get() = preferences.all

    /**
     * Retrieves a stored int value.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.
     * @throws ClassCastException if there is a preference with this name that is not
     * an int.
     * @see android.content.SharedPreferences.getInt
     */
    fun getInt(key: String, defValue: Int): Int {
        return preferences.getInt(key, defValue)
    }

    /**
     * Retrieves a stored int value, or 0 if the preference does not exist.
     *
     * @param key      The name of the preference to retrieve.
     * @return Returns the preference value if it exists, or 0.
     * @throws ClassCastException if there is a preference with this name that is not
     * an int.
     * @see android.content.SharedPreferences.getInt
     */
    fun getInt(key: String): Int {
        return preferences.getInt(key, 0)
    }


    /**
     * Retrieves a stored boolean value.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.
     * @throws ClassCastException if there is a preference with this name that is not a boolean.
     * @see android.content.SharedPreferences.getBoolean
     */
    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferences.getBoolean(key, defValue)
    }

    /**
     * Retrieves a stored boolean value, or false if the preference does not exist.
     *
     * @param key      The name of the preference to retrieve.
     * @return Returns the preference value if it exists, or false.
     * @throws ClassCastException if there is a preference with this name that is not a boolean.
     * @see android.content.SharedPreferences.getBoolean
     */
    fun getBoolean(key: String): Boolean {
        return preferences.getBoolean(key, false)
    }


    /**
     * Retrieves a stored long value.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.
     * @throws ClassCastException if there is a preference with this name that is not a long.
     * @see android.content.SharedPreferences.getLong
     */
    fun getLong(key: String, defValue: Long): Long {
        return preferences.getLong(key, defValue)
    }

    /**
     * Retrieves a stored long value, or 0 if the preference does not exist.
     *
     * @param key      The name of the preference to retrieve.
     * @return Returns the preference value if it exists, or 0.
     * @throws ClassCastException if there is a preference with this name that is not a long.
     * @see android.content.SharedPreferences.getLong
     */
    fun getLong(key: String): Long {
        return preferences.getLong(key, 0L)
    }

    /**
     * Returns the double that has been saved as a long raw bits value in the long preferences.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue the double Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.
     * @throws ClassCastException if there is a preference with this name that is not a long.
     * @see android.content.SharedPreferences.getLong
     */
    fun getDouble(key: String, defValue: Double): Double {
        return java.lang.Double.longBitsToDouble(
            preferences.getLong(
                key,
                java.lang.Double.doubleToLongBits(defValue)
            )
        )
    }

    /**
     * Returns the double that has been saved as a long raw bits value in the long preferences.
     * Returns 0 if the preference does not exist.
     *
     * @param key      The name of the preference to retrieve.
     * @return Returns the preference value if it exists, or 0.
     * @throws ClassCastException if there is a preference with this name that is not a long.
     * @see android.content.SharedPreferences.getLong
     */
    fun getDouble(key: String): Double {
        return java.lang.Double.longBitsToDouble(
            preferences.getLong(
                key,
                java.lang.Double.doubleToLongBits(0.0)
            )
        )
    }

    /**
     * Retrieves a stored float value.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.
     * @throws ClassCastException if there is a preference with this name that is not a float.
     * @see android.content.SharedPreferences.getFloat
     */
    fun getFloat(key: String, defValue: Float): Float {
        return preferences.getFloat(key, defValue)
    }

    /**
     * Retrieves a stored float value, or 0 if the preference does not exist.
     *
     * @param key      The name of the preference to retrieve.
     * @return Returns the preference value if it exists, or 0.
     * @throws ClassCastException if there is a preference with this name that is not a float.
     * @see android.content.SharedPreferences.getFloat
     */
    fun getFloat(key: String): Float {
        return preferences.getFloat(key, 0.0f)
    }


    /**
     * Retrieves a stored String value.
     *
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.
     * @throws ClassCastException if there is a preference with this name that is not a String.
     * @see android.content.SharedPreferences.getString
     */
    fun getString(key: String, defValue: String?): String? {
        return preferences.getString(key, defValue)
    }

    /**
     * Retrieves a stored String value, or an empty string if the preference does not exist.
     *
     * @param key      The name of the preference to retrieve.
     * @return Returns the preference value if it exists, or "".
     * @throws ClassCastException if there is a preference with this name that is not a String.
     * @see android.content.SharedPreferences.getString
     */
    fun getString(key: String): String? {
        return preferences.getString(key, null)
    }

    /**
     * Stores a long value.
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     * @see android.content.SharedPreferences.Editor.putLong
     */
    fun putLong(key: String, value: Long) {
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    /**
     * Stores an integer value.
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     * @see android.content.SharedPreferences.Editor.putInt
     */
    fun putInt(key: String, value: Int) {
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    /**
     * Stores a double value as a long raw bits value.
     *
     * @param key   The name of the preference to modify.
     * @param value The double value to be save in the preferences.
     * @see android.content.SharedPreferences.Editor.putLong
     */
    fun putDouble(key: String, value: Double) {
        val editor = preferences.edit()
        editor.putLong(key, java.lang.Double.doubleToRawLongBits(value))
        editor.apply()
    }

    /**
     * Stores a float value.
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     * @see android.content.SharedPreferences.Editor.putFloat
     */
    fun putFloat(key: String, value: Float) {
        val editor = preferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    /**
     * Stores a boolean value.
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     * @see android.content.SharedPreferences.Editor.putBoolean
     */
    fun putBoolean(key: String, value: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /**
     * Stores a String value.
     *
     * @param key   The name of the preference to modify.
     * @param value The new value for the preference.
     * @see android.content.SharedPreferences.Editor.putString
     */
    fun putString(key: String, value: String?) {
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    /**
     * Removes a preference value.
     *
     * @param key The name of the preference to remove.
     * @see android.content.SharedPreferences.Editor.remove
     */
    fun remove(key: String) {
        val prefs = preferences
        val editor = prefs.edit()
        editor.remove(key)
        editor.apply()
    }

    /**
     * Checks if a value is stored for the given key.
     *
     * @param key The name of the preference to check.
     * @return `true` if the storage contains this key value, `false` otherwise.
     * @see android.content.SharedPreferences.contains
     */
    operator fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

    /**
     * Removed all the stored keys and values.
     *
     * @return the [Editor] for chaining. The changes have already been committed/applied
     * through the execution of this method.
     * @see android.content.SharedPreferences.Editor.clear
     */
    fun clear(context: Context) {
        val username = getString(EnvironmentKey.USERNAME.name)
        val password = getString(EnvironmentKey.PASSWORD.name)
        val unitMeasurement = getString(EnvironmentKey.MEASUREMENT_TYPE_KEY.name)
        val isMetaLoaded = getBoolean(EnvironmentKey.ISMETALOADED.name)
        val loginResponseString = getString(EnvironmentKey.USER_RESPONSE.name)
        val culture = getString(EnvironmentKey.USER_CULTURE.name)
        try {
            preferences.edit().clear().apply()
        } catch (e: Exception) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply()
        } finally {
            putString(EnvironmentKey.USERNAME.name, username)
            putString(EnvironmentKey.PASSWORD.name, password)
            putString(EnvironmentKey.MEASUREMENT_TYPE_KEY.name, unitMeasurement)
            putBoolean(EnvironmentKey.ISMETALOADED.name, isMetaLoaded)
            putString(EnvironmentKey.USER_RESPONSE.name, loginResponseString)
            putString(EnvironmentKey.USER_CULTURE.name, culture)
        }
    }

    class Builder {
        fun build(
            mKey: String,
            context: Context
        ) {
            initPrefs(context, mKey + DEFAULT_SUFFIX)
        }
    }

    fun putScreeningEntry(map: Map<String, Any>) {
        try {
            val type: Type = object : TypeToken<Map<String, Any>>() {}.type
            putString(EnvironmentKey.CURRENT_PATIENT_SCREENING.name, Gson().toJson(map, type))
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    fun getScreeningEntry(): Map<String, Any>? {
        try {
            val screeningEntry = getString(EnvironmentKey.CURRENT_PATIENT_SCREENING.name)
            screeningEntry?.let {
                val type: Type = object : TypeToken<Map<String, Any>>() {}.type
                return Gson().fromJson(it, type)
            } ?: return null
        } catch (e: Exception) {
            Timber.d(e)
        }
        return null
    }

    fun putSelectedSiteEntity(siteEntity: SiteEntity) {
        val siteEntityString = Gson().toJson(siteEntity)
        putString(EnvironmentKey.SELECTED_SITE.name, siteEntityString)
    }

    fun getSelectedSiteEntity(): SiteEntity? {
        return try {
            val siteEntityString = getString(EnvironmentKey.SELECTED_SITE.name)
            val type: Type = object : TypeToken<SiteEntity>() {}.type
            val siteEntity = Gson().fromJson<SiteEntity>(siteEntityString, type)
            siteEntity
        } catch (e: Exception) {
            null
        }
    }

    fun getChosenSiteEntity(): SiteDetails? {
        return try {
            val siteDetailsString = getString(EnvironmentKey.SITE_DETAIL.name, "")
            val siteDetails: SiteDetails =
                Gson().fromJson(siteDetailsString, SiteDetails::class.java)
            siteDetails
        } catch (e: Exception) {
            null
        }
    }

    fun getChosenSiteDetailRawString(): String {
        return getString(EnvironmentKey.SITE_DETAIL.name, "") ?: ""
    }


    fun getScreeningEntryRawString(): String? {
        return getString(EnvironmentKey.CURRENT_PATIENT_SCREENING.name)
    }

    fun setKeyForeGroundEnabled(status: Boolean) {
        putBoolean(EnvironmentKey.KEY_FOREGROUND_ENABLED.name, status)
    }

    fun getKeyForeGroundEnabled(): Boolean {
        return getBoolean(EnvironmentKey.KEY_FOREGROUND_ENABLED.name)
    }

    fun putUserResponse(loginResponse: LoginResponse) {
        val loginResponseString = Gson().toJson(loginResponse)
        putString(EnvironmentKey.USER_RESPONSE.name, loginResponseString)
    }

    fun getUserId(): Long {
        return getUserDetails()._id
    }

    fun getUserName(): String {
        return getUserDetails().firstName.toString()
    }

    fun getCountryCode(): String? {
        return getUserDetails().countryCode
    }

    fun getCountryID(): Long {
        return getUserDetails().country.id
    }

    fun getDeviceID(): Long? {
        return getUserDetails().deviceInfoId
    }

    fun isPHQ4Enabled(): Boolean {
        return getClinicalWorkflow()?.phq4 ?: false
    }

    fun isPregnancyWorkFlowEnabled(): Boolean {
        return getClinicalWorkflow()?.pregnancy ?: false
    }

    fun getClinicalWorkflow(): WorkflowModel? {
        val data = getString(EnvironmentKey.CLINICAL_WORKFLOW.name)
        data?.let {
            val type: Type = object : TypeToken<WorkflowModel>() {}.type
            val workflowModel = Gson().fromJson<WorkflowModel>(data, type)
            return workflowModel
        }
        return null
    }

    fun saveClinicalWorkflow(workflowModel: WorkflowModel?) {
        if (workflowModel != null) {
            val model = Gson().toJson(workflowModel)
            putString(EnvironmentKey.CLINICAL_WORKFLOW.name, model)
        } else
            putString(EnvironmentKey.CLINICAL_WORKFLOW.name, null)
    }

    fun saveDeviceDetails(deviceInfo: Long?) {
        val userDetails = getUserDetails()
        userDetails.deviceInfoId = deviceInfo
        putUserResponse(userDetails)
    }

    fun getUserDetails(): LoginResponse {
        val userResponseString = getString(EnvironmentKey.USER_RESPONSE.name)
        val type: Type = object : TypeToken<LoginResponse>() {}.type
        val usersResponse = Gson().fromJson<LoginResponse>(userResponseString, type)
        return usersResponse
    }

    fun getLoginTenantId(): Long? {
        val userResponseString = getString(EnvironmentKey.USER_RESPONSE.name, null)
        val type: Type = object : TypeToken<LoginResponse>() {}.type
        val usersResponse = Gson().fromJson<LoginResponse>(userResponseString, type)
        return usersResponse?.tenantId
    }

    fun getTimeZoneId(): String? {
        getString(EnvironmentKey.USER_RESPONSE.name)?.let { userResponseString ->
            val type: Type = object : TypeToken<LoginResponse>() {}.type
            val usersResponse = Gson().fromJson<LoginResponse>(userResponseString, type)
            return usersResponse.timezone?.offset
        }
        return null
    }

    fun getUnitMeasurementType(): String {
        getString(
            EnvironmentKey.MEASUREMENT_TYPE_KEY.name,
            DefinedParams.Unit_Measurement_Metric_Type
        )?.let {
            return it
        } ?: kotlin.run {
            return DefinedParams.Unit_Measurement_Metric_Type
        }
    }

    fun getTranslationToggle(): Boolean {
        return false
    }

    fun changeTenantId(tenantId: Long) {
        val userDetails = getUserDetails()
        userDetails.tenantId = tenantId
        putUserResponse(userDetails)
    }
    fun setUserPreference(locale: Long, name: String, enabled: Boolean) {
        val culture = getCulturePreference()
        if (culture == null)
            saveCulturePreference(CulturePreference(locale, name, enabled))
        else {
            culture.cultureId = locale
            culture.name = name
            culture.isTranslationEnabled = enabled
            saveCulturePreference(culture)
        }
        CommonUtils.localeCode = CommonUtils.parseUserLocale()
    }

    fun getIsTranslationEnabled() = getCulturePreference()?.isTranslationEnabled ?: false
    fun getCultureName() = getCulturePreference()?.name ?: DefinedParams.EN_Locale

    private fun saveCulturePreference(model: CulturePreference) {
        val culture = Gson().toJson(model)
        putString(EnvironmentKey.USER_CULTURE.name, culture)
    }

    private fun getCulturePreference(): CulturePreference? {
        val userResponseString = getString(EnvironmentKey.USER_CULTURE.name)
        val type: Type = object : TypeToken<CulturePreference>() {}.type
        return Gson().fromJson(userResponseString, type)
    }

    fun getCultureId() = getCulturePreference()?.cultureId ?: 0

    enum class EnvironmentKey {
        CURRENT_PATIENT_SCREENING,
        KEY_FOREGROUND_ENABLED,
        SITE_DETAIL,
        TOKEN,
        SELECTED_SITE,
        USERNAME,
        PASSWORD,
        USER_RESPONSE,
        ISLOGGEDIN,
        ISMETALOADED,
        CLINICAL_WORKFLOW,
        ISOFFLINELOGIN,
        MEASUREMENT_TYPE_KEY,
        ACCOUNT,
        OPERATING_UNIT,
        USER_CULTURE
    }

}