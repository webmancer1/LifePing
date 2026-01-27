package com.example.lifeping.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeping.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false // Initial value false (Light Mode)
        )

    fun toggleTheme() {
        android.util.Log.d("ThemeDebug", "ViewModel: toggleTheme called")
        viewModelScope.launch {
            try {
                repository.toggleTheme()
                android.util.Log.d("ThemeDebug", "ViewModel: repository.toggleTheme completed")
            } catch (e: Exception) {
                android.util.Log.e("ThemeDebug", "ViewModel: Error toggling theme", e)
            }
        }
    }
}
