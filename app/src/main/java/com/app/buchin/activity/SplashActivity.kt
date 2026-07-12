package com.app.buchin.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import com.app.buchin.R
import com.app.buchin.constant.GlobalFuntion

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val handler = Handler(mainLooper)
        handler.postDelayed({ goToFeatureActivity() }, 2000)
    }

    private fun goToFeatureActivity() {
        GlobalFuntion.startActivity(this, FeatureActivity::class.java)
        finish()
    }
}