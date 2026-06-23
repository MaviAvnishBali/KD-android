package com.kiladarbar.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kila_darbar_session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_ACCESS_TOKEN  = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID       = stringPreferencesKey("user_id")
        private val KEY_USER_NAME     = stringPreferencesKey("user_name")
        private val KEY_USER_PHONE    = stringPreferencesKey("user_phone")
        private val KEY_USER_ROLE     = stringPreferencesKey("user_role")
        private val KEY_IS_GUEST      = booleanPreferencesKey("is_guest")
    }

    val accessToken:  Flow<String?>  = context.dataStore.data.map { it[KEY_ACCESS_TOKEN]  }
    val refreshToken: Flow<String?>  = context.dataStore.data.map { it[KEY_REFRESH_TOKEN] }
    val userId:       Flow<String?>  = context.dataStore.data.map { it[KEY_USER_ID]       }
    val userName:     Flow<String?>  = context.dataStore.data.map { it[KEY_USER_NAME]     }

    /** Reactive: emits true when the active session belongs to a guest */
    val isGuestFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_GUEST] ?: false }

    suspend fun saveSession(
        accessToken:  String,
        refreshToken: String,
        userId:       String,
        userName:     String?,
        phone:        String?,
        role:         String,
        isGuest:      Boolean = false,
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN]  = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID]       = userId
            prefs[KEY_USER_NAME]     = userName ?: ""
            prefs[KEY_USER_PHONE]    = phone ?: ""
            prefs[KEY_USER_ROLE]     = role
            prefs[KEY_IS_GUEST]      = isGuest
        }
    }

    suspend fun saveAccessToken(newToken: String) {
        context.dataStore.edit { it[KEY_ACCESS_TOKEN] = newToken }
    }

    suspend fun isGuest(): Boolean =
        context.dataStore.data.first()[KEY_IS_GUEST] == true

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun isLoggedIn(): Boolean =
        context.dataStore.data.first()[KEY_ACCESS_TOKEN]?.isNotBlank() == true
}
