package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.PersonalityProfile
import com.example.model.TopicPreference
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
import com.example.ui.viewmodel.PersonalityViewModel

@Composable
fun PersonalityScreen(
    viewModel: PersonalityViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val selectedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val preferences by viewModel.currentPreferences.collectAsStateWithLifecycle()
    val editorMessage by viewModel.editorMessage.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showResetMemoryDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    if (showCreateDialog) {
        CreateCustomProfileDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, tagline, desc, prefs, avoids, explore, skip, watch ->
                viewModel.createCustomProfile(name, tagline, desc, prefs, avoids, explore, skip, watch)
                showCreateDialog = false
            }
        )
    }

    if (showResetMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showResetMemoryDialog = false },
            title = { Text("Reset Agent Memory?", color = TextPrimary) },
            text = {
                Text(
                    "This will clear all accumulated topic scores, like histories, and skip metrics for '${selectedProfile?.name}'. The profile configuration will be preserved.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetActiveProfileMemory()
                        showResetMemoryDialog = false
                    }
                ) {
                    Text("Wipe Memory", color = TerminalRose, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetMemoryDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DeepMidnightNavy
        )
    }

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
            Spacer(modifier = Modifier.height(36.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyberCardSurface)
                            .border(1.dp, CyberCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "AGENT PERSONALITIES",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan, CircleShape)
                        .testTag("create_custom_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Profile",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (editorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerminalEmerald.copy(alpha = 0.15f))
                        .border(1.dp, TerminalEmerald.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = editorMessage!!,
                        color = TerminalEmerald,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SELECT ARCHETYPE",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Profile List
            profiles.forEach { profile ->
                val isSelected = profile.id == selectedProfile?.id
                ProfileSelectCard(
                    profile = profile,
                    isSelected = isSelected,
                    onSelect = { viewModel.selectProfile(profile.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Learned Memory Section for selected profile
            val active = selectedProfile
            if (active != null) {
                Text(
                    text = "LOCAL MEMORY & TASTE DRIFT (${active.name.uppercase()})",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberCardSurface)
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Learned Topic Affinities",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { showResetMemoryDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset Memory",
                                    tint = TerminalRose,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (preferences.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Zero interactions recorded yet for this agent. Start a break to begin building memory.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            preferences.forEach { pref ->
                                TopicPreferenceRow(pref = pref)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun ProfileSelectCard(
    profile: PersonalityProfile,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val isCoder = profile.avatarType == "CODER" || profile.coderAffinity >= 0.7f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) CyberCardSurface else DeepMidnightNavy)
            .border(
                1.5.dp,
                if (isSelected) NeonCyan else CyberCardBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .padding(14.dp)
            .testTag("profile_card_${profile.id}")
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isCoder) TerminalEmerald.copy(alpha = 0.2f) else ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCoder) Icons.Default.Code else Icons.Default.Psychology,
                            contentDescription = null,
                            tint = if (isCoder) TerminalEmerald else ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile.name,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (profile.isCustom) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CUSTOM",
                                    color = TerminalAmber,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(TerminalAmber.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = profile.tagline,
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = profile.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Trait Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TraitPill(label = "Curiosity: ${(profile.explorationRate * 100).toInt()}%")
                TraitPill(label = "Skip: ${profile.skipThresholdSec}s")
                TraitPill(label = "Coder Affinity: ${(profile.coderAffinity * 100).toInt()}%")
            }
        }
    }
}

@Composable
private fun TraitPill(label: String) {
    Box(
        modifier = Modifier
            .background(ObsidianBg.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
            .border(0.5.dp, CyberCardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(text = label, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun TopicPreferenceRow(pref: TopicPreference) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = pref.topic.uppercase(),
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${(pref.score * 100).toInt()}% (${pref.watches}w, ${pref.likes}L, ${pref.skips}s)",
                color = if (pref.score >= 0.3f) TerminalEmerald else if (pref.score <= -0.2f) TerminalRose else TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        val normalized = ((pref.score + 1f) / 2f).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { normalized },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = if (pref.score >= 0f) TerminalEmerald else TerminalRose,
            trackColor = CyberCardBorder
        )
    }
}

@Composable
private fun CreateCustomProfileDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, List<String>, List<String>, Float, Float, Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var tagline by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var topicsString by remember { mutableStateOf("coding, technology, space") }
    var explorationRate by remember { mutableFloatStateOf(0.35f) }
    var skipSec by remember { mutableFloatStateOf(3.5f) }
    var watchSec by remember { mutableFloatStateOf(12f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Design Custom Agent", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Agent Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("Tagline") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Personality Directive") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = topicsString,
                    onValueChange = { topicsString = it },
                    label = { Text("Preferred Topics (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text("Curiosity / Exploration: ${(explorationRate * 100).toInt()}%", color = TextSecondary, fontSize = 12.sp)
                Slider(
                    value = explorationRate,
                    onValueChange = { explorationRate = it },
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Skip Threshold: ${String.format("%.1f", skipSec)}s", color = TextSecondary, fontSize = 12.sp)
                Slider(
                    value = skipSec,
                    onValueChange = { skipSec = it },
                    valueRange = 1.5f..8.0f,
                    colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val topics = topicsString.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                    onCreate(name, tagline, description, topics, emptyList(), explorationRate, skipSec, watchSec)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ObsidianBg)
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = DeepMidnightNavy
    )
}
