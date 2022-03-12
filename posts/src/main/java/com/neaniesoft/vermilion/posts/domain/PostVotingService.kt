package com.neaniesoft.vermilion.posts.domain

import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.posts.domain.entities.Post
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostVotingService @Inject constructor(
    private val database: VermilionDatabase,
    private val postDao: PostDao
) {
    fun upVote(post: Post) {

    }
}
