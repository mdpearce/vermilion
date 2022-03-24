package com.neaniesoft.vermilion.app

enum class VermilionScreen {
    Home,
    Posts,
    PostDetails,
    CommentThread,
    MyAccount,
    CustomTab,
    Image,
    ImageGallery,
    Video,
    ExternalVideo,
    YouTube;

    companion object {
        fun fromRoute(route: String?): VermilionScreen = when (route?.substringBefore("/")) {
            Home.name -> Home
            Posts.name -> Posts
            PostDetails.name -> PostDetails
            CommentThread.name -> CommentThread
            MyAccount.name -> MyAccount
            CustomTab.name -> CustomTab
            Image.name -> Image
            ImageGallery.name -> ImageGallery
            Video.name -> Video
            ExternalVideo.name -> ExternalVideo
            YouTube.name -> YouTube
            null -> Home
            else -> throw IllegalArgumentException("Unrecognized route: $route")
        }
    }
}
