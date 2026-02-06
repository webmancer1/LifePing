package com.example.lifeping.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "check_ins")
data class CheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: String, // Storing as String for simplicity in this pass, could be Long/OffsetDateTime
    val status: CheckInStatus = CheckInStatus.COMPLETED,
    val synced: Boolean = false
)

enum class CheckInStatus {
    COMPLETED,
    MISSED,
    LATE
}
