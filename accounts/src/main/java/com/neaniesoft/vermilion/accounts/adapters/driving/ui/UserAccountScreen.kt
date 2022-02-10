package com.neaniesoft.vermilion.accounts.adapters.driving.ui

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.neaniesoft.vermilion.accounts.R
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

@Composable
fun UserAccountScreen(viewModel: UserAccountViewModel = hiltViewModel()) {
    val currentUserAccount by viewModel.currentUser.collectAsState()

    val loginClicked: MutableState<Boolean> = remember { mutableStateOf(false) }
    val logoutClicked: MutableState<Boolean> = remember { mutableStateOf(false) }

    if (loginClicked.value) {
        val intent = viewModel.onLoginClicked()
        LaunchAuthFlow(
            intent
        ) { response, exception -> viewModel.onAuthorizationResponse(response, exception) }
    }
    if (logoutClicked.value) {
        viewModel.onLogoutClicked()
    }

    if (currentUserAccount == null) {
        NotLoggedIn {
            loginClicked.value = true
        }
    } else {
        LoggedIn {
            logoutClicked.value = true
        }
    }
}

@Composable
fun LaunchAuthFlow(
    intent: Intent,
    onAuthResponse: (AuthorizationResponse?, AuthorizationException?) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val data = requireNotNull(result.data)
            val response = AuthorizationResponse.fromIntent(data)
            val exception = AuthorizationException.fromIntent(data)

            if (exception != null) {
                Log.e("LaunchAuthFlow", exception.error.toString())
                Log.e("LaunchAuthFlow", exception.errorDescription.toString())
            }
            onAuthResponse(response, exception)
        })

    LaunchedEffect(Unit) {
        launcher.launch(intent)
    }
}

@Composable
fun NotLoggedIn(onLogInClicked: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Button(onLogInClicked, Modifier.align(Alignment.TopCenter)) {
            Text(
                text = stringResource(id = R.string.my_account_log_in),
                style = MaterialTheme.typography.button
            )
        }
    }
}

@Composable
fun LoggedIn(onLogOutClicked: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Button(onLogOutClicked, Modifier.align(Alignment.TopCenter)) {
            Text(
                text = stringResource(id = R.string.my_account_log_out),
                style = MaterialTheme.typography.button
            )
        }
    }
}

@Preview
@Composable
fun NotLoggedInPreview() {
    VermilionTheme {
        NotLoggedIn {}
    }
}
