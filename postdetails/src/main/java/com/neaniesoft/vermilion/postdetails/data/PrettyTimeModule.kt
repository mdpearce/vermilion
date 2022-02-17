package com.neaniesoft.vermilion.postdetails.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ocpsoft.prettytime.PrettyTime

@Module
@InstallIn(SingletonComponent::class)
class PrettyTimeModule {
    @Provides
    fun providePrettyTime(): PrettyTime = PrettyTime()
}
