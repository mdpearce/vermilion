package com.neaniesoft.vermilion.accounts

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.util.UUID

@Dao
interface UserAccountDao {
    @Query("SELECT * from user_accounts")
    suspend fun getAll(): List<UserAccount>

    @Query("SELECT * from user_accounts WHERE id=:userId")
    suspend fun getById(userId: UUID): UserAccount

    @Insert
    suspend fun insertAll(vararg userAccount: UserAccount)

    @Delete
    suspend fun delete(userAccount: UserAccount)
}