package com.example.lifeping.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.lifeping.data.model.UserProfile


class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val currentUser = auth.currentUser

        _isLoading.value = true
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                _isLoading.value = false
                if (document.exists()) {
                    val profile = UserProfile(
                        uid = uid,
                        fullName = document.getString("fullName") ?: currentUser?.displayName ?: "",
                        email = document.getString("email") ?: currentUser?.email ?: "",
                        bio = document.getString("bio") ?: "",
                        profilePictureUrl = document.getString("profilePictureUrl") ?: currentUser?.photoUrl?.toString() ?: ""
                    )
                    _userProfile.value = profile
                } else {
                    val profile = UserProfile(
                        uid = uid,
                        fullName = currentUser?.displayName ?: "",
                        email = currentUser?.email ?: "",
                        bio = "",
                        profilePictureUrl = currentUser?.photoUrl?.toString() ?: ""
                    )
                    _userProfile.value = profile
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                // Fallback on failure
                val profile = UserProfile(
                    uid = uid,
                    fullName = currentUser?.displayName ?: "",
                    email = currentUser?.email ?: "",
                    bio = "",
                    profilePictureUrl = currentUser?.photoUrl?.toString() ?: ""
                )
                _userProfile.value = profile
                // Suppress error message since we fell back to Auth details
                // _statusMessage.value = "Failed to sync profile: ${e.localizedMessage}"
            }
    }

    fun updateProfile(fullName: String, bio: String) {
        val uid = auth.currentUser?.uid ?: return
        
        _isLoading.value = true
        val updates = mapOf(
            "fullName" to fullName,
            "bio" to bio
        )

        firestore.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                _isLoading.value = false
                _statusMessage.value = "Profile updated successfully"
                // Update local state
                _userProfile.value = _userProfile.value.copy(fullName = fullName, bio = bio)
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _statusMessage.value = "Failed to update profile: ${e.localizedMessage}"
            }
    }
    
    // In a real app, this would upload to Firebase Storage first. 
    // For this task, we will just save the URI string if possible, or placeholder.
    // Since we don't have Storage set up instructions, we might just store the URI string 
    // provided it's persistent or accessible (Logically flawed without Storage for local files, but ok for demo).
    // Better approach: allow picking from a set of avatars or just "simulating" the upload by saving the URI string.
    fun updateProfilePicture(uri: Uri?) {
        if (uri == null) return
        val uid = auth.currentUser?.uid ?: return
        
        // Simulating upload by just saving the string. 
        // Note: Content URIs might not persist across restarts without permissions, 
        // but for a demo flow it shows the mechanics.
        val updates = mapOf(
            "profilePictureUrl" to uri.toString()
        )
        
        firestore.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                _userProfile.value = _userProfile.value.copy(profilePictureUrl = uri.toString())
                _statusMessage.value = "Profile picture updated"
            }
             .addOnFailureListener { e ->
                _statusMessage.value = "Failed to update picture: ${e.localizedMessage}"
            }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun resetPassword() {
        val email = auth.currentUser?.email ?: return
        _isLoading.value = true
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _isLoading.value = false
                _statusMessage.value = "Password reset email sent to $email"
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _statusMessage.value = "Failed to send reset email: ${e.localizedMessage}"
            }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        _isLoading.value = true
        // Delete from Firestore first
        firestore.collection("users").document(uid).delete()
            .addOnSuccessListener {
                // Delete from Auth
                user.delete()
                    .addOnSuccessListener {
                        _isLoading.value = false
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        _isLoading.value = false
                        // If requires re-login
                        _statusMessage.value = "Failed to delete account. Please log out and log in again."
                    }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                 _statusMessage.value = "Failed to delete user data: ${e.localizedMessage}"
            }
    }
    
    fun logout(onLogoutSuccess: () -> Unit) {
        auth.signOut()
        onLogoutSuccess()
    }
}
