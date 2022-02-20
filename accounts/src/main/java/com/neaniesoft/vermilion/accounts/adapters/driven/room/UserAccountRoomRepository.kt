package com.neaniesoft.vermilion.accounts.adapters.driven.room

import androidx.room.withTransaction
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import com.neaniesoft.vermilion.accounts.domain.entities.AccountError
import com.neaniesoft.vermilion.accounts.domain.entities.DatabaseError
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccountId
import com.neaniesoft.vermilion.accounts.domain.entities.UserName
import com.neaniesoft.vermilion.accounts.domain.ports.UserAccountRepository
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.useraccount.UserAccountDao
import com.neaniesoft.vermilion.dbentities.useraccount.UserAccountRecord
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAccountRoomRepository @Inject constructor(
    private val database: VermilionDatabase,
    private val userAccountDao: UserAccountDao
) :
    UserAccountRepository {
    override suspend fun getUserAccountWithId(id: UserAccountId): UserAccount {
        val dbRecord = database.withTransaction { userAccountDao.getById(id.value.toString()) }

        return dbRecord.toUserAccount()
    }

    override suspend fun saveUserAccount(account: UserAccount): Result<UserAccount, AccountError> {
        return runCatching {
            database.withTransaction {
                userAccountDao.insertAll(account.toRecord())
            }
        }.map { account }
            .mapError { DatabaseError(it) }
    }

    override suspend fun clearAccount(account: UserAccount) {
        database.withTransaction {
            userAccountDao.delete(account.toRecord())
        }
    }
}

private fun UserAccount.toRecord(): UserAccountRecord {
    return UserAccountRecord(
        id.value.toString(),
        username.value
    )
}

private fun UserAccountRecord.toUserAccount(): UserAccount {
    return UserAccount(
        UserAccountId(UUID.fromString(id)),
        UserName(userName)
    )
}
