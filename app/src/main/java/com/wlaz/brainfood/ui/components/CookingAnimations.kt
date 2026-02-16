package com.wlaz.brainfood.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

enum class CookingAction {
    CHOP, // Cutting, Chopping
    COOK, // Frying, Pan, Searing
    BOIL, // Boiling, Pot
    MIX,  // Mixing, Bowl
    NONE
}

@Composable
fun CookingStepAnimation(
    action: CookingAction,
    modifier: Modifier = Modifier.size(64.dp),
    color: Color = Color.White
) {
    Box(modifier = modifier) {
        when (action) {
            CookingAction.CHOP -> AnimatedChop(modifier, color)
            CookingAction.COOK -> AnimatedCook(modifier, color)
            CookingAction.BOIL -> AnimatedBoil(modifier, color)
            CookingAction.MIX -> AnimatedMix(modifier, color)
            CookingAction.NONE -> {}
        }
    }
}

@Composable
private fun AnimatedChop(modifier: Modifier, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "chop")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "knife_rotation"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Draw cutting board (static)
        drawRoundRect(
            color = color.copy(alpha = 0.5f),
            topLeft = Offset(w * 0.1f, h * 0.8f),
            size = Size(w * 0.8f, h * 0.1f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Draw Knife (animated)
        rotate(rotation, pivot = Offset(w * 0.8f, h * 0.4f)) {
            // Blade
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.8f, h * 0.4f) // Handle start
                    lineTo(w * 0.2f, h * 0.7f) // Tip
                    lineTo(w * 0.8f, h * 0.7f) // Heel
                    close()
                },
                color = color,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            // Handle
            drawLine(
                color = color,
                start = Offset(w * 0.8f, h * 0.4f),
                end = Offset(w * 0.9f, h * 0.3f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun AnimatedCook(modifier: Modifier, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "cook")
    val steamOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steam"
    )
    val steamAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steam_alpha"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Pan body
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.2f, h * 0.5f),
            size = Size(w * 0.6f, h * 0.3f),
            style = Stroke(width = 3.dp.toPx())
        )
        drawLine(
            color = color,
            start = Offset(w * 0.2f, h * 0.5f),
            end = Offset(w * 0.8f, h * 0.5f),
            strokeWidth = 3.dp.toPx()
        )
        // Handle
        drawLine(
            color = color,
            start = Offset(w * 0.8f, h * 0.55f),
            end = Offset(w * 0.95f, h * 0.45f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Sizzle particles / Steam
        val particleX = listOf(0.35f, 0.5f, 0.65f)
        particleX.forEachIndexed { index, xFactor ->
            val offsetLimit = if(index % 2 == 0) steamOffset else steamOffset * 0.8f
             drawLine(
                color = color.copy(alpha = steamAlpha),
                start = Offset(w * xFactor, h * 0.45f), // Just above pan
                end = Offset(w * xFactor, h * 0.45f + offsetLimit), // Moving up
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun AnimatedBoil(modifier: Modifier, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "boil")
    val bubbleY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble"
    )
    
     Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Pot
         drawRect(
            color = color,
            topLeft = Offset(w * 0.25f, h * 0.4f),
            size = Size(w * 0.5f, h * 0.4f),
            style = Stroke(width = 3.dp.toPx())
        )
        // Handles
        drawLine(
            color = color,
            start = Offset(w * 0.25f, h * 0.5f),
            end = Offset(w * 0.15f, h * 0.5f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.75f, h * 0.5f),
            end = Offset(w * 0.85f, h * 0.5f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Bubbles
        drawCircle(
            color = color.copy(alpha = if(bubbleY < -25f) 0f else 1f),
            radius = 3.dp.toPx(),
            center = Offset(w * 0.4f, h * 0.4f + bubbleY)
        )
        drawCircle(
            color = color.copy(alpha = if(bubbleY < -20f) 0f else 1f),
            radius = 2.dp.toPx(),
            center = Offset(w * 0.6f, h * 0.5f + bubbleY * 0.7f)
        )
    }
}

@Composable
private fun AnimatedMix(modifier: Modifier, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "mix")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spoon_angle"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Bowl
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.2f, h * 0.5f),
            size = Size(w * 0.6f, h * 0.3f),
            style = Stroke(width = 3.dp.toPx())
        )

        // Spoon (orbiting)
        // Calculate spoon head position
        val radius = w * 0.15f
        val cx = w * 0.5f
        val cy = h * 0.55f
        // Oval orbit physics approximation
        val spoonX = cx + radius * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat()
        val spoonY = cy + radius * 0.3f * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() 

        drawLine(
            color = color,
            start = Offset(spoonX, spoonY),
            end = Offset(spoonX + w * 0.2f, spoonY - h * 0.3f), // Handle pointing up-right
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = color,
            radius = 4.dp.toPx(),
            center = Offset(spoonX, spoonY),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
