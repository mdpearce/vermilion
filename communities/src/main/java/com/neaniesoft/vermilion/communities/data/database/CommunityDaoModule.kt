package com.neaniesoft.vermilion.communities.data.database

import com.neaniesoft.vermilion.db.CommunityQueries
import com.neaniesoft.vermilion.db.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class CommunityDaoModule {

    @Provides
    fun provideCommunityQueries(db: Database): CommunityQueries = db.communityQueries
}
