package com.wlaz.brainfood.ui.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wlaz.brainfood.data.Ingredient
import com.wlaz.brainfood.ui.backpack.getIngredientEmoji
import com.wlaz.brainfood.ui.components.GlassCard
import com.wlaz.brainfood.ui.components.IngredientIcon
import com.wlaz.brainfood.ui.theme.TextHint
import com.wlaz.brainfood.ui.theme.TextPrimary
import com.wlaz.brainfood.ui.theme.TextSecondary

@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    onBack: () -> Unit,
    onStartPreparation: (Int) -> Unit,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val allRecipes by viewModel.recommendedRecipes.collectAsState()
    val userInventory by viewModel.userInventory.collectAsState()
    val favoriteRecipeIds by viewModel.favoriteRecipeIds.collectAsState()
    
    val inventoryIds = remember(userInventory) { userInventory.map { it.id }.toSet() }
    val matchResult = allRecipes.find { it.recipe.id == recipeId }

    if (matchResult == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔍", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text("Receta no encontrada", color = TextSecondary)
            }
        }
        return
    }

    val recipe = matchResult.recipe
    val isFavorite = favoriteRecipeIds.contains(recipe.id)
    
    val mealEmoji = when (recipe.mealType) {
        "Desayuno" -> "🌅"
        "Almuerzo" -> "🌞"
        "Cena" -> "🌙"
        else -> "🍽️"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header with back button
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, end = 16.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Detalle de Receta",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                com.wlaz.brainfood.ui.components.PulsatingHeart(
                    isFavorite = isFavorite,
                    onToggle = { viewModel.toggleFavorite(recipe.id) }
                ) { isFav ->
                    Icon(
                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFav) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (isFav) Color(0xFFE91E63) else TextSecondary
                    )
                }
            }
        }

        // Recipe title + info
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                isHighlighted = matchResult.matchPercentage >= 90
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = mealEmoji, fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = recipe.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = recipe.mealType,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextHint
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoChip("⏱", "${recipe.prepTimeMinutes} min")
                        InfoChip("📊", "${matchResult.matchPercentage}% match")
                        if (matchResult.missingIngredients.isNotEmpty()) {
                            InfoChip("❌", "${matchResult.missingIngredients.size} faltantes")
                        }
                    }
                }
            }
        }

        // Ingredients section
        item {
            Text(
                text = "📋 Ingredientes",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        items(matchResult.ingredients) { detail ->
            val hasIngredient = inventoryIds.contains(detail.ingredient.id)
            val emoji = getIngredientEmoji(detail.ingredient.name)
            val statusIcon = when {
                hasIngredient -> "✅"
                detail.isOptional -> "⚡"
                else -> "❌"
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IngredientIcon(
                        name = detail.ingredient.name,
                        size = 32.dp,
                        emojiSize = 24.sp
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = detail.ingredient.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = detail.quantity,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextHint
                        )
                        if (detail.isOptional) {
                            Text(
                                text = "Opcional${detail.impact?.let { ": $it" } ?: ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFA726)
                            )
                        }
                    }
                    Text(text = statusIcon, fontSize = 18.sp)
                }
            }
        }

        if (matchResult.missingIngredients.isNotEmpty()) {
            item {
                Button(
                    onClick = { viewModel.addMissingIngredientsToShoppingList(matchResult.missingIngredients) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFF3E0),
                        contentColor = Color(0xFFEF6C00)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🛒 Agregar ${matchResult.missingIngredients.size} faltantes a lista",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Substitutions
        if (matchResult.substitutions.isNotEmpty()) {
            item {
                Text(
                    text = "🔄 Sustituciones disponibles",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
                )
            }
            items(matchResult.substitutions) { sub ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "🔄 $sub",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFA726),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Warnings
        if (matchResult.warnings.isNotEmpty()) {
            item {
                Text(
                    text = "⚠️ Avisos",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
                )
            }
            items(matchResult.warnings) { warning ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "⚡ $warning",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextHint,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Prepare button
        item {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onStartPreparation(recipe.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = "👨‍🍳 ¡Preparar esta receta!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InfoChip(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
