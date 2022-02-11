package com.neaniesoft.vermilion.app

enum class VermilionScreen {
    Posts,
    MyAccount;

    companion object {
        fun fromRoute(route: String?): VermilionScreen = when (route?.substringBefore("/")) {
            Posts.name -> Posts
            MyAccount.name -> MyAccount
            null -> Posts
            else -> throw IllegalArgumentException("Unrecognized route: $route")
        }
    }
}
