package com.neaniesoft.vermilion.accounts.adapters.driven.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.util.UUID

@Dao
interface UserAccountDao {
    @Query("SELECT * from user_accounts")
    suspend fun getAll(): List<UserAccountRecord>

    @Query("SELECT * from user_accounts WHERE id=:userId")
    suspend fun getById(userId: UUID): UserAccountRecord

    @Insert
    suspend fun insertAll(vararg userAccount: UserAccountRecord)

    @Delete
    suspend fun delete(userAccount: UserAccountRecord)
}