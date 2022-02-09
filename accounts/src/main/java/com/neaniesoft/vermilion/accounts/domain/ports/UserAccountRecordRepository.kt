package com.neaniesoft.vermilion.accounts.domain.ports

import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccountId

interface UserAccountRecordRepository {
    suspend fun getUserAccountWithId(id: UserAccountId): UserAccount
}