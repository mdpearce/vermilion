package com.neaniesoft.vermilion.accounts.domain

import com.neaniesoft.vermilion.accounts.domain.entities.AuthTokenId
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccountId
import com.neaniesoft.vermilion.accounts.domain.ports.UserAccountRecordRepository
import com.neaniesoft.vermilion.auth.AuthorizationStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAccountRepository @Inject constructor(
    private val userAccountRecordRepository: UserAccountRecordRepository,
    private val authorizationStore: AuthorizationStore
) {
    private val _currentUserAccount: MutableStateFlow<UserAccount?> = MutableStateFlow(currentLoggedInUserAccount())
    val currentUserAccount: StateFlow<UserAccount?> = _currentUserAccount.asStateFlow()

    private fun getLoggedInUserId() = authorizationStore.getCurrentLoggedInUserAccountId()
    private fun getLoggedInUserAuthTokenId() = authorizationStore.getCurrentLoggedInUserAuthTokenId()

    private fun currentLoggedInUserAccount(): UserAccount? {
        val currentUserId = getLoggedInUserId()
        val currentAuthTokenId = getLoggedInUserAuthTokenId()
        return if (currentUserId != null && currentAuthTokenId != null) {
            UserAccount(
                UserAccountId(currentUserId),
                AuthTokenId(currentAuthTokenId)
            )
        } else {
            null
        }
    }

}