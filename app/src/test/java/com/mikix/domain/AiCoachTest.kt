package com.mikix.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AiCoachTest {
    @Test
    fun generateRoutine_respectsDaysPerWeek() {
        val routine = AiCoach.generateRoutine(
            RoutineInput(
                goal = Goal.STRENGTH,
                daysPerWeek = 4,
                equipment = setOf(Equipment.BARBELL, Equipment.DUMBBELL),
                focusMuscles = setOf("chest", "back"),
                experience = Experience.INTERMEDIATE
            )
        )

        assertThat(routine.workouts).hasSize(4)
        assertThat(routine.progressionNote).contains("2.5kg")
    }

    @Test
    fun recoveryScore_lowReadinessSuggestsRecovery() {
        val score = AiCoach.recoveryScore(
            RecoveryInputs(
                sleepHours = 4.8,
                restingHeartRateDelta = 15,
                soreness = 8,
                stress = 9,
                readinessSelfScore = 2
            )
        )

        assertThat(score.zone).isEqualTo("Recover")
        assertThat(score.score).isLessThan(50)
    }
}
