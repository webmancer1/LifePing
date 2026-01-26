package com.example.lifeping.ui.auth

import android.app.Application
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

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

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val isFormValid: StateFlow<Boolean> = combine(_email, _password, _emailError, _passwordError) { email, password, emailError, passwordError ->
        email.isNotBlank() && password.isNotBlank() && emailError == null && passwordError == null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        val savedEmail = sharedPreferences.getString("saved_email", null)
        if (savedEmail != null) {
            _email.value = savedEmail
            _rememberMe.value = true
        }
    }

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
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signInWithEmailAndPassword(_email.value, _password.value)
                    .addOnCompleteListener { task ->
                        _isLoading.value = false
                        if (task.isSuccessful) {
                            if (_rememberMe.value) {
                                sharedPreferences.edit().putString("saved_email", _email.value).apply()
                            } else {
                                sharedPreferences.edit().remove("saved_email").apply()
                            }
                            onSuccess()
                        } else {
                            _passwordError.value = task.exception?.localizedMessage ?: "Authentication failed."
                        }
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                _passwordError.value = e.localizedMessage ?: "An error occurred."
            }
        }
    }

    fun forgotPassword() {
        val email = _email.value
        if (email.isBlank()) {
            _message.value = "Please enter your email address to reset password."
            return
        }
        
        validateEmail(email)
        if (_emailError.value != null) {
             _message.value = "Please enter a valid email address."
             return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        _isLoading.value = false
                        if (task.isSuccessful) {
                            _message.value = "Password reset email sent to $email"
                        } else {
                            _message.value = task.exception?.localizedMessage ?: "Failed to send reset email."
                        }
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                _message.value = e.localizedMessage ?: "An error occurred."
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun onGoogleSignInResult(result: Result<com.google.firebase.auth.AuthResult>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (result.isSuccess) {
                onSuccess()
            } else {
                _passwordError.value = result.exceptionOrNull()?.localizedMessage ?: "Google Sign-In failed"
            }
        }
    }
}
