package com.mikix.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MikixDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)

    @Query("SELECT * FROM Exercise ORDER BY name")
    fun exercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM Exercise WHERE name LIKE '%' || :search || '%' ORDER BY name")
    fun searchExercises(search: String): Flow<List<Exercise>>

    @Insert
    suspend fun insertTemplate(template: WorkoutTemplate): Long

    @Insert
    suspend fun insertTemplateExercises(rows: List<TemplateExercise>)

    @Query("SELECT * FROM WorkoutTemplate ORDER BY createdAt DESC")
    fun templates(): Flow<List<WorkoutTemplate>>

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Query("UPDATE WorkoutSession SET endedAt=:endedAt,totalVolume=:totalVolume,notes=:notes,perceivedEffort=:rpe WHERE id=:sessionId")
    suspend fun finishSession(sessionId: Long, endedAt: Long, totalVolume: Double, notes: String, rpe: Float)

    @Insert
    suspend fun insertSessionExercise(row: SessionExercise): Long

    @Insert
    suspend fun insertSet(set: SetEntry)

    @Query("SELECT s.* FROM SetEntry s JOIN SessionExercise se ON se.id=s.sessionExerciseId WHERE se.exerciseId=:exerciseId ORDER BY s.completedAt DESC LIMIT 1")
    suspend fun lastPerformedSet(exerciseId: Long): SetEntry?

    @Query("SELECT MAX(weight * reps) FROM SetEntry s JOIN SessionExercise se ON s.sessionExerciseId = se.id WHERE se.exerciseId=:exerciseId")
    suspend fun bestSetVolume(exerciseId: Long): Double?

    @Query("SELECT strftime('%W', datetime(s.completedAt/1000, 'unixepoch')) AS weekStart, SUM(s.weight*s.reps) AS volume FROM SetEntry s GROUP BY weekStart ORDER BY weekStart")
    fun weeklyVolumes(): Flow<List<WeeklyVolume>>

    @Query("SELECT e.muscleGroup AS name, SUM(s.weight*s.reps) AS volume FROM SetEntry s JOIN SessionExercise se ON s.sessionExerciseId = se.id JOIN Exercise e ON e.id=se.exerciseId WHERE s.completedAt > :weekStart GROUP BY e.muscleGroup")
    suspend fun muscleVolume(weekStart: Long): List<ExerciseStat>

    @Query("SELECT * FROM WorkoutSession ORDER BY startedAt DESC")
    fun sessions(): Flow<List<WorkoutSession>>
}
