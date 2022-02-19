package com.neaniesoft.vermilion.postdetails.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.commonmark.parser.Parser

@Module
@InstallIn(SingletonComponent::class)
class CommonMarkModule {
    @Provides
    fun provideParser(): Parser = Parser.builder().build()
}
