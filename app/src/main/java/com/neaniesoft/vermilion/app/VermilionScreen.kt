package com.neaniesoft.vermilion.app

enum class VermilionScreen {
    Home,
    Posts,
    PostDetails,
    MyAccount,
    CustomTab,
    Image;

    companion object {
        fun fromRoute(route: String?): VermilionScreen = when (route?.substringBefore("/")) {
            Home.name -> Home
            Posts.name -> Posts
            PostDetails.name -> PostDetails
            MyAccount.name -> MyAccount
            CustomTab.name -> CustomTab
            Image.name -> Image
            null -> Home
            else -> throw IllegalArgumentException("Unrecognized route: $route")
        }
    }
}
