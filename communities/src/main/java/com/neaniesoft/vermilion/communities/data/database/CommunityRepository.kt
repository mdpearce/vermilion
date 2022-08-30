package com.neaniesoft.vermilion.communities.data.database

import androidx.room.withTransaction
import com.neaniesoft.vermilion.api.entities.SubredditThing
import com.neaniesoft.vermilion.communities.data.http.CommunitiesApiService
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.coreentities.CommunityId
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.db.CommunityQueries
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.communities.CommunityDao
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

interface CommunityRepository {
    fun subscribedCommunities(): Flow<List<Community>>
    suspend fun updateSubscribedCommunities()
}

@Singleton
class CommunitySqlDelightRepository @Inject constructor(
    private val queries: CommunityQueries,
    private val api: CommunitiesApiService,
    private val clock: Clock
) : CommunityRepository {
    companion object {
        private val CACHE_TIMEOUT = Duration.ofDays(1).toMillis()
    }

    override fun subscribedCommunities(): Flow<List<Community>> {
        return queries.selectAllSubscribedCommunities(mapper = { id, inserted_at, community_id, name, is_subscribed ->
            NamedCommunity(
                name = CommunityName(name),
                id = CommunityId(community_id),
                isSubscribed = is_subscribed != 0L
            )
        }).asFlow()
            .mapToList()
    }

    override suspend fun updateSubscribedCommunities() {
        val lastInsertedTime = queries.selectLastInsertedTime().executeAsOneOrNull() ?: 0L
        val shouldUpdate = clock.millis() > lastInsertedTime + CACHE_TIMEOUT

        if (shouldUpdate) {
            val communities =
                getSubscribedCommunitiesFromApi(
                    null,
                    0
                ) // Will loop through the pages until we've got them all

            queries.transaction {
                queries.deleteAllCommunities()
                communities.map {
                    it.toCommunitySqlRecord(clock)
                }.forEach {
                    queries.insert(it)
                }
            }
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

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val database: VermilionDatabase,
    private val dao: CommunityDao,
    private val api: CommunitiesApiService,
    private val clock: Clock
) : CommunityRepository {
    companion object {
        private val CACHE_TIMEOUT = Duration.ofDays(1).toMillis()
    }

    override fun subscribedCommunities(): Flow<List<Community>> {
        return dao.observeAllSubscribedCommunities()
            .map { it.map { record -> record.toCommunity() } }
    }

    override suspend fun updateSubscribedCommunities() {
        val lastInsertedTime = database.withTransaction { dao.getLastInsertedTime() } ?: 0
        val shouldUpdate = clock.millis() > lastInsertedTime + CACHE_TIMEOUT

        if (shouldUpdate) {
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
//    @Binds
//    abstract fun bindCommunityRepositoryImpl(impl: CommunityRepositoryImpl): CommunityRepository

    @Binds
    abstract fun bindCommunitySqlDelightRepository(repository: CommunitySqlDelightRepository): CommunityRepository
}
