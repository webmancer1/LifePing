package com.example.lifeping.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    private val CHECK_IN_INTERVAL = longPreferencesKey("check_in_interval")
    private val GRACE_PERIOD = longPreferencesKey("grace_period")
    private val NOTIFY_UPCOMING = booleanPreferencesKey("notify_upcoming")
    private val AUTO_ALERT = booleanPreferencesKey("auto_alert")

    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            val isDark = preferences[IS_DARK_THEME] ?: false
            android.util.Log.d("ThemeDebug", "Repository: Emitting isDarkTheme = $isDark")
            isDark
        }

    val checkInInterval: Flow<Long> = context.dataStore.data
        .map { preferences -> preferences[CHECK_IN_INTERVAL] ?: (4 * 60 * 60 * 1000L) } // Default 4 hours

    val gracePeriod: Flow<Long> = context.dataStore.data
        .map { preferences -> preferences[GRACE_PERIOD] ?: (30 * 60 * 1000L) } // Default 30 minutes

    val notifyUpcoming: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[NOTIFY_UPCOMING] ?: true }

    val autoAlert: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[AUTO_ALERT] ?: true }

    suspend fun saveThemePreference(isDarkTheme: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDarkTheme
            android.util.Log.d("ThemeDebug", "Repository: Saved isDarkTheme = $isDarkTheme")
        }
    }
    
    suspend fun toggleTheme() {
        android.util.Log.d("ThemeDebug", "Repository: toggleTheme called")
        context.dataStore.edit { preferences ->
            val current = preferences[IS_DARK_THEME] ?: false
            preferences[IS_DARK_THEME] = !current
            android.util.Log.d("ThemeDebug", "Repository: Toggled from $current to ${!current}")
        }
    }

    suspend fun saveCheckInInterval(interval: Long) {
        context.dataStore.edit { preferences ->
            preferences[CHECK_IN_INTERVAL] = interval
        }
    }

    suspend fun saveGracePeriod(period: Long) {
        context.dataStore.edit { preferences ->
            preferences[GRACE_PERIOD] = period
        }
    }

    suspend fun saveNotifyUpcoming(notify: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFY_UPCOMING] = notify
        }
    }

    suspend fun saveAutoAlert(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_ALERT] = enabled
        }
    }
}
