package com.example.lifeping.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


data class CheckInItem(
    val id: Int,
    val time: String,
    val status: String = "Completed"
)

data class HomeStats(
    val totalCheckIns: Int,
    val streakDays: Int,
    val missedCheckIns: Int
)

class HomeViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userName = MutableStateFlow("")

    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")

    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userProfilePictureUrl = MutableStateFlow("")
    val userProfilePictureUrl: StateFlow<String> = _userProfilePictureUrl.asStateFlow()


    private val _status = MutableStateFlow("All Good")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _stats = MutableStateFlow(HomeStats(47, 12, 0))
    val stats: StateFlow<HomeStats> = _stats.asStateFlow()

    private val _nextCheckInTime = MutableStateFlow(LocalDateTime.now().plusHours(4))
    
    private val _countdownText = MutableStateFlow("Loading...")
    val countdownText: StateFlow<String> = _countdownText.asStateFlow()

    private val _checkInHistory = MutableStateFlow<List<CheckInItem>>(emptyList())
    val checkInHistory: StateFlow<List<CheckInItem>> = _checkInHistory.asStateFlow()

    init {
        loadUserProfile()
        loadMockData()

        startTimer()
    }

    private fun loadMockData() {
        _checkInHistory.value = listOf(
            CheckInItem(1, "Today, 2:30 PM"),
            CheckInItem(2, "Today, 10:15 AM"),
            CheckInItem(3, "Yesterday, 6:45 PM"),
            CheckInItem(4, "Yesterday, 2:20 PM")
        )
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            // Use snapshot listener for real-time updates
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


    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                updateCountdown()
                delay(60000) // Update every minute
            }
        }
    }

    private fun updateCountdown() {
        val now = LocalDateTime.now()
        val target = _nextCheckInTime.value
        val duration = Duration.between(now, target)

        if (duration.isNegative) {
            _countdownText.value = "Now!"
        } else {
            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60
            _countdownText.value = "${hours}h ${minutes}m"
        }
    }

    fun onCheckInNow() {
        // Handle check-in logic
        val now = LocalDateTime.now()
        val formattedTime = now.format(DateTimeFormatter.ofPattern("EEEE, h:mm a"))
        
        val newItem = CheckInItem(
            id = (_checkInHistory.value.maxOfOrNull { it.id } ?: 0) + 1,
            time = "Today, ${now.format(DateTimeFormatter.ofPattern("h:mm a"))}"
        )
        
        _checkInHistory.value = listOf(newItem) + _checkInHistory.value
        
        // Update stats
        _stats.value = _stats.value.copy(
            totalCheckIns = _stats.value.totalCheckIns + 1,
            streakDays = _stats.value.streakDays // Logic to update streak would go here
        )
        
        // Reset timer for demo
        _nextCheckInTime.value = now.plusHours(4)
        updateCountdown()
    }
}
