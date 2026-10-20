package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.DeepMidnightNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalEmerald
import com.example.ui.theme.TerminalRose
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ReelVisualCard(
    visualType: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reel_animation")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_progress"
    )

    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_progress"
    )

    Box(modifier = modifier.fillMaxSize()) {
        when (visualType) {
            "TERMINAL_CODE" -> TerminalCodeScene(animProgress = animProgress)
            "SPACE_NEBULA" -> SpaceNebulaScene(animProgress = animProgress, pulse = pulseProgress)
            "CYBER_CHIP" -> CyberChipScene(animProgress = animProgress)
            "COMEDY_STAGE" -> ComedyStageScene(animProgress = animProgress, pulse = pulseProgress)
            "CHEF_KITCHEN" -> ChefKitchenScene(animProgress = animProgress)
            "GAMING_ARENA" -> GamingArenaScene(animProgress = animProgress)
            "ANIMAL_JOY" -> AnimalJoyScene(animProgress = animProgress, pulse = pulseProgress)
            else -> AbstractAiScene(animProgress = animProgress, pulse = pulseProgress)
        }
    }
}

@Composable
private fun TerminalCodeScene(animProgress: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF030A14), Color(0xFF0A192F), Color(0xFF02070D))
                )
            )
    ) {
        val width = size.width
        val height = size.height

        // Matrix scanline backdrop
        val scanlineY = (animProgress * height) % height
        drawLine(
            color = NeonCyan.copy(alpha = 0.35f),
            start = Offset(0f, scanlineY),
            end = Offset(width, scanlineY),
            strokeWidth = 3f
        )

        // Simulated terminal code blocks
        val lines = 12
        for (i in 0 until lines) {
            val yPos = height * 0.22f + (i * 32f)
            val lineLengthRatio = when (i % 4) {
                0 -> 0.75f
                1 -> 0.45f
                2 -> 0.60f
                else -> 0.35f
            }
            val indent = if (i % 3 == 0) 40f else 80f
            val alpha = (0.4f + 0.5f * sin((animProgress * 6.28f) + i)).coerceIn(0.2f, 0.9f)
            val lineColor = if (i % 2 == 0) NeonCyan.copy(alpha = alpha) else TerminalEmerald.copy(alpha = alpha)

            drawRoundRect(
                color = lineColor,
                topLeft = Offset(indent, yPos),
                size = Size(width * lineLengthRatio, 14f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }

        // Blinking terminal cursor
        val cursorY = height * 0.22f + (lines * 32f)
        if (animProgress > 0.5f) {
            drawRect(
                color = NeonCyan,
                topLeft = Offset(80f, cursorY),
                size = Size(16f, 22f)
            )
        }
    }
}

@Composable
private fun SpaceNebulaScene(animProgress: Float, pulse: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF2E1065), Color(0xFF0F172A), Color(0xFF020617))
                )
            )
    ) {
        val center = Offset(size.width * 0.5f, size.height * 0.45f)
        val maxRadius = size.width * 0.45f * pulse

        // Glowing nebula rings
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.45f), Color.Transparent),
                center = center,
                radius = maxRadius
            ),
            center = center,
            radius = maxRadius
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonCyan.copy(alpha = 0.35f), Color.Transparent),
                center = center,
                radius = maxRadius * 0.6f
            ),
            center = center,
            radius = maxRadius * 0.6f
        )

        // Orbiting celestial particles
        val particles = 18
        for (i in 0 until particles) {
            val angle = (animProgress * 6.28f) + (i * (6.28f / particles))
            val dist = maxRadius * (0.3f + 0.6f * ((i % 5) / 5f))
            val x = center.x + cos(angle) * dist
            val y = center.y + sin(angle) * (dist * 0.65f)
            drawCircle(
                color = if (i % 2 == 0) NeonCyan else Color.White,
                center = Offset(x, y),
                radius = if (i % 3 == 0) 4f else 2.5f
            )
        }
    }
}

@Composable
private fun CyberChipScene(animProgress: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF061A2B), Color(0xFF0A2540), Color(0xFF040E18))
                )
            )
    ) {
        val center = Offset(size.width * 0.5f, size.height * 0.42f)
        val chipSize = size.width * 0.38f

        // Central silicon processor die
        drawRoundRect(
            color = CyberCardSurface,
            topLeft = Offset(center.x - chipSize / 2, center.y - chipSize / 2),
            size = Size(chipSize, chipSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = ElectricBlue,
            topLeft = Offset(center.x - chipSize / 2, center.y - chipSize / 2),
            size = Size(chipSize, chipSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
            style = Stroke(width = 3f)
        )

        // Copper circuit traces
        val traces = 8
        for (i in 0 until traces) {
            val offset = (i - 4) * 24f
            val traceAlpha = (0.3f + 0.6f * sin((animProgress * 6.28f) + i)).coerceIn(0.2f, 0.9f)
            val traceColor = NeonCyan.copy(alpha = traceAlpha)

            // Left bus
            drawLine(
                color = traceColor,
                start = Offset(0f, center.y + offset),
                end = Offset(center.x - chipSize / 2, center.y + offset),
                strokeWidth = 2.5f
            )
            // Right bus
            drawLine(
                color = traceColor,
                start = Offset(center.x + chipSize / 2, center.y + offset),
                end = Offset(size.width, center.y + offset),
                strokeWidth = 2.5f
            )
        }
    }
}

@Composable
private fun ComedyStageScene(animProgress: Float, pulse: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF3B0764), Color(0xFF1E1B4B), Color(0xFF0F172A))
                )
            )
    ) {
        val width = size.width
        val height = size.height

        // Dynamic theatre spotlight
        val spotX = width * (0.3f + 0.4f * sin(animProgress * 6.28f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(TerminalAmber.copy(alpha = 0.40f * pulse), Color.Transparent),
                center = Offset(spotX, height * 0.45f),
                radius = width * 0.5f
            ),
            center = Offset(spotX, height * 0.45f),
            radius = width * 0.5f
        )

        // Sound-wave laughter equalizer bars
        val bars = 16
        val barWidth = width * 0.035f
        val gap = width * 0.02f
        val startX = (width - ((bars * barWidth) + ((bars - 1) * gap))) / 2

        for (i in 0 until bars) {
            val barHeight = (height * 0.12f) * (0.3f + 0.7f * sin((animProgress * 12f) + i * 1.5f).coerceIn(0.1f, 1f))
            val x = startX + i * (barWidth + gap)
            val y = height * 0.60f - barHeight

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(TerminalRose, TerminalAmber)
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
        }
    }
}

@Composable
private fun ChefKitchenScene(animProgress: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF451A03), Color(0xFF78350F), Color(0xFF1C1917))
                )
            )
    ) {
        val center = Offset(size.width * 0.5f, size.height * 0.45f)
        val radius = size.width * 0.35f

        // Gourmet skillet glowing circle
        drawCircle(
            color = Color(0xFF292524),
            center = center,
            radius = radius
        )
        drawCircle(
            color = TerminalAmber.copy(alpha = 0.6f),
            center = center,
            radius = radius,
            style = Stroke(width = 4f)
        )

        // Sizzling flame sparks
        val sparks = 14
        for (i in 0 until sparks) {
            val angle = (animProgress * 6.28f) + (i * 2.1f)
            val dist = radius * (0.4f + 0.5f * sin((animProgress * 5f) + i))
            val sx = center.x + cos(angle) * dist
            val sy = center.y + sin(angle) * dist
            drawCircle(
                color = if (i % 2 == 0) TerminalAmber else TerminalRose,
                center = Offset(sx, sy),
                radius = 3.5f
            )
        }
    }
}

@Composable
private fun GamingArenaScene(animProgress: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF020617))
                )
            )
    ) {
        val width = size.width
        val height = size.height
        val horizonY = height * 0.55f

        // Cyberpunk retro perspective grid
        val lines = 10
        for (i in -lines..lines) {
            val bottomX = (width * 0.5f) + (i * (width * 0.12f))
            drawLine(
                color = NeonCyan.copy(alpha = 0.35f),
                start = Offset(width * 0.5f, horizonY),
                end = Offset(bottomX, height),
                strokeWidth = 2f
            )
        }

        val hLines = 6
        for (j in 1..hLines) {
            val progressY = (j.toFloat() / hLines)
            val y = horizonY + (height - horizonY) * (progressY * progressY)
            val animatedY = (y + (animProgress * 25f)) % (height - horizonY) + horizonY
            drawLine(
                color = ElectricBlue.copy(alpha = 0.4f),
                start = Offset(0f, animatedY),
                end = Offset(width, animatedY),
                strokeWidth = 1.5f
            )
        }
    }
}

@Composable
private fun AnimalJoyScene(animProgress: Float, pulse: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF022C22))
                )
            )
    ) {
        val width = size.width
        val height = size.height

        // Floating playful orbs/bubbles
        val bubbles = 8
        for (i in 0 until bubbles) {
            val baseY = height * (0.3f + 0.4f * (i.toFloat() / bubbles))
            val animatedY = (baseY - (animProgress * height * 0.3f)) % (height * 0.6f) + (height * 0.2f)
            val x = width * (0.2f + 0.6f * ((i * 37 % 100) / 100f))
            val r = (width * 0.06f) * (0.8f + 0.4f * sin(i.toFloat()))

            drawCircle(
                color = TerminalEmerald.copy(alpha = 0.25f),
                center = Offset(x, animatedY),
                radius = r
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                center = Offset(x, animatedY),
                radius = r,
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
private fun AbstractAiScene(animProgress: Float, pulse: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepMidnightNavy, ObsidianBg)
                )
            )
    ) {
        val center = Offset(size.width * 0.5f, size.height * 0.45f)
        val r = size.width * 0.35f * pulse

        // Concentric geometric rings
        drawCircle(
            color = NeonCyan.copy(alpha = 0.3f),
            center = center,
            radius = r,
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = ElectricBlue.copy(alpha = 0.25f),
            center = center,
            radius = r * 0.7f,
            style = Stroke(width = 2.5f)
        )
        drawCircle(
            color = TerminalEmerald.copy(alpha = 0.2f),
            center = center,
            radius = r * 0.4f,
            style = Stroke(width = 3f)
        )
    }
}
