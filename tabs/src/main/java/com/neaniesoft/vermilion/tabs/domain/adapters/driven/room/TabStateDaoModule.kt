package com.neaniesoft.vermilion.tabs.domain.adapters.driven.room

import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.tabs.TabStateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class TabStateDaoModule {
    @Provides
    fun provideTabStateDao(database: VermilionDatabase): TabStateDao = database.tabStateDao()
}
