package com.neaniesoft.vermilion.tabs.domain

import com.neaniesoft.vermilion.coreentities.ScrollPosition
import com.neaniesoft.vermilion.postdetails.data.CommentRepository
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.tabs.domain.entities.NewTabState
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.tabs.domain.ports.TabRepository
import com.neaniesoft.vermilion.uistate.ActiveTabClosedEvent
import com.neaniesoft.vermilion.uistate.TabType
import com.neaniesoft.vermilion.uistate.UiStateProvider
import com.neaniesoft.vermilion.utils.logger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TabSupervisor @Inject constructor(
    private val repository: TabRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val clock: Clock,
) : UiStateProvider {
    val currentTabs = repository.currentTabs

    private val _removedTabs: MutableSharedFlow<TabState> = MutableSharedFlow()
    val removedTabs = _removedTabs.asSharedFlow()

    private val logger by logger()

    override suspend fun setActiveTab(type: TabType, parentId: String) {
        logger.debugIfEnabled { "Setting active tab defined by parentId: $parentId, type: ${type.name}" }
        addNewTabIfNotExists(ParentId(parentId), type)

        repository.setActiveTab(ParentId(parentId), type)
    }

    private suspend fun addNewTabIfNotExists(parentId: ParentId, type: TabType): TabState {
        val displayName = repository.displayName(parentId, type)

        val tab = NewTabState(
            parentId,
            type,
            displayName,
            Instant.ofEpochMilli(clock.millis()),
            ScrollPosition(0, 0),
            isActive = false
        )
        return repository.addNewTabIfNotExists(tab)
    }

    suspend fun removeTab(tab: TabState) {
        repository.removeTab(tab)
        when (tab.type) {
            TabType.POSTS -> postRepository.deleteByQuery(tab.parentId.value)
            TabType.POST_DETAILS -> commentRepository.deleteByPost(PostId(tab.parentId.value))
            else -> {} // Not implemented
        }
        _removedTabs.emit(tab)
    }

    private suspend fun updateScrollState(
        parentId: ParentId,
        type: TabType,
        scrollPosition: ScrollPosition
    ) {
        logger.debugIfEnabled { "Persisting scroll state for $parentId to $scrollPosition" }
        repository.updateScrollStateForTab(
            parentId,
            type,
            scrollPosition
        )
    }

    private suspend fun scrollPositionForTab(parentId: String, tabType: TabType): ScrollPosition? {
        return repository.findTab(
            ParentId(parentId),
            tabType
        )?.scrollPosition.also { logger.warnIfEnabled { "Tab not found, defaulting to null scroll position" } }
    }

    override suspend fun scrollPositionTab(tabType: TabType, parentId: String): ScrollPosition? {
        return scrollPositionForTab(parentId, tabType)
    }

    override suspend fun updateScrollPositionForTab(
        tabType: TabType,
        parentId: String,
        scrollPosition: ScrollPosition
    ) {
        repository.updateScrollStateForTab(ParentId(parentId), tabType, scrollPosition)
    }

    override val activeTabClosedEvents: Flow<ActiveTabClosedEvent> =
        repository.activeTab.filter { it == null }.map { ActiveTabClosedEvent }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TabSupervisorModule {
    @Binds
    abstract fun tabSupervisorUiProvider(tabSupervisor: TabSupervisor): UiStateProvider
}
