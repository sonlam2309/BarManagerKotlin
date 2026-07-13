package com.app.buchin.activity

import android.annotation.SuppressLint
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
import com.app.buchin.adapter.ManageDrinkAdapter
import com.app.buchin.constant.GlobalFuntion
import com.app.buchin.listener.IOnSingleClickListener
import com.app.buchin.model.Drink
import com.app.buchin.utils.StringUtil
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import java.util.*

class ManageDrinkActivity : BaseActivity() {

    private var mListDrink: MutableList<Drink>? = null
    private var mManageDrinkAdapter: ManageDrinkAdapter? = null
    private var edtSearchName: EditText? = null
    private var mKeySeach: String? = null
    private val mChildEventListener: ChildEventListener = object : ChildEventListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val drink: Drink? = dataSnapshot.getValue<Drink>(Drink::class.java)
            if (drink == null || mListDrink == null || mManageDrinkAdapter == null) {
                return
            }
            if (StringUtil.isEmpty(mKeySeach)) {
                mListDrink!!.add(0, drink)
            } else {
                if (GlobalFuntion.getTextSearch(drink.getName())!!.lowercase(Locale.getDefault())
                                .contains(GlobalFuntion.getTextSearch(mKeySeach)!!.lowercase(Locale.getDefault()))) {
                    mListDrink!!.add(0, drink)
                }
            }
            mManageDrinkAdapter!!.notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val drink: Drink? = dataSnapshot.getValue<Drink>(Drink::class.java)
            if (drink == null || mListDrink == null || mListDrink!!.isEmpty() || mManageDrinkAdapter == null) {
                return
            }
            for (i in mListDrink!!.indices) {
                if (drink.getId() == mListDrink!![i].getId()) {
                    mListDrink!![i] = drink
                    break
                }
            }
            mManageDrinkAdapter!!.notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            val drink: Drink? = dataSnapshot.getValue<Drink>(Drink::class.java)
            if (drink == null || mListDrink == null || mListDrink!!.isEmpty() || mManageDrinkAdapter == null) {
                return
            }
            for (drinkObject in mListDrink!!) {
                if (drink.getId() == drinkObject.getId()) {
                    mListDrink!!.remove(drinkObject)
                    break
                }
            }
            mManageDrinkAdapter!!.notifyDataSetChanged()
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {
            showToast(databaseError.message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_drink)
        initToolbar()
        initUi()
        getListDrink()
    }

    private fun initToolbar() {
        if (supportActionBar != null) {
            supportActionBar!!.title = getString(R.string.feature_manage_drink)
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
                    GlobalFuntion.hideSoftKeyboard(this@ManageDrinkActivity)
                }
            }
        })
        val rcvDrink = findViewById<RecyclerView?>(R.id.rcv_data)
        val linearLayoutManager = LinearLayoutManager(this)
        rcvDrink?.layoutManager = linearLayoutManager
        mListDrink = ArrayList()
        mManageDrinkAdapter = ManageDrinkAdapter(mListDrink, object : ManageDrinkAdapter.IManagerDrinkListener {
            override fun clickItem(drink: Drink?) {
                GlobalFuntion.goToDrinkDetailActivity(this@ManageDrinkActivity, drink)
            }
        })
        rcvDrink?.adapter = mManageDrinkAdapter
    }

    fun getListDrink() {
        if (mListDrink != null) {
            mListDrink!!.clear()
            MyApplication[this].getDrinkDatabaseReference().removeEventListener(mChildEventListener)
        }
        MyApplication[this].getDrinkDatabaseReference().addChildEventListener(mChildEventListener)
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
}
