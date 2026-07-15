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
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.MyApplication
import com.app.buchin.R
import com.app.buchin.adapter.DrinkAdapter
import com.app.buchin.adapter.SelectUnitAdapter
import com.app.buchin.constant.GlobalFuntion
import com.app.buchin.listener.IOnSingleClickListener
import com.app.buchin.model.*
import com.app.buchin.utils.StringUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import java.util.*

class ListDrinkActivity : BaseActivity() {

    private var mListDrink: MutableList<Drink>? = null
    private var mDrinkAdapter: DrinkAdapter? = null
    private var mListUnit: MutableList<UnitObject>? = null
    private var mUnitSelected: UnitObject? = null
    private var edtSearchName: EditText? = null
    private var mKeySeach: String? = null
    private val mChildEventListener: ChildEventListener = object : ChildEventListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onChildAdded(
            dataSnapshot: DataSnapshot,
            s: String?
        ) {

            Log.d("LIST_DRINK", "==============")
            Log.d("LIST_DRINK", "Child Added")
            Log.d("LIST_DRINK", "Key = ${dataSnapshot.key}")
            Log.d("LIST_DRINK", "Value = ${dataSnapshot.value}")

            val drink = dataSnapshot.getValue(Drink::class.java)

            Log.d("LIST_DRINK", "Drink = $drink")

            if (drink == null) {

                Log.d("LIST_DRINK", "Drink NULL")

                return
            }

            mListDrink!!.add(0, drink)

            Log.d("LIST_DRINK", "List Size = ${mListDrink!!.size}")

            mDrinkAdapter!!.notifyDataSetChanged()

            Log.d(
                "LIST_DRINK",
                "Adapter Size = ${mDrinkAdapter!!.itemCount}"
            )
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val drink: Drink? = dataSnapshot.getValue<Drink>(Drink::class.java)
            if (drink == null || mListDrink == null || mListDrink!!.isEmpty() || mDrinkAdapter == null) {
                return
            }
            for (i in mListDrink!!.indices) {
                if (drink.getId() == mListDrink!![i].getId()) {
                    mListDrink!![i] = drink
                    break
                }
            }
            mDrinkAdapter!!.notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            val drink: Drink? = dataSnapshot.getValue<Drink>(Drink::class.java)
            if (drink == null || mListDrink == null || mListDrink!!.isEmpty() || mDrinkAdapter == null) {
                return
            }
            for (drinkObject in mListDrink!!) {
                if (drink.getId() == drinkObject.getId()) {
                    mListDrink!!.remove(drinkObject)
                    break
                }
            }
            mDrinkAdapter!!.notifyDataSetChanged()
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {
            showToast(databaseError.message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("LIST_DRINK", "onCreate")

        setContentView(R.layout.activity_list_drink)

        initToolbar()
        initUi()
        getListUnit()
        getListDrink()
    }

    private fun initToolbar() {
        if (supportActionBar != null) {
            supportActionBar!!.title = getString(R.string.feature_list_menu)
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
                searchDrink()
            }
        })
        edtSearchName?.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchDrink()
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
                    getListDrink()
                    GlobalFuntion.hideSoftKeyboard(this@ListDrinkActivity)
                }
            }
        })
        val fabAdd = findViewById<FloatingActionButton?>(R.id.fab_add_data)
        fabAdd?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                onClickAddOrEditDrink(null)
            }
        })
        val layoutDeleteAll = findViewById<LinearLayout?>(R.id.layout_delete_all)
        layoutDeleteAll?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (mListDrink == null || mListDrink!!.isEmpty()) {
                    return
                }
                onClickDeleteAllDrink()
            }
        })
        val rcvDrink = findViewById<RecyclerView?>(R.id.rcv_data)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvDrink?.layoutManager = linearLayoutManager
        mListUnit = ArrayList()
        mListDrink = ArrayList()
        mDrinkAdapter = DrinkAdapter(mListDrink, object : DrinkAdapter.IManagerDrinkListener {
            override fun editDrink(drink: Drink?) {
                onClickAddOrEditDrink(drink)
            }

            override fun deleteDrink(drink: Drink?) {
                onClickDeleteDrink(drink)
            }

            override fun onClickItemDrink(drink: Drink?) {
                GlobalFuntion.goToDrinkDetailActivity(this@ListDrinkActivity, drink)
            }
        })
        rcvDrink?.adapter = mDrinkAdapter
        rcvDrink?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    private fun getListUnit() {
        MyApplication[this].getUnitDatabaseReference().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (mListUnit != null) mListUnit!!.clear()
                for (dataSnapshot in snapshot.children) {
                    val unitObject: UnitObject? = dataSnapshot.getValue<UnitObject>(UnitObject::class.java)
                    if (unitObject != null) {
                        mListUnit?.add(0, unitObject)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(error.message)
            }
        })
    }

    fun getListDrink() {

        Log.d("LIST_DRINK", "getListDrink() called")

        if (mListDrink != null) {
            mListDrink!!.clear()

            Log.d("LIST_DRINK", "List cleared")

            MyApplication[this]
                .getDrinkDatabaseReference()
                .removeEventListener(mChildEventListener)
        }

        Log.d("LIST_DRINK", "Checking Firebase...")

        MyApplication[this]
            .getDrinkDatabaseReference()
            .get()
            .addOnSuccessListener { snapshot ->

                Log.d("FIREBASE_CHECK", "========== FIREBASE CHECK ==========")
                Log.d("FIREBASE_CHECK", "Exists = ${snapshot.exists()}")
                Log.d("FIREBASE_CHECK", "Children Count = ${snapshot.childrenCount}")
                Log.d("FIREBASE_CHECK", "Value = ${snapshot.value}")
                Log.d("FIREBASE_CHECK", "===================================")

            }
            .addOnFailureListener { e ->

                Log.e("FIREBASE_CHECK", "========== FIREBASE ERROR ==========")
                Log.e("FIREBASE_CHECK", e.message ?: "Unknown Error")
                Log.e("FIREBASE_CHECK", "===================================")

            }

        Log.d("LIST_DRINK", "Add ChildEventListener")

        MyApplication[this]
            .getDrinkDatabaseReference()
            .addChildEventListener(mChildEventListener)
    }

    private fun searchDrink() {
        if (mListDrink == null || mListDrink!!.isEmpty()) {
            GlobalFuntion.hideSoftKeyboard(this)
            return
        }
        mKeySeach = edtSearchName?.text.toString().trim { it <= ' ' }
        getListDrink()
        GlobalFuntion.hideSoftKeyboard(this)
    }

    private fun onClickAddOrEditDrink(drink: Drink?) {
        MyApplication[this].getUnitDatabaseReference()
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (mListUnit != null) mListUnit!!.clear()
                        for (dataSnapshot in snapshot.children) {
                            val unitObject: UnitObject? = dataSnapshot.getValue<UnitObject>(UnitObject::class.java)
                            if (unitObject != null) {
                                mListUnit?.add(0, unitObject)
                            }
                        }
                        showAddOrEditDrinkDialog(drink)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showToast(error.message)
                    }
                })
    }

    private fun showAddOrEditDrinkDialog(drink: Drink?) {
        if (mListUnit == null || mListUnit!!.isEmpty()) {
            showToast(getString(R.string.msg_list_unit_require))
            return
        }
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_dialog_add_and_edit_drink)
        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        // Get view
        val tvTitleDialog = dialog.findViewById<TextView?>(R.id.tv_title_dialog)
        val edtDrinkName = dialog.findViewById<EditText?>(R.id.edt_drink_name)
        val tvDialogCancel = dialog.findViewById<TextView?>(R.id.tv_dialog_cancel)
        val tvDialogAction = dialog.findViewById<TextView?>(R.id.tv_dialog_action)
        val spnUnit = dialog.findViewById<Spinner?>(R.id.spinner_unit)
        val selectUnitAdapter = SelectUnitAdapter(this, R.layout.item_choose_option, mListUnit!!)
        spnUnit?.adapter = selectUnitAdapter
        spnUnit?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mUnitSelected = selectUnitAdapter.getItem(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Set data
        if (drink == null) {
            tvTitleDialog?.text = getString(R.string.add_drink_name)
            tvDialogAction?.text = getString(R.string.action_add)
        } else {
            tvTitleDialog?.text = getString(R.string.edit_drink_name)
            tvDialogAction?.text = getString(R.string.action_edit)
            edtDrinkName?.setText(drink.getName())
            spnUnit?.setSelection(getPositionUnitUpdate(drink))
        }

        // Set listener
        tvDialogCancel?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                dialog.dismiss()
            }
        })
        tvDialogAction?.setOnClickListener(object : IOnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                val strDrinkName = edtDrinkName?.text.toString().trim { it <= ' ' }
                if (StringUtil.isEmpty(strDrinkName)) {
                    showToast(getString(R.string.msg_drink_name_require))
                    return
                }
                if (isDrinkExist(strDrinkName, drink)) {
                    showToast(getString(R.string.msg_drink_exist))
                    return
                }
                if (drink == null) {
                    val id = System.currentTimeMillis()
                    val drinkObject = Drink()
                    drinkObject.setId(id)
                    drinkObject.setName(strDrinkName)
                    drinkObject.setUnitId(mUnitSelected!!.getId())
                    drinkObject.setUnitName(mUnitSelected!!.getName())
                    MyApplication[this@ListDrinkActivity].getDrinkDatabaseReference()
                            .child(id.toString()).setValue(drinkObject) { databaseError: DatabaseError?, _: DatabaseReference? ->
                                if (databaseError != null) {
                                    showToast(databaseError.message)
                                    return@setValue
                                }
                                GlobalFuntion.hideSoftKeyboard(this@ListDrinkActivity, edtDrinkName)
                                showToast(getString(R.string.msg_add_drink_success))
                                dialog.dismiss()
                                GlobalFuntion.hideSoftKeyboard(this@ListDrinkActivity)
                            }
                } else {
                    val map: MutableMap<String?, Any?> = HashMap()
                    map["name"] = strDrinkName
                    map["unitId"] = mUnitSelected!!.getId()
                    map["unitName"] = mUnitSelected!!.getName()
                    MyApplication[this@ListDrinkActivity].getDrinkDatabaseReference()
                            .child(drink.getId().toString()).updateChildren(map) { databaseError: DatabaseError?, _: DatabaseReference? ->
                                if (databaseError != null) {
                                    showToast(databaseError.message)
                                    return@updateChildren
                                }
                                GlobalFuntion.hideSoftKeyboard(this@ListDrinkActivity, edtDrinkName)
                                showToast(getString(R.string.msg_edit_drink_success))
                                dialog.dismiss()
                                GlobalFuntion.hideSoftKeyboard(this@ListDrinkActivity)
                                updateDrinkInHistory(Drink(drink.getId(), strDrinkName,
                                        mUnitSelected!!.getId(), mUnitSelected!!.getName()))
                            }
                }
            }
        })
        dialog.show()
    }

    private fun getPositionUnitUpdate(drink: Drink?): Int {
        if (mListUnit == null || mListUnit!!.isEmpty()) {
            return 0
        }
        for (i in mListUnit!!.indices) {
            if (drink?.getUnitId() == mListUnit!![i].getId()) {
                return i
            }
        }
        return 0
    }

    private fun isDrinkExist(drinkName: String?, currentDrink: Drink?): Boolean {
        if (mListDrink == null || mListDrink!!.isEmpty()) {
            return false
        }
        for (drink in mListDrink!!) {
            if (currentDrink?.getId() != drink.getId() && drinkName == drink.getName()) {
                return true
            }
        }
        return false
    }

    private fun onClickDeleteDrink(drink: Drink?) {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_delete)) { _: DialogInterface?, _: Int ->
                    MyApplication[this@ListDrinkActivity].getDrinkDatabaseReference()
                            .child(drink?.getId().toString()).removeValue { databaseError: DatabaseError?, _: DatabaseReference? ->
                                if (databaseError != null) {
                                    showToast(databaseError.message)
                                    return@removeValue
                                }
                                showToast(getString(R.string.msg_delete_drink_success))
                                GlobalFuntion.hideSoftKeyboard(this@ListDrinkActivity)
                            }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    private fun onClickDeleteAllDrink() {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.msg_confirm_delete_all))
                .setPositiveButton(getString(R.string.delete_all)) { _: DialogInterface?, _: Int ->
                    MyApplication[this@ListDrinkActivity].getDrinkDatabaseReference()
                            .removeValue { databaseError: DatabaseError?, _: DatabaseReference? ->
                                if (databaseError != null) {
                                    showToast(databaseError.message)
                                    return@removeValue
                                }
                                showToast(getString(R.string.msg_delete_all_drink_success))
                                GlobalFuntion.hideSoftKeyboard(this@ListDrinkActivity)
                            }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    private fun updateDrinkInHistory(drink: Drink?) {
        MyApplication[this].getHistoryDatabaseReference()
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val list: MutableList<History?> = ArrayList()
                        for (dataSnapshot in snapshot.children) {
                            val history: History? = dataSnapshot.getValue<History>(History::class.java)
                            if (history != null && history.getDrinkId() == drink?.getId()) {
                                list.add(history)
                            }
                        }
                        MyApplication[this@ListDrinkActivity].getHistoryDatabaseReference()
                                .removeEventListener(this)
                        if (list.isEmpty()) {
                            return
                        }
                        for (history in list) {
                            val map: MutableMap<String?, Any?> = HashMap()
                            map["drinkName"] = drink?.getName()
                            map["unitId"] = drink?.getUnitId()
                            map["unitName"] = drink?.getUnitName()
                            MyApplication[this@ListDrinkActivity].getHistoryDatabaseReference()
                                    .child(history?.getId().toString())
                                    .updateChildren(map)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showToast(error.message)
                    }
                })
    }
}
