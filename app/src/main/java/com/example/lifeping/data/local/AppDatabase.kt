package com.example.lifeping.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lifeping.data.model.Contact
import com.example.lifeping.data.model.CheckIn

@Database(entities = [Contact::class, CheckIn::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun checkInDao(): CheckInDao
}
