package com.app.buchin.activity

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.MyApplication
import com.app.buchin.R
import com.app.buchin.adapter.StatisticalAdapter
import com.app.buchin.constant.Constants
import com.app.buchin.constant.GlobalFuntion
import com.app.buchin.listener.IGetDateListener
import com.app.buchin.listener.IOnSingleClickListener
import com.app.buchin.model.*
import com.app.buchin.utils.DateTimeUtils
import com.app.buchin.utils.StringUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*

class StatisticalActivity : BaseActivity() {

    private var tvTotalValue: TextView? = null
    private var tvDateFrom: TextView? = null
    private var tvDateTo: TextView? = null
    private var rcvData: RecyclerView? = null
    private var mType = 0
    private var isDrinkPopular = false
    private var mListStatisticals: MutableList<Statistical>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistical)
        getDataIntent()
        initToolbar()
        initUi()
        getListStatistical()
    }

    private fun getDataIntent() {
        val bundle = intent.extras ?: return
        mType = bundle.getInt(Constants.KEY_TYPE_STATISTICAL)
        isDrinkPopular = bundle.getBoolean(Constants.KEY_DRINK_POPULAR)
    }

    private fun initToolbar() {
        if (supportActionBar == null) {
            return
        }
        when (mType) {
            Constants.TYPE_REVENUE -> supportActionBar!!.title = getString(R.string.feature_revenue)
            Constants.TYPE_COST -> supportActionBar!!.title = getString(R.string.feature_cost)
        }
        if (isDrinkPopular) {
            supportActionBar!!.title = getString(R.string.feature_drink_popular)
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initUi() {
        tvDateFrom = findViewById<TextView?>(R.id.tv_date_from)
        tvDateTo = findViewById<TextView?>(R.id.tv_date_to)
        tvTotalValue = findViewById<TextView?>(R.id.tv_total_value)
        rcvData = findViewById<RecyclerView?>(R.id.rcv_data)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvData?.layoutManager = linearLayoutManager
        val layoutFilter = findViewById<LinearLayout?>(R.id.layout_filter)
        val viewDivider = findViewById<View?>(R.id.view_divider)
        val layoutBottom = findViewById<RelativeLayout?>(R.id.layout_bottom)
        if (isDrinkPopular) {
            layoutFilter?.visibility = View.GONE
            viewDivider?.visibility = View.GONE
            layoutBottom?.visibility = View.GONE
        } else {
            layoutFilter?.visibility = View.VISIBLE
            viewDivider?.visibility = View.VISIBLE
            layoutBottom?.visibility = View.VISIBLE
        }
        val labelTotalValue = findViewById<TextView?>(R.id.label_total_value)
        when (mType) {
            Constants.TYPE_REVENUE -> labelTotalValue?.text = getString(R.string.label_total_revenue)
            Constants.TYPE_COST -> labelTotalValue?.text = getString(R.string.label_total_cost)
        }
        tvDateFrom?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                GlobalFuntion.showDatePicker(this@StatisticalActivity, tvDateFrom?.text.toString(), object : IGetDateListener {
                    override fun getDate(date: String?) {
                        tvDateFrom?.text = date
                        getListStatistical()
                    }
                })
            }
        })
        tvDateTo?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                GlobalFuntion.showDatePicker(this@StatisticalActivity, tvDateTo?.text.toString(), object : IGetDateListener {
                    override fun getDate(date: String?) {
                        tvDateTo?.text = date
                        getListStatistical()
                    }
                })
            }
        })
    }

    private fun getListStatistical() {
        MyApplication[this].getHistoryDatabaseReference()
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val list: MutableList<History> = ArrayList()
                        for (dataSnapshot in snapshot.children) {
                            val history: History? = dataSnapshot.getValue<History>(History::class.java)
                            if (canAddHistory(history) && history != null) {
                                list.add(history)
                            }
                        }
                        handleDataHistories(list)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        showToast(databaseError.message)
                    }
                })
    }

    private fun canAddHistory(history: History?): Boolean {
        if (history == null) {
            return false
        }
        if (Constants.TYPE_REVENUE == mType) {
            if (history.isAdd()) {
                return false
            }
        } else {
            if (!history.isAdd()) {
                return false
            }
        }
        val strDateFrom = tvDateFrom?.text.toString()
        val strDateTo = tvDateTo?.text.toString()
        if (StringUtil.isEmpty(strDateFrom) && StringUtil.isEmpty(strDateTo)) {
            return true
        }
        if (StringUtil.isEmpty(strDateFrom) && !StringUtil.isEmpty(strDateTo)) {
            val longDateTo = DateTimeUtils.convertDateToTimeStamp(strDateTo).toLong()
            return history.getDate() <= longDateTo
        }
        if (!StringUtil.isEmpty(strDateFrom) && StringUtil.isEmpty(strDateTo)) {
            val longDateFrom = DateTimeUtils.convertDateToTimeStamp(strDateFrom).toLong()
            return history.getDate() >= longDateFrom
        }
        val longDateTo = DateTimeUtils.convertDateToTimeStamp(strDateTo).toLong()
        val longDateFrom = DateTimeUtils.convertDateToTimeStamp(strDateFrom).toLong()
        return history.getDate() in longDateFrom..longDateTo
    }

    private fun handleDataHistories(list: MutableList<History>?) {
        if (list == null || list.isEmpty()) {
            return
        }
        if (mListStatisticals != null) {
            mListStatisticals!!.clear()
        } else {
            mListStatisticals = ArrayList()
        }
        for (history in list) {
            val drinkId = history.getDrinkId()
            if (checkStatisticalExist(drinkId)) {
                getStatisticalFromDrinkId(drinkId)?.getHistories()?.add(history)
            } else {
                val statistical = Statistical()
                statistical.setDrinkId(history.getDrinkId())
                statistical.setDrinkName(history.getDrinkName())
                statistical.setDrinkUnitId(history.getUnitId())
                statistical.setDrinkUnitName(history.getUnitName())
                statistical.getHistories()!!.add(history)
                mListStatisticals!!.add(statistical)
            }
        }
        if (isDrinkPopular) {
            val listPopular: MutableList<Statistical> = ArrayList(mListStatisticals!!)
            listPopular.sortWith(Comparator { statistical1: Statistical?, statistical2: Statistical? -> statistical2!!.getTotalPrice() - statistical1!!.getTotalPrice() })
            val statisticalAdapter = StatisticalAdapter(listPopular, object : StatisticalAdapter.IManagerStatisticalListener {
                override fun onClickItem(statistical: Statistical?) {
                    val drink = statistical?.let {
                        Drink(it.getDrinkId(), it.getDrinkName(),
                                it.getDrinkUnitId(), it.getDrinkUnitName())
                    }
                    GlobalFuntion.goToDrinkDetailActivity(this@StatisticalActivity, drink)
                }
            })
            rcvData?.adapter = statisticalAdapter
        } else {
            val statisticalAdapter = StatisticalAdapter(mListStatisticals, object : StatisticalAdapter.IManagerStatisticalListener {
                override fun onClickItem(statistical: Statistical?) {
                    val drink = statistical?.let {
                        Drink(it.getDrinkId(), it.getDrinkName(),
                                it.getDrinkUnitId(), it.getDrinkUnitName())
                    }
                    GlobalFuntion.goToDrinkDetailActivity(this@StatisticalActivity, drink)
                }
            })
            rcvData?.adapter = statisticalAdapter
        }

        // Calculate total
        val strTotalValue = getTotalValues().toString() + Constants.CURRENCY
        tvTotalValue?.text = strTotalValue
    }

    private fun checkStatisticalExist(drinkId: Long): Boolean {
        if (mListStatisticals == null || mListStatisticals!!.isEmpty()) {
            return false
        }
        var result = false
        for (statistical in mListStatisticals!!) {
            if (drinkId == statistical.getDrinkId()) {
                result = true
                break
            }
        }
        return result
    }

    private fun getStatisticalFromDrinkId(drinkId: Long): Statistical? {
        var result: Statistical? = null
        for (statistical in mListStatisticals!!) {
            if (drinkId == statistical.getDrinkId()) {
                result = statistical
                break
            }
        }
        return result
    }

    private fun getTotalValues(): Int {
        if (mListStatisticals == null || mListStatisticals!!.isEmpty()) {
            return 0
        }
        var total = 0
        for (statistical in mListStatisticals!!) {
            total += statistical.getTotalPrice()
        }
        return total
    }
}
