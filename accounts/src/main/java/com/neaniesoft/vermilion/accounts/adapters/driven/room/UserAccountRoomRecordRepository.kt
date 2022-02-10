package com.neaniesoft.vermilion.accounts.adapters.driven.room

import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccountId
import com.neaniesoft.vermilion.accounts.domain.entities.UserName
import com.neaniesoft.vermilion.accounts.domain.ports.UserAccountRecordRepository
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.useraccount.UserAccountDao
import com.neaniesoft.vermilion.dbentities.useraccount.UserAccountRecord
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAccountRoomRecordRepository @Inject constructor(private val userAccountDao: UserAccountDao) :
    UserAccountRecordRepository {
    override suspend fun getUserAccountWithId(id: UserAccountId): UserAccount {
        val dbRecord = userAccountDao.getById(id.value)

        return dbRecord.toUserAccount()
    }

    override suspend fun saveUserAccount(account: UserAccount) {
        userAccountDao.insertAll(account.toRecord())
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserAccountRecordModule {
    @Binds
    abstract fun provideUserAccountRecordRepository(repo: UserAccountRoomRecordRepository): UserAccountRecordRepository
}

private fun UserAccount.toRecord(): UserAccountRecord {
    return UserAccountRecord(
        id.value,
        username.value
    )
}

private fun UserAccountRecord.toUserAccount(): UserAccount {
    return UserAccount(
        UserAccountId(id),
        UserName(userName)
    )
}

@Module
@InstallIn(SingletonComponent::class)
class UserAccountDaoModule {

    @Provides
    @Singleton
    fun provideUserAccountDao(db: VermilionDatabase): UserAccountDao =
        db.userAccountDao()
}
