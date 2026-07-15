package com.app.buchin.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.viewpager2.widget.ViewPager2
import com.app.buchin.R
import com.app.buchin.adapter.MyPagerAdapter
import com.app.buchin.constant.Constants
import com.app.buchin.model.Drink
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class DrinkDetailActivity : BaseActivity() {

    private var mDrink: Drink? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_detail)
        getDataIntent()
        initToolbar()
        initView()
    }

    private fun getDataIntent() {
        val bundle = intent.extras ?: return
        mDrink = bundle[Constants.KEY_INTENT_DRINK_OBJECT] as Drink?
    }

    private fun initToolbar() {
        if (supportActionBar != null) {
            supportActionBar!!.title = mDrink?.getName()
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

    private fun initView() {
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager2 = findViewById<ViewPager2>(R.id.view_pager_2)
        val myPagerAdapter = MyPagerAdapter(this, mDrink)
        viewPager2?.adapter = myPagerAdapter
        TabLayoutMediator(tabLayout, viewPager2) { tab: TabLayout.Tab?, position: Int ->
            if (position == 0) {
                tab?.text = getString(R.string.label_added)
            } else {
                tab?.text = getString(R.string.label_used)
            }
        }.attach()
    }
}