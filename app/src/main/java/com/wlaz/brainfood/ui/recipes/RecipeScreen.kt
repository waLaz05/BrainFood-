package com.wlaz.brainfood.ui.recipes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wlaz.brainfood.domain.MatchResult
import com.wlaz.brainfood.ui.components.GlassCard
import com.wlaz.brainfood.ui.theme.TextHint
import com.wlaz.brainfood.ui.theme.TextPrimary
import com.wlaz.brainfood.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun RecipeScreen(
    onNavigateToBackpack: () -> Unit = {},
    onRecipeClick: (Int) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val allRecipes by viewModel.recommendedRecipes.collectAsState()
    val userInventory by viewModel.userInventory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsState()

    // Auto-detect meal type by time
    val autoMealType = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour in 5..10 -> "Desayuno"
            hour in 11..15 -> "Almuerzo"
            hour in 16..22 -> "Cena"
            else -> null
        }
    }
    var selectedMealType by remember { mutableStateOf(autoMealType) }

    // Filter recipes by meal type (ViewModel handles Search + Favorites)
    val filteredRecipes = remember(allRecipes, selectedMealType) {
        if (selectedMealType == null) allRecipes
        else allRecipes.filter { 
            it.recipe.mealType == selectedMealType || it.recipe.mealType == "Cualquiera"
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🍽️ Recetas",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNavigateToProfile) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        // Search Bar
        androidx.compose.material3.TextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            placeholder = { Text("Buscar recetas o ingredientes...", color = TextHint) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextHint) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.14f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                disabledContainerColor = Color.White.copy(alpha = 0.06f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Color(0xFF4CAF50)
            ),
            singleLine = true
        )

        if (userInventory.isEmpty()) {
            // ──── Estado vacío ────
            EmptyRecipeState(onNavigateToBackpack = onNavigateToBackpack)
        } else {
            // ──── Meal Type & Favorites Filter ────
            MealTypeFilterRow(
                selectedMealType = selectedMealType,
                showFavoritesOnly = showFavoritesOnly,
                onMealTypeSelected = { selectedMealType = it },
                onToggleFavorites = viewModel::toggleShowFavorites
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (filteredRecipes.isEmpty()) {
                // No matching recipes
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (showFavoritesOnly) "No tienes favoritos aún" else "No se encontraron recetas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // ──── Recipe List ────
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Show info about auto-detection (only if not searching/filtering favs)
                    item {
                        if (searchQuery.isEmpty() && !showFavoritesOnly && autoMealType != null && selectedMealType == autoMealType) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "⏰ Recomendado para ${autoMealType.lowercase()} " +
                                           "(basado en la hora actual)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextHint,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    items(filteredRecipes, key = { it.recipe.id }) { matchResult ->
                        val index = filteredRecipes.indexOf(matchResult)
                        com.wlaz.brainfood.ui.components.StaggeredAnimation(index = index) {
                            RecipeListItem(
                                matchResult = matchResult,
                                onClick = { onRecipeClick(matchResult.recipe.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ──── Meal Type Filter Row ──────────────────────

@Composable
private fun MealTypeFilterRow(
    selectedMealType: String?,
    showFavoritesOnly: Boolean,
    onMealTypeSelected: (String?) -> Unit,
    onToggleFavorites: (Boolean) -> Unit
) {
    data class MealChip(val emoji: String, val label: String, val type: String?, val isFav: Boolean = false)

    val chips = listOf(
        MealChip("❤️", "Favoritos", null, isFav = true),
        MealChip("✨", "Todas", null),
        MealChip("🌅", "Desayuno", "Desayuno"),
        MealChip("🌞", "Almuerzo", "Almuerzo"),
        MealChip("🌙", "Cena", "Cena")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            val isSelected = if (chip.isFav) showFavoritesOnly else (selectedMealType == chip.type && !showFavoritesOnly)
            
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color(0xFF1E1E1E), // Darker gray for unselected
                animationSpec = tween(200), label = "mealChipBg"
            )
            val textColor = if (isSelected) Color.Black else Color.White
            val borderColor = if (isSelected) Color.White else Color(0xFF333333)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bgColor)
                    .clickable { 
                        if (chip.isFav) {
                            onToggleFavorites(!showFavoritesOnly)
                        } else {
                            if (showFavoritesOnly) onToggleFavorites(false)
                            onMealTypeSelected(chip.type) 
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(text = chip.emoji, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = chip.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ──── Empty State ────────────────────────────────

@Composable
private fun EmptyRecipeState(onNavigateToBackpack: () -> Unit) {
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
                text = "¡Hola! Soy Chef BrainFood",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Agrega ingredientes a tu mochila\npara que pueda recomendarte recetas",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onNavigateToBackpack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🎒 Ir a la Mochila", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ──── Recipe List Item ───────────────────────────

@Composable
private fun RecipeListItem(
    matchResult: MatchResult,
    onClick: () -> Unit
) {
    val recipe = matchResult.recipe
    val matchPct = matchResult.matchPercentage

    // Match color
    val matchColor = when {
        matchPct >= 90 -> Color(0xFF4CAF50)
        matchPct >= 70 -> Color(0xFFFFA726)
        else -> Color(0xFFEF5350)
    }

    // Meal type emoji
    val mealEmoji = when (recipe.mealType) {
        "Desayuno" -> "🌅"
        "Almuerzo" -> "🌞"
        "Cena" -> "🌙"
        else -> "🍽️"
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        isHighlighted = matchPct >= 90
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Match badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(matchColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$matchPct%",
                    style = MaterialTheme.typography.labelLarge,
                    color = matchColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = mealEmoji, fontSize = 16.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⏱ ${recipe.prepTimeMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHint
                    )
                    if (matchResult.missingIngredients.isNotEmpty()) {
                        Text(
                            text = "❌ Faltan ${matchResult.missingIngredients.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFEF5350)
                        )
                    }
                    if (matchResult.substitutions.isNotEmpty()) {
                        Text(
                            text = "🔄 ${matchResult.substitutions.size} sustitución",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFA726)
                        )
                    }
                }
            }
        }
    }
}
