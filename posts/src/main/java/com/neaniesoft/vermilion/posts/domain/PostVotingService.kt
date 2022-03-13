package com.neaniesoft.vermilion.posts.domain

import androidx.room.withTransaction
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.posts.data.http.PostsService
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostFlags
import com.neaniesoft.vermilion.posts.domain.entities.fullName
import com.neaniesoft.vermilion.posts.domain.entities.isDownVoted
import com.neaniesoft.vermilion.posts.domain.entities.isUpVoted
import com.neaniesoft.vermilion.utils.CoroutinesModule
import com.neaniesoft.vermilion.utils.logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class PostVotingService @Inject constructor(
    private val database: VermilionDatabase,
    private val postDao: PostDao,
    private val postsService: PostsService,
    @Named(CoroutinesModule.IO_DISPATCHER) private val coroutineDispatcher: CoroutineDispatcher
) {
    private val logger by logger()

    suspend fun toggleUpVote(post: Post) {
        if (post.isUpVoted()) {
            vote(0, post)
        } else {
            vote(1, post)
        }
    }

    suspend fun toggleDownVote(post: Post) {
        if (post.isDownVoted()) {
            vote(0, post)
        } else {
            vote(-1, post)
        }
    }

    private suspend fun vote(
        direction: Int,
        post: Post,
    ) {
        val flags = when (direction) {
            -1 -> post.flags + PostFlags.DOWN_VOTED - PostFlags.UP_VOTED
            0 -> post.flags - PostFlags.UP_VOTED - PostFlags.DOWN_VOTED
            1 -> post.flags + PostFlags.UP_VOTED - PostFlags.DOWN_VOTED
            else -> throw IllegalArgumentException("Unexpected vote direction $direction")
        }
        withContext(coroutineDispatcher) {
            database.withTransaction {
                postDao.updateFlags(post.id.value, flags.joinToString(",") { it.name })
            }
            try {
                postsService.vote(direction, post.id.fullName())
            } catch (httpException: HttpException) {
                logger.errorIfEnabled(httpException) { "HTTP Exception while processing vote" }
            }
        }
    }
}
