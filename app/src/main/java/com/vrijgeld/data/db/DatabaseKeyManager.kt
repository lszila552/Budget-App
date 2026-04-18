package com.vrijgeld.data.db

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.SecureRandom

class DatabaseKeyManager(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        "vrijgeld_db_key",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getOrCreateKey(): ByteArray {
        val stored = prefs.getString(KEY, null)
        if (stored != null) return Base64.decode(stored, Base64.NO_WRAP)
        val key = ByteArray(32).also { SecureRandom().nextBytes(it) }
        prefs.edit().putString(KEY, Base64.encodeToString(key, Base64.NO_WRAP)).apply()
        return key
    }

    companion object {
        private const val KEY = "db_passphrase"
    }
}
