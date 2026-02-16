package com.wlaz.brainfood.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.wlaz.brainfood.ui.theme.BrainFoodBlack

@Composable
fun BrainFoodBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0E0E0E), // Gris muy oscuro arriba
                        BrainFoodBlack,     // Negro puro abajo
                        Color(0xFF000000)
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        // Orbes decorativos de luz — blancos sutiles
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Orbe superior — blanco muy sutil
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    center = Offset(x = size.width * 0.2f, y = size.height * 0.05f),
                    radius = size.width * 0.7f
                ),
                radius = size.width * 0.7f,
                center = Offset(x = size.width * 0.2f, y = size.height * 0.05f)
            )

            // Orbe inferior derecho — blanco aún más sutil
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.02f),
                        Color.Transparent
                    ),
                    center = Offset(x = size.width * 0.9f, y = size.height * 0.8f),
                    radius = size.width * 0.5f
                ),
                radius = size.width * 0.5f,
                center = Offset(x = size.width * 0.9f, y = size.height * 0.8f)
            )
        }

        // Contenido
        content()
    }
}
