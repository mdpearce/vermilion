package com.neaniesoft.vermilion.accounts.adapters.driven.room

import com.neaniesoft.vermilion.accounts.domain.entities.AuthTokenId
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccountId
import com.neaniesoft.vermilion.accounts.domain.ports.UserAccountRecordRepository
import javax.inject.Inject

class UserAccountRoomRecordRepository @Inject constructor(private val userAccountDao: UserAccountDao) :
    UserAccountRecordRepository {
    override suspend fun getUserAccountWithId(id: UserAccountId): UserAccount {
        val dbRecord = userAccountDao.getById(id.value)

        return dbRecord.toUserAccount()
    }
}

private fun UserAccountRecord.toUserAccount(): UserAccount {
    return UserAccount(
        UserAccountId(id),
        AuthTokenId(authTokenId)
    )
}
