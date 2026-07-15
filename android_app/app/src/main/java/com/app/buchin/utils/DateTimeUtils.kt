package com.app.buchin.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    const val DEFAULT_FORMAT_DATE: String = "dd/MM/yyyy"
    fun convertDateToTimeStamp(strDate: String?): String {
        var result = ""
        if (strDate != null) {
            try {
                val format = SimpleDateFormat(DEFAULT_FORMAT_DATE, Locale.ENGLISH)
                val date = format.parse(strDate)
                if (date != null) {
                    val timestamp = date.time / 1000
                    result = timestamp.toString()
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return result
    }

    fun convertTimeStampToDate(strTimeStamp: String?): String {
        var result = ""
        if (strTimeStamp != null) {
            try {
                val floatTimestamp = strTimeStamp.toFloat()
                val timestamp = (floatTimestamp * 1000).toLong()
                val sdf = SimpleDateFormat(DEFAULT_FORMAT_DATE, Locale.ENGLISH)
                val date = Date(timestamp)
                result = sdf.format(date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }
}