package com.example.lifeping.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    val isFormValid: StateFlow<Boolean> = combine(_email, _password, _emailError, _passwordError) { email, password, emailError, passwordError ->
        email.isNotBlank() && password.isNotBlank() && emailError == null && passwordError == null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        validateEmail(newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        validatePassword(newPassword)
    }

    fun onRememberMeChange(isChecked: Boolean) {
        _rememberMe.value = isChecked
    }

    private fun validateEmail(email: String) {
        if (email.isBlank()) {
            _emailError.value = "Email is required"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.value = "Invalid email format"
        } else {
            _emailError.value = null
        }
    }

    private fun validatePassword(password: String) {
        if (password.length < 6) {
            _passwordError.value = "Password must be at least 6 characters"
        } else {
            _passwordError.value = null
        }
    }

    fun signIn(onSuccess: () -> Unit) {
        // Validate one last time before submitting
        validateEmail(_email.value)
        validatePassword(_password.value)
        
        if (_emailError.value != null || _passwordError.value != null) return

        viewModelScope.launch {
            _isLoading.value = true
            delay(2000) // Simulate network call
            _isLoading.value = false
            onSuccess()
        }
    }
}
