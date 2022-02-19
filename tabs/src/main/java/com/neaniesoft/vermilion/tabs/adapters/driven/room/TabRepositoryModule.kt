package com.neaniesoft.vermilion.tabs.adapters.driven.room

import com.neaniesoft.vermilion.tabs.domain.ports.TabRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TabRepositoryModule {
    @Binds
    abstract fun bindTabRepository(impl: RoomBackedTabRepository): TabRepository
}
