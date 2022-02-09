package com.neaniesoft.vermilion.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neaniesoft.vermilion.accounts.UserAccount
import com.neaniesoft.vermilion.accounts.UserAccountDao

@Database(entities = [UserAccount::class], version = 1)
abstract class VermilionDatabase : RoomDatabase() {
    abstract fun userAccountDao(): UserAccountDao
}