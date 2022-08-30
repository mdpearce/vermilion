package com.neaniesoft.vermilion.db

import android.content.Context
import androidx.room.Room
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
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
        private const val SQL_DB_NAME = "vermilion_store"
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VermilionDatabase =
        Room.databaseBuilder(context, VermilionDatabase::class.java, DB_NAME).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideSqlDriver(@ApplicationContext context: Context): SqlDriver =
        AndroidSqliteDriver(Database.Schema, context, SQL_DB_NAME)

    @Provides
    @Singleton
    fun provideSqlDelightDatabase(driver: SqlDriver): Database =
        Database(driver)
}
