package com.mikix.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikix.data.Exercise

private val Gold = Color(0xFFF5B938)
private val Bg = Color(0xFF050C1A)
private val Surface = Color(0xFF0B162B)

@Composable
fun MikiXApp(vm: MainViewModel = hiltViewModel()) {
    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = Gold, secondary = Gold)) {
        var selectedTab by remember { mutableIntStateOf(0) }
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                BottomAppBar(containerColor = Surface) {
                    bottomTabs.forEachIndexed { index, tab ->
                        NavigationBarItem(selected = selectedTab == index, onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        "Home" -> Icons.Outlined.Home
                                        "Log" -> Icons.Outlined.PlayCircle
                                        "Progress" -> Icons.Outlined.AutoGraph
                                        "Community" -> Icons.Outlined.Groups
                                        else -> Icons.Outlined.Person
                                    }, contentDescription = tab
                                )
                            }, label = { Text(tab) })
                    }
                }
            }
        ) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                AnimatedContent(targetState = selectedTab, transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) togetherWith fadeOut()
                }, label = "tab") { tab ->
                    when (tab) {
                        0 -> HomeScreen(vm)
                        1 -> LogScreen(vm)
                        2 -> ProgressScreen(vm)
                        3 -> CommunityScreen(vm)
                        else -> ProfileScreen(vm)
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumHeader(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, style = MaterialTheme.typography.titleMedium, color = Gold)
    }
}

@Composable
fun PhoneMockCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(20.dp, RoundedCornerShape(26.dp)),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(26.dp)
    ) { Box(Modifier.padding(16.dp)) { content() } }
}

@Composable
fun BulletList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✓", color = Gold, modifier = Modifier.padding(end = 8.dp))
                Text(it, color = Color(0xFFD0D8E8))
            }
        }
    }
}

@Composable
fun HomeScreen(vm: MainViewModel) {
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxSize().verticalScroll(scroll).background(Bg)) {
        PremiumHeader("MikiX Command Center", "Every set. Every rep. Every PR.")
        listOf("Continue Workout", "Today's Plan", "Recovery", "Quick Start", "Weekly Overview").forEachIndexed { i, title ->
            val alpha by animateFloatAsState(if (scroll.value > i * 10) 1f else 0.65f, label = "stagger")
            PhoneMockCard {
                Column(Modifier.alpha(alpha)) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Dark premium card with spring motion and soft shadow.", color = Color(0xFFA8B3C6))
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(vm: MainViewModel) {
    val state by vm.uiState.collectAsState()
    Column(Modifier.fillMaxSize().background(Bg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PremiumHeader("Workout Tracking", "Fast logging with smart defaults")
        PhoneMockCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Workout Session", color = Color.White, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatChip("Sets", state.completedSets.toString())
                    StatChip("Volume", "${state.volume.toInt()} kg")
                    StatChip("1RM", "${vm.computeStrengthIndex().toInt()}")
                }
                BulletList(listOf("Swipe delete + warmup tagging", "Auto rest timer sheet", "PR confetti micro-animation"))
                androidx.compose.material3.Button(onClick = { vm.completeSet(80.0, 8) }) {
                    Icon(Icons.Outlined.Timer, contentDescription = "complete")
                    Text(" Complete Set")
                }
            }
        }
        Text("Exercise Library", color = Color.White, fontWeight = FontWeight.Bold)
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items((1..12).map { "Exercise #$it" }) {
                PhoneMockCard { Text("$it • Last time 72 x 8", color = Color(0xFFD0D8E8)) }
            }
        }
    }
}

@Composable
fun ProgressScreen(vm: MainViewModel) {
    val score = vm.computeStrengthIndex()
    Column(Modifier.fillMaxSize().background(Bg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PremiumHeader("Analytics & Progress", "Data-driven decisions")
        PhoneMockCard {
            Column {
                Text("MikiX Strength Index", color = Color.White, fontWeight = FontWeight.Bold)
                Text(score.toInt().toString(), color = Gold, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
                Text("One number. Total strength.", color = Color(0xFFA8B3C6))
                SparkChart(Modifier.fillMaxWidth().height(100.dp))
            }
        }
        PhoneMockCard {
            Column {
                Text("Muscle Balance", color = Color.White)
                RadarGrid()
            }
        }
    }
}

@Composable
fun CommunityScreen(vm: MainViewModel) {
    val groups by vm.groups.collectAsState()
    val feed by vm.feed.collectAsState()
    val challenges by vm.challenges.collectAsState()
    Column(Modifier.fillMaxSize().background(Bg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PremiumHeader("Group Workouts & Challenges", "Compete. Motivate. Win together.")
        PhoneMockCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (if (challenges.isEmpty()) demoChallenges.map { com.mikix.data.ChallengeEntity(it.title,it.title,it.progress,"kg",100) } else challenges).forEach {
                    Text(it.title, color = Color.White)
                    LinearProgressIndicator(progress = { it.progress }, color = Gold, trackColor = Color(0xFF24324D))
                    Text("${it.unit} target: ${it.target}", color = Color(0xFFA8B3C6))
                }
            }
        }
        PhoneMockCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Groups", color = Color.White, fontWeight = FontWeight.Bold)
                (if (groups.isEmpty()) listOf("No synced groups yet") else groups.map { "${it.name} • ${it.memberCount} members" }).take(3).forEach {
                    Text(it, color = Color(0xFFD0D8E8))
                }
                Text("Feed", color = Color.White, fontWeight = FontWeight.Bold)
                (if (feed.isEmpty()) mockFeed else feed.map { "${it.authorName}: ${it.message}" }).take(3).forEach {
                    Text(it, color = Color(0xFFD0D8E8))
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(vm: MainViewModel) {
    Column(Modifier.fillMaxSize().background(Bg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PremiumHeader("MikiX Profile", "Integrations, export, and personalization")
        val filamentEnabled by vm.filament3dEnabled.collectAsState()
        val lowPerf by vm.lowPerformanceMode.collectAsState()
        val cloudSync by vm.cloudSyncEnabled.collectAsState()
        PhoneMockCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                profileStats.forEach { StatChip(it.first, it.second) }
                Text("Adaptive suggestion: ${vm.suggestion(hit = true, missedBy = 0, currentWeight = 80.0)}", color = Color(0xFFD0D8E8))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cloud sync")
                    Switch(checked = cloudSync, onCheckedChange = vm::setCloudSyncEnabled)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Filament 3D X")
                    Switch(checked = filamentEnabled, onCheckedChange = vm::setFilamentEnabled)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Low performance mode")
                    Switch(checked = lowPerf, onCheckedChange = vm::setLowPerformanceMode)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.login("demo@mikix.app", "password123") }) { Text("Login") }
                    Button(onClick = { vm.triggerCloudSync() }) { Text("Sync now") }
                }
            }
        }
        PhoneMockCard {
            BulletList(listOf("Theme tokenized dark/light", "Units kg/lb via DataStore", "CSV export + Health Connect placeholder"))
        }
        PerformanceGated3DX(filamentEnabled = filamentEnabled, lowPerformanceMode = lowPerf)
    }
}

@Composable
fun StatChip(label: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101F39))) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(label, color = Color(0xFFA8B3C6))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SparkChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val points = listOf(0.1f, 0.3f, 0.25f, 0.6f, 0.55f, 0.8f)
        val path = androidx.compose.ui.graphics.Path()
        points.forEachIndexed { i, p ->
            val x = size.width * i / (points.size - 1)
            val y = size.height * (1 - p)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = Gold, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f, cap = StrokeCap.Round))
    }
}

@Composable
fun RadarGrid() {
    Canvas(Modifier.fillMaxWidth().height(130.dp)) {
        val center = this.center
        val radius = size.minDimension / 3
        repeat(5) { i ->
            drawCircle(Color(0xFF283B5A), radius * (i + 1) / 5f, center, style = androidx.compose.ui.graphics.drawscope.Stroke(1f))
        }
    }
}

@Composable
fun RotatingX3DMark() {
    var phase by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(1100); phase++ } }
    val rot by animateFloatAsState((phase % 360).toFloat(), animationSpec = spring(dampingRatio = 0.7f), label = "x-rot")
    Box(
        Modifier.fillMaxWidth().height(120.dp).background(
            Brush.radialGradient(listOf(Color(0x33F5B938), Color.Transparent))
        ), contentAlignment = Alignment.Center
    ) { Text("X", color = Gold, style = MaterialTheme.typography.displayMedium, modifier = Modifier.alpha(0.7f + (rot % 10) / 40f)) }
}

@Composable
fun SplashScreen() = Box(Modifier.fillMaxSize().background(Bg), contentAlignment = Alignment.Center) { RotatingX3DMark() }

@Composable
fun OnboardingScreen() {
    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(onboardingSlides) {
            PhoneMockCard {
                Column {
                    Text(it.first, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(it.second, color = Color(0xFFA8B3C6))
                }
            }
        }
    }
}

@Composable
fun ExerciseLibraryScreen(exercises: List<Exercise>) {
    Column(Modifier.fillMaxSize().background(Bg)) {
        PremiumHeader("Exercise Library", "50+ starter movements with filters")
        LazyColumn { items(exercises.take(30)) { PhoneMockCard { Text("${it.name} • ${it.muscleGroup}", color = Color.White) } } }
    }
}

@Composable
fun IntegrationsScreen() {
    Column(Modifier.fillMaxSize().background(Bg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PremiumHeader("Integrations", "Works with your ecosystem")
        BulletList(listOf("Health Connect placeholder", "Google Fit placeholder", "CSV export implemented local"))
    }
}
