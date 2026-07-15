package com.app.buchin.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.MyApplication
import com.app.buchin.R
import com.app.buchin.adapter.HistoryAdapter
import com.app.buchin.adapter.HistoryAdapter.IManagerHistoryListener
import com.app.buchin.adapter.SelectDrinkAdapter
import com.app.buchin.constant.Constants
import com.app.buchin.constant.GlobalFuntion
import com.app.buchin.listener.IGetDateListener
import com.app.buchin.listener.IOnSingleClickListener
import com.app.buchin.model.*
import com.app.buchin.utils.DateTimeUtils
import com.app.buchin.utils.StringUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class HistoryDrinkActivity : BaseActivity() {

    private var mTvDateSelected: TextView? = null
    private var tvTotalPrice: TextView? = null
    private var mListDrink: MutableList<Drink>? = null
    private var mListHistory: MutableList<History>? = null
    private var mHistoryAdapter: HistoryAdapter? = null
    private var mDrinkSelected: Drink? = null
    private var isDrinkUsed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_drink)
        getDataIntent()
        initToolbar()
        initUi()
        getListDrinks()
    }

    private fun getDataIntent() {
        val bundle = intent.extras ?: return
        isDrinkUsed = bundle.getBoolean(Constants.KEY_INTENT_DRINK_USED)
    }

    private fun initToolbar() {
        if (supportActionBar == null) {
            return
        }
        if (isDrinkUsed) {
            supportActionBar!!.title = getString(R.string.feature_drink_used)
        } else {
            supportActionBar!!.title = getString(R.string.feature_add_drink)
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
        val tvListTitle = findViewById<TextView?>(R.id.tv_list_title)
        if (isDrinkUsed) {
            tvListTitle?.text = getString(R.string.list_drink_used)
        } else {
            tvListTitle?.text = getString(R.string.list_drink_buy)
        }
        tvTotalPrice = findViewById<TextView?>(R.id.tv_total_price)
        mTvDateSelected = findViewById<TextView?>(R.id.tv_date_selected)
        val currentDate = SimpleDateFormat(DateTimeUtils.DEFAULT_FORMAT_DATE, Locale.ENGLISH).format(Date())
        mTvDateSelected?.text = currentDate
        getListHistoryDrinkOfDate(currentDate)
        val layoutSelectDate = findViewById<RelativeLayout?>(R.id.layout_select_date)
        layoutSelectDate?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                GlobalFuntion.showDatePicker(this@HistoryDrinkActivity,
                        mTvDateSelected?.text.toString(), object : IGetDateListener{
                    override fun getDate(date: String?) {
                        mTvDateSelected?.text = date
                        getListHistoryDrinkOfDate(date)
                    }
                })
            }
        })
        val fabAddData = findViewById<FloatingActionButton?>(R.id.fab_add_data)
        fabAddData?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                onClickAddOrEditHistory(null)
            }
        })
        val rcvHistory = findViewById<RecyclerView?>(R.id.rcv_history)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvHistory?.layoutManager = linearLayoutManager
        mListDrink = ArrayList()
        mListHistory = ArrayList()
        mHistoryAdapter = HistoryAdapter(mListHistory, false,
                object : IManagerHistoryListener {
                    override fun editHistory(history: History?) {
                        onClickAddOrEditHistory(history)
                    }

                    override fun deleteHistory(history: History?) {
                        onClickDeleteHistory(history)
                    }

                    override fun onClickItemHistory(history: History?) {
                        val drink = history?.let {
                            Drink(it.getDrinkId(), it.getDrinkName(),
                                    it.getUnitId(), it.getUnitName())
                        }
                        GlobalFuntion.goToDrinkDetailActivity(this@HistoryDrinkActivity, drink)
                    }
                })
        rcvHistory?.adapter = mHistoryAdapter
        rcvHistory?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    fabAddData?.hide()
                } else {
                    fabAddData?.show()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun getListDrinks() {
        MyApplication[this].getDrinkDatabaseReference().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (mListDrink != null) mListDrink!!.clear()
                for (dataSnapshot in snapshot.children) {
                    val drink: Drink? = dataSnapshot.getValue<Drink>(Drink::class.java)
                    if (drink != null) {
                        mListDrink?.add(0, drink)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(getString(R.string.msg_get_data_error))
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getListHistoryDrinkOfDate(date: String?) {
        val longDate = DateTimeUtils.convertDateToTimeStamp(date).toLong()
        MyApplication[this].getHistoryDatabaseReference()
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (mListHistory != null) mListHistory!!.clear()
                        for (dataSnapshot in snapshot.children) {
                            val history: History? = dataSnapshot.getValue<History>(History::class.java)
                            if (history != null) {
                                if (longDate == history.getDate()) {
                                    addHistoryToList(history)
                                }
                            }
                        }
                        mHistoryAdapter?.notifyDataSetChanged()

                        // Calculator price
                        val strTotalPrice = getTotalPrice().toString() + Constants.CURRENCY
                        tvTotalPrice?.text = strTotalPrice
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        showToast(getString(R.string.msg_get_data_error))
                    }
                })
    }

    private fun addHistoryToList(history: History?) {
        if (history == null) {
            return
        }
        if (isDrinkUsed) {
            if (!history.isAdd()) {
                mListHistory?.add(0, history)
            }
        } else {
            if (history.isAdd()) {
                mListHistory?.add(0, history)
            }
        }
    }

    private fun onClickAddOrEditHistory(history: History?) {
        if (mListDrink == null || mListDrink!!.isEmpty()) {
            showToast(getString(R.string.msg_list_drink_require))
            return
        }
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_history)
        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        // Get view
        val tvTitleDialog = dialog.findViewById<TextView?>(R.id.tv_title_dialog)
        val spnDrink = dialog.findViewById<Spinner?>(R.id.spinner_drink)
        val edtQuantity = dialog.findViewById<EditText?>(R.id.edt_quantity)
        val tvUnitName = dialog.findViewById<TextView?>(R.id.tv_unit_name)
        val edtPrice = dialog.findViewById<EditText?>(R.id.edt_price)
        val tvDialogCancel = dialog.findViewById<TextView?>(R.id.tv_dialog_cancel)
        val tvDialogAdd = dialog.findViewById<TextView?>(R.id.tv_dialog_add)

        // Set data
        if (isDrinkUsed) {
            tvTitleDialog?.text = getString(R.string.feature_drink_used)
        } else {
            tvTitleDialog?.text = getString(R.string.feature_add_drink)
        }
        val selectDrinkAdapter = SelectDrinkAdapter(this, R.layout.item_choose_option, mListDrink!!)
        spnDrink?.adapter = selectDrinkAdapter
        spnDrink?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mDrinkSelected = selectDrinkAdapter.getItem(position)
                tvUnitName?.text = mDrinkSelected?.getUnitName()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        if (history != null) {
            if (isDrinkUsed) {
                tvTitleDialog?.text = getString(R.string.edit_history_used)
            } else {
                tvTitleDialog?.text = getString(R.string.edit_history_add)
            }
            spnDrink?.setSelection(getPositionDrinkUpdate(history))
            edtQuantity?.setText(history.getQuantity().toString())
            edtPrice?.setText(history.getPrice().toString())
        }

        // Listener
        tvDialogCancel?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                dialog.dismiss()
            }
        })
        tvDialogAdd?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                val strQuantity = edtQuantity?.text.toString().trim { it <= ' ' }
                val strPrice = edtPrice?.text.toString().trim { it <= ' ' }
                if (StringUtil.isEmpty(strQuantity) || StringUtil.isEmpty(strPrice)) {
                    showToast(getString(R.string.msg_enter_full_infor))
                    return
                }
                if (history == null && mDrinkSelected != null) {
                    val newHistory = History()
                    newHistory.setId(System.currentTimeMillis())
                    newHistory.setDrinkId(mDrinkSelected!!.getId())
                    newHistory.setDrinkName(mDrinkSelected!!.getName())
                    newHistory.setUnitId(mDrinkSelected!!.getUnitId())
                    newHistory.setUnitName(mDrinkSelected!!.getUnitName())
                    newHistory.setQuantity(strQuantity.toInt())
                    newHistory.setPrice(strPrice.toInt())
                    newHistory.setTotalPrice(newHistory.getQuantity() * newHistory.getPrice())
                    newHistory.setAdd(!isDrinkUsed)
                    val strDate = DateTimeUtils.convertDateToTimeStamp(mTvDateSelected?.text.toString())
                    newHistory.setDate(strDate.toLong())
                    MyApplication[this@HistoryDrinkActivity].getHistoryDatabaseReference()
                            .child(newHistory.getId().toString())
                            .setValue(newHistory) { databaseError: DatabaseError?, _: DatabaseReference? ->
                                if (databaseError != null) {
                                    showToast(databaseError.message)
                                    return@setValue
                                }
                                if (isDrinkUsed) {
                                    showToast(getString(R.string.msg_used_drink_success))
                                } else {
                                    showToast(getString(R.string.msg_add_drink_success))
                                }
                                changeQuantity(newHistory.getDrinkId(), newHistory.getQuantity(), !isDrinkUsed)
                                GlobalFuntion.hideSoftKeyboard(this@HistoryDrinkActivity)
                                dialog.dismiss()
                            }
                    return
                }

                // Edit history
                val map: MutableMap<String?, Any?> = HashMap()
                map["drinkId"] = mDrinkSelected?.getId()
                map["drinkName"] = mDrinkSelected?.getName()
                map["unitId"] = mDrinkSelected?.getUnitId()
                map["unitName"] = mDrinkSelected?.getUnitName()
                map["quantity"] = strQuantity.toInt()
                map["price"] = strPrice.toInt()
                map["totalPrice"] = strQuantity.toInt() * strPrice.toInt()
                MyApplication[this@HistoryDrinkActivity].getHistoryDatabaseReference()
                        .child(history!!.getId().toString())
                        .updateChildren(map) { databaseError: DatabaseError?, _: DatabaseReference? ->
                            if (databaseError != null) {
                                showToast(databaseError.message)
                                return@updateChildren
                            }
                            GlobalFuntion.hideSoftKeyboard(this@HistoryDrinkActivity)
                            if (isDrinkUsed) {
                                showToast(getString(R.string.msg_edit_used_history_success))
                            } else {
                                showToast(getString(R.string.msg_edit_add_history_success))
                            }
                            updateQuantityAfterEditHistory(history, mDrinkSelected?.getId() ?: history.getDrinkId(),
                                    strQuantity.toInt())
                            dialog.dismiss()
                        }
            }
        })
        dialog.show()
    }

    private fun updateQuantityAfterEditHistory(history: History, newDrinkId: Long, newQuantity: Int) {
        val isAddHistory = !isDrinkUsed
        if (history.getDrinkId() == newDrinkId) {
            changeQuantity(history.getDrinkId(), newQuantity - history.getQuantity(), isAddHistory)
            return
        }
        changeQuantity(history.getDrinkId(), history.getQuantity(), !isAddHistory)
        changeQuantity(newDrinkId, newQuantity, isAddHistory)
    }

    private fun changeQuantity(drinkId: Long, quantity: Int, isAdd: Boolean) {
        MyApplication[this@HistoryDrinkActivity].getQuantityDatabaseReference(drinkId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentQuantity = snapshot.getValue<Int>(Int::class.java) ?: 0
                        val totalQuantity = if (isAdd) {
                            currentQuantity + quantity
                        } else {
                            currentQuantity - quantity
                        }
                        updateQuantityToFirebase(drinkId, totalQuantity)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showToast(error.message)
                    }
                })
    }

    private fun updateQuantityToFirebase(drinkId: Long, quantity: Int) {
        MyApplication[this@HistoryDrinkActivity].getQuantityDatabaseReference(drinkId)
                .setValue(quantity) { databaseError: DatabaseError?, _: DatabaseReference? ->
                    if (databaseError != null) {
                        showToast(databaseError.message)
                    }
                }
    }

    private fun getPositionDrinkUpdate(history: History?): Int {
        if (mListDrink == null || mListDrink!!.isEmpty()) {
            return 0
        }
        for (i in mListDrink!!.indices) {
            if (history?.getDrinkId() == mListDrink!![i].getId()) {
                return i
            }
        }
        return 0
    }

    private fun getTotalPrice(): Int {
        if (mListHistory == null || mListHistory!!.isEmpty()) {
            return 0
        }
        var totalPrice = 0
        for (history in mListHistory!!) {
            totalPrice += history.getTotalPrice()
        }
        return totalPrice
    }

    private fun onClickDeleteHistory(history: History?) {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_delete)) { _: DialogInterface?, _: Int ->
                    MyApplication[this@HistoryDrinkActivity].getHistoryDatabaseReference()
                            .child(history?.getId().toString())
                            .removeValue { databaseError: DatabaseError?, _: DatabaseReference? ->
                                if (databaseError != null) {
                                    showToast(databaseError.message)
                                    return@removeValue
                                }
                                if (isDrinkUsed) {
                                    showToast(getString(R.string.msg_delete_used_history_success))
                                } else {
                                    showToast(getString(R.string.msg_delete_add_history_success))
                                }
                                if (history != null) {
                                    changeQuantity(history.getDrinkId(), history.getQuantity(), isDrinkUsed)
                                }
                                GlobalFuntion.hideSoftKeyboard(this@HistoryDrinkActivity)
                            }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }
}
