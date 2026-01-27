package com.example.lifeping.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")

    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            val isDark = preferences[IS_DARK_THEME] ?: false
            android.util.Log.d("ThemeDebug", "Repository: Emitting isDarkTheme = $isDark")
            isDark
        }

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
}
