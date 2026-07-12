package com.app.buchin.constant

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import com.app.buchin.activity.DrinkDetailActivity
import com.app.buchin.listener.IGetDateListener
import com.app.buchin.model.Drink
import com.app.buchin.utils.StringUtil
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

object GlobalFuntion {

    fun startActivity(context: Context?, clz: Class<*>?) {
        val intent = Intent(context, clz)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)
    }

    fun startActivity(context: Context?, clz: Class<*>?, bundle: Bundle) {
        val intent = Intent(context, clz)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)
    }

    fun hideSoftKeyboard(activity: Activity?) {
        try {
            val inputMethodManager = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }
    }

    fun hideSoftKeyboard(activity: Activity?, editText: EditText?) {
        try {
            val inputMethodManager = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(editText?.windowToken, 0)
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }
    }

    fun getTextSearch(input: String?): String? {
        val nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("")
    }

    fun showDatePicker(context: Context?, currentDate: String?, getDateListener: IGetDateListener?) {
        if (context == null) {
            return
        }
        val mCalendar = Calendar.getInstance()
        var currentDay = mCalendar[Calendar.DATE]
        var currentMonth = mCalendar[Calendar.MONTH]
        var currentYear = mCalendar[Calendar.YEAR]
        mCalendar[currentYear, currentMonth] = currentDay
        if (!StringUtil.isEmpty(currentDate)) {
            val split: Array<String?> = currentDate!!.split("/".toRegex()).toTypedArray()
            currentDay = split[0]!!.toInt()
            currentMonth = split[1]!!.toInt()
            currentYear = split[2]!!.toInt()
            mCalendar[currentYear, currentMonth - 1] = currentDay
        }
        val callBack = OnDateSetListener { _: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
            val date = StringUtil.getDoubleNumber(dayOfMonth) + "/" +
                    StringUtil.getDoubleNumber(monthOfYear + 1) + "/" + year
            getDateListener?.getDate(date)
        }
        val datePicker = DatePickerDialog(context,
                callBack, mCalendar[Calendar.YEAR], mCalendar[Calendar.MONTH],
                mCalendar[Calendar.DATE])
        datePicker.show()
    }

    fun showToast(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun goToDrinkDetailActivity(context: Context?, drink: Drink?) {
        val bundle = Bundle()
        bundle.putSerializable(Constants.KEY_INTENT_DRINK_OBJECT, drink)
        startActivity(context, DrinkDetailActivity::class.java, bundle)
    }
}