package com.neaniesoft.vermilion.ui.images

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.rememberImagePainter

@Composable
fun ImageDialog(imageUri: Uri) {
    Log.d("ImageDialog", "Loading image uri: $imageUri")
    Surface(Modifier.fillMaxSize()) {
        val painter = rememberImagePainter(imageUri)

        Image(modifier = Modifier.fillMaxSize(), painter = painter, contentDescription = "Image")
    }
}
