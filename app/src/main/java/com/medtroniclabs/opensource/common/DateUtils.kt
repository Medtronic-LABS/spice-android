package com.medtroniclabs.opensource.common

import com.medtroniclabs.opensource.custom.SecuredPreference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    const val DATE_FORMAT_ddMMyyyy = "dd-MM-yyyy"
    const val DATE_ddMMyyyy = "dd/MM/yyyy"
    const val DATE_FORMAT_ddMMyyHHmmss = "dd/MM/yy HH:mm"
    const val DATE_FORMAT_ddMMyy_GRAPH = "dd-MM-yy"
    const val DATE_FORMAT_yyyyMMddHHmmss = "yyyy-MM-dd'T'HH:mm:ss"
    const val DATE_FORMAT_yyyyMMddHHmmssZZZZZ = "yyyy-MM-dd'T'HH:mm:ssZZZZZ"
    const val DATE_FORMAT_ddMMMyyyy = "dd MMM, yyyy"

    fun getTodayDateInMilliseconds(): Long {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getDateString(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat(
            DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
           Locale.ENGLISH
        )
        return format.format(date)
    }
    fun getDateStringInFormat(input: String?, ipFormat: String, opFormat: String): String {
        try {
            input?.let {
                if (it.isNotBlank()) {
                    val selectedCalendar = Calendar.getInstance()
                    val currentCalendar = Calendar.getInstance()
                    val inputFormat = SimpleDateFormat(ipFormat, Locale.ENGLISH)
                    val date = inputFormat.parse(it)
                    date?.let {
                        selectedCalendar.time = date
                        currentCalendar.set(Calendar.DAY_OF_MONTH, selectedCalendar.get(Calendar.DAY_OF_MONTH))
                        currentCalendar.set(Calendar.MONTH, selectedCalendar.get(Calendar.MONTH))
                        currentCalendar.set(Calendar.YEAR, selectedCalendar.get(Calendar.YEAR))
                        val outputFormat =
                            SimpleDateFormat(opFormat, Locale.ENGLISH)
                        return outputFormat.format(currentCalendar.time)
                    }
                }
            }
        } catch (e: Exception) {
            return ""
        }
        return ""
    }

    fun formatStringToDate(dateString: String): Date? {
        val defaultFormat = getDateDDMMYYYY()
        return defaultFormat.parse(dateString)
    }

    fun getDateDDMMYYYY() = SimpleDateFormat(DATE_ddMMyyyy, Locale.ENGLISH)

    fun convertDateTimeToDate(
        inputText: String?,
        inputFormat: String,
        outputFormat: String,
        inUserTimeZone: Boolean? = false) : String {
        try {
            inputText?.let {
                if (it.isNotBlank()) {
                    var userTimeZone: TimeZone? = null
                    val isTimeZoneFormat = inputFormat == DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    if (isTimeZoneFormat || inUserTimeZone == true)
                        getTimeZoneInput(inputText, isTimeZoneFormat)?.let { timeZone ->
                            userTimeZone = timeZone
                        }
                    val sdfInput = SimpleDateFormat(inputFormat, Locale.ENGLISH)
                    userTimeZone?.let {
                        sdfInput.timeZone = userTimeZone
                    }
                    val date = sdfInput.parse(it)
                    date?.let {
                        val sdfOutput = SimpleDateFormat(outputFormat, Locale.ENGLISH)
                        userTimeZone?.let {
                            sdfOutput.timeZone = userTimeZone
                        }
                        return sdfOutput.format(date)
                    }
                }
            }
        } catch (e: Exception) {
            return ""
        }
        return ""

    }

    private fun getTimeZoneInput(inputText: String, timeZoneFormat: Boolean): TimeZone? {
        var timeZoneInput = SecuredPreference.getTimeZoneId()
        if (timeZoneInput.isNullOrBlank() && timeZoneFormat) {
            timeZoneInput = "GMT${
                if (inputText.contains("+")) inputText.substring(inputText.indexOf("+"))
                else inputText.substring(inputText.indexOf("-"))
            }"
        } else if (!timeZoneInput.isNullOrBlank())
            timeZoneInput = "GMT$timeZoneInput"

        return TimeZone.getTimeZone(timeZoneInput)
    }

    fun parseTextInUserTimeZone(inputText: String?, inputFormat: String, outputFormat: String) : String {
        try {
            val timeZoneInput = SecuredPreference.getTimeZoneId()
            inputText?.let {
                if (it.isNotBlank() && timeZoneInput != null) {
                    val userTimeZone = TimeZone.getTimeZone(timeZoneInput)
                    val sdfInput = SimpleDateFormat(inputFormat, Locale.ENGLISH)
                    if (userTimeZone != null) {
                        sdfInput.timeZone = userTimeZone
                    }
                    val date = sdfInput.parse(it)
                    date?.let {
                        val sdfOutput = SimpleDateFormat(outputFormat, Locale.ENGLISH)
                        if (userTimeZone != null)
                            sdfOutput.timeZone = userTimeZone
                        return sdfOutput.format(date)
                    }
                }
            }
        } catch (e: Exception) {
            return ""
        }
        return ""

    }

    fun getYearMonthAndDate(dateString: String): Triple<Int?, Int?, Int?> {
        try {
            val date = formatStringToDate(dateString)
            date?.let {
                val cal = Calendar.getInstance()
                cal.time = date
                return Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE))
            }
        }catch (exception:Exception){
            return Triple(null, null, null)
        }
        return Triple(null, null, null)
    }

    fun getDatePatternDDMMYYYY() = SimpleDateFormat(DATE_FORMAT_ddMMyyyy, Locale.ENGLISH)

    fun getAge(dateOfBirth: String): String {
        var year = 0
        try {
            val sdf = SimpleDateFormat(DATE_FORMAT_ddMMyyyy, Locale.getDefault())
            val date = Calendar.getInstance()
            val today = Calendar.getInstance()

            sdf.parse(dateOfBirth)?.let {
                date.time = it
                year = today.get(Calendar.YEAR) - date.get(Calendar.YEAR)

                if ((date.get(Calendar.MONTH) > today.get(Calendar.MONTH)) ||
                    (date.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                            && date.get(Calendar.DATE) > today.get(Calendar.DATE))
                ) {
                    year--
                }
            }

        } catch (ex: Exception) {
            /* Catch block*/
        } finally {
            if (year > 0) year.toString() else "0"
        }
        return if (year > 0) year.toString() else "0"
    }

    fun getDOBFromAge(ageInYrs: String): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.YEAR, -(ageInYrs.toInt()))
        return getDateDDMMYYYY().format(calendar.time)
    }

    fun getTodayDateDDMMYYYY(): String {
        val calendar = Calendar.getInstance()
        return getDateString(calendar.time.time)
    }

    fun convertMilliSecondsToDate(milliSeconds: Long, format: String): String {
        val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
        return simpleDateFormat.format(milliSeconds)
    }

    fun getCurrentDateTime(inputFormat: String) : String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat(inputFormat, Locale.ENGLISH).format(calendar.time)
    }

    fun getCurrentDateTimeInUserTimeZone(inputFormat: String): String {
        val calendar = Calendar.getInstance()
        return parseDateWithTimeZone(inputFormat).format(calendar.time)
    }

    fun parseDateWithTimeZone(inputFormat : String): SimpleDateFormat {
        val sdf = SimpleDateFormat(inputFormat, Locale.ENGLISH)
        SecuredPreference.getTimeZoneId()?.let {
            sdf.timeZone = TimeZone.getTimeZone("GMT$it")
        }
        return sdf
    }
}