package com.app.buchin.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.MyApplication
import com.app.buchin.R
import com.app.buchin.adapter.UnitAdapter
import com.app.buchin.adapter.UnitAdapter.IManagerUnitListener
import com.app.buchin.constant.GlobalFuntion
import com.app.buchin.listener.IOnSingleClickListener
import com.app.buchin.model.*
import com.app.buchin.utils.StringUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import java.util.*

class UnitActivity : BaseActivity() {

    private var mListUnit: MutableList<UnitObject>? = null
    private var mUnitAdapter: UnitAdapter? = null
    private var edtSearchName: EditText? = null
    private var mKeySeach: String? = null
    private val mChildEventListener: ChildEventListener = object : ChildEventListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val unitObject: UnitObject? = dataSnapshot.getValue<UnitObject>(UnitObject::class.java)
            if (unitObject == null || mListUnit == null || mUnitAdapter == null) {
                return
            }
            if (StringUtil.isEmpty(mKeySeach)) {
                mListUnit!!.add(0, unitObject)
            } else {
                if (GlobalFuntion.getTextSearch(unitObject.getName())!!.toLowerCase(Locale.getDefault())
                                .contains(GlobalFuntion.getTextSearch(mKeySeach)!!.toLowerCase(Locale.getDefault()))) {
                    mListUnit!!.add(0, unitObject)
                }
            }
            mUnitAdapter!!.notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val unitObject: UnitObject? = dataSnapshot.getValue<UnitObject>(UnitObject::class.java)
            if (unitObject == null || mListUnit == null || mListUnit!!.isEmpty() || mUnitAdapter == null) {
                return
            }
            for (i in mListUnit!!.indices) {
                if (unitObject.getId() == mListUnit!![i].getId()) {
                    mListUnit!![i] = unitObject
                    break
                }
            }
            mUnitAdapter!!.notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            val unitObject: UnitObject? = dataSnapshot.getValue<UnitObject>(UnitObject::class.java)
            if (unitObject == null || mListUnit == null || mListUnit!!.isEmpty() || mUnitAdapter == null) {
                return
            }
            for (unit in mListUnit!!) {
                if (unitObject.getId() == unit.getId()) {
                    mListUnit!!.remove(unit)
                    break
                }
            }
            mUnitAdapter!!.notifyDataSetChanged()
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {
            showToast(getString(R.string.msg_get_data_error))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unit)
        initToolbar()
        initUi()
        getListUnit()
    }

    private fun initToolbar() {
        if (supportActionBar != null) {
            supportActionBar!!.title = getString(R.string.feature_manage_unit)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
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
        edtSearchName = findViewById<EditText?>(R.id.edt_search_name)
        val imgSearch = findViewById<ImageView?>(R.id.img_search)
        imgSearch?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                searchUnit()
            }
        })
        edtSearchName?.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchUnit()
                return@setOnEditorActionListener true
            }
            false
        }
        edtSearchName?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do nothing
            }

            override fun afterTextChanged(s: Editable?) {
                val strKey = s.toString().trim { it <= ' ' }
                if (strKey == "" || strKey.isEmpty()) {
                    mKeySeach = ""
                    getListUnit()
                    GlobalFuntion.hideSoftKeyboard(this@UnitActivity)
                }
            }
        })
        val fabAdd = findViewById<FloatingActionButton?>(R.id.fab_add_data)
        fabAdd?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                onClickAddOrEditUnit(null)
            }
        })
        val layoutDeleteAll = findViewById<LinearLayout?>(R.id.layout_delete_all)
        layoutDeleteAll?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (mListUnit == null || mListUnit!!.isEmpty()) {
                    return
                }
                onClickDeleteAllUnit()
            }
        })
        val rcvUnit = findViewById<RecyclerView?>(R.id.rcv_data)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvUnit?.layoutManager = linearLayoutManager
        mListUnit = ArrayList()
        mUnitAdapter = UnitAdapter(mListUnit, object : IManagerUnitListener {
            override fun editUnit(unitObject: UnitObject?) {
                onClickAddOrEditUnit(unitObject)
            }

            override fun deleteUnit(unitObject: UnitObject?) {
                onClickDeleteUnit(unitObject)
            }
        })
        rcvUnit?.adapter = mUnitAdapter
        rcvUnit?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    fabAdd?.hide()
                } else {
                    fabAdd?.show()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    fun getListUnit() {
        if (mListUnit != null) {
            mListUnit!!.clear()
            MyApplication[this].getUnitDatabaseReference().removeEventListener(mChildEventListener)
        }
        MyApplication[this].getUnitDatabaseReference().addChildEventListener(mChildEventListener)
    }

    private fun searchUnit() {
        if (mListUnit == null || mListUnit!!.isEmpty()) {
            GlobalFuntion.hideSoftKeyboard(this)
            return
        }
        mKeySeach = edtSearchName?.text.toString().trim { it <= ' ' }
        getListUnit()
        GlobalFuntion.hideSoftKeyboard(this)
    }

    private fun onClickAddOrEditUnit(unitObject: UnitObject?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_add_and_edit_unit)
        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        // Get view
        val tvTitleDialog = dialog.findViewById<TextView?>(R.id.tv_title_dialog)
        val edtUnitName = dialog.findViewById<EditText?>(R.id.edt_unit_name)
        val tvDialogCancel = dialog.findViewById<TextView?>(R.id.tv_dialog_cancel)
        val tvDialogAction = dialog.findViewById<TextView?>(R.id.tv_dialog_action)

        // Set data
        if (unitObject == null) {
            tvTitleDialog?.text = getString(R.string.add_unit_name)
            tvDialogAction?.text = getString(R.string.action_add)
        } else {
            tvTitleDialog?.text = getString(R.string.edit_unit_name)
            tvDialogAction?.text = getString(R.string.action_edit)
            edtUnitName?.setText(unitObject.getName())
        }

        // Set listener
        tvDialogCancel?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                dialog.dismiss()
            }
        })
        tvDialogAction?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                val strUnitName = edtUnitName?.text.toString().trim { it <= ' ' }
                if (StringUtil.isEmpty(strUnitName)) {
                    showToast(getString(R.string.msg_unit_name_require))
                    return
                }
                if (isUnitExist(strUnitName)) {
                    showToast(getString(R.string.msg_unit_exist))
                    return
                }
                if (unitObject == null) {
                    val id = System.currentTimeMillis()
                    val unit = UnitObject()
                    unit.setId(id)
                    unit.setName(strUnitName)
                    MyApplication[this@UnitActivity].getUnitDatabaseReference()
                            .child(id.toString()).setValue(unit) { _: DatabaseError?, _: DatabaseReference? ->
                                GlobalFuntion.hideSoftKeyboard(this@UnitActivity, edtUnitName)
                                showToast(getString(R.string.msg_add_unit_success))
                                dialog.dismiss()
                                GlobalFuntion.hideSoftKeyboard(this@UnitActivity)
                            }
                } else {
                    val map: MutableMap<String?, Any?> = HashMap()
                    map["name"] = strUnitName
                    MyApplication[this@UnitActivity].getUnitDatabaseReference()
                            .child(unitObject.getId().toString()).updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
                                GlobalFuntion.hideSoftKeyboard(this@UnitActivity, edtUnitName)
                                showToast(getString(R.string.msg_edit_unit_success))
                                dialog.dismiss()
                                GlobalFuntion.hideSoftKeyboard(this@UnitActivity)
                                updateUnitInDrink(UnitObject(unitObject.getId(), strUnitName))
                                updateUnitInHistory(UnitObject(unitObject.getId(), strUnitName))
                            }
                }
            }
        })
        dialog.show()
    }

    private fun isUnitExist(unitName: String?): Boolean {
        if (mListUnit == null || mListUnit!!.isEmpty()) {
            return false
        }
        for (unitObject in mListUnit!!) {
            if (unitName == unitObject.getName()) {
                return true
            }
        }
        return false
    }

    private fun onClickDeleteUnit(unitObject: UnitObject?) {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_delete)) { _: DialogInterface?, _: Int ->
                    MyApplication[this@UnitActivity].getUnitDatabaseReference()
                            .child(unitObject?.getId().toString()).removeValue { _: DatabaseError?, _: DatabaseReference? ->
                                showToast(getString(R.string.msg_delete_unit_success))
                                GlobalFuntion.hideSoftKeyboard(this@UnitActivity)
                            }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    private fun onClickDeleteAllUnit() {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.msg_confirm_delete_all))
                .setPositiveButton(getString(R.string.delete_all)) { _: DialogInterface?, _: Int ->
                    MyApplication[this@UnitActivity].getUnitDatabaseReference()
                            .removeValue { _: DatabaseError?, _: DatabaseReference? ->
                                showToast(getString(R.string.msg_delete_all_unit_success))
                                GlobalFuntion.hideSoftKeyboard(this@UnitActivity)
                            }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    private fun updateUnitInDrink(unitObject: UnitObject?) {
        MyApplication[this].getDrinkDatabaseReference()
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val list: MutableList<Drink?> = ArrayList()
                        for (dataSnapshot in snapshot.children) {
                            val drink: Drink? = dataSnapshot.getValue<Drink>(Drink::class.java)
                            if (drink != null && drink.getUnitId() == unitObject?.getId()) {
                                list.add(drink)
                            }
                        }
                        MyApplication[this@UnitActivity].getDrinkDatabaseReference()
                                .removeEventListener(this)
                        if (list.isEmpty()) {
                            return
                        }
                        for (drink in list) {
                            MyApplication[this@UnitActivity].getDrinkDatabaseReference()
                                    .child(drink?.getId().toString())
                                    .child("unitName").setValue(unitObject?.getName())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }

    private fun updateUnitInHistory(unitObject: UnitObject?) {
        MyApplication[this].getHistoryDatabaseReference()
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val list: MutableList<History?> = ArrayList()
                        for (dataSnapshot in snapshot.children) {
                            val history: History? = dataSnapshot.getValue<History>(History::class.java)
                            if (history != null && history.getUnitId() == unitObject?.getId()) {
                                list.add(history)
                            }
                        }
                        MyApplication[this@UnitActivity].getHistoryDatabaseReference()
                                .removeEventListener(this)
                        if (list.isEmpty()) {
                            return
                        }
                        for (history in list) {
                            MyApplication[this@UnitActivity].getHistoryDatabaseReference()
                                    .child(history?.getId().toString())
                                    .child("unitName").setValue(unitObject?.getName())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }
}