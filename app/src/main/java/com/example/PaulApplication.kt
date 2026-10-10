package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class PaulApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            Log.e("PaulApplication", "FirebaseApp init warning: ${e.message}")
        }
    }
}
