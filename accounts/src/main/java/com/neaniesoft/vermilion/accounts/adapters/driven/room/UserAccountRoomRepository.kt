package com.neaniesoft.vermilion.accounts.adapters.driven.room

import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccountId
import com.neaniesoft.vermilion.accounts.domain.entities.UserName
import com.neaniesoft.vermilion.accounts.domain.ports.UserAccountRepository
import com.neaniesoft.vermilion.dbentities.useraccount.UserAccountDao
import com.neaniesoft.vermilion.dbentities.useraccount.UserAccountRecord
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAccountRoomRepository @Inject constructor(private val userAccountDao: UserAccountDao) :
    UserAccountRepository {
    override suspend fun getUserAccountWithId(id: UserAccountId): UserAccount {
        val dbRecord = userAccountDao.getById(id.value.toString())

        return dbRecord.toUserAccount()
    }

    override suspend fun saveUserAccount(account: UserAccount) {
        userAccountDao.insertAll(account.toRecord())
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
