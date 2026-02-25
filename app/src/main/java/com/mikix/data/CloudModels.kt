package com.mikix.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val email: String, val password: String)

@Serializable
data class RefreshTokenRequest(@SerialName("refresh_token") val refreshToken: String)

@Serializable
data class AuthResponse(
    @SerialName("user_id") val userId: String,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in_sec") val expiresInSec: Long = 3600
)

@Serializable
data class CloudSessionDto(
    val id: Long,
    @SerialName("started_at") val startedAt: Long,
    @SerialName("ended_at") val endedAt: Long?,
    @SerialName("template_id") val templateId: Long?,
    val notes: String,
    @SerialName("perceived_effort") val perceivedEffort: Float,
    @SerialName("total_volume") val totalVolume: Double
)

@Serializable
data class GroupDto(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("member_count") val memberCount: Int
)

@Serializable
data class FeedPostDto(
    val id: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("author_name") val authorName: String,
    val message: String,
    @SerialName("created_at") val createdAt: Long
)

@Serializable
data class ChallengeDto(
    val id: String,
    val title: String,
    val progress: Float,
    val unit: String,
    val target: Int
)

@Serializable
data class PaginationMeta(
    val page: Int,
    val limit: Int,
    @SerialName("next_cursor") val nextCursor: String? = null,
    @SerialName("has_more") val hasMore: Boolean = false
)

@Serializable
data class GroupPageResponse(
    val items: List<GroupDto>,
    val meta: PaginationMeta
)

@Serializable
data class FeedPageResponse(
    val items: List<FeedPostDto>,
    val meta: PaginationMeta
)

@Serializable
data class ChallengePageResponse(
    val items: List<ChallengeDto>,
    val meta: PaginationMeta
)

@Serializable
data class LiveFeedEvent(
    val type: String,
    val payload: String,
    @SerialName("created_at") val createdAt: Long
)
