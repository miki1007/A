package com.mikix.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TrainingCalculatorsTest {
    @Test fun volume_isCorrect() {
        assertThat(TrainingCalculators.volume(100.0, 5)).isEqualTo(500.0)
    }

    @Test fun epley_estimate() {
        assertThat(TrainingCalculators.epleyOneRm(100.0, 5)).isWithin(0.01).of(116.666)
    }

    @Test fun strengthIndex_weightsAndBodyWeightApplied() {
        val score = TrainingCalculators.strengthIndex(
            lifts = mapOf("Squat" to 150.0, "Bench" to 100.0, "Deadlift" to 180.0),
            bodyWeight = 75.0
        )
        assertThat(score).isGreaterThan(140.0)
    }
}
