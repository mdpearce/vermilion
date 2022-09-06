package com.neaniesoft.vermilion.tabs.adapters.driven.sqldelight

import com.neaniesoft.vermilion.coreentities.ScrollPosition
import com.neaniesoft.vermilion.db.Database
import com.neaniesoft.vermilion.db.PostQueries
import com.neaniesoft.vermilion.db.TabQueries
import com.neaniesoft.vermilion.tabs.domain.entities.*
import com.neaniesoft.vermilion.tabs.domain.ports.TabRepository
import com.neaniesoft.vermilion.uistate.TabType
import com.neaniesoft.vermilion.utils.logger
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SqlDelightTabRepository @Inject constructor(
    private val tabQueries: TabQueries,
    private val postQueries: PostQueries
) : TabRepository {

    private val logger by logger()

    private val tabStateMapper =
        { id: Long,
          type: String,
          parent_id: String,
          display_name: String,
          created_at: Long,
          tab_sort_order: Long,
          scroll_position: Long,
          scroll_offset: Long,
          is_active: Long ->
            TabState(
                TabId(id.toInt()),
                ParentId(parent_id),
                TabType.valueOf(type),
                DisplayName(display_name),
                Instant.ofEpochMilli(created_at),
                TabSortOrderIndex(tab_sort_order.toInt()),
                ScrollPosition(scroll_position.toInt(), scroll_offset.toInt())
            )
        }

    override val currentTabs: Flow<List<TabState>> =
        tabQueries.selectAllCurrentTabs(tabStateMapper).asFlow()
            .mapToList()


    override val activeTab: Flow<TabState?> = tabQueries.selectActiveTab(tabStateMapper)
        .asFlow()
        .mapToOneOrNull()


    override suspend fun addNewTabIfNotExists(tab: NewTabState): TabState {
        return tabQueries.transactionWithResult {
            val existingTab = tabQueries.findByParentAndType(
                tab.parentId.value,
                tab.type.name,
                mapper = tabStateMapper
            ).executeAsOneOrNull()

            if (existingTab == null) {
                val leftMostIndex = tabQueries.selectLeftMostSortIndex().executeAsOneOrNull() ?: 0L

                if (leftMostIndex == 0L) {
                    tabQueries.shiftAllTabsFrom(0L)
                }

                tabQueries.insert(
                    id = null,
                    type = tab.type.name,
                    parent_id = tab.parentId.value,
                    tab.displayName.value,
                    tab.createdAt.toEpochMilli(),
                    tab_sort_order = leftMostIndex - 1L,
                    scroll_position = tab.scrollPosition.index.toLong(),
                    scroll_offset = tab.scrollPosition.offset.toLong(),
                    is_active = if (tab.isActive) {
                        1L
                    } else {
                        0L
                    }
                )

                tabQueries.findByParentAndType(
                    parentId = tab.parentId.value,
                    type = tab.type.name,
                    mapper = tabStateMapper
                ).executeAsOne()
            } else {
                existingTab
            }
        }
    }

    override suspend fun removeTab(tab: TabState) {
        tabQueries.deleteTabWithId(tab.id.value.toLong())
    }

    override suspend fun removeAll() {
        tabQueries.deleteAll()
    }

    override suspend fun updateScrollStateForTab(
        parentId: ParentId,
        type: TabType,
        scrollPosition: ScrollPosition
    ) {
        logger.debugIfEnabled { "Updating record for $parentId to $scrollPosition" }

        tabQueries.updateTabWithScrollState(
            scrollPosition = scrollPosition.index.toLong(),
            scrollOffset = scrollPosition.offset.toLong(),
            parentId = parentId.value,
            type = type.name
        )
    }

    override suspend fun displayName(parentId: ParentId, type: TabType): DisplayName {
        return when (type) {
            TabType.POST_DETAILS -> DisplayName(
                postQueries.postWithId(parentId.value).executeAsOne().title
            )
            TabType.POSTS -> DisplayName(parentId.value)
            TabType.HOME -> DisplayName("Home") // lol const
        }
    }

    override suspend fun findTab(parentId: ParentId, type: TabType): TabState? {
        return tabQueries.findByParentAndType(parentId.value, type.name, tabStateMapper)
            .executeAsOneOrNull()
    }

    override suspend fun setActiveTab(parentId: ParentId, type: TabType) {
        tabQueries.transaction {
            val activeTab = tabQueries.selectActiveTab().executeAsOneOrNull()
            if (!(activeTab?.type == type.name && activeTab.parent_id == parentId.value)) {
                tabQueries.updateAllTabsToInactive()
                tabQueries.setActiveTab(parentId.value, type.name)
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
class TabQueriesModule {
    @Provides
    @Singleton
    fun provideTabQueries(database: Database): TabQueries = database.tabQueries
}