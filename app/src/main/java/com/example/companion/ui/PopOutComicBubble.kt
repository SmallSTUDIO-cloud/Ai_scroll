package com.example.companion.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.DeepMidnightNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalEmerald
import com.example.ui.theme.TerminalRose
import com.example.ui.theme.TextPrimary

data class ComicThought(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val emoji: String = "👀",
    val tag: String = "REEL BOT",
    val accentType: BubbleAccent = BubbleAccent.CYAN
)

enum class BubbleAccent {
    CYAN,
    ROSE,
    AMBER,
    EMERALD,
    BLUE
}

@Composable
fun PopOutComicBubble(
    thought: ComicThought?,
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && thought != null,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        if (thought != null) {
            val accentColor = when (thought.accentType) {
                BubbleAccent.CYAN -> NeonCyan
                BubbleAccent.ROSE -> TerminalRose
                BubbleAccent.AMBER -> TerminalAmber
                BubbleAccent.EMERALD -> TerminalEmerald
                BubbleAccent.BLUE -> ElectricBlue
            }

            val emojiScale by animateFloatAsState(
                targetValue = if (visible) 1.15f else 0.8f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
                label = "emoji_bounce"
            )

            Box(
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(CyberCardSurface, DeepMidnightNavy.copy(alpha = 0.96f))
                        )
                    )
                    .border(1.6.dp, accentColor, RoundedCornerShape(16.dp))
                    .clickable { onClick() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("popout_comic_bubble")
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bouncing expressive emoji
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .scale(emojiScale)
                                .background(ObsidianBg.copy(alpha = 0.8f), CircleShape)
                                .border(0.8.dp, accentColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = thought.emoji,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Category tag
                        Text(
                            text = thought.tag.uppercase(),
                            color = accentColor,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.padding(top = 4.dp))

                    // Thought text
                    Text(
                        text = thought.text,
                        color = TextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}
