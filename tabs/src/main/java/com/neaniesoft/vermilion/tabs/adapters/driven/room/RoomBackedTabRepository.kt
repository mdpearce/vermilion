package com.neaniesoft.vermilion.tabs.adapters.driven.room

import androidx.room.withTransaction
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.dbentities.tabs.NewTabStateRecord
import com.neaniesoft.vermilion.dbentities.tabs.TabStateDao
import com.neaniesoft.vermilion.dbentities.tabs.TabStateRecord
import com.neaniesoft.vermilion.tabs.domain.entities.DisplayName
import com.neaniesoft.vermilion.tabs.domain.entities.NewTabState
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.TabId
import com.neaniesoft.vermilion.tabs.domain.entities.TabSortOrderIndex
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.tabs.domain.ports.TabRepository
import com.neaniesoft.vermilion.uistate.TabType
import com.neaniesoft.vermilion.utils.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomBackedTabRepository @Inject constructor(
    private val database: VermilionDatabase,
    private val tabStateDao: TabStateDao,
    private val postDao: PostDao
) : TabRepository {

    private val logger by logger()

    override val currentTabs: Flow<List<TabState>> =
        tabStateDao.getAllCurrentTabs().distinctUntilChanged().map {
            it.map { record ->
                record.toTabState()
            }
        }

    override val activeTab: Flow<TabState?> =
        tabStateDao.getActiveTab().distinctUntilChanged().map {
            it?.toTabState()
        }

    override suspend fun addNewTabIfNotExists(tab: NewTabState): TabState {
        return database.withTransaction {
            val existingTab = tabStateDao.findByParentAndType(tab.parentId.value, tab.type.name)
            if (existingTab == null) {
                val newRecord = tab.toNewTabStateRecord()
                if (newRecord.tabSortOrder == -1) {
                    tabStateDao.shiftAllTabsFrom(0)
                }
                tabStateDao.insertAll(newRecord)
                tabStateDao.findByParentAndType(tab.parentId.value, tab.type.name)
                    ?.toTabState() ?: throw IllegalStateException("Could not find saved tab record")
            } else {
                existingTab.toTabState()
            }
        }
    }

    override suspend fun updateScrollStateForTab(
        parentId: ParentId,
        type: TabType,
        scrollPosition: com.neaniesoft.vermilion.coreentities.ScrollPosition
    ) {
        database.withTransaction {
            tabStateDao.updateTabWithScrollState(
                parentId.value,
                type.name,
                scrollPosition.index,
                scrollPosition.offset
            )
            tabStateDao.findByParentAndType(parentId.value, TabType.POST_DETAILS.name)
        }
    }

    override suspend fun findTab(parentId: ParentId, type: TabType): TabState? {
        return database.withTransaction {
            tabStateDao.findByParentAndType(parentId.value, type.name)
        }?.toTabState().also { logger.debugIfEnabled { "Found tab: $it" } }
    }

    private suspend fun NewTabState.toNewTabStateRecord(): NewTabStateRecord {
        val leftMostIndex = tabStateDao.getLeftMostSortIndex() ?: Int.MAX_VALUE
        return NewTabStateRecord(
            type.name,
            parentId.value,
            displayName.value,
            createdAt.toEpochMilli(),
            leftMostIndex - 1,
            scrollPosition.index,
            scrollPosition.offset,
            isActive
        )
    }

    override suspend fun setActiveTab(parentId: ParentId, type: TabType) {
        database.withTransaction {
            tabStateDao.updateAllTabsToInactive()
            tabStateDao.setActiveTab(parentId.value, type.name)
        }
    }

    private suspend fun displayNameForPostDetails(parentId: ParentId): DisplayName {
        val post = postDao.postWithId(parentId.value)
            ?: throw IllegalStateException("Post ${parentId.value} not found in db")

        return DisplayName(post.title)
    }

    override suspend fun displayName(parentId: ParentId, type: TabType): DisplayName {
        return when (type) {
            TabType.POST_DETAILS -> displayNameForPostDetails(parentId)
            TabType.POSTS -> DisplayName(parentId.value)
            TabType.HOME -> DisplayName("Home")
        }
    }

    override suspend fun removeAll() {
        database.withTransaction {
            tabStateDao.deleteAll()
        }
    }

    override suspend fun removeTab(tab: TabState) {
        database.withTransaction {
            tabStateDao.deleteTabWithId(tab.id.value)
        }
    }

    private fun TabStateRecord.toTabState(): TabState {
        return TabState(
            TabId(id),
            ParentId(parentId),
            TabType.valueOf(type),
            DisplayName(displayName),
            Instant.ofEpochMilli(createdAt),
            TabSortOrderIndex(tabSortOrder),
            com.neaniesoft.vermilion.coreentities.ScrollPosition(scrollPosition, scrollOffset)
        )
    }
}
