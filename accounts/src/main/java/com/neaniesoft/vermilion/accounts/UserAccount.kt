package com.neaniesoft.vermilion.accounts

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val id: UUID,
    @ColumnInfo(name = "username") val userName: String,
    @ColumnInfo(name = "auth_token_id") val authTokenId: String
)
