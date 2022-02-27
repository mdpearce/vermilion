package com.neaniesoft.vermilion.communities.data.database

import androidx.room.withTransaction
import com.neaniesoft.vermilion.api.entities.SubredditThing
import com.neaniesoft.vermilion.communities.data.http.CommunitiesApiService
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.communities.CommunityDao
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

interface CommunityRepository {
    fun subscribedCommunities(): Flow<List<Community>>
    suspend fun updateSubscribedCommunities()
}

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val database: VermilionDatabase,
    private val dao: CommunityDao,
    private val api: CommunitiesApiService,
    private val clock: Clock
) : CommunityRepository {
    override fun subscribedCommunities(): Flow<List<Community>> {
        return dao.observeAllSubscribedCommunities()
            .map { it.map { record -> record.toCommunity() } }
    }

    override suspend fun updateSubscribedCommunities() {
        val communities =
            getSubscribedCommunitiesFromApi(
                null,
                0
            ) // Will loop through the pages until we've got them all

        database.withTransaction {
            dao.removeAllCommunities()
            dao.insertAll(communities.map { it.toCommunityRecord(clock) })
        }
    }

    private suspend fun getSubscribedCommunitiesFromApi(
        afterKey: String? = null,
        currentCount: Int
    ): List<Community> {
        val result = api.subscribedCommunities(
            before = null,
            after = afterKey,
            count = currentCount,
            limit = 50
        ).data
        val nextAfterKey = result.after
        val communities =
            result.children.mapNotNull { (it as? SubredditThing)?.data?.toCommunity() }
        return if (!nextAfterKey.isNullOrEmpty()) {
            communities + getSubscribedCommunitiesFromApi(
                nextAfterKey,
                currentCount + communities.size
            )
        } else {
            communities
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CommunityRepositoryModule {
    @Binds
    abstract fun bindCommunityRepositoryImpl(impl: CommunityRepositoryImpl): CommunityRepository
}

