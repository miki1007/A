package com.mikix.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MikixRepository @Inject constructor(
    private val dao: MikixDao,
    private val authApi: AuthApi,
    private val syncApi: SyncApi,
    private val communityApi: CommunityApi,
    private val authSessionManager: AuthSessionManager,
    private val liveFeedSocket: CommunityLiveFeedSocket
) {
    fun exercises(search: String = ""): Flow<List<Exercise>> = if (search.isBlank()) dao.exercises() else dao.searchExercises(search)
    fun templates() = dao.templates()
    fun sessions() = dao.sessions()
    fun weeklyVolumes() = dao.weeklyVolumes()
    fun groups() = dao.groups()
    fun feed() = dao.feed()
    fun challenges() = dao.challenges()
    fun account() = dao.account()
    fun liveFeedEvents() = liveFeedSocket.events

    suspend fun seedExercisesIfEmpty() {
        dao.insertExercises(SeedExercises.list)
    }

    suspend fun createTemplate(name: String, description: String, exerciseIds: List<Long>) {
        val id = dao.insertTemplate(WorkoutTemplate(name = name, description = description))
        dao.insertTemplateExercises(exerciseIds.mapIndexed { i, e ->
            TemplateExercise(templateId = id, exerciseId = e, sortOrder = i, defaultSets = 3, defaultRepsRange = "8-12", defaultRestSec = 90)
        })
    }

    suspend fun startSession(templateId: Long? = null) = dao.insertSession(WorkoutSession(startedAt = System.currentTimeMillis(), templateId = templateId))

    suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long, sortOrder: Int) =
        dao.insertSessionExercise(SessionExercise(sessionId = sessionId, exerciseId = exerciseId, sortOrder = sortOrder))

    suspend fun addSet(sessionExerciseId: Long, index: Int, weight: Double, reps: Int, warmup: Boolean, rpe: Float? = null) {
        dao.insertSet(
            SetEntry(
                sessionExerciseId = sessionExerciseId,
                setIndex = index,
                weight = weight,
                reps = reps,
                isWarmup = warmup,
                completedAt = System.currentTimeMillis(),
                rpe = rpe
            )
        )
    }

    suspend fun finishSession(sessionId: Long, totalVolume: Double, notes: String, rpe: Float) =
        dao.finishSession(sessionId, System.currentTimeMillis(), totalVolume, notes, rpe)

    suspend fun lastSet(exerciseId: Long) = dao.lastPerformedSet(exerciseId)
    suspend fun bestSetVolume(exerciseId: Long) = dao.bestSetVolume(exerciseId) ?: 0.0
    suspend fun muscleBalance(weekStart: Long) = dao.muscleVolume(weekStart)

    suspend fun login(email: String, password: String): AuthResponse {
        val response = retryIo { authApi.login(AuthRequest(email, password)) }
        dao.upsertAccount(Account(id = response.userId, email = email))
        authSessionManager.saveTokens(response)
        liveFeedSocket.connect(response.accessToken)
        return response
    }

    suspend fun register(email: String, password: String): AuthResponse {
        val response = retryIo { authApi.register(AuthRequest(email, password)) }
        dao.upsertAccount(Account(id = response.userId, email = email))
        authSessionManager.saveTokens(response)
        liveFeedSocket.connect(response.accessToken)
        return response
    }

    suspend fun syncSessions(accessToken: String? = null) {
        val token = accessToken ?: authSessionManager.ensureValidAccessToken() ?: return
        val local = sessionsSnapshot().map {
            CloudSessionDto(
                id = it.id,
                startedAt = it.startedAt,
                endedAt = it.endedAt,
                templateId = it.templateId,
                notes = it.notes,
                perceivedEffort = it.perceivedEffort,
                totalVolume = it.totalVolume
            )
        }
        retryIo { syncApi.pushSessions("Bearer $token", local) }

        var page = 1
        var batch: List<CloudSessionDto>
        do {
            batch = retryIo { syncApi.pullSessions("Bearer $token", sinceEpochMillis = null, page = page, limit = 50) }
            batch.forEach {
                dao.insertSession(
                    WorkoutSession(
                        id = it.id,
                        startedAt = it.startedAt,
                        endedAt = it.endedAt,
                        templateId = it.templateId,
                        notes = it.notes,
                        perceivedEffort = it.perceivedEffort,
                        totalVolume = it.totalVolume
                    )
                )
            }
            page++
        } while (batch.isNotEmpty())
    }

    suspend fun refreshCommunity(accessToken: String? = null) {
        val token = accessToken ?: authSessionManager.ensureValidAccessToken() ?: return
        val bearer = "Bearer $token"

        var page = 1
        var hasMore = true
        while (hasMore) {
            val groupsResponse = retryIo { communityApi.groups(bearer, page = page, limit = 20) }
            dao.upsertGroups(groupsResponse.items.map { GroupEntity(it.id, it.name, it.description, it.memberCount) })
            hasMore = groupsResponse.meta.hasMore
            page++
        }

        var cursor: String? = null
        do {
            val feedResponse = retryIo { communityApi.feed(bearer, cursor = cursor, limit = 30) }
            dao.upsertFeed(feedResponse.items.map { FeedPostEntity(it.id, it.groupId, it.authorName, it.message, it.createdAt) })
            cursor = feedResponse.meta.nextCursor
        } while (cursor != null)

        page = 1
        hasMore = true
        while (hasMore) {
            val challengeResponse = retryIo { communityApi.challenges(bearer, page = page, limit = 20) }
            dao.upsertChallenges(challengeResponse.items.map { ChallengeEntity(it.id, it.title, it.progress, it.unit, it.target) })
            hasMore = challengeResponse.meta.hasMore
            page++
        }
    }

    fun disconnectLiveFeed() = liveFeedSocket.disconnect()

    private suspend fun sessionsSnapshot(): List<WorkoutSession> = sessions().first()
}
