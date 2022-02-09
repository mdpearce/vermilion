package com.neaniesoft.vermilion.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neaniesoft.vermilion.accounts.adapters.driven.room.UserAccountRecord
import com.neaniesoft.vermilion.accounts.adapters.driven.room.UserAccountDao

@Database(entities = [UserAccountRecord::class], version = 1)
abstract class VermilionDatabase : RoomDatabase() {
    abstract fun userAccountDao(): UserAccountDao
}