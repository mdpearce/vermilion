package com.neaniesoft.vermilion.posts.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.ui.theme.VermilionTheme

@Composable
fun PostDetails(post: Post, modifier: Modifier = Modifier) {

}


@Preview("Post Details Light")
@Composable
fun PostDetailsLightPreview() {
    VermilionTheme {
        PostDetails(post = DUMMY_TEXT_POST)
    }
}

@Preview("Post Details Dark")
@Composable
fun PostDetailsDarkPreview() {
    VermilionTheme(darkTheme = true) {
        PostDetails(post = DUMMY_TEXT_POST)
    }
}
