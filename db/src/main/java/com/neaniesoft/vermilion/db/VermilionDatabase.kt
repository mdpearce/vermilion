package com.neaniesoft.vermilion.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.dbentities.posts.PostRecord
import com.neaniesoft.vermilion.dbentities.useraccount.UserAccountDao
import com.neaniesoft.vermilion.dbentities.useraccount.UserAccountRecord

@Database(entities = [UserAccountRecord::class, PostRecord::class], version = 2)
abstract class VermilionDatabase : RoomDatabase() {
    abstract fun userAccountDao(): UserAccountDao
    abstract fun postDao(): PostDao
}
