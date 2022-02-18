package com.neaniesoft.vermilion.tabs.adapters.driven.room

import androidx.room.withTransaction
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.tabs.TabStateDao
import com.neaniesoft.vermilion.dbentities.tabs.TabStateRecord
import com.neaniesoft.vermilion.tabs.domain.entities.DisplayName
import com.neaniesoft.vermilion.tabs.domain.entities.Route
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.tabs.domain.entities.TabId
import com.neaniesoft.vermilion.tabs.domain.entities.TabSortOrderIndex
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import com.neaniesoft.vermilion.tabs.domain.ports.TabRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomBackedTabRepository @Inject constructor(
    private val database: VermilionDatabase,
    private val tabStateDao: TabStateDao
) : TabRepository {

    override val currentTabs: Flow<List<TabState>> =
        tabStateDao.getAllCurrentTabs().distinctUntilChanged().map {
            it.map { record ->
                record.toTabState()
            }
        }

    override suspend fun addNewTab(tab: TabState) {
        database.withTransaction {
            tabStateDao.insertAll(tab.toTabStateRecord())
        }
    }

    override suspend fun removeTab(tab: TabState) {
        database.withTransaction {
            tabStateDao.deleteTabWithId(tab.id.value)
        }
    }
}

internal fun TabStateRecord.toTabState(): TabState {
    return TabState(
        TabId(id),
        TabType.valueOf(type),
        Route(route),
        DisplayName(displayName),
        Instant.ofEpochMilli(createdAt),
        TabSortOrderIndex(tabSortOrder),
        ScrollPosition(scrollPosition)
    )
}

internal fun TabState.toTabStateRecord(): TabStateRecord {
    return TabStateRecord(
        id.value,
        type.name,
        route.value,
        displayName.value,
        createdAt.toEpochMilli(),
        tabSortOrder.value,
        scrollPosition.value
    )
}
