package com.neaniesoft.vermilion.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neaniesoft.vermilion.dbentities.useraccount.UserAccountDao
import com.neaniesoft.vermilion.dbentities.useraccount.UserAccountRecord

@Database(entities = [UserAccountRecord::class], version = 1)
abstract class VermilionDatabase : RoomDatabase() {
    abstract fun userAccountDao(): UserAccountDao
}
