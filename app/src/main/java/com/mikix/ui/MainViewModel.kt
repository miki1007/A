package com.mikix.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikix.data.Exercise
import com.mikix.data.MikixRepository
import com.mikix.domain.TrainingCalculators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MikixRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val completedSets = MutableStateFlow(0)
    private val totalVolume = MutableStateFlow(0.0)

    val exercises = query.combine(repository.exercises()) { q, ex -> ex.filter { it.name.contains(q, true) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates = repository.templates().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sessions = repository.sessions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val weeklyVolumes = repository.weeklyVolumes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val uiState: StateFlow<SessionUiState> = combine(completedSets, totalVolume) { sets, vol -> SessionUiState(sets, vol) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionUiState())

    init {
        viewModelScope.launch { repository.seedExercisesIfEmpty() }
    }

    fun updateSearch(value: String) { query.value = value }

    fun completeSet(weight: Double, reps: Int) {
        completedSets.value += 1
        totalVolume.value += TrainingCalculators.volume(weight, reps)
        viewModelScope.launch {
            dataStore.edit {
                it[longPreferencesKey("autosave_sets")] = completedSets.value.toLong()
                it[doublePreferencesKey("autosave_volume")] = totalVolume.value
            }
        }
    }

    fun computeStrengthIndex(): Double = TrainingCalculators.strengthIndex(
        lifts = mapOf("Squat" to 140.0, "Bench" to 105.0, "Deadlift" to 175.0),
        bodyWeight = 78.0
    )

    fun suggestion(hit: Boolean, missedBy: Int, currentWeight: Double) =
        TrainingCalculators.adaptiveSuggestion(hit, missedBy, currentWeight)
}

data class SessionUiState(val completedSets: Int = 0, val volume: Double = 0.0)

data class Challenge(val title: String, val progress: Float, val subtitle: String)

val demoChallenges = listOf(
    Challenge("Weekly Volume Sprint", 0.72f, "72% of 40,000 kg"),
    Challenge("5-day Consistency", 0.4f, "2 / 5 sessions done"),
    Challenge("PR Hunt", 0.55f, "11 PRs this month")
)

val onboardingSlides = listOf(
    "Track every set" to "Fast logging, warmups, PR checks",
    "Rest timers + PRs" to "Auto timer, haptics, confetti micro wins",
    "Progress analytics" to "Volume, trends, and strength curves",
    "Adaptive plans" to "Local AI-like progression logic",
    "Community challenges" to "Train together and stay accountable"
)

val bottomTabs = listOf("Home", "Log", "Progress", "Community", "Profile")

val profileStats = listOf("Streak" to "14 days", "Badges" to "26", "Sessions" to "184")

val mockFeed = listOf("Aisha hit a 120kg squat PR", "Musa completed Push Day", "Zara joined Volume Sprint")

fun Exercise.lastTimeLabel(): String = "Last: ${(70..110).random()} x ${(5..10).random()}"
