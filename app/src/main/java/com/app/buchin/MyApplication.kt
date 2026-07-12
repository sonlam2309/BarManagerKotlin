package com.app.buchin

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

    fun getUnitDatabaseReference(): DatabaseReference {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/my_unit")
    }

    fun getDrinkDatabaseReference(): DatabaseReference {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/drink")
    }

    fun getHistoryDatabaseReference(): DatabaseReference {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/history")
    }

    fun getQuantityDatabaseReference(drinkId: Long): DatabaseReference {
        return FirebaseDatabase.getInstance(FIREBASE_URL).getReference("/drink/$drinkId/quantity")
    }

    companion object {
        operator fun get(context: Context?): MyApplication {
            return context?.applicationContext as MyApplication
        }
        private const val FIREBASE_URL = "https://buchin-bar-default-rtdb.firebaseio.com"
    }
}