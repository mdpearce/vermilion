package com.neaniesoft.vermilion.accounts.adapters.driven.sqldelight

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.neaniesoft.vermilion.accounts.domain.entities.AccountError
import com.neaniesoft.vermilion.accounts.domain.entities.DatabaseError
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccountId
import com.neaniesoft.vermilion.accounts.domain.entities.UserName
import com.neaniesoft.vermilion.accounts.domain.ports.UserAccountRepository
import com.neaniesoft.vermilion.db.UserAccountQueries
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAccountSqlDelightRepository @Inject constructor(
    private val queries: UserAccountQueries
) : UserAccountRepository {
    override suspend fun getUserAccountWithId(id: UserAccountId): UserAccount {
        return queries.selectById(id.value.toString())
            .asFlow()
            .mapToOne()
            .map {
                UserAccount(
                    id = UserAccountId(UUID.fromString(it.id)),
                    username = UserName(it.user_name)
                )
            }
            .first()
    }

    override suspend fun saveUserAccount(account: UserAccount): Result<UserAccount, AccountError> {
        return com.github.michaelbull.result.runCatching {
            queries.insert(account.id.value.toString(), account.username.value)
        }.map { account }
            .mapError { DatabaseError(it) }
    }

    override suspend fun clearAccount(account: UserAccount) {
        queries.delete(account.id.value.toString())
    }
}

@Module
@InstallIn(SingletonComponent::class)
class UserAccountRepositoryModule {
    @Provides
    fun provideUserAccountRepository(queries: UserAccountQueries): UserAccountRepository =
        UserAccountSqlDelightRepository(queries)
}
