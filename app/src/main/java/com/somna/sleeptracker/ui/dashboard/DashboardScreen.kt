package com.somna.sleeptracker.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import com.somna.sleeptracker.data.local.entity.SleepSessionEntity
import com.somna.sleeptracker.ui.theme.CelestialCyan
import com.somna.sleeptracker.ui.theme.DeepSurface
import com.somna.sleeptracker.ui.theme.DeepSurfaceElevated
import com.somna.sleeptracker.ui.theme.DeepSurfaceVariant
import com.somna.sleeptracker.ui.theme.IndigoPrimary
import com.somna.sleeptracker.ui.theme.MidnightDark
import com.somna.sleeptracker.ui.theme.MysticViolet
import com.somna.sleeptracker.ui.theme.QualityFair
import com.somna.sleeptracker.ui.theme.QualityFragmented
import com.somna.sleeptracker.ui.theme.QualityOptimal
import com.somna.sleeptracker.ui.theme.QualityRestful
import com.somna.sleeptracker.ui.theme.StarGold
import com.somna.sleeptracker.ui.theme.TextMuted
import com.somna.sleeptracker.ui.theme.TextPrimary
import com.somna.sleeptracker.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardState,
    onIntent: (DashboardIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            onIntent(DashboardIntent.DismissMessage)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MidnightDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(IndigoPrimary, MysticViolet)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Nightlight,
                                contentDescription = "Somna Icon",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Somna",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Autonomous Sleep Tracking",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onIntent(DashboardIntent.TogglePrivacyDialog(true)) },
                        modifier = Modifier.testTag("privacy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Privacy & Security",
                            tint = CelestialCyan
                        )
                    }
                    IconButton(
                        onClick = { onIntent(DashboardIntent.SeedDemoData) },
                        modifier = Modifier.testTag("seed_demo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Data",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(DashboardIntent.OpenManualLogDialog) },
                containerColor = IndigoPrimary,
                contentColor = MidnightDark,
                shape = CircleShape,
                modifier = Modifier.testTag("log_sleep_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Sleep Manually")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Sleep Score & Status Card
            item {
                HeroSleepCard(
                    session = state.latestSession,
                    isAnalyzing = state.isAnalyzing,
                    onTriggerInference = { onIntent(DashboardIntent.TriggerAnalysis) }
                )
            }

            // 2. Telemetry Guardian Status Card
            item {
                GuardianStatusCard(
                    isActive = state.isGuardianActive,
                    eventCount = state.recentTelemetryCount,
                    isAnalyzing = state.isAnalyzing,
                    onToggle = { onIntent(DashboardIntent.ToggleGuardian(it)) },
                    onAnalyze = { onIntent(DashboardIntent.TriggerAnalysis) }
                )
            }

            // 3. Circadian Profile & Rhythm Alignment
            item {
                CircadianAlignmentCard(profile = state.circadianProfile)
            }

            // 4. 7-Day Sleep Trends Chart
            item {
                WeeklyTrendChart(sessions = state.recentSessions)
            }

            // 5. Recent Sleep Sessions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Sleep History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${state.recentSessions.size} recorded",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                }
            }

            // Sleep Session Cards
            if (state.recentSessions.isEmpty()) {
                item {
                    EmptySessionsCard(onSeed = { onIntent(DashboardIntent.SeedDemoData) })
                }
            } else {
                items(state.recentSessions, key = { it.id }) { session ->
                    SessionItemCard(
                        session = session,
                        onClick = { onIntent(DashboardIntent.SelectSessionDetail(session)) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // Dialogs
    if (state.showManualLogDialog) {
        ManualLogDialog(
            onDismiss = { onIntent(DashboardIntent.DismissManualLogDialog) },
            onSave = { start, end, quality ->
                onIntent(DashboardIntent.SaveManualSession(start, end, quality))
            }
        )
    }

    state.selectedSessionDetail?.let { session ->
        SessionDetailDialog(
            session = session,
            onDismiss = { onIntent(DashboardIntent.SelectSessionDetail(null)) },
            onDelete = { onIntent(DashboardIntent.DeleteSession(session.id)) }
        )
    }

    if (state.showPrivacyDialog) {
        PrivacyDialog(
            onDismiss = { onIntent(DashboardIntent.TogglePrivacyDialog(false)) }
        )
    }
}

@Composable
fun HeroSleepCard(
    session: SleepSessionEntity?,
    isAnalyzing: Boolean,
    onTriggerInference: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_sleep_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LAST NIGHT'S SLEEP",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CelestialCyan,
                    letterSpacing = 1.2.sp
                )
                session?.let {
                    QualityBadge(quality = it.quality)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (session != null) {
                val hours = session.durationMinutes / 60
                val minutes = session.durationMinutes % 60
                val efficiency = session.efficiencyScore

                // Animated Circular Score Gauge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(170.dp)
                ) {
                    val animatedScore by animateFloatAsState(
                        targetValue = efficiency / 100f,
                        animationSpec = tween(durationMillis = 1000),
                        label = "ScoreGauge"
                    )

                    Canvas(modifier = Modifier.size(160.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        // Background track
                        drawArc(
                            color = DeepSurfaceVariant,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Progress track
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(IndigoPrimary, CelestialCyan, MysticViolet, IndigoPrimary)
                            ),
                            startAngle = 135f,
                            sweepAngle = 270f * animatedScore,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$efficiency%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Sleep Quality",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${hours}h ${minutes}m",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Time Anchors Row
                val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
                val bedTimeStr = timeFormat.format(Date(session.startTime))
                val wakeTimeStr = timeFormat.format(Date(session.endTime))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepSurfaceVariant, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Bedtime",
                            tint = IndigoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text("Bedtime", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(bedTimeStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(DeepSurfaceElevated)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Wake time",
                            tint = StarGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text("Wake Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(wakeTimeStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(DeepSurfaceElevated)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Awakenings",
                            tint = CelestialCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text("Awakenings", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("${session.awakeningsCount}x", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Nightlight,
                        contentDescription = "Pending Sleep Analysis",
                        tint = IndigoPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Awaiting Tonight's Sleep Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Somna passively monitors phone stillness & charging state overnight.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onTriggerInference,
                        enabled = !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MidnightDark,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Telemetry...")
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analyze Now")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuardianStatusCard(
    isActive: Boolean,
    eventCount: Int,
    isAnalyzing: Boolean,
    onToggle: (Boolean) -> Unit,
    onAnalyze: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guardian_status_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isActive) CelestialCyan.copy(alpha = 0.15f) else DeepSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Shield else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isActive) CelestialCyan else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Telemetry Guardian",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isActive) QualityOptimal else QualityFragmented)
                        )
                    }
                    Text(
                        text = if (isActive) "$eventCount events recorded • Passive (<0.1% batt)" else "Monitoring paused",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Switch(
                checked = isActive,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MidnightDark,
                    checkedTrackColor = CelestialCyan,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DeepSurfaceElevated
                ),
                modifier = Modifier.testTag("guardian_toggle")
            )
        }
    }
}

@Composable
fun CircadianAlignmentCard(profile: CircadianProfileEntity?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MysticViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Bayesian Circadian Alignment",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "${profile?.confidenceSamples ?: 0} samples",
                    style = MaterialTheme.typography.labelSmall,
                    color = MysticViolet
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val bedHour = profile?.meanBedtimeHour ?: 23.25f
            val wakeHour = profile?.meanWakeHour ?: 7.15f
            val bedHourInt = bedHour.toInt() % 24
            val bedMinInt = ((bedHour - bedHour.toInt()) * 60).toInt()
            val wakeHourInt = wakeHour.toInt() % 24
            val wakeMinInt = ((wakeHour - wakeHour.toInt()) * 60).toInt()

            val bedFormatted = String.format(Locale.US, "%d:%02d %s", if (bedHourInt > 12) bedHourInt - 12 else if (bedHourInt == 0) 12 else bedHourInt, bedMinInt, if (bedHourInt >= 12) "PM" else "AM")
            val wakeFormatted = String.format(Locale.US, "%d:%02d %s", if (wakeHourInt > 12) wakeHourInt - 12 else if (wakeHourInt == 0) 12 else wakeHourInt, wakeMinInt, if (wakeHourInt >= 12) "PM" else "AM")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Baseline Sleep Window", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("$bedFormatted → $wakeFormatted", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Inference Variance", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    val varStr = String.format(Locale.US, "±%.1fh", (profile?.bedtimeVariance ?: 1.2f))
                    Text(varStr, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = CelestialCyan)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Circadian Rhythm Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DeepSurfaceElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(IndigoPrimary, MysticViolet, CelestialCyan)
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "On-device Bayesian filters continuously calibrate your personal rhythm with each night's inactivity gap.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun WeeklyTrendChart(sessions: List<SleepSessionEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Sleep Duration",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Target: 8h",
                    style = MaterialTheme.typography.labelSmall,
                    color = CelestialCyan
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val displaySessions = sessions.take(7).reversed()

            if (displaySessions.isEmpty()) {
                Text(
                    text = "No sleep trends yet. Trends appear as sessions are recorded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxMinutes = 600f // 10h ceiling

                    displaySessions.forEach { session ->
                        val hours = session.durationMinutes / 60f
                        val barHeightFraction = (session.durationMinutes / maxMinutes).coerceIn(0.1f, 1.0f)
                        val dayLabel = runCatching {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val date = sdf.parse(session.dateString)
                            val daySdf = SimpleDateFormat("EEE", Locale.US)
                            daySdf.format(date ?: Date())
                        }.getOrDefault("Day")

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", hours),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (hours >= 7.5f) CelestialCyan else TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .fillMaxWidth(0.6f)
                                    .height((100 * barHeightFraction).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        if (hours >= 7.5f) {
                                            Brush.verticalGradient(listOf(CelestialCyan, IndigoPrimary))
                                        } else if (hours >= 6.0f) {
                                            Brush.verticalGradient(listOf(IndigoPrimary, MysticViolet))
                                        } else {
                                            Brush.verticalGradient(listOf(QualityFair, QualityFragmented))
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionItemCard(
    session: SleepSessionEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = session.dateString,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (session.isManual) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MysticViolet.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Manual",
                                style = MaterialTheme.typography.labelSmall,
                                color = MysticViolet,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
                val bedStr = timeFormat.format(Date(session.startTime))
                val wakeStr = timeFormat.format(Date(session.endTime))
                Text(
                    text = "$bedStr - $wakeStr • ${session.awakeningsCount} awakenings",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val hours = session.durationMinutes / 60
                val mins = session.durationMinutes % 60
                Text(
                    text = "${hours}h ${mins}m",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                QualityBadge(quality = session.quality)
            }
        }
    }
}

@Composable
fun QualityBadge(quality: String) {
    val (color, label) = when (quality) {
        "Optimal" -> QualityOptimal to "Optimal"
        "Restful" -> QualityRestful to "Restful"
        "Fair" -> QualityFair to "Fair"
        else -> QualityFragmented to "Fragmented"
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f),
        border = null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
fun EmptySessionsCard(onSeed: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepSurfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No sleep records yet",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Text(
                text = "Sessions will appear automatically as nights pass, or you can log a session manually.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSeed,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Load Demo History")
            }
        }
    }
}

@Composable
fun ManualLogDialog(
    onDismiss: () -> Unit,
    onSave: (startTime: Long, endTime: Long, quality: String) -> Unit
) {
    var bedtimeHour by remember { mutableFloatStateOf(23f) } // 11 PM
    var bedtimeMinute by remember { mutableFloatStateOf(0f) }
    var wakeHour by remember { mutableFloatStateOf(7f) }    // 7 AM
    var wakeMinute by remember { mutableFloatStateOf(0f) }
    var selectedQuality by remember { mutableStateOf("Restful") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepSurface,
        title = {
            Text("Log Sleep Session", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "Bedtime: ${bedtimeHour.toInt()}:${if (bedtimeMinute.toInt() < 10) "0" else ""}${bedtimeMinute.toInt()} PM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    "Wake time: ${wakeHour.toInt()}:${if (wakeMinute.toInt() < 10) "0" else ""}${wakeMinute.toInt()} AM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )

                Text("Sleep Quality", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Optimal", "Restful", "Fair", "Fragmented").forEach { q ->
                        val isSelected = selectedQuality == q
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) IndigoPrimary else DeepSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedQuality = q }
                        ) {
                            Text(
                                text = q,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MidnightDark else TextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    cal.set(Calendar.HOUR_OF_DAY, bedtimeHour.toInt())
                    cal.set(Calendar.MINUTE, bedtimeMinute.toInt())
                    val start = cal.timeInMillis

                    val calWake = Calendar.getInstance()
                    calWake.set(Calendar.HOUR_OF_DAY, wakeHour.toInt())
                    calWake.set(Calendar.MINUTE, wakeMinute.toInt())
                    val end = calWake.timeInMillis

                    onSave(start, end, selectedQuality)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Save Session", color = MidnightDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun SessionDetailDialog(
    session: SleepSessionEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sleep Details", color = TextPrimary, fontWeight = FontWeight.Bold)
                QualityBadge(quality = session.quality)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val timeFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
                val bedStr = timeFormat.format(Date(session.startTime))
                val wakeStr = SimpleDateFormat("h:mm a", Locale.US).format(Date(session.endTime))

                Text("Date: ${session.dateString}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Text("Interval: $bedStr - $wakeStr", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("Duration: ${session.durationMinutes / 60} hours ${session.durationMinutes % 60} minutes", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Text("Efficiency Score: ${session.efficiencyScore}%", style = MaterialTheme.typography.bodyMedium, color = CelestialCyan)
                Text("Awakenings: ${session.awakeningsCount}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text("Confidence: ${(session.confidence * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                session.notes?.let {
                    Text("Notes: $it", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Close", color = MidnightDark)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDelete,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = QualityFragmented)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete")
            }
        }
    )
}

@Composable
fun PrivacyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepSurface,
        icon = {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = CelestialCyan,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text("100% On-Device Privacy", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Somna never uploads your biometric or phone activity data to any cloud server or third party.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    "• Screen State: Listens to screen on/off broadcast events to identify sleep onset and awakening.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    "• Power Connection: Correlates overnight charging with rest intervals.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    "• Bayesian Calibration: Adapts strictly inside local Room SQLite storage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    "• Automatic Retention: Raw system event logs are purged after 14 days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CelestialCyan)
            ) {
                Text("Understood", color = MidnightDark, fontWeight = FontWeight.Bold)
            }
        }
    )
}
