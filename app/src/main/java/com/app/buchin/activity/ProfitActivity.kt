package com.app.buchin.activity

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.MyApplication
import com.app.buchin.R
import com.app.buchin.adapter.ProfitAdapter
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

class ProfitActivity : BaseActivity() {

    private var tvTotalProfit: TextView? = null
    private var tvDateFrom: TextView? = null
    private var tvDateTo: TextView? = null
    private var rcvData: RecyclerView? = null
    private var mListProfit: MutableList<Profit>? = null
    private var mProfitAdapter: ProfitAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profit)
        initToolbar()
        initUi()
        getListProfit()
    }

    private fun initToolbar() {
        if (supportActionBar == null) {
            return
        }
        supportActionBar!!.title = getString(R.string.feature_profit)
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
        tvTotalProfit = findViewById<TextView?>(R.id.tv_total_profit)
        rcvData = findViewById<RecyclerView?>(R.id.rcv_data)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvData?.layoutManager = linearLayoutManager
        tvDateFrom?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                GlobalFuntion.showDatePicker(this@ProfitActivity, tvDateFrom?.text.toString(), object : IGetDateListener {
                    override fun getDate(date: String?) {
                        tvDateFrom?.text = date
                        getListProfit()
                    }
                })
            }
        })
        tvDateTo?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                GlobalFuntion.showDatePicker(this@ProfitActivity, tvDateTo?.text.toString(), object : IGetDateListener {
                    override fun getDate(date: String?) {
                        tvDateTo?.text = date
                        getListProfit()
                    }
                })
            }
        })
    }

    private fun getListProfit() {
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
        if (mListProfit != null) {
            mListProfit!!.clear()
        } else {
            mListProfit = ArrayList()
        }
        for (history in list) {
            val drinkId = history.getDrinkId()
            if (checkProfitExist(drinkId)) {
                getProfitFromDrinkId(drinkId)?.getHistories()?.add(history)
            } else {
                val profit = Profit()
                profit.setDrinkId(history.getDrinkId())
                profit.setDrinkName(history.getDrinkName())
                profit.setDrinkUnitId(history.getUnitId())
                profit.setDrinkUnitName(history.getUnitName())
                profit.getHistories()!!.add(history)
                mListProfit!!.add(profit)
            }
        }
        mProfitAdapter = ProfitAdapter(this, mListProfit, object : ProfitAdapter.IManagerProfitListener {
            override fun onClickItem(profit: Profit?) {
                val drink = profit?.let {
                    Drink(it.getDrinkId(), it.getDrinkName(),
                            it.getDrinkUnitId(), it.getDrinkUnitName())
                }
                GlobalFuntion.goToDrinkDetailActivity(this@ProfitActivity, drink)
            }
        })
        rcvData?.adapter = mProfitAdapter

        // Calculate total
        val profitValue = getTotalProfit()
        val strTotalProfit: String
        if (profitValue > 0) {
            tvTotalProfit?.setTextColor(resources.getColor(R.color.green))
            strTotalProfit = "+" + profitValue + Constants.CURRENCY
        } else if (profitValue == 0) {
            tvTotalProfit?.setTextColor(resources.getColor(R.color.yellow))
            strTotalProfit = profitValue.toString() + Constants.CURRENCY
        } else {
            tvTotalProfit?.setTextColor(resources.getColor(R.color.background_red))
            strTotalProfit = profitValue.toString() + Constants.CURRENCY
        }
        tvTotalProfit?.text = strTotalProfit
    }

    private fun checkProfitExist(drinkId: Long): Boolean {
        if (mListProfit == null || mListProfit!!.isEmpty()) {
            return false
        }
        var result = false
        for (profit in mListProfit!!) {
            if (drinkId == profit.getDrinkId()) {
                result = true
                break
            }
        }
        return result
    }

    private fun getProfitFromDrinkId(drinkId: Long): Profit? {
        var result: Profit? = null
        for (profit in mListProfit!!) {
            if (drinkId == profit.getDrinkId()) {
                result = profit
                break
            }
        }
        return result
    }

    private fun getTotalProfit(): Int {
        if (mListProfit == null || mListProfit!!.isEmpty()) {
            return 0
        }
        var total = 0
        for (profit in mListProfit!!) {
            total += profit.getProfit()
        }
        return total
    }

    override fun onDestroy() {
        super.onDestroy()
        mProfitAdapter?.release()
    }
}
