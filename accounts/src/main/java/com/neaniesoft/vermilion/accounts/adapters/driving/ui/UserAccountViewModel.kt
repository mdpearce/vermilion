package com.neaniesoft.vermilion.accounts.adapters.driving.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.neaniesoft.vermilion.accounts.domain.UserAccountRepository
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.flow.StateFlow
import net.openid.appauth.AuthorizationService
import javax.inject.Inject

@HiltViewModel
class UserAccountViewModel @Inject constructor(
    private val userAccountRepository: UserAccountRepository
) : ViewModel() {

    val currentUser: StateFlow<UserAccount?> = userAccountRepository.currentUserAccount

    private fun startAuthFlow() {
        TODO("Not yet implemented")
    }

    fun startLoginFlow() {
        TODO("Not yet implemented")
    }
}

interface AuthUiProvider {
    fun startAuthFlow()
}

// class AppAuthUiProvider @Inject constructor(
//     @Named(RedditApiClientModule.REDDIT_API_CLIENT_ID) private val clientId: String,
//     private val authorizationService: AuthorizationService
// ) : AuthUiProvider {
//     companion object {
//         private val REDIRECT_URL = "com.neaniesoft.vermilion://oauth2redirect".toUri()
//         private const val RESPONSE_TYPE = "code"
//         private const val DURATION = "permanent"
//         private const val SCOPE = "read"
//     }
//
//     override fun startAuthFlow() {
//         val stateString = stateString()
//
//         val configuration = configuration()
//
//         val request = AuthorizationRequest.Builder(configuration, clientId, RESPONSE_TYPE, REDIRECT_URL)
//             .setScope(SCOPE)
//             .setAdditionalParameters(mapOf("duration" to DURATION))
//             .build()
//
//         authorizationService.performAuthorizationRequest(PendingIntent())
//     }
//
//     private fun configuration() = AuthorizationServiceConfiguration(
//         authorizationUrl(),
//         tokenUrl()
//     )
//
//     private fun authorizationUrl(): Uri =
//         "https://www.reddit.com/api/v1/authorize?duration=$DURATION&scope=$SCOPE".toUri()
//
//     private fun tokenUrl(): Uri =
//         "https://www.reddit.com/api/v1/access_token".toUri()
//
//     private fun stateString() = UUID.randomUUID().toString()
// }

@Module
@InstallIn(ActivityComponent::class)
class AppAuthModule {
    @Provides
    fun provideAuthorizationService(@ActivityContext context: Context): AuthorizationService =
        AuthorizationService(context)
}