package com.example.lifeping.ui.about

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userProfilePictureUrl = MutableStateFlow("")
    val userProfilePictureUrl: StateFlow<String> = _userProfilePictureUrl.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection("users").document(uid).addSnapshotListener { document, e ->
                if (e != null) return@addSnapshotListener
                
                if (document != null && document.exists()) {
                    _userName.value = document.getString("fullName") ?: auth.currentUser?.displayName ?: "User"
                    _userEmail.value = document.getString("email") ?: auth.currentUser?.email ?: ""
                    _userProfilePictureUrl.value = document.getString("profilePictureUrl") ?: auth.currentUser?.photoUrl?.toString() ?: ""
                } else {
                    _userName.value = auth.currentUser?.displayName ?: "User"
                    _userEmail.value = auth.currentUser?.email ?: ""
                    _userProfilePictureUrl.value = auth.currentUser?.photoUrl?.toString() ?: ""
                }
            }
        }
    }
}
