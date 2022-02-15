package com.neaniesoft.vermilion.app

enum class VermilionScreen {
    Posts,
    PostDetails,
    MyAccount,
    CustomTab;

    companion object {
        fun fromRoute(route: String?): VermilionScreen = when (route?.substringBefore("/")) {
            Posts.name -> Posts
            PostDetails.name -> PostDetails
            MyAccount.name -> MyAccount
            CustomTab.name -> CustomTab
            null -> Posts
            else -> throw IllegalArgumentException("Unrecognized route: $route")
        }
    }
}
