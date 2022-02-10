package com.neaniesoft.vermilion.accounts.adapters.driven.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "user_accounts")
data class UserAccountRecord(
    @PrimaryKey val id: UUID,
    @ColumnInfo(name = "username") val userName: String
)
