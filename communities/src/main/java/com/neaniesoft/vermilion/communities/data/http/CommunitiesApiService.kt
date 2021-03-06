package com.neaniesoft.vermilion.communities.data.http

import com.neaniesoft.vermilion.api.RedditApiClientModule
import com.neaniesoft.vermilion.api.entities.ListingResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Named

interface CommunitiesApiService {
    @GET("subreddits/mine/subscriber")
    suspend fun subscribedCommunities(
        @Query("limit") limit: Int,
        @Query("before") before: String?,
        @Query("after") after: String?,
        @Query("count") count: Int?
    ): ListingResponse
}

@Module
@InstallIn(SingletonComponent::class)
class CommunitiesApiServiceModule {
    @Provides
    fun provideApiService(@Named(RedditApiClientModule.AUTHENTICATED) retrofit: Retrofit) =
        retrofit.create<CommunitiesApiService>()
}
