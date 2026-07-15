package com.app.buchin

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        Log.d("MY_APP", "FirebaseApp count = ${FirebaseApp.getApps(this).size}")

        testFirebase()
    }


    /**
     * Kiểm tra kết nối Firebase
     */
    private fun testFirebase() {

        Log.d("MY_APP", "========== testFirebase() called ==========")

        val db = FirebaseDatabase.getInstance(FIREBASE_URL)
        val ref = db.reference

        Log.d("MY_APP", "Database instance obtained, URL: $FIREBASE_URL")

        ref.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                Log.d("FIREBASE_TEST", "========== CONNECT SUCCESS ==========")
                Log.d("FIREBASE_TEST", "Exists = ${snapshot.exists()}")
                Log.d("FIREBASE_TEST", "Children Count = ${snapshot.childrenCount}")
                Log.d("FIREBASE_TEST", "Data = ${snapshot.value}")
                Log.d("FIREBASE_TEST", "=====================================")
            } else {
                val e = task.exception
                Log.e("FIREBASE_TEST", "========== CONNECT FAIL ==========")
                Log.e("FIREBASE_TEST", "Message = ${e?.message}")
                Log.e("FIREBASE_TEST", "Error Code = ${e.toString()}")
                Log.e("FIREBASE_TEST", "==================================")
            }
        }
    }

    fun getUnitDatabaseReference(): DatabaseReference {
        return FirebaseDatabase
            .getInstance(FIREBASE_URL)
            .getReference("/my_unit")
    }

    fun getDrinkDatabaseReference(): DatabaseReference {
        return FirebaseDatabase
            .getInstance(FIREBASE_URL)
            .getReference("/drink")
    }

    fun getHistoryDatabaseReference(): DatabaseReference {
        return FirebaseDatabase
            .getInstance(FIREBASE_URL)
            .getReference("/history")
    }

    fun getQuantityDatabaseReference(drinkId: Long): DatabaseReference {
        return FirebaseDatabase
            .getInstance(FIREBASE_URL)
            .getReference("/drink/$drinkId/quantity")
    }

    companion object {

        operator fun get(context: Context?): MyApplication {
            return context?.applicationContext as MyApplication
        }

        private const val FIREBASE_URL =
            "https://barmanagerkotlin-default-rtdb.asia-southeast1.firebasedatabase.app"
    }
}