package com.neaniesoft.vermilion.dbentities.useraccount

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserAccountDao {
    @Query("SELECT * from user_accounts")
    suspend fun getAll(): List<UserAccountRecord>

    @Query("SELECT * from user_accounts WHERE id=:userId")
    suspend fun getById(userId: String): UserAccountRecord

    @Insert
    suspend fun insertAll(vararg userAccount: UserAccountRecord)

    @Delete
    suspend fun delete(userAccount: UserAccountRecord)
}
