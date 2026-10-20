package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ModelInfo
import com.example.model.PersonalityProfile
import com.example.model.SessionRecord
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.DeepMidnightNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalEmerald
import com.example.ui.theme.TerminalRose
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    profile: PersonalityProfile?,
    currentModel: ModelInfo?,
    recentSessions: List<SessionRecord>,
    onStartBreak: () -> Unit,
    onChoosePersonality: () -> Unit,
    onOpenModelManager: () -> Unit,
    onOpenRealDeviceSetup: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectPastSession: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val activeProfile = profile ?: PersonalityProfile.PRESET_TECH_NERD
    val isModelLoaded = currentModel?.isLoaded == true

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepMidnightNavy, ObsidianBg)
                )
            )
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header HUD: Title & Quick Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AI BREAK",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Autonomous On-Device Social Feed",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CyberCardSurface)
                            .border(1.dp, CyberCardBorder, CircleShape)
                            .testTag("dashboard_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Engine Status Banner
            EngineStatusBadge(
                isModelLoaded = isModelLoaded,
                modelName = currentModel?.fileName,
                modelFormat = currentModel?.modelFormat,
                onOpenModelManager = onOpenModelManager
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Active Agent Card
            ActiveAgentCard(
                profile = activeProfile,
                onChoosePersonality = onChoosePersonality
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Primary CTA: Start AI Break
            Button(
                onClick = onStartBreak,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = ObsidianBg
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("start_ai_break_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "START AI BREAK",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Hub Navigation Cards
            Text(
                text = "LABORATORY CONTROLS",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardNavCard(
                    title = "Personality",
                    subtitle = activeProfile.name,
                    icon = Icons.Default.Psychology,
                    accentColor = ElectricBlue,
                    onClick = onChoosePersonality,
                    modifier = Modifier.weight(1f),
                    testTag = "nav_personality_card"
                )
                DashboardNavCard(
                    title = "Local AI Model",
                    subtitle = if (isModelLoaded) "Loaded" else "Import/GGUF",
                    icon = Icons.Default.Memory,
                    accentColor = if (isModelLoaded) TerminalEmerald else TerminalAmber,
                    onClick = onOpenModelManager,
                    modifier = Modifier.weight(1f),
                    testTag = "nav_model_card"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            DashboardNavCard(
                title = "Real App Control (Accessibility)",
                subtitle = "Authorized scrolling on physical device apps",
                icon = Icons.Default.AccessibilityNew,
                accentColor = TerminalAmber,
                onClick = onOpenRealDeviceSetup,
                modifier = Modifier.fillMaxWidth(),
                testTag = "nav_real_device_card"
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Past Breaks Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PAST BREAKS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                if (recentSessions.isNotEmpty()) {
                    Text(
                        text = "View All (${recentSessions.size})",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onOpenHistory() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (recentSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CyberCardSurface.copy(alpha = 0.5f))
                        .border(1.dp, CyberCardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No breaks recorded yet.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Hit 'START AI BREAK' to let your agent explore its first feed.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentSessions.take(3).forEach { session ->
                        PastSessionRow(session = session, onClick = { onSelectPastSession(session.sessionId) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EngineStatusBadge(
    isModelLoaded: Boolean,
    modelName: String?,
    modelFormat: String?,
    onOpenModelManager: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberCardSurface)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
            .clickable { onOpenModelManager() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isModelLoaded) TerminalEmerald else TerminalAmber)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isModelLoaded) "LOCAL AI MODEL LOADED" else "OFFLINE SIMULATION MODE",
                        color = if (isModelLoaded) TerminalEmerald else TerminalAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isModelLoaded) "$modelName ($modelFormat)" else "Deterministic heuristic decision engine active",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Manage Models",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ActiveAgentCard(
    profile: PersonalityProfile,
    onChoosePersonality: () -> Unit
) {
    val isEvolvedCoder = profile.coderAffinity >= 0.70f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(CyberCardSurface, DeepMidnightNavy)
                )
            )
            .border(1.dp, if (isEvolvedCoder) TerminalEmerald.copy(alpha = 0.5f) else CyberCardBorder, RoundedCornerShape(18.dp))
            .clickable { onChoosePersonality() }
            .padding(16.dp)
            .testTag("active_agent_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isEvolvedCoder) TerminalEmerald.copy(alpha = 0.2f) else ElectricBlue.copy(alpha = 0.2f))
                            .border(1.dp, if (isEvolvedCoder) TerminalEmerald else ElectricBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEvolvedCoder) Icons.Default.Code else Icons.Default.Psychology,
                            contentDescription = null,
                            tint = if (isEvolvedCoder) TerminalEmerald else ElectricBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile.name,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isEvolvedCoder) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CODER AGENT",
                                    color = TerminalEmerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier
                                        .background(TerminalEmerald.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = profile.tagline,
                            color = NeonCyan,
                            fontSize = 12.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Edit Profile",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = profile.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Coder Affinity Evolution Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "CODER TRAIT AFFINITY",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${(profile.coderAffinity * 100).toInt()}%",
                    color = if (isEvolvedCoder) TerminalEmerald else NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { profile.coderAffinity.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = if (isEvolvedCoder) TerminalEmerald else NeonCyan,
                trackColor = CyberCardBorder
            )
        }
    }
}

@Composable
private fun DashboardNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CyberCardSurface)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Column {
            Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
private fun PastSessionRow(
    session: SessionRecord,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberCardSurface)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.summaryHeadline,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = "${session.durationSeconds}s break • ${session.totalSeen} reels (${session.totalLiked} likes, ${session.totalSkipped} skips)",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Report",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
