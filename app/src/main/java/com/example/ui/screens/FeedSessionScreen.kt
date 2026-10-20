package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.FeedItem
import com.example.ui.components.AgentThoughtOverlay
import com.example.ui.components.ReelVisualCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.DeepMidnightNavy
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalEmerald
import com.example.ui.theme.TerminalRose
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FeedSessionViewModel

@Composable
fun FeedSessionScreen(
    viewModel: FeedSessionViewModel,
    onSessionFinished: (String) -> Unit,
    onExitToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = uiState.currentIndex,
        pageCount = { uiState.items.size }
    )

    var showConfirmExitDialog by remember { mutableStateOf(false) }

    // Sync ViewModel programmatic reel advance to Pager state
    LaunchedEffect(uiState.currentIndex) {
        if (pagerState.currentPage != uiState.currentIndex) {
            pagerState.animateScrollToPage(uiState.currentIndex)
        }
    }

    // Sync user manual swipe gestures on Pager to ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.onUserSwipeToPage(page)
        }
    }

    // When session completes, route to session report
    LaunchedEffect(uiState.isSessionCompleted, uiState.completedSessionId) {
        if (uiState.isSessionCompleted && uiState.completedSessionId != null) {
            onSessionFinished(uiState.completedSessionId!!)
        }
    }

    BackHandler {
        showConfirmExitDialog = true
    }

    if (showConfirmExitDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmExitDialog = false },
            title = { Text("End AI Break?", color = TextPrimary) },
            text = {
                Text(
                    "Stopping the session now will finalize the post-break debrief report with the reels viewed so far.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmExitDialog = false
                        viewModel.stopSession()
                    }
                ) {
                    Text("End & View Report", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmExitDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DeepMidnightNavy
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Full screen Vertical Reel Pager
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val item = uiState.items.getOrNull(pageIndex)
            if (item != null) {
                ReelPageContent(
                    item = item,
                    onToggleUserLike = { viewModel.toggleUserLike() },
                    onToggleUserSave = { viewModel.toggleUserSave() }
                )
            }
        }

        // Top Minimal Bar (Back Button + Live Session Counter)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { showConfirmExitDialog = true },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ObsidianBg.copy(alpha = 0.7f))
                    .border(0.8.dp, CyberCardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianBg.copy(alpha = 0.75f))
                    .border(0.8.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "SEEN: ${uiState.totalSeen} • ${uiState.sessionDurationSec}s",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Floating Agent Thought Overlay at Bottom
        AgentThoughtOverlay(
            profile = uiState.profile,
            decision = uiState.currentDecision,
            engineName = uiState.engineName,
            secondsLeftOnReel = uiState.secondsLeftOnReel,
            dwellTotalSeconds = uiState.dwellTotalSeconds,
            isPaused = uiState.isPaused,
            onTogglePause = { viewModel.togglePause() },
            onStopSession = { viewModel.stopSession() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        )
    }
}

@Composable
private fun ReelPageContent(
    item: FeedItem,
    onToggleUserLike: () -> Unit,
    onToggleUserSave: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background generative scene
        ReelVisualCard(visualType = item.generativeVisualType)

        // Gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 300f
                    )
                )
        )

        // Right side floating action column (User like, save, share)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Heart button (User Like)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onToggleUserLike,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(ObsidianBg.copy(alpha = 0.65f))
                        .border(
                            1.dp,
                            if (item.isUserLiked) TerminalRose else CyberCardBorder,
                            CircleShape
                        )
                        .testTag("user_like_button")
                ) {
                    Icon(
                        imageVector = if (item.isUserLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "User Like",
                        tint = if (item.isUserLiked) TerminalRose else TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.likesCount / 1000}k",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Save bookmark button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onToggleUserSave,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(ObsidianBg.copy(alpha = 0.65f))
                        .border(
                            1.dp,
                            if (item.isUserSaved) TerminalAmber else CyberCardBorder,
                            CircleShape
                        )
                        .testTag("user_save_button")
                ) {
                    Icon(
                        imageVector = if (item.isUserSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Reel",
                        tint = if (item.isUserSaved) TerminalAmber else TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.savesCount / 1000}k",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Left Bottom Reel Details (Above the thought overlay)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(start = 20.dp, bottom = 220.dp)
        ) {
            // Category & Tag pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .border(0.8.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.category.uppercase(),
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = item.creator,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.description,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item.tags.take(3).forEach { tag ->
                    Text(
                        text = "#$tag",
                        color = NeonCyan.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
