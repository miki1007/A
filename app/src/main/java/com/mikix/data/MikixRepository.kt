package com.mikix.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MikixRepository @Inject constructor(private val dao: MikixDao) {
    fun exercises(search: String = ""): Flow<List<Exercise>> = if (search.isBlank()) dao.exercises() else dao.searchExercises(search)
    fun templates() = dao.templates()
    fun sessions() = dao.sessions()
    fun weeklyVolumes() = dao.weeklyVolumes()

    suspend fun seedExercisesIfEmpty() {
        dao.insertExercises(SeedExercises.list)
    }

    suspend fun createTemplate(name: String, description: String, exerciseIds: List<Long>) {
        val id = dao.insertTemplate(WorkoutTemplate(name = name, description = description))
        dao.insertTemplateExercises(exerciseIds.mapIndexed { i, e -> TemplateExercise(templateId = id, exerciseId = e, sortOrder = i, defaultSets = 3, defaultRepsRange = "8-12", defaultRestSec = 90) })
    }

    suspend fun startSession(templateId: Long? = null) = dao.insertSession(WorkoutSession(startedAt = System.currentTimeMillis(), templateId = templateId))

    suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long, sortOrder: Int) = dao.insertSessionExercise(SessionExercise(sessionId = sessionId, exerciseId = exerciseId, sortOrder = sortOrder))

    suspend fun addSet(sessionExerciseId: Long, index: Int, weight: Double, reps: Int, warmup: Boolean, rpe: Float? = null) {
        dao.insertSet(SetEntry(sessionExerciseId = sessionExerciseId, setIndex = index, weight = weight, reps = reps, isWarmup = warmup, completedAt = System.currentTimeMillis(), rpe = rpe))
    }

    suspend fun finishSession(sessionId: Long, totalVolume: Double, notes: String, rpe: Float) =
        dao.finishSession(sessionId, System.currentTimeMillis(), totalVolume, notes, rpe)

    suspend fun lastSet(exerciseId: Long) = dao.lastPerformedSet(exerciseId)
    suspend fun bestSetVolume(exerciseId: Long) = dao.bestSetVolume(exerciseId) ?: 0.0
    suspend fun muscleBalance(weekStart: Long) = dao.muscleVolume(weekStart)
}
