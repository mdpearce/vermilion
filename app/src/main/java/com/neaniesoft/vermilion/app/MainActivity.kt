package com.neaniesoft.vermilion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.ExperimentalPagingApi
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import dagger.hilt.android.AndroidEntryPoint
import java.time.Clock
import javax.inject.Inject

@ExperimentalPagingApi
@ExperimentalMaterialNavigationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var clock: Clock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VermilionApp(clock)
        }
    }
}

@ExperimentalPagingApi
@ExperimentalMaterialNavigationApi
@Preview(showBackground = true)
@Composable
fun VermilionAppPreview() {
    VermilionApp(Clock.systemUTC())
}
