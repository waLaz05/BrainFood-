package com.wlaz.brainfood.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tarjeta de vidrio premium monocromática.
 * Fondo negro semitransparente con bordes blancos sutiles.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    isHighlighted: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    // Fondo negro semitransparente
    val bgColor = if (isHighlighted) {
        Color(0xFF1A1A1A).copy(alpha = 0.85f)
    } else {
        Color(0xFF141414).copy(alpha = 0.70f)
    }

    // Borde sutil — blanco para highlighted, gris para normal
    val borderBrush = Brush.verticalGradient(
        colors = if (isHighlighted) {
            listOf(
                Color.White.copy(alpha = 0.25f),
                Color.White.copy(alpha = 0.05f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.02f)
            )
        }
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor, shape)
            .border(BorderStroke(1.dp, borderBrush), shape)
    ) {
        content()
    }
}

/**
 * Chip/Badge estilizado para categorías y tags
 */
@Composable
fun GlassChip(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.10f),
    borderColor: Color = Color.White.copy(alpha = 0.20f),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .background(backgroundColor, shape)
            .border(1.dp, borderColor, shape)
            .clip(shape)
    ) {
        content()
    }
}
