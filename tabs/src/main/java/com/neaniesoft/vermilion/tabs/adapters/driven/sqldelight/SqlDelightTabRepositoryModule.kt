package com.neaniesoft.vermilion.tabs.adapters.driven.sqldelight

import com.neaniesoft.vermilion.tabs.domain.ports.TabRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SqlDelightTabRepositoryModule {
    @Binds
    abstract fun bindSqlDelightTabRepository(impl: SqlDelightTabRepository): TabRepository
}