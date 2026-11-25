package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "jak_session_prefs")

class SessionPreferences(private val context: Context) {
    private val activeEmailKey = stringPreferencesKey("active_user_email")

    val activeEmailFlow: Flow<String?> = context.sessionDataStore.data.map { prefs ->
        prefs[activeEmailKey]?.takeIf { it.isNotBlank() }
    }

    suspend fun setActiveEmail(email: String?) {
        context.sessionDataStore.edit { prefs ->
            if (email.isNullOrBlank()) {
                prefs.remove(activeEmailKey)
            } else {
                prefs[activeEmailKey] = email.trim().lowercase()
            }
        }
    }
}
