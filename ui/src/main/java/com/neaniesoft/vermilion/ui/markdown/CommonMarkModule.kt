package com.neaniesoft.vermilion.ui.markdown

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
