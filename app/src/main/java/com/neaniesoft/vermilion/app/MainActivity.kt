package com.neaniesoft.vermilion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.paging.ExperimentalPagingApi
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.neaniesoft.vermilion.accounts.domain.UserAccountService
import dagger.hilt.android.AndroidEntryPoint
import java.time.Clock
import javax.inject.Inject

@ExperimentalPagingApi
@ExperimentalMaterialNavigationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var clock: Clock

    @Inject
    lateinit var userAccountService: UserAccountService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VermilionApp(clock, userAccountService)
        }
    }
}
