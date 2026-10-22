package com.example.companion.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.companion.FloatingCompanionController
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
import com.example.ui.theme.TextSecondary

@Composable
fun CompactOverlayPopup(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentThought by FloatingCompanionController.currentThought.collectAsStateWithLifecycle()
    val isProcrastinating by FloatingCompanionController.isProcrastinating.collectAsStateWithLifecycle()
    val isReadingComments by FloatingCompanionController.isReadingComments.collectAsStateWithLifecycle()
    val lastReel by FloatingCompanionController.lastExtractedReel.collectAsStateWithLifecycle()
    val activeEmoji by FloatingCompanionController.activeEmojiReaction.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .widthIn(max = 300.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(CyberCardSurface, DeepMidnightNavy)
                )
            )
            .border(
                1.5.dp,
                if (isProcrastinating) TerminalAmber else if (isReadingComments) TerminalRose else NeonCyan.copy(alpha = 0.85f),
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
            .testTag("compact_overlay_popup")
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.companion_avatar),
                            contentDescription = "Companion Avatar",
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .border(1.dp, NeonCyan, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = activeEmoji ?: "✨",
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            text = "AI BREAK BOT",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = when {
                                isProcrastinating -> "QUIRK: ONE MORE REEL"
                                isReadingComments -> "SCANNING COMMENTS..."
                                lastReel != null -> "PERCEIVING: ${lastReel?.appName?.uppercase()}"
                                else -> "AUTONOMOUS REEL AGENT"
                            },
                            color = when {
                                isProcrastinating -> TerminalAmber
                                isReadingComments -> TerminalRose
                                else -> NeonCyan
                            },
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp).testTag("close_overlay_popup_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Real Screen Perception Tag (if detected)
            if (lastReel != null && (!lastReel?.caption.isNullOrBlank() || !lastReel?.creator.isNullOrBlank())) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalEmerald.copy(alpha = 0.12f))
                        .border(0.6.dp, TerminalEmerald.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "👀 ${lastReel?.getSummary()}",
                        color = TerminalEmerald,
                        fontSize = 9.sp,
                        maxLines = 1,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Internal Monologue Thought Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianBg.copy(alpha = 0.88f))
                    .border(0.8.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = currentThought,
                    color = if (isProcrastinating) TerminalAmber else TextPrimary,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 1: Swipe Next & Like (Double Tap)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { FloatingCompanionController.triggerHumanNextReel(context) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCardBorder.copy(alpha = 0.7f),
                        contentColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("popup_scroll_next_button")
                ) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SWIPE", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = { FloatingCompanionController.triggerHumanLikeReel(context) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TerminalRose.copy(alpha = 0.2f),
                        contentColor = TerminalRose
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("popup_like_reel_button")
                ) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LIKE ❤️", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Read Comments & Explain Reel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { FloatingCompanionController.triggerInspectAndReadComments(context) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TerminalAmber.copy(alpha = 0.2f),
                        contentColor = TerminalAmber
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("popup_read_comments_button")
                ) {
                    Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("COMMENTS 💬", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = { FloatingCompanionController.explainCurrentScreen() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TerminalEmerald.copy(alpha = 0.2f),
                        contentColor = TerminalEmerald
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("popup_explain_button")
                ) {
                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EXPLAIN 🧠", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 3: Draft Comment
            Button(
                onClick = { FloatingCompanionController.draftAndCopyComment(context) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue.copy(alpha = 0.15f),
                    contentColor = ElectricBlue
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                modifier = Modifier.fillMaxWidth().height(32.dp).testTag("popup_draft_comment_button")
            ) {
                Icon(imageVector = Icons.Default.Comment, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("DRAFT REPLY (COPY TO CLIPBOARD)", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stop Button with Procrastination Quirk
            Button(
                onClick = {
                    FloatingCompanionController.triggerStopWithProcrastination(context) {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TerminalAmber.copy(alpha = 0.18f),
                    contentColor = TerminalAmber
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(36.dp).testTag("popup_stop_button")
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("STOP BREAK (ONE MORE REEL!)", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
