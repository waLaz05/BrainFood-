package com.wlaz.brainfood.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wlaz.brainfood.data.ChefEmotion
import com.wlaz.brainfood.data.ChefTip
import com.wlaz.brainfood.ui.theme.TextPrimary
import com.wlaz.brainfood.ui.theme.TextSecondary

@Composable
fun ChefWelcomeHeader(
    tip: ChefTip,
    modifier: Modifier = Modifier
) {
    // Emoji del chef basado en la emoción
    val chefEmoji = when (tip.emotion) {
        ChefEmotion.HAPPY -> "👨‍🍳"
        ChefEmotion.THINKING -> "🤔"
        ChefEmotion.SURPRISED -> "😲"
        ChefEmotion.NEUTRAL -> "🧑‍🍳"
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        isHighlighted = true
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del Chef — Círculo con emoji grande
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chefEmoji,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Texto del tip
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Chef BrainFood",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tip.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun GlassSpeechBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    val bubbleShape = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )

    Box(
        modifier = modifier
            .background(
                color = Color(0xFF1A1A1A).copy(alpha = 0.8f),
                shape = bubbleShape
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.03f)
                    )
                ),
                shape = bubbleShape
            )
            .padding(12.dp)
    ) {
        Text(
            text = text,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp
        )
    }
}
