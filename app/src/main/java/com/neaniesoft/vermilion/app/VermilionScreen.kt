package com.neaniesoft.vermilion.app

enum class VermilionScreen {
    Home,
    Posts,
    PostDetails,
    MyAccount,
    CustomTab,
    Image,
    Video,
    YouTube;

    companion object {
        fun fromRoute(route: String?): VermilionScreen = when (route?.substringBefore("/")) {
            Home.name -> Home
            Posts.name -> Posts
            PostDetails.name -> PostDetails
            MyAccount.name -> MyAccount
            CustomTab.name -> CustomTab
            Image.name -> Image
            Video.name -> Video
            YouTube.name -> YouTube
            null -> Home
            else -> throw IllegalArgumentException("Unrecognized route: $route")
        }
    }
}
