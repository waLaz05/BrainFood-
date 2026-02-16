package com.wlaz.brainfood.ui.recipes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wlaz.brainfood.ui.components.GlassCard
import com.wlaz.brainfood.ui.theme.BrainFoodGreen
import com.wlaz.brainfood.ui.theme.TextHint
import com.wlaz.brainfood.ui.theme.TextPrimary
import com.wlaz.brainfood.ui.theme.TextSecondary

import com.wlaz.brainfood.ui.components.CookingAction
import com.wlaz.brainfood.ui.components.CookingStepAnimation

@Composable
fun PreparationScreen(
    recipeId: Int?,
    onBack: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    if (recipeId == null) {
        // ──── Empty state: no recipe selected ────
        EmptyPreparationState(onNavigateToRecipes = onNavigateToRecipes)
        return
    }

    val allRecipes by viewModel.recommendedRecipes.collectAsState()
    val matchResult = allRecipes.find { it.recipe.id == recipeId }

    if (matchResult == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⏳", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text("Cargando receta...", color = TextSecondary)
            }
        }
        return
    }

    // Parse instructions into steps (Action + Text)
    val steps = remember(matchResult) {
        matchResult.recipe.instructions
            .split("\n")
            .map { it.trimStart() }
            .filter { it.isNotBlank() }
            .map { line ->
                // Extract tag [TAG]
                val tagRegex = Regex("^\\[(CHOP|COOK|BOIL|MIX|SERVE)]")
                val match = tagRegex.find(line)
                val action = when(match?.value) {
                    "[CHOP]" -> CookingAction.CHOP
                    "[COOK]" -> CookingAction.COOK
                    "[BOIL]" -> CookingAction.BOIL
                    "[MIX]" -> CookingAction.MIX
                    else -> CookingAction.NONE
                }
                
                val textWithoutTag = line.replace(tagRegex, "").trim()
                // Remove leading number+dot pattern like "1. "
                val finalText = textWithoutTag.replace(Regex("^\\d+\\.\\s*"), "")
                
                action to finalText
            }
    }

    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = steps.size
    val isLastStep = currentStep >= totalSteps - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary
                )
            }
            Column {
                Text(
                    text = "👨‍🍳 Preparación",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = matchResult.recipe.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Progress bar
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Paso ${currentStep + 1} de $totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = "⏱ ${matchResult.recipe.prepTimeMinutes} min total",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextHint
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (currentStep + 1).toFloat() / totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = BrainFoodGreen,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Step card with animation
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                        (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "stepAnimation"
            ) { step ->
                val (action, text) = steps.getOrElse(step) { CookingAction.NONE to "" }
                StepCard(
                    stepNumber = step + 1,
                    action = action,
                    instruction = text,
                    isLastStep = step == totalSteps - 1
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Substitutions/warnings for current step
        if (matchResult.substitutions.isNotEmpty() || matchResult.warnings.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    matchResult.substitutions.forEach { sub ->
                        Text(
                            text = "🔄 $sub",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFA726),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    matchResult.warnings.forEach { warning ->
                        Text(
                            text = "⚡ $warning",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextHint,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Previous
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Anterior", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Anterior")
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            // Next / Finish
            Button(
                onClick = {
                    if (isLastStep) {
                        // Done! go back
                        onBack()
                    } else {
                        currentStep++
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLastStep) Color(0xFF4CAF50) else Color.White,
                    contentColor = if (isLastStep) Color.White else Color.Black
                )
            ) {
                Text(
                    text = if (isLastStep) "🎉 ¡Listo!" else "Siguiente",
                    fontWeight = FontWeight.Bold
                )
                if (!isLastStep) {
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Siguiente", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: Int,
    action: CookingAction,
    instruction: String,
    isLastStep: Boolean
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isHighlighted = isLastStep
    ) {
        Column(
            modifier = Modifier
                .padding(28.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animation or Number
            if (action != CookingAction.NONE) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    CookingStepAnimation(
                        action = action,
                        modifier = Modifier.size(60.dp),
                        color = BrainFoodGreen
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "PASO $stepNumber",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextHint
                )
            } else {
                // Fallback to number badge if no animation
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLastStep) "🎉" else "$stepNumber",
                        fontSize = if (isLastStep) 28.sp else 24.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = instruction,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 30.sp
            )

            if (isLastStep) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "¡Último paso! Ya casi está listo 👨‍🍳",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextHint,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EmptyPreparationState(onNavigateToRecipes: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("👨‍🍳", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Preparación Guiada",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Selecciona una receta primero\npara ver la preparación paso a paso",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onNavigateToRecipes,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🍳 Buscar Recetas", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
