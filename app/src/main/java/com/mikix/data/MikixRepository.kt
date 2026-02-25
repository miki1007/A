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
    private val communityApi: CommunityApi
) {
    fun exercises(search: String = ""): Flow<List<Exercise>> = if (search.isBlank()) dao.exercises() else dao.searchExercises(search)
    fun templates() = dao.templates()
    fun sessions() = dao.sessions()
    fun weeklyVolumes() = dao.weeklyVolumes()
    fun groups() = dao.groups()
    fun feed() = dao.feed()
    fun challenges() = dao.challenges()
    fun account() = dao.account()

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
        val response = authApi.login(AuthRequest(email, password))
        dao.upsertAccount(Account(id = response.userId, email = email))
        return response
    }

    suspend fun register(email: String, password: String): AuthResponse {
        val response = authApi.register(AuthRequest(email, password))
        dao.upsertAccount(Account(id = response.userId, email = email))
        return response
    }

    suspend fun syncSessions(accessToken: String) {
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
        syncApi.pushSessions("Bearer $accessToken", local)
        val remote = syncApi.pullSessions("Bearer $accessToken")
        remote.forEach {
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
    }

    suspend fun refreshCommunity(accessToken: String) {
        val bearer = "Bearer $accessToken"
        dao.upsertGroups(communityApi.groups(bearer).map { GroupEntity(it.id, it.name, it.description, it.memberCount) })
        dao.upsertFeed(communityApi.feed(bearer).map { FeedPostEntity(it.id, it.groupId, it.authorName, it.message, it.createdAt) })
        dao.upsertChallenges(communityApi.challenges(bearer).map { ChallengeEntity(it.id, it.title, it.progress, it.unit, it.target) })
    }

    private suspend fun sessionsSnapshot(): List<WorkoutSession> = sessions().first()
}
