package com.example.lifeping.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

// Wrapper to make List stable
@androidx.compose.runtime.Immutable
data class CheckInHistoryWrapper(
    val items: List<CheckInItem>
)

data class HomeStats(
    val totalCheckIns: Int,
    val streakDays: Int,
    val missedCheckIns: Int
)

@dagger.hilt.android.lifecycle.HiltViewModel
class HomeViewModel @javax.inject.Inject constructor(
    private val checkInManager: com.example.lifeping.data.repository.CheckInManager,
    private val checkInDao: com.example.lifeping.data.local.CheckInDao
) : ViewModel() {

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

    // Real stats calculation
    val stats: StateFlow<HomeStats> = checkInDao.getAllCheckIns()
        .map { checkIns ->
            val total = checkIns.size
            // Simple streak logic: consecutive days with check-ins (simplified)
            val streak = if (checkIns.isNotEmpty()) 1 else 0 
            val missed = checkIns.count { it.status == com.example.lifeping.data.model.CheckInStatus.MISSED }
            HomeStats(total, streak, missed)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeStats(0, 0, 0)
        )

    private val _nextCheckInTime = MutableStateFlow<LocalDateTime?>(null)
    private var currentIntervalMs = 0L
    private var lastCheckInTime = LocalDateTime.now()
    
    private val _countdownText = MutableStateFlow("Loading...")
    val countdownText: StateFlow<String> = _countdownText.asStateFlow()

    private val _targetTimeText = MutableStateFlow("...")
    val targetTimeText: StateFlow<String> = _targetTimeText.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _isCheckInAllowed = MutableStateFlow(false)
    val isCheckInAllowed: StateFlow<Boolean> = _isCheckInAllowed.asStateFlow()

    val checkInHistory: StateFlow<CheckInHistoryWrapper> = checkInDao.getRecentCheckIns(10)
        .map { checkIns ->
            val items = checkIns.map { entity ->
                 // Parse timestamp string back to LocalDateTime for formatting or just use string if formatted
                 // In CheckInManager we stored ISO String.
                 val parsedTime = try {
                     LocalDateTime.parse(entity.timestamp)
                 } catch (e: Exception) { LocalDateTime.now() }
                 
                 CheckInItem(
                     id = entity.id,
                     time = parsedTime.format(DateTimeFormatter.ofPattern("EEEE, h:mm a")),
                     status = entity.status.name
                 )
            }
            CheckInHistoryWrapper(items)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CheckInHistoryWrapper(emptyList())
        )

    init {
        loadUserProfile()
        observeNextDeadline()
        startTimer()
    }
    
    private fun observeNextDeadline() {
        viewModelScope.launch {
            checkInManager.getNextDeadlineFlow().collect { info ->
                _nextCheckInTime.value = info.targetTime
                _targetTimeText.value = info.targetTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                currentIntervalMs = info.intervalMs
                lastCheckInTime = info.lastCheckInTime
                updateCountdown()
            }
        }
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
        val target = _nextCheckInTime.value ?: return
        val duration = Duration.between(now, target)

        if (duration.isNegative) {
            _countdownText.value = "Overdue!"
            _status.value = "Attention Needed"
            _progress.value = 1f
            _isCheckInAllowed.value = true
        } else {
            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60
            _countdownText.value = "${hours}h ${minutes}m"
            _status.value = "All Good"
            
            val elapsed = java.time.Duration.between(lastCheckInTime, now).toMillis()
            val calculatedProgress = if (currentIntervalMs > 0) elapsed.toFloat() / currentIntervalMs else 0f
            _progress.value = calculatedProgress.coerceIn(0f, 1f)
            _isCheckInAllowed.value = false
        }
    }

    fun onCheckInNow() {
        viewModelScope.launch {
            checkInManager.performCheckIn()
            // Real-time flow handles UI updates automatically
        }
    }
}
