package com.medtroniclabs.opensource.common

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.widget.DatePicker
import com.medtroniclabs.opensource.R
import java.lang.reflect.Field
import java.util.*


object ViewUtil {


    fun getResId(resName: String, c: Class<*>): Int {
        return try {
            val idField: Field = c.getDeclaredField(resName)
            idField.getInt(idField)
        } catch (e: Exception) {
            -1
        }
    }

    fun showDatePicker(
        context: Context,
        disableFutureDate: Boolean = false,
        minDate: Long? = null,
        maxDate: Long? = null,
        date : Triple<Int?,Int?,Int?>?=null,
        cancelCallBack: (() -> Unit)? = null,
        callBack: (dialog: DatePicker, year: Int, month: Int, dayOfMonth: Int) -> Unit,
    ): DatePickerDialog {

        val calendar = Calendar.getInstance()
        var thisYear = calendar.get(Calendar.YEAR)
        var thisMonth = calendar.get(Calendar.MONTH)
        var thisDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dialog: DatePickerDialog?

        if (date?.first != null && date.second != null && date.third != null) {
            thisYear = date.first!!
            thisMonth = date.second!!
            thisDay = date.third!!
        }

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
                callBack.invoke(datePicker, year, month + 1, dayOfMonth)
            }

        dialog = DatePickerDialog(
            context,
            dateSetListener,
            thisYear,
            thisMonth,
            thisDay
        )

        if (cancelCallBack != null) {
            dialog.setOnCancelListener {
                cancelCallBack.invoke()
            }
        }

        minDate?.let {
            dialog.datePicker.minDate = it
        }
        maxDate?.let {
            dialog.datePicker.maxDate = it
        }

        if (disableFutureDate) dialog.datePicker.maxDate = System.currentTimeMillis()

        dialog.setCancelable(false)

        dialog.show()

        return dialog

    }

    fun getStartDate(): Long {
        val todayDate = Calendar.getInstance()
        todayDate.set(Calendar.HOUR, todayDate.getActualMinimum(Calendar.HOUR))
        todayDate.set(Calendar.HOUR_OF_DAY, todayDate.getActualMinimum(Calendar.HOUR_OF_DAY))
        todayDate.set(Calendar.MINUTE, todayDate.getActualMinimum(Calendar.MINUTE))
        todayDate.set(Calendar.SECOND, todayDate.getActualMinimum(Calendar.SECOND))
        todayDate.set(Calendar.MILLISECOND, todayDate.getActualMinimum(Calendar.MILLISECOND))
        return todayDate.timeInMillis
    }

    fun getEndDate(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR, calendar.getActualMaximum(Calendar.HOUR))
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE))
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND))
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND))
        return calendar.timeInMillis
    }

    fun statusCheck(context: Context) {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        manager?.let { locationManager ->
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps(context)
            }
        }

    }

    private fun buildAlertMessageNoGps(context: Context) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.gps_enabled))
            .setCancelable(false)
            .setPositiveButton(
                context.getText(R.string.yes)
            ) { dialog, _ ->
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialog.cancel()
            }
            .setNegativeButton(
                context.getText(R.string.no)
            ) { dialog, _ ->
                dialog.cancel()
            }
        val alert = builder.create()
        alert.show()
    }


}