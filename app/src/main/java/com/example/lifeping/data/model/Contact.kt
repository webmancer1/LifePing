package com.example.lifeping.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val relationship: String,
    val email: String,
    val phoneNumber: String,
    val notifyViaSms: Boolean = true,
    val notifyViaEmail: Boolean = false,
    val notifyViaWhatsapp: Boolean = false
)
