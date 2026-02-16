package com.wlaz.brainfood.ui.backpack

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import com.wlaz.brainfood.ui.components.IngredientIcon
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wlaz.brainfood.data.Ingredient
import com.wlaz.brainfood.ui.components.GlassCard
import com.wlaz.brainfood.ui.theme.BrainFoodGreen
import com.wlaz.brainfood.ui.theme.BrainFoodGreenDark
import com.wlaz.brainfood.ui.theme.TextHint
import com.wlaz.brainfood.ui.theme.TextPrimary
import com.wlaz.brainfood.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientScreen(
    onBack: () -> Unit,
    viewModel: BackpackViewModel = hiltViewModel()
) {
    val allIngredients by viewModel.allIngredients.collectAsState()
    val inventoryIds by viewModel.inventoryIds.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) } // null = Todos

    // Filtrado combinado: búsqueda + categoría
    val filteredItems = remember(allIngredients, searchQuery, selectedCategory) {
        allIngredients.filter { ingredient ->
            val matchesSearch = searchQuery.isBlank() ||
                ingredient.name.contains(searchQuery, ignoreCase = true) ||
                ingredient.category.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null ||
                ingredient.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    val grouped = filteredItems.groupBy { it.category }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Agregar Ingredientes", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ──── Category Filter Chips (estilo PedidosYa/Tottus) ────
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Grid Catalog
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                grouped.forEach { (category, items) ->
                    // Header (spans full width)
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "${getCategoryEmoji(category)} $category",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    // Items
                    items(items, key = { it.id }) { ingredient ->
                        IngredientGridItem(
                            ingredient = ingredient,
                            isSelected = ingredient.id in inventoryIds,
                            onToggle = { viewModel.toggleIngredient(ingredient.id, it) }
                        )
                    }
                }
            }
        }
    }
}

// ──── Category Filter Chips ────────────────────

@Composable
private fun CategoryFilterRow(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // "Todos" chip
        CategoryChip(
            emoji = "✨",
            label = "Todos",
            isSelected = selectedCategory == null,
            onClick = { onCategorySelected(null) }
        )

        // Category chips
        INGREDIENT_CATEGORIES.forEach { category ->
            CategoryChip(
                emoji = getCategoryEmoji(category),
                label = category,
                isSelected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.06f),
        animationSpec = tween(200),
        label = "chipBg"
    )
    val textColor = if (isSelected) Color.Black else TextSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
    ) {
        // Círculo con emoji
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

// ──── Search Bar ───────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                cursorBrush = SolidColor(BrainFoodGreen),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            "Buscar (ej. Pollo, Arroz)...",
                            color = TextHint,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    innerTextField()
                }
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = TextHint, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ──── Ingredient Grid Item ─────────────────────

@Composable
fun IngredientGridItem(
    ingredient: Ingredient,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) BrainFoodGreenDark.copy(alpha = 0.5f) else Color.Transparent,
        animationSpec = tween(200),
        label = "gridBg"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onToggle(isSelected) },
        isHighlighted = isSelected
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(12.dp)
        ) {
            // Checkmark (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White else Color.White.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = TextHint,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.Center
            ) {
                // Emoji único o icono custom
                IngredientIcon(
                    name = ingredient.name,
                    size = 40.dp,
                    emojiSize = 32.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1
                )

                if (ingredient.isBasic) {
                    Text(
                        text = "Básico",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHint
                    )
                }
            }
        }
    }
}

// ─── Preview ───────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080808)
@Composable
private fun AddIngredientPreview() {
    val sampleIngredients = listOf(
        Ingredient(id = 1, name = "Pollo", category = "Proteínas"),
        Ingredient(id = 2, name = "Huevo", category = "Proteínas"),
        Ingredient(id = 3, name = "Carne Molida", category = "Proteínas"),
        Ingredient(id = 4, name = "Arroz", category = "Granos", isBasic = true),
        Ingredient(id = 5, name = "Pasta", category = "Granos"),
        Ingredient(id = 6, name = "Tomate", category = "Verduras"),
        Ingredient(id = 7, name = "Cebolla", category = "Verduras", isBasic = true),
        Ingredient(id = 8, name = "Ají Amarillo", category = "Condimentos"),
    )
    val selectedIds = setOf(1, 4, 7)

    com.wlaz.brainfood.ui.theme.BrainFoodTheme {
        com.wlaz.brainfood.ui.components.BrainFoodBackground {
            Column(Modifier.fillMaxSize()) {
                // Simulated category chips
                CategoryFilterRow(
                    selectedCategory = null,
                    onCategorySelected = {}
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Grid items
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(sampleIngredients, key = { it.id }) { ingredient ->
                        IngredientGridItem(
                            ingredient = ingredient,
                            isSelected = ingredient.id in selectedIds,
                            onToggle = {}
                        )
                    }
                }
            }
        }
    }
}
