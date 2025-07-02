/*
 * Copyright (c) 2025 Babel Software.
 */

package com.babelsoftware.airnote.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFERENCE_FILE_NAME = "airnote_secure_prefs"
        private const val KEY_USER_GEMINI_API = "user_gemini_api_key"
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        PREFERENCE_FILE_NAME,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveUserApiKey(apiKey: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_USER_GEMINI_API, apiKey)
            apply()
        }
    }

    fun getUserApiKey(): String? {
        return sharedPreferences.getString(KEY_USER_GEMINI_API, null)
    }
}