package com.wlaz.brainfood.ui.backpack

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wlaz.brainfood.data.ChefTip
import com.wlaz.brainfood.data.Ingredient
import com.wlaz.brainfood.ui.components.ChefWelcomeHeader
import com.wlaz.brainfood.ui.components.IngredientIcon
import com.wlaz.brainfood.ui.components.GlassCard
import com.wlaz.brainfood.ui.components.SlidingSelector
import com.wlaz.brainfood.ui.theme.BrainFoodGreen
import com.wlaz.brainfood.ui.theme.TextHint
import com.wlaz.brainfood.ui.theme.TextPrimary
import com.wlaz.brainfood.ui.theme.TextSecondary

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import com.wlaz.brainfood.data.repository.ShoppingListItemDetail

@Composable
fun BackpackScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToRecipes: () -> Unit = {},
    viewModel: BackpackViewModel = hiltViewModel()
) {
    val userInventory by viewModel.userInventory.collectAsState()
    val shoppingList by viewModel.shoppingList.collectAsState()
    val chefTip by viewModel.chefTip.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) } // 0: Despensa, 1: Shopping List

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ──── Tab Selector (Premium) ────
            SlidingSelector(
                options = listOf("🎒 Despensa", "🛒 Lista de Compras"),
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.padding(16.dp)
            )

            if (selectedTab == 0) {
                // Main backpack content
                BackpackContent(
                    userInventory = userInventory,
                    chefTip = chefTip,
                    onRemoveIngredient = { viewModel.removeFromInventory(it) }
                )
            } else {
                // Shopping List Content
                ShoppingListContent(
                    shoppingList = shoppingList,
                    onToggleItem = { id, checked -> viewModel.updateShoppingItemStatus(id, checked) },
                    onRemoveItem = { viewModel.removeFromShoppingList(it) },
                    onAnalyzeSort = { /* Optional: Sort logic */ }
                )
            }
        }

        // Bottom row: Buttons (Only visible in Despensa for now, or adapted)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
             if (selectedTab == 0) {
                 Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (userInventory.isNotEmpty()) {
                        androidx.compose.material3.Button(
                            onClick = onNavigateToRecipes,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("🍳 Buscar Recetas", style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        Spacer(Modifier)
                    }

                    FloatingActionButton(
                        onClick = onNavigateToAdd,
                        containerColor = BrainFoodGreen,
                        contentColor = Color.Black,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar ingrediente")
                    }
                }
             } else {
                 // Shopping List Actions (Clear Checked, Move to Pantry)
                 if (shoppingList.any { it.item.isChecked }) {
                     Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                     ) {
                         androidx.compose.material3.Button(
                             onClick = { viewModel.clearCheckedShoppingItems() },
                             colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                 containerColor = Color(0xFFFFEBEE),
                                 contentColor = Color(0xFFD32F2F)
                             ),
                             shape = RoundedCornerShape(16.dp)
                         ) {
                             Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                             Spacer(Modifier.width(8.dp))
                             Text("Limpiar", style = MaterialTheme.typography.labelLarge)
                         }

                         androidx.compose.material3.Button(
                             onClick = { viewModel.moveCheckedToInventory() },
                             colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                 containerColor = BrainFoodGreen,
                                 contentColor = Color.Black
                             ),
                             shape = RoundedCornerShape(16.dp)
                         ) {
                             Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                             Spacer(Modifier.width(8.dp))
                             Text("Mover a Despensa", style = MaterialTheme.typography.labelLarge)
                         }
                     }
                 }
             }
        }
    }
}


@Composable
private fun ShoppingListContent(
    shoppingList: List<ShoppingListItemDetail>,
    onToggleItem: (Int, Boolean) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onAnalyzeSort: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (shoppingList.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                     Column(
                        modifier = Modifier
                            .padding(40.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🛒", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "Lista vacía",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Agrega ingredientes faltantes desde las recetas para tenerlos siempre a la mano.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                             textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            items(shoppingList, key = { it.item.id }) { detail ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth().clickable { 
                        onToggleItem(detail.item.id, !detail.item.isChecked) 
                    },
                    isHighlighted = detail.item.isChecked
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox (Custom visual)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (detail.item.isChecked) BrainFoodGreen else Color.White.copy(alpha = 0.1f)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (detail.item.isChecked) BrainFoodGreen else TextHint,
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (detail.item.isChecked) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        IngredientIcon(
                            name = detail.ingredient.name,
                            size = 32.dp,
                            emojiSize = 24.sp
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = detail.ingredient.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (detail.item.isChecked) TextSecondary else TextPrimary,
                            textDecoration = if (detail.item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = { onRemoveItem(detail.item.id) }) {
                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = TextHint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackpackContent(
    userInventory: List<Ingredient>,
    chefTip: ChefTip,
    onRemoveIngredient: (Int) -> Unit
) { LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Mi Mochila",
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧑‍🍳", fontSize = 16.sp)
                    }
                }
                Text(
                    if (userInventory.isNotEmpty()) "${userInventory.size} ingredientes listos para cocinar"
                    else "Organiza tu cocina y descubre qué puedes preparar",
                    color = if (userInventory.isNotEmpty()) BrainFoodGreen else TextSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        letterSpacing = 0.2.sp
                    )
                )
            }
        }

        // Chef Tip
        item {
            ChefWelcomeHeader(tip = chefTip)
        }

        if (userInventory.isEmpty()) {
            // Empty state
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .padding(40.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎒", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "Tu mochila está vacía",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Toca el botón '+' para registrar lo que tienes en casa y recibir sugerencias inteligentes.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            // Group user's ingredients by category
            val grouped = userInventory.groupBy { it.category }

            grouped.forEach { (category, items) ->
                // Category header
                item(key = "header_$category") {
                    val dotColor = getCategoryColor(category)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Items
                items(items = items, key = { it.id }) { ingredient ->
                    // Calculate a rough global index for staggering to avoid complex state
                    val index = userInventory.indexOf(ingredient)
                    com.wlaz.brainfood.ui.components.StaggeredAnimation(index = index) {
                        InventoryItemCard(
                            ingredient = ingredient,
                            onRemove = { onRemoveIngredient(ingredient.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryItemCard(
    ingredient: Ingredient,
    onRemove: () -> Unit
) {
    val categoryColor = getCategoryColor(ingredient.category)

    GlassCard(modifier = Modifier.fillMaxWidth(), isHighlighted = true) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                    IngredientIcon(
                        name = ingredient.name,
                        size = 28.dp,
                        emojiSize = 20.sp
                    )
                }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(BrainFoodGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DISPONIBLE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = BrainFoodGreen
                    )
                }
            }

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Quitar",
                    tint = TextHint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
