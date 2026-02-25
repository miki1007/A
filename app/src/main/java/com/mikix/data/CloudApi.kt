package com.mikix.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("v1/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("v1/auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): AuthResponse
}

interface SyncApi {
    @POST("v1/sync/sessions")
    suspend fun pushSessions(
        @Header("Authorization") bearer: String,
        @Body sessions: List<CloudSessionDto>
    )

    @GET("v1/sync/sessions")
    suspend fun pullSessions(
        @Header("Authorization") bearer: String,
        @Query("since") sinceEpochMillis: Long? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): List<CloudSessionDto>
}

interface CommunityApi {
    @GET("v1/community/groups")
    suspend fun groups(
        @Header("Authorization") bearer: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): GroupPageResponse

    @GET("v1/community/feed")
    suspend fun feed(
        @Header("Authorization") bearer: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 30
    ): FeedPageResponse

    @GET("v1/community/challenges")
    suspend fun challenges(
        @Header("Authorization") bearer: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ChallengePageResponse
}
