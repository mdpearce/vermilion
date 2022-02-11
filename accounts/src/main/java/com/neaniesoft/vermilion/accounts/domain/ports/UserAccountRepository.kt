package com.neaniesoft.vermilion.accounts.domain.ports

import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccountId

interface UserAccountRepository {
    suspend fun getUserAccountWithId(id: UserAccountId): UserAccount
    suspend fun saveUserAccount(account: UserAccount)
}