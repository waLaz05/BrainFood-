package com.wlaz.brainfood

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wlaz.brainfood.ui.auth.LoginScreen
import com.wlaz.brainfood.ui.backpack.AddIngredientScreen
import com.wlaz.brainfood.ui.backpack.BackpackScreen
import com.wlaz.brainfood.ui.components.BrainFoodBackground
import com.wlaz.brainfood.ui.recipes.RecipeScreen
import com.wlaz.brainfood.ui.recipes.RecipeDetailScreen
import com.wlaz.brainfood.ui.recipes.PreparationScreen
import com.wlaz.brainfood.ui.profile.ProfileScreen
import com.wlaz.brainfood.ui.theme.BrainFoodGreen
import com.wlaz.brainfood.ui.theme.BrainFoodTheme
import com.wlaz.brainfood.ui.theme.TextHint

sealed class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Recipes : BottomTab(
        route = "recipes",
        label = "Recetas",
        selectedIcon = Icons.Filled.Restaurant,
        unselectedIcon = Icons.Outlined.Restaurant
    )
    object Backpack : BottomTab(
        route = "backpack",
        label = "Mochila",
        selectedIcon = Icons.Filled.Kitchen,
        unselectedIcon = Icons.Outlined.Kitchen
    )
    object Preparation : BottomTab(
        route = "preparation",
        label = "Preparación",
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook
    )
}

private val tabs = listOf(BottomTab.Recipes, BottomTab.Backpack, BottomTab.Preparation)

@Composable
fun BrainFoodAppNavigation() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("brainfood_prefs", Context.MODE_PRIVATE)
    val hasOnboarded = prefs.getBoolean("has_onboarded", false)
    val startRoute = if (hasOnboarded) "recipes" else "login"

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Ocultar BottomBar en pantallas de detalle/adición/login
    val showBottomBar = currentRoute in listOf("recipes", "backpack", "preparation")

    BrainFoodTheme {
        BrainFoodBackground {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    if (showBottomBar) {
                        NavigationBar(
                            containerColor = Color(0xFF0A0A0A).copy(alpha = 0.95f),
                            contentColor = BrainFoodGreen,
                            tonalElevation = 0.dp
                        ) {
                            tabs.forEach { tab ->
                                val isSelected = currentDestination?.hierarchy?.any {
                                    it.route == tab.route
                                } == true

                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BrainFoodGreen,
                                        selectedTextColor = BrainFoodGreen,
                                        unselectedIconColor = TextHint,
                                        unselectedTextColor = TextHint,
                                        indicatorColor = BrainFoodGreen.copy(alpha = 0.12f)
                                    )
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = startRoute,
                    modifier = Modifier.padding(paddingValues),
                    enterTransition = { 
                        // Default: Slide in from right (for details)
                        slideInHorizontally(initialOffsetX = { it }) + fadeIn()
                    },
                    exitTransition = { 
                        // Default: Scale down (depth effect)
                        scaleOut(targetScale = 0.92f) + fadeOut()
                    },
                    popEnterTransition = { 
                        // Default: Scale up (returning from depth)
                        scaleIn(initialScale = 0.92f) + fadeIn()
                    },
                    popExitTransition = { 
                        // Default: Slide out to right
                        slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                    }
                ) {
                    // ─── Login Screen ───
                    composable(
                        "login",
                        enterTransition = { fadeIn(tween(400)) },
                        exitTransition = { fadeOut(tween(300)) }
                    ) {
                        LoginScreen(
                            onContinueAsGuest = {
                                prefs.edit().putBoolean("has_onboarded", true).apply()
                                navController.navigate("recipes") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onLoginSuccess = {
                                prefs.edit().putBoolean("has_onboarded", true).apply()
                                navController.navigate("recipes") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Tab 1 — Recetas (Root)
                    composable(
                        "recipes",
                        enterTransition = { fadeIn(tween(300)) },
                        exitTransition = { 
                            if (targetState.destination.route?.startsWith("recipe/") == true) {
                                scaleOut(targetScale = 0.92f) + fadeOut()
                            } else {
                                fadeOut(tween(300)) // Crossfade between tabs
                            }
                        },
                        popEnterTransition = { 
                           if (initialState.destination.route?.startsWith("recipe/") == true) {
                               scaleIn(initialScale = 0.92f) + fadeIn()
                           } else {
                               fadeIn(tween(300))
                           }
                        }
                    ) {
                        RecipeScreen(
                            onNavigateToBackpack = {
                                navController.navigate("backpack") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onRecipeClick = { recipeId ->
                                navController.navigate("recipe/$recipeId")
                            },
                            onNavigateToProfile = {
                                navController.navigate("profile")
                            }
                        )
                    }

                    // Tab 2 — Mochila
                    composable(
                        "backpack",
                        enterTransition = { fadeIn(tween(300)) },
                        exitTransition = { fadeOut(tween(300)) }
                    ) {
                         BackpackScreen(
                             onNavigateToAdd = { navController.navigate("backpack/add") },
                             onNavigateToRecipes = {
                                 navController.navigate("recipes") {
                                     popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                     launchSingleTop = true
                                     restoreState = true
                                 }
                             }
                         )
                    }

                    // Modal: Add Ingredient (Slide Up)
                    composable(
                        "backpack/add",
                        enterTransition = { slideInVertically { it } + fadeIn() },
                        exitTransition = { slideOutVertically { it } + fadeOut() },
                        popExitTransition = { slideOutVertically { it } + fadeOut() }
                    ) {
                        AddIngredientScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Detalle de receta (Uses default Z-Axis defined in NavHost)
                    composable("recipe/{recipeId}") { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull() ?: 0
                        RecipeDetailScreen(
                            recipeId = recipeId,
                            onBack = { navController.popBackStack() },
                            onStartPreparation = { id ->
                                navController.navigate("preparation/$id")
                            }
                        )
                    }

                    // Tab 3 — Preparación (Root)
                    composable(
                        "preparation",
                        enterTransition = { fadeIn(tween(300)) },
                        exitTransition = { fadeOut(tween(300)) }
                    ) {
                        PreparationScreen(
                            recipeId = null,
                            onBack = { navController.popBackStack() },
                            onNavigateToRecipes = {
                                navController.navigate("recipes") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    // Preparación guiada (Detail flow)
                    composable("preparation/{recipeId}") { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull()
                        PreparationScreen(
                            recipeId = recipeId,
                            onBack = { navController.popBackStack() },
                            onNavigateToRecipes = {
                                navController.navigate("recipes") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    // Profile Screen
                    composable("profile") {
                        ProfileScreen(
                            onBack = { navController.popBackStack() },
                            onLogout = {
                                navController.navigate("recipes") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
