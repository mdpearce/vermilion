package com.neaniesoft.vermilion.posts.adapters.driven

import com.neaniesoft.vermilion.posts.adapters.driving.ui.DUMMY_TEXT_POST
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.ports.Community
import com.neaniesoft.vermilion.posts.domain.ports.PostRepository
import com.neaniesoft.vermilion.posts.domain.ports.ResultSet
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor() : PostRepository {
    override suspend fun postsForCommunity(community: Community): ResultSet<Post> {
        return ResultSet(listOf(DUMMY_TEXT_POST, DUMMY_TEXT_POST, DUMMY_TEXT_POST, DUMMY_TEXT_POST, DUMMY_TEXT_POST))
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PostRepositoryModule {
    @Binds
    abstract fun postRepository(postRepository: PostRepositoryImpl): PostRepository
}