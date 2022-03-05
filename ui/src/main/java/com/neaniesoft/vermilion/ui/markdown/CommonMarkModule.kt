package com.neaniesoft.vermilion.ui.markdown

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.commonmark.Extension
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.parser.Parser

@Module
@InstallIn(SingletonComponent::class)
class CommonMarkModule {
    @Provides
    fun provideParser(extensions: Set<@JvmSuppressWildcards Extension>): Parser =
        Parser.builder().extensions(extensions).build()

    @Provides
    @IntoSet
    fun provideAutolinkExtension(): Extension = AutolinkExtension.create()
}
