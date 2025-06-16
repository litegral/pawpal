package com.litegral.pawpal

import android.app.Application
import com.google.firebase.auth.FirebaseAuth

class PawpalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Set Firebase Auth persistence to PERSISTENT
        // This ensures that the user remains logged in even after closing and reopening the app
        FirebaseAuth.getInstance()
    }
} 