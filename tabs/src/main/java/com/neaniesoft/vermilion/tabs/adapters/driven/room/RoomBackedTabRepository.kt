package com.neaniesoft.vermilion.tabs.adapters.driven.room

import androidx.room.withTransaction
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.dbentities.tabs.NewTabStateRecord
import com.neaniesoft.vermilion.dbentities.tabs.TabStateDao
import com.neaniesoft.vermilion.dbentities.tabs.TabStateRecord
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.tabs.domain.entities.DisplayName
import com.neaniesoft.vermilion.tabs.domain.entities.NewTabState
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.tabs.domain.entities.TabId
import com.neaniesoft.vermilion.tabs.domain.entities.TabSortOrderIndex
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import com.neaniesoft.vermilion.tabs.domain.ports.TabRepository
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
        scrollPosition: ScrollPosition
    ) {
        logger.debugIfEnabled { "Updating scroll state for post: ${parentId.value} to: ${scrollPosition.value}" }
        database.withTransaction {
            tabStateDao.updateTabWithScrollState(
                parentId.value,
                type.name,
                scrollPosition.value
            )
            tabStateDao.findByParentAndType(parentId.value, TabType.POST_DETAILS.name)
        }
    }

    private suspend fun NewTabState.toNewTabStateRecord(): NewTabStateRecord {
        val leftMostIndex = tabStateDao.getLeftMostSortIndex() ?: Int.MAX_VALUE
        return NewTabStateRecord(
            type.name,
            parentId.value,
            displayName.value,
            createdAt.toEpochMilli(),
            leftMostIndex - 1,
            scrollPosition.value
        )
    }

    override suspend fun displayNameForPostDetails(postId: PostId): DisplayName {
        val post = postDao.postWithId(postId.value)
            ?: throw IllegalStateException("Post ${postId.value} not found in db")

        return DisplayName(post.title)
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
            ScrollPosition(scrollPosition)
        )
    }
}
