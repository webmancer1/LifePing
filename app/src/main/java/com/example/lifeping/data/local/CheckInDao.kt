package com.example.lifeping.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lifeping.data.model.CheckIn
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckIn)

    @Query("SELECT * FROM check_ins ORDER BY id DESC LIMIT 1")
    fun getLatestCheckIn(): Flow<CheckIn?>

    @Query("SELECT * FROM check_ins ORDER BY id DESC")
    fun getAllCheckIns(): Flow<List<CheckIn>>
    
    @Query("SELECT * FROM check_ins ORDER BY id DESC LIMIT :limit")
    fun getRecentCheckIns(limit: Int): Flow<List<CheckIn>>
}
