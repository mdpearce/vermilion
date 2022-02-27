package com.neaniesoft.vermilion.communities.data.database

import com.neaniesoft.vermilion.dbentities.communities.CommunityDao
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface CommunityRepository {
}

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val dao: CommunityDao
) : CommunityRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class CommunityRepositoryModule {
    @Binds
    abstract fun bindCommunityRepositoryImpl(impl: CommunityRepositoryImpl): CommunityRepository
}
