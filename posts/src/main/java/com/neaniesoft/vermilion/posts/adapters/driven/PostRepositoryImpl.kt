package com.neaniesoft.vermilion.posts.adapters.driven

import com.neaniesoft.vermilion.posts.domain.entities.*
import com.neaniesoft.vermilion.posts.domain.ports.*
import com.vermilion.api.Link
import com.vermilion.api.LinkThing
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.net.URL
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postsService: PostsService
) : PostRepository {
    override suspend fun postsForCommunity(community: Community): ResultSet<Post> {
        val response = when (community) {
            is FrontPage -> postsService.frontPageBest()
            is NamedCommunity -> TODO()
        }

        val posts = response.data.children.map { child ->
            if (child is LinkThing) {
                child.data.toPost()
            } else {
                throw IllegalArgumentException("Unknown thing type in posts response")
            }
        }

        return ResultSet(posts)
    }
}

internal fun Link.toPost(): Post {
    return Post(
        PostTitle(title),
        TextPostSummary(PreviewText(selfText)),
        CommunityName(subreddit),
        null,
        AuthorName(author),
        Instant.ofEpochMilli((created * 1000.0).roundToLong()),
        CommentCount(numComments),
        PostScore(score),
        URL(url)
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PostRepositoryModule {
    @Binds
    abstract fun postRepository(postRepository: PostRepositoryImpl): PostRepository
}