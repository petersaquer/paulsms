package com.example

import android.app.Application
import com.google.firebase.FirebaseApp

class PaulApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (_: Exception) {}
    }
}
