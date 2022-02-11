package com.neaniesoft.vermilion.dbentities.useraccount

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountRecord(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "username") val userName: String
)
