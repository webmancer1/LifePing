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

class RegisterViewModel : ViewModel() {

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error states
    private val _fullNameError = MutableStateFlow<String?>(null)
    val fullNameError: StateFlow<String?> = _fullNameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

    val isFormValid: StateFlow<Boolean> = combine(
        combine(_fullName, _email, _phoneNumber, _password, _confirmPassword) { name, email, phone, password, confirm ->
            name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && password.isNotBlank() && confirm.isNotBlank()
        },
        combine(_fullNameError, _emailError, _phoneError, _passwordError, _confirmPasswordError) { nameError, emailError, phoneError, passwordError, confirmError ->
            nameError == null && emailError == null && phoneError == null && passwordError == null && confirmError == null
        }
    ) { inputsFilled, noErrors ->
        inputsFilled && noErrors
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onFullNameChange(input: String) {
        _fullName.value = input
        validateFullName(input)
    }

    fun onEmailChange(input: String) {
        _email.value = input
        validateEmail(input)
    }

    fun onPhoneNumberChange(input: String) {
        _phoneNumber.value = input
        validatePhone(input)
    }

    fun onPasswordChange(input: String) {
        _password.value = input
        validatePassword(input)
        // Re-validate confirm password if it's not empty, as password change might fix/break match
        if (_confirmPassword.value.isNotEmpty()) {
            validateConfirmPassword(_confirmPassword.value)
        }
    }

    fun onConfirmPasswordChange(input: String) {
        _confirmPassword.value = input
        validateConfirmPassword(input)
    }

    private fun validateFullName(name: String) {
        if (name.isBlank()) {
            _fullNameError.value = "Full Name is required"
        } else {
            _fullNameError.value = null
        }
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

    private fun validatePhone(phone: String) {
        if (phone.isBlank()) {
            _phoneError.value = "Phone number is required"
        } else if (!Patterns.PHONE.matcher(phone).matches() || phone.length < 10) {
            _phoneError.value = "Invalid phone number format"
        } else {
            _phoneError.value = null
        }
    }

    private fun validatePassword(password: String) {
        if (password.length < 8) {
            _passwordError.value = "Password must be at least 8 characters"
        } else {
             // You can add more complex logic here (e.g. regex for special chars)
            _passwordError.value = null
        }
    }

    private fun validateConfirmPassword(confirm: String) {
        if (confirm != _password.value) {
            _confirmPasswordError.value = "Passwords do not match"
        } else {
            _confirmPasswordError.value = null
        }
    }

    fun createAccount(onSuccess: () -> Unit) {
        // Final validation
        validateFullName(_fullName.value)
        validateEmail(_email.value)
        validatePhone(_phoneNumber.value)
        validatePassword(_password.value)
        validateConfirmPassword(_confirmPassword.value)

        if (_fullNameError.value != null || _emailError.value != null || 
            _phoneError.value != null || _passwordError.value != null || 
            _confirmPasswordError.value != null) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
             try {
                com.google.firebase.auth.FirebaseAuth.getInstance().createUserWithEmailAndPassword(_email.value, _password.value)
                    .addOnCompleteListener { task ->
                        _isLoading.value = false
                        if (task.isSuccessful) {
                            // Optionally save user details (fullName, phone) to Firestore/Database here
                            // For now, adhering to the "Authentication process" request strictly, just auth.
                            onSuccess()
                        } else {
                            // Show error. Using a field error for feedback.
                             _emailError.value = task.exception?.localizedMessage ?: "Registration failed."
                        }
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                 _emailError.value = e.localizedMessage ?: "An error occurred."
            }
        }
    }
}
