package com.app.buchin.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.buchin.R
import com.app.buchin.adapter.FeatureAdapter
import com.app.buchin.constant.Constants
import com.app.buchin.constant.GlobalFuntion
import com.app.buchin.model.Feature
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.util.*

class FeatureActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature)
        initUi()
        showAdmobBanner()
    }

    private fun initUi() {
        val rcvFeature = findViewById<RecyclerView?>(R.id.rcv_feature)
        val gridLayoutManager = GridLayoutManager(this, 2)
        rcvFeature?.layoutManager = gridLayoutManager
        val featureAdapter = FeatureAdapter(getListFeatures(), object : FeatureAdapter.IManagerFeatureListener {
            override fun clickFeatureItem(feature: Feature?) {
                onClickItemFeature(feature)
            }
        })
        rcvFeature?.adapter = featureAdapter
    }

    private fun showAdmobBanner() {
        MobileAds.initialize(this, "ca-app-pub-8577216370890753~4422934437")
        val adView = findViewById<AdView?>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)
    }

    private fun getListFeatures(): MutableList<Feature> {
        val list: MutableList<Feature> = ArrayList()
        list.add(Feature(Feature.FEATURE_MANAGE_UNIT, R.drawable.ic_manage_unit, getString(R.string.feature_manage_unit)))
        list.add(Feature(Feature.FEATURE_LIST_MENU, R.drawable.ic_list_drink, getString(R.string.feature_list_menu)))
        list.add(Feature(Feature.FEATURE_ADD_DRINK, R.drawable.ic_add_drink, getString(R.string.feature_add_drink)))
        list.add(Feature(Feature.FEATURE_DRINK_USED, R.drawable.ic_drink_used, getString(R.string.feature_drink_used)))
        list.add(Feature(Feature.FEATURE_MANAGE_DRINK, R.drawable.ic_manage_drink, getString(R.string.feature_manage_drink)))
        list.add(Feature(Feature.FEATURE_DRINK_OUT_OF_STOCK, R.drawable.ic_drink_out_of_stock, getString(R.string.feature_drink_out_of_stock)))
        list.add(Feature(Feature.FEATURE_REVELUE, R.drawable.ic_revenue, getString(R.string.feature_revenue)))
        list.add(Feature(Feature.FEATURE_COST, R.drawable.ic_cost, getString(R.string.feature_cost)))
        list.add(Feature(Feature.FEATURE_PROFIT, R.drawable.ic_profit, getString(R.string.feature_profit)))
        list.add(Feature(Feature.FEATURE_DRINK_POPULAR, R.drawable.ic_drink_popular, getString(R.string.feature_drink_popular)))
        return list
    }

    override fun onBackPressed() {
        showDialogExitApp()
    }

    private fun showDialogExitApp() {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.msg_confirm_exit_app))
                .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int -> finishAffinity() }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    fun onClickItemFeature(feature: Feature?) {
        when (feature?.getId()) {
            Feature.FEATURE_LIST_MENU -> GlobalFuntion.startActivity(this, ListDrinkActivity::class.java)
            Feature.FEATURE_MANAGE_UNIT -> GlobalFuntion.startActivity(this, UnitActivity::class.java)
            Feature.FEATURE_ADD_DRINK -> GlobalFuntion.startActivity(this, HistoryDrinkActivity::class.java)
            Feature.FEATURE_DRINK_USED -> {
                val bundle = Bundle()
                bundle.putBoolean(Constants.KEY_INTENT_DRINK_USED, true)
                GlobalFuntion.startActivity(this, HistoryDrinkActivity::class.java, bundle)
            }
            Feature.FEATURE_MANAGE_DRINK -> GlobalFuntion.startActivity(this, ManageDrinkActivity::class.java)
            Feature.FEATURE_DRINK_OUT_OF_STOCK -> GlobalFuntion.startActivity(this, DrinkOutOfStockActivity::class.java)
            Feature.FEATURE_REVELUE -> goToStatisticalActivity(Constants.TYPE_REVENUE)
            Feature.FEATURE_COST -> goToStatisticalActivity(Constants.TYPE_COST)
            Feature.FEATURE_PROFIT -> GlobalFuntion.startActivity(this, ProfitActivity::class.java)
            Feature.FEATURE_DRINK_POPULAR -> goToListDrinkPopular()
        }
    }

    private fun goToStatisticalActivity(type: Int) {
        val bundle = Bundle()
        bundle.putInt(Constants.KEY_TYPE_STATISTICAL, type)
        GlobalFuntion.startActivity(this, StatisticalActivity::class.java, bundle)
    }

    private fun goToListDrinkPopular() {
        val bundle = Bundle()
        bundle.putInt(Constants.KEY_TYPE_STATISTICAL, Constants.TYPE_REVENUE)
        bundle.putBoolean(Constants.KEY_DRINK_POPULAR, true)
        GlobalFuntion.startActivity(this, StatisticalActivity::class.java, bundle)
    }
}