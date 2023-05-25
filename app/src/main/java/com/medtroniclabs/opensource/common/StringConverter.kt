package com.medtroniclabs.opensource.common

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.opensource.data.model.ErrorResponse
import com.medtroniclabs.opensource.db.tables.SiteEntity
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import okhttp3.ResponseBody
import java.lang.reflect.Type


object StringConverter {

    fun convertStringToMap(data: String): Map<String, Any>? {
        return try {
            val gson = Gson()
            val type: Type = object : TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any>? = gson.fromJson(data, type)
            map
        } catch (e: Exception) {
            null
        }
    }

    fun convertGivenMapToString(map: HashMap<*, *>?): String? {
        val gson = Gson()
        return gson.toJson(map)
    }

    fun getErrorMessage(errorBody: ResponseBody?): String? {
        if (errorBody == null)
            return null
        return try {
            val errorResponse =
                Gson().fromJson(errorBody.string(), ErrorResponse::class.java)
            errorResponse?.message
        } catch (e: Exception) {
            null
        }
    }

    fun checkForHeader(responseBody: String?): String? {
        if (responseBody == null)
            return null
        return try {
            val errorResponse =
                Gson().fromJson(responseBody, HashMap::class.java)
            if (errorResponse.containsKey(DefinedParams.Message))
                (errorResponse[DefinedParams.Message] as String?)
            else null
        } catch (e: Exception) {
            null
        }
    }


    fun getPHQ4ReadableName(score: Int): String {
        return when (score) {
            in 0..2 -> {
                "$score - Normal"
            }
            in 3..5 -> {
                "$score - Mild"
            }
            in 6..8 -> {
                "$score - Moderate"
            }
            in 9..12 -> {
                "$score - Severe"
            }
            else -> score.toString()
        }
    }

    fun convertSiteModelToMap(map: SiteEntity): Map<String,Any>? {
        val gson = Gson()
        val json = gson.toJson(map)
        return convertStringToMap(json)
    }


    fun convertStringToListOfMap(data: String): ArrayList<Map<String, Any>>? {
        return try {
            val gson = Gson()
            val type: Type = object : TypeToken<ArrayList<Map<String, Any>>>() {}.type
            val map: ArrayList<Map<String, Any>>? = gson.fromJson(data, type)
            map
        } catch (e: Exception) {
            null
        }
    }

    fun appendTexts(firstText: String, vararg input: String?, separator: String? = null): String {
        val strBuilder = StringBuilder(firstText)
        for (text in input) {
            text?.let {
                if(text.isNotBlank()) {
                    if (separator.isNullOrBlank())
                        strBuilder.append(" $it")
                    else
                        strBuilder.append(" $separator $it")
                }
            }
        }
        return strBuilder.trim().toString()
    }

    fun getJsonObject(inputJson: String): JsonObject {
        return Gson().fromJson(inputJson, JsonObject::class.java)
    }
}