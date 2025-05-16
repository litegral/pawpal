package com.litegral.pawpal

import android.content.Context
import android.content.SharedPreferences

class UserPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "pawpal_user_preferences"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_BIO = "user_bio"
        private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"

        @Volatile
        private var instance: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return instance ?: synchronized(this) {
                instance ?: UserPreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "") ?: ""
    }

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserBio(): String {
        return prefs.getString(KEY_USER_BIO, "") ?: ""
    }

    fun setUserBio(bio: String) {
        prefs.edit().putString(KEY_USER_BIO, bio).apply()
    }

    fun getProfileImageUri(): String? {
        return prefs.getString(KEY_PROFILE_IMAGE_URI, null)
    }

    fun setProfileImageUri(uri: String) {
        prefs.edit().putString(KEY_PROFILE_IMAGE_URI, uri).apply()
    }
}
