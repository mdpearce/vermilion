package com.neaniesoft.vermilion.db

import android.content.Context
import androidx.room.Room
import com.neaniesoft.vermilion.accounts.UserAccountDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    companion object {
        private const val DB_NAME = "vermilion"
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VermilionDatabase =
        Room.databaseBuilder(context, VermilionDatabase::class.java, DB_NAME).build()

    @Provides
    fun provideUserAccountDao(db: VermilionDatabase): UserAccountDao =
        db.userAccountDao()
}