package com.example.companion.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.example.R
import com.example.companion.model.CompanionState
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.DeepMidnightNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalEmerald
import com.example.ui.theme.TerminalRose

@Composable
fun CompanionAvatarBubble(
    state: CompanionState,
    providerLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val haloRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_rotation"
    )

    val stateColor = when (state) {
        CompanionState.IDLE -> NeonCyan
        CompanionState.LISTENING -> TerminalAmber
        CompanionState.THINKING -> ElectricBlue
        CompanionState.ANALYZING_SCREEN -> TerminalEmerald
        CompanionState.SPEAKING -> NeonCyan
        CompanionState.HAPPY -> TerminalEmerald
        CompanionState.CURIOUS -> ElectricBlue
        CompanionState.ERROR -> TerminalRose
    }

    Box(
        modifier = modifier
            .size(72.dp)
            .scale(if (state == CompanionState.IDLE) pulseScale else 1.0f)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        stateColor.copy(alpha = 0.25f),
                        DeepMidnightNavy.copy(alpha = 0.95f),
                        ObsidianBg
                    )
                )
            )
            .border(2.dp, stateColor, CircleShape)
            .clickable { onClick() }
            .testTag("companion_avatar_bubble"),
        contentAlignment = Alignment.Center
    ) {
        // User's pixel creature avatar image
        Image(
            painter = painterResource(id = R.drawable.companion_avatar),
            contentDescription = "AI BREAK Companion Avatar (${state.displayName})",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )

        // Status Badge Overlay at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-2).dp)
                .background(ObsidianBg.copy(alpha = 0.85f), CircleShape)
                .border(0.6.dp, stateColor, CircleShape)
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = state.displayName.take(7).uppercase(),
                color = stateColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Active State Icon Accent at top right
        if (state == CompanionState.LISTENING) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .background(TerminalAmber, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(10.dp))
            }
        } else if (state == CompanionState.THINKING) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .background(ElectricBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(10.dp))
            }
        }
    }
}

@Composable
fun DraggableCompanionAvatar(
    state: CompanionState,
    providerLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(40f) }
    var offsetY by remember { mutableFloatStateOf(450f) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        CompanionAvatarBubble(
            state = state,
            providerLabel = providerLabel,
            onClick = onClick
        )
    }
}

