package com.app.buchin.utils

object StringUtil {
    fun isEmpty(input: String?): Boolean {
        return input == null || input.isEmpty() || "" == input.trim { it <= ' ' }
    }

    fun getDoubleNumber(number: Int): String {
        return if (number < 10) {
            "0$number"
        } else "" + number
    }
}