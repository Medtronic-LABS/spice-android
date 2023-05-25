package com.medtroniclabs.opensource.db

import androidx.room.TypeConverter
import com.google.gson.Gson

import com.google.gson.reflect.TypeToken
import com.medtroniclabs.opensource.db.tables.LifeStyleAnswer
import com.medtroniclabs.opensource.db.tables.ProgramSites
import java.lang.reflect.Type


class Converters {

    @TypeConverter
    fun fromString(value: String?): ArrayList<LifeStyleAnswer?>? {
        val listType: Type = object : TypeToken<ArrayList<LifeStyleAnswer?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<LifeStyleAnswer?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromArrayListToString(list: ArrayList<String?>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromStringToArrayList(value: String?): ArrayList<String?>? {
        val listType = object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromProgramSitesListToString(list: ArrayList<ProgramSites>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromStringToProgramSitesList(value: String): ArrayList<ProgramSites> {
        val listType = object : TypeToken<ArrayList<ProgramSites?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

}