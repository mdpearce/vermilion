package com.neaniesoft.vermilion.posts.domain

import com.neaniesoft.vermilion.posts.data.PostHistoryRepository
import com.neaniesoft.vermilion.posts.domain.entities.PostHistoryEntry
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.utils.CoroutinesModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Clock
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class PostHistoryService @Inject constructor(
    private val postHistoryRepository: PostHistoryRepository,
    @Named(CoroutinesModule.IO_DISPATCHER) private val dispatcher: CoroutineDispatcher,
    private val clock: Clock
) {
    private val scope = CoroutineScope(dispatcher)

    fun markPostAsRead(postId: PostId) {
        scope.launch {
            postHistoryRepository.addHistoryEntry(
                PostHistoryEntry(
                    postId = postId,
                    viewedAt = clock.instant()
                )
            )
        }
    }

    // TODO Should probably filter by time period. We probably don't need to check entries from years ago
    suspend fun getAllHistoryEntries(): List<PostHistoryEntry> {
        return withContext(dispatcher) {
            postHistoryRepository.allEntries()
        }
    }
}
