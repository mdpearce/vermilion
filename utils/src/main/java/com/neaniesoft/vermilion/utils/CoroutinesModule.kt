package com.neaniesoft.vermilion.utils

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class CoroutinesModule {
    companion object {
        const val IO_DISPATCHER = "io_dispatcher"
    }

    @Provides
    @Named(IO_DISPATCHER)
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
