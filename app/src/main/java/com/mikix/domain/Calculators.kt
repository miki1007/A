package com.mikix.domain

import kotlin.math.max

object TrainingCalculators {
    fun volume(weight: Double, reps: Int): Double = weight * reps
    fun epleyOneRm(weight: Double, reps: Int): Double = if (reps <= 0) 0.0 else weight * (1 + reps / 30.0)

    fun strengthIndex(
        lifts: Map<String, Double>,
        bodyWeight: Double?,
        liftWeights: Map<String, Double> = mapOf("Squat" to 0.35, "Bench" to 0.3, "Deadlift" to 0.35)
    ): Double {
        val raw = lifts.entries.sumOf { (k, v) -> (liftWeights[k] ?: 0.2) * v }
        val bwFactor = bodyWeight?.let { max(0.7, minOf(1.3, 75.0 / it)) } ?: 1.0
        return raw * bwFactor
    }

    fun adaptiveSuggestion(hitAllTargets: Boolean, missedBy: Int, currentWeight: Double): String = when {
        hitAllTargets -> "+2.5kg next session or +1 rep"
        missedBy >= 3 -> "Deload 5% and rebuild"
        else -> "Repeat weight and stabilize form"
    }
}
