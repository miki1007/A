package com.mikix.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Account(
    @PrimaryKey val id: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val memberCount: Int
)

@Entity
data class FeedPostEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val authorName: String,
    val message: String,
    val createdAt: Long
)

@Entity
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val progress: Float,
    val unit: String,
    val target: Int
)
