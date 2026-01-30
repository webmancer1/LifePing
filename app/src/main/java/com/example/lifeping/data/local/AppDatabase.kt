package com.example.lifeping.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lifeping.data.model.Contact

@Database(entities = [Contact::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
