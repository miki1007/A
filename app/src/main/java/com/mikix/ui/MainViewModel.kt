package com.mikix.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.mikix.data.Exercise
import com.mikix.data.MikixRepository
import com.mikix.domain.AiCoach
import com.mikix.domain.Equipment
import com.mikix.domain.Experience
import com.mikix.domain.Goal
import com.mikix.domain.RecoveryInputs
import com.mikix.domain.TrainingCalculators
import com.mikix.worker.CloudSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MikixRepository,
    private val dataStore: DataStore<Preferences>,
    private val workManager: WorkManager
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val completedSets = MutableStateFlow(0)
    private val totalVolume = MutableStateFlow(0.0)
    private val accessToken = MutableStateFlow<String?>(null)
    private val _generatedRoutine = MutableStateFlow<String>("No AI routine generated yet")
    val generatedRoutine = _generatedRoutine.asStateFlow()
    private val _recoverySummary = MutableStateFlow("Recovery score pending")
    val recoverySummary = _recoverySummary.asStateFlow()
    private val _liveFeedEvent = MutableStateFlow("Waiting for live events")
    val liveFeedEvent = _liveFeedEvent.asStateFlow()

    val exercises = query.combine(repository.exercises()) { q, ex -> ex.filter { it.name.contains(q, true) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates = repository.templates().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sessions = repository.sessions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val weeklyVolumes = repository.weeklyVolumes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val groups = repository.groups().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val feed = repository.feed().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val challenges = repository.challenges().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val account = repository.account().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val filament3dEnabled = dataStore.data.map { it[FeatureFlags.filament3dX] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val lowPerformanceMode = dataStore.data.map { it[FeatureFlags.lowPerformanceMode] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val cloudSyncEnabled = dataStore.data.map { it[FeatureFlags.cloudSync] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val uiState: StateFlow<SessionUiState> = combine(completedSets, totalVolume) { sets, vol -> SessionUiState(sets, vol) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionUiState())

    init {
        viewModelScope.launch { repository.seedExercisesIfEmpty() }
        viewModelScope.launch {
            accessToken.value = dataStore.data.first()[stringPreferencesKey("access_token")]
        }
        viewModelScope.launch {
            repository.liveFeedEvents().collect { _liveFeedEvent.value = it }
        }
    }

    fun updateSearch(value: String) {
        query.value = value
    }

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

    fun login(email: String, password: String) {
        viewModelScope.launch {
            runCatching { repository.login(email, password) }.onSuccess { auth ->
                accessToken.value = auth.accessToken
                dataStore.edit {
                    it[stringPreferencesKey("access_token")] = auth.accessToken
                    it[stringPreferencesKey("refresh_token")] = auth.refreshToken
                    it[FeatureFlags.cloudSync] = true
                }
                triggerCloudSync(auth.accessToken)
                repository.refreshCommunity(auth.accessToken)
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            runCatching { repository.register(email, password) }.onSuccess { auth ->
                accessToken.value = auth.accessToken
                dataStore.edit {
                    it[stringPreferencesKey("access_token")] = auth.accessToken
                    it[stringPreferencesKey("refresh_token")] = auth.refreshToken
                    it[FeatureFlags.cloudSync] = true
                }
                triggerCloudSync(auth.accessToken)
            }
        }
    }

    fun triggerCloudSync(token: String? = accessToken.value) {
        val t = token ?: return
        val request = OneTimeWorkRequestBuilder<CloudSyncWorker>()
            .setInputData(workDataOf("accessToken" to t))
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork("mikix-cloud-sync", ExistingWorkPolicy.REPLACE, request)
    }

    fun setFilamentEnabled(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[FeatureFlags.filament3dX] = enabled } }
    }

    fun setLowPerformanceMode(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[FeatureFlags.lowPerformanceMode] = enabled } }
    }

    fun setCloudSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[FeatureFlags.cloudSync] = enabled } }
    }

    fun generateAiRoutine() {
        val routine = AiCoach.generateRoutine(
            input = com.mikix.domain.RoutineInput(
                goal = Goal.STRENGTH,
                daysPerWeek = 4,
                equipment = setOf(Equipment.BARBELL, Equipment.DUMBBELL, Equipment.MACHINE),
                focusMuscles = setOf("chest", "back", "legs"),
                experience = Experience.INTERMEDIATE
            )
        )
        _generatedRoutine.value = buildString {
            append(routine.blockName)
            append("\n")
            routine.workouts.forEach {
                append("Day ${it.day}: ${it.title} -> ${it.exercises.joinToString()}\n")
            }
            append(routine.progressionNote)
        }
    }

    fun updateRecoveryScore() {
        val score = AiCoach.recoveryScore(
            RecoveryInputs(
                sleepHours = 7.2,
                restingHeartRateDelta = 3,
                soreness = 4,
                stress = 5,
                readinessSelfScore = 7
            )
        )
        _recoverySummary.value = "${score.zone} (${score.score}) • ${score.guidance}"
    }
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
