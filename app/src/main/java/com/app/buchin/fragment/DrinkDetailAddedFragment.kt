package com.app.buchin.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.MyApplication
import com.app.buchin.R
import com.app.buchin.adapter.HistoryAdapter
import com.app.buchin.adapter.HistoryAdapter.IManagerHistoryListener
import com.app.buchin.constant.Constants
import com.app.buchin.constant.GlobalFuntion
import com.app.buchin.listener.IOnSingleClickListener
import com.app.buchin.model.Drink
import com.app.buchin.model.History
import com.app.buchin.utils.DateTimeUtils
import com.app.buchin.utils.StringUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class DrinkDetailAddedFragment(private val mDrink: Drink?) : Fragment() {

    private var mView: View? = null
    private var tvTotalPrice: TextView? = null
    private var tvTotalQuantity: TextView? = null
    private var mListHistory: MutableList<History>? = null
    private var mHistoryAdapter: HistoryAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_drink_detail_added, container, false)
        initUi()
        getListHistoryAdded()
        return mView
    }

    private fun initUi() {
        tvTotalQuantity = mView?.findViewById<TextView?>(R.id.tv_total_quantity)
        tvTotalPrice = mView?.findViewById<TextView?>(R.id.tv_total_price)
        val rcvHistory: RecyclerView? = mView?.findViewById(R.id.rcv_history)
        val linearLayoutManager = LinearLayoutManager(activity)
        rcvHistory?.layoutManager = linearLayoutManager
        mListHistory = ArrayList()
        mHistoryAdapter = HistoryAdapter(mListHistory, true, object : IManagerHistoryListener {
            override fun editHistory(history: History?) {
                onClickAddOrEditHistory(history)
            }

            override fun deleteHistory(history: History?) {
                onClickDeleteHistory(history)
            }

            override fun onClickItemHistory(history: History?) {}
        })
        rcvHistory?.adapter = mHistoryAdapter
        val fabAddData: FloatingActionButton? = mView?.findViewById(R.id.fab_add_data)
        fabAddData?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                onClickAddOrEditHistory(null)
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getListHistoryAdded() {
        if (activity == null) {
            return
        }
        MyApplication[activity].getHistoryDatabaseReference()
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (mListHistory != null) {
                            mListHistory!!.clear()
                        }
                        for (dataSnapshot in snapshot.children) {
                            val history: History? = dataSnapshot.getValue<History>(History::class.java)
                            if (history != null) {
                                if (mDrink?.getId() == history.getDrinkId() && history.isAdd()) {
                                    mListHistory?.add(0, history)
                                }
                            }
                        }
                        mHistoryAdapter?.notifyDataSetChanged()
                        displayLayoutBottomInfor()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(activity, getString(R.string.msg_get_data_error),
                                Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun displayLayoutBottomInfor() {
        // Calculator quantity
        val strTotalQuantity = getTotalQuantity().toString() + " " + mDrink?.getUnitName()
        tvTotalQuantity?.text = strTotalQuantity
        // Calculator price
        val strTotalPrice = getTotalPrice().toString() + Constants.CURRENCY
        tvTotalPrice?.text = strTotalPrice
    }

    private fun getTotalQuantity(): Int {
        if (mListHistory == null || mListHistory!!.isEmpty()) {
            return 0
        }
        var totalQuantity = 0
        for (history in mListHistory!!) {
            totalQuantity += history.getQuantity()
        }
        return totalQuantity
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

    private fun onClickAddOrEditHistory(history: History?) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_detail_drink_edit)
        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        // Get view
        val tvTitleDialog = dialog.findViewById<TextView?>(R.id.tv_title_dialog)
        val tvDrinkName = dialog.findViewById<TextView?>(R.id.tv_drink_name)
        val edtQuantity = dialog.findViewById<EditText?>(R.id.edt_quantity)
        val tvUnitName = dialog.findViewById<TextView?>(R.id.tv_unit_name)
        val edtPrice = dialog.findViewById<EditText?>(R.id.edt_price)
        val tvDialogCancel = dialog.findViewById<TextView?>(R.id.tv_dialog_cancel)
        val tvDialogAdd = dialog.findViewById<TextView?>(R.id.tv_dialog_add)

        // Set data
        if (history == null) {
            tvTitleDialog?.text = getString(R.string.feature_add_drink)
            tvDrinkName?.text = mDrink?.getName()
            tvUnitName?.text = mDrink?.getUnitName()
        } else {
            tvTitleDialog?.text = getString(R.string.edit_history_add)
            tvDrinkName?.text = history.getDrinkName()
            tvUnitName?.text = history.getUnitName()
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
                    GlobalFuntion.showToast(activity, getString(R.string.msg_enter_full_infor))
                    return
                }
                if (history == null) {
                    val historyObject = History()
                    historyObject.setId(System.currentTimeMillis())
                    historyObject.setDrinkId(mDrink!!.getId())
                    historyObject.setDrinkName(mDrink.getName())
                    historyObject.setUnitId(mDrink.getUnitId())
                    historyObject.setUnitName(mDrink.getUnitName())
                    historyObject.setQuantity(strQuantity.toInt())
                    historyObject.setPrice(strPrice.toInt())
                    historyObject.setTotalPrice(historyObject.getQuantity() * historyObject.getPrice())
                    historyObject.setAdd(true)
                    val currentDate = SimpleDateFormat(DateTimeUtils.DEFAULT_FORMAT_DATE, Locale.ENGLISH).format(Date())
                    val strDate = DateTimeUtils.convertDateToTimeStamp(currentDate)
                    historyObject.setDate(strDate.toLong())
                    if (activity != null) {
                        MyApplication[activity].getHistoryDatabaseReference()
                                .child(historyObject.getId().toString())
                                .setValue(historyObject) { databaseError: DatabaseError?, _: DatabaseReference? ->
                                    if (databaseError != null) {
                                        GlobalFuntion.showToast(activity, databaseError.message)
                                        return@setValue
                                    }
                                    GlobalFuntion.showToast(activity, getString(R.string.msg_add_drink_success))
                                    changeQuantity(historyObject.getDrinkId(), historyObject.getQuantity(), true)
                                    GlobalFuntion.hideSoftKeyboard(activity)
                                    dialog.dismiss()
                                }
                    }
                } else {
                    // Edit history
                    val map: MutableMap<String?, Any?> = HashMap()
                    map["quantity"] = strQuantity.toInt()
                    map["price"] = strPrice.toInt()
                    map["totalPrice"] = strQuantity.toInt() * strPrice.toInt()
                    if (activity != null) {
                        MyApplication[activity].getHistoryDatabaseReference()
                                .child(history.getId().toString())
                                .updateChildren(map) { databaseError: DatabaseError?, _: DatabaseReference? ->
                                    if (databaseError != null) {
                                        GlobalFuntion.showToast(activity, databaseError.message)
                                        return@updateChildren
                                    }
                                    GlobalFuntion.hideSoftKeyboard(activity)
                                    GlobalFuntion.showToast(activity, getString(R.string.msg_edit_add_history_success))
                                    changeQuantity(history.getDrinkId(), strQuantity.toInt() - history.getQuantity(), true)
                                    dialog.dismiss()
                                }
                    }
                }
            }
        })
        dialog.show()
    }

    private fun changeQuantity(drinkId: Long, quantity: Int, isAdd: Boolean) {
        if (activity == null) {
            return
        }
        MyApplication[activity].getQuantityDatabaseReference(drinkId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentQuantity = snapshot.getValue<Int>(Int::class.java) ?: 0
                        val totalQuantity = if (isAdd) {
                            currentQuantity + quantity
                        } else {
                            currentQuantity - quantity
                        }
                        if (activity != null) {
                            MyApplication[activity].getQuantityDatabaseReference(drinkId)
                                    .setValue(totalQuantity) { databaseError: DatabaseError?, _: DatabaseReference? ->
                                        if (databaseError != null) {
                                            GlobalFuntion.showToast(activity, databaseError.message)
                                        }
                                    }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        GlobalFuntion.showToast(activity, error.message)
                    }
                })
    }

    private fun onClickDeleteHistory(history: History?) {
        if (activity == null) {
            return
        }
        AlertDialog.Builder(activity)
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_delete)) { _: DialogInterface?, _: Int ->
                    MyApplication[activity].getHistoryDatabaseReference()
                            .child(history?.getId().toString())
                            .removeValue { databaseError: DatabaseError?, _: DatabaseReference? ->
                                if (databaseError != null) {
                                    GlobalFuntion.showToast(activity, databaseError.message)
                                    return@removeValue
                                }
                                GlobalFuntion.showToast(activity, getString(R.string.msg_delete_add_history_success))
                                changeQuantity(history!!.getDrinkId(), history.getQuantity(), false)
                                GlobalFuntion.hideSoftKeyboard(activity)
                            }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }
}
