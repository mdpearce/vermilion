package com.neaniesoft.vermilion.posts.domain

import androidx.room.withTransaction
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostFlags
import com.neaniesoft.vermilion.utils.CoroutinesModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class PostVotingService @Inject constructor(
    private val database: VermilionDatabase,
    private val postDao: PostDao,
    @Named(CoroutinesModule.IO_DISPATCHER) private val coroutineDispatcher: CoroutineDispatcher
) {
    suspend fun upVote(post: Post) {
        val flags = post.flags + PostFlags.UP_VOTED

        withContext(coroutineDispatcher) {
            database.withTransaction {
                postDao.updateFlags(post.id.value, flags.joinToString(",") { it.name })
            }
        }
    }
}
