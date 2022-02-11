package com.neaniesoft.vermilion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalMaterialNavigationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VermilionApp()
        }
    }
}

@ExperimentalMaterialNavigationApi
@Preview(showBackground = true)
@Composable
fun VermilionAppPreview() {
    VermilionApp()
}
