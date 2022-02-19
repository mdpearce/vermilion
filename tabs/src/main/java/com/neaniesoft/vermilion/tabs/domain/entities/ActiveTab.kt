package com.neaniesoft.vermilion.tabs.domain.entities

sealed class ActiveTab {
    object None : ActiveTab()
    object Home : ActiveTab()
    data class Tab(val id: TabId) : ActiveTab()
}
