package com.mikix.domain

import kotlin.math.roundToInt

data class RoutineInput(
    val goal: Goal,
    val daysPerWeek: Int,
    val equipment: Set<Equipment>,
    val focusMuscles: Set<String>,
    val experience: Experience
)

enum class Goal { STRENGTH, HYPERTROPHY, FAT_LOSS, GENERAL_FITNESS }
enum class Equipment { BARBELL, DUMBBELL, MACHINE, BODYWEIGHT, CABLE }
enum class Experience { BEGINNER, INTERMEDIATE, ADVANCED }

data class PlannedWorkout(val day: Int, val title: String, val exercises: List<String>)
data class GeneratedRoutine(val blockName: String, val workouts: List<PlannedWorkout>, val progressionNote: String)

data class RecoveryInputs(
    val sleepHours: Double,
    val restingHeartRateDelta: Int,
    val soreness: Int,
    val stress: Int,
    val readinessSelfScore: Int
)

data class RecoveryScore(val score: Int, val zone: String, val guidance: String)

object AiCoach {
    fun generateRoutine(input: RoutineInput): GeneratedRoutine {
        val split = when {
            input.daysPerWeek <= 2 -> listOf("Full Body", "Full Body")
            input.daysPerWeek == 3 -> listOf("Push", "Pull", "Legs")
            input.daysPerWeek == 4 -> listOf("Upper", "Lower", "Upper", "Lower")
            else -> listOf("Push", "Pull", "Legs", "Upper", "Lower")
        }

        val basePool = mutableListOf<String>().apply {
            if (Equipment.BARBELL in input.equipment) addAll(listOf("Back Squat", "Bench Press", "Deadlift", "Overhead Press"))
            if (Equipment.DUMBBELL in input.equipment) addAll(listOf("DB Incline Press", "DB Row", "DB Split Squat"))
            if (Equipment.CABLE in input.equipment) addAll(listOf("Cable Fly", "Lat Pulldown", "Face Pull"))
            if (Equipment.MACHINE in input.equipment) addAll(listOf("Leg Press", "Machine Row", "Chest Press"))
            if (Equipment.BODYWEIGHT in input.equipment) addAll(listOf("Push-up", "Pull-up", "Walking Lunge", "Plank"))
        }.distinct().ifEmpty { listOf("Push-up", "Goblet Squat", "Row", "Plank") }

        val workouts = split.mapIndexed { index, title ->
            PlannedWorkout(
                day = index + 1,
                title = title,
                exercises = pickExercisesForDay(title, basePool, input.focusMuscles)
            )
        }

        val progressionNote = when (input.goal) {
            Goal.STRENGTH -> "Top set @RPE 8 then 2 back-off sets; add 2.5kg when all targets are hit."
            Goal.HYPERTROPHY -> "3-4 sets per lift, 6-15 reps, add 1 rep weekly before adding load."
            Goal.FAT_LOSS -> "Keep rests shorter, add finishers, maintain loads to preserve muscle."
            Goal.GENERAL_FITNESS -> "Use mixed rep ranges and keep effort around RPE 7-8."
        }

        return GeneratedRoutine(
            blockName = "MikiX ${input.goal.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }} Block",
            workouts = workouts,
            progressionNote = progressionNote
        )
    }

    fun recoveryScore(inputs: RecoveryInputs): RecoveryScore {
        val sleepScore = (inputs.sleepHours.coerceIn(4.0, 9.0) - 4.0) / 5.0 * 30
        val rhrScore = (20 - inputs.restingHeartRateDelta.coerceIn(-5, 20)) / 20.0 * 20
        val sorenessScore = (10 - inputs.soreness.coerceIn(0, 10)) / 10.0 * 20
        val stressScore = (10 - inputs.stress.coerceIn(0, 10)) / 10.0 * 20
        val readinessScore = inputs.readinessSelfScore.coerceIn(0, 10) / 10.0 * 10

        val score = (sleepScore + rhrScore + sorenessScore + stressScore + readinessScore)
            .coerceIn(0.0, 100.0)
            .roundToInt()

        return when {
            score >= 75 -> RecoveryScore(score, "Ready", "Green light: run your planned heavy/volume session.")
            score >= 50 -> RecoveryScore(score, "Moderate", "Train, but trim one top set or reduce load by 2-5%.")
            else -> RecoveryScore(score, "Recover", "Deload, mobility, or technique day is recommended.")
        }
    }

    private fun pickExercisesForDay(title: String, pool: List<String>, focus: Set<String>): List<String> {
        val focusList = focus.map { it.lowercase() }
        val weighted = pool.sortedByDescending {
            val n = it.lowercase()
            focusList.count { m -> n.contains(m) }
        }

        val defaultBySplit = when (title) {
            "Push" -> listOf("Bench Press", "Overhead Press", "Cable Fly", "Triceps Pushdown")
            "Pull" -> listOf("Deadlift", "Barbell Row", "Lat Pulldown", "Hammer Curl")
            "Legs" -> listOf("Back Squat", "Romanian Deadlift", "Leg Press", "Calf Raise")
            "Upper" -> listOf("Bench Press", "Row", "Overhead Press", "Lat Pulldown")
            "Lower" -> listOf("Back Squat", "Romanian Deadlift", "Lunge", "Calf Raise")
            else -> listOf("Back Squat", "Bench Press", "Row", "Plank")
        }

        return (defaultBySplit + weighted).distinct().take(6)
    }
}
