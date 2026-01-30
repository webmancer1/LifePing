package com.example.lifeping.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeping.data.model.Contact
import com.example.lifeping.data.preferences.UserPreferencesRepository
import com.example.lifeping.data.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lifeping.worker.CheckInWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManager: WorkManager
) : ViewModel() {

    val contacts: StateFlow<List<Contact>> = contactRepository.getAllContacts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val checkInInterval = userPreferencesRepository.checkInInterval.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 4 * 60 * 60 * 1000L
    )

    val gracePeriod = userPreferencesRepository.gracePeriod.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 30 * 60 * 1000L
    )

    val notifyUpcoming = userPreferencesRepository.notifyUpcoming.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val autoAlert = userPreferencesRepository.autoAlert.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun addContact(contact: Contact) {
        viewModelScope.launch {
            contactRepository.insertContact(contact)
        }
    }

    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            contactRepository.updateContact(contact)
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            contactRepository.deleteContact(contact)
        }
    }

    fun setCheckInInterval(interval: Long) {
        viewModelScope.launch {
            userPreferencesRepository.saveCheckInInterval(interval)
            
            val workRequest = PeriodicWorkRequestBuilder<CheckInWorker>(interval, TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "check_in_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }

    fun setGracePeriod(period: Long) {
        viewModelScope.launch {
            userPreferencesRepository.saveGracePeriod(period)
        }
    }

    fun setNotifyUpcoming(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveNotifyUpcoming(enabled)
        }
    }

    fun setAutoAlert(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveAutoAlert(enabled)
        }
    }
}
