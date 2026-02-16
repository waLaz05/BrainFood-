package com.wlaz.brainfood.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlaz.brainfood.data.Ingredient
import com.wlaz.brainfood.data.repository.BrainFoodRepository
import com.wlaz.brainfood.domain.MatchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repository: BrainFoodRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly = _showFavoritesOnly.asStateFlow()

    val favoriteRecipeIds = repository.favoriteRecipeIds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    private val _recommendedRecipesOriginal = repository.getRecommendedRecipes()

    val recommendedRecipes: StateFlow<List<MatchResult>> = combine(
        _recommendedRecipesOriginal,
        _searchQuery,
        _showFavoritesOnly,
        favoriteRecipeIds
    ) { recipes, query, showFavs, favIds ->
        var result = recipes
        
        // Filter by favorites
        if (showFavs) {
            result = result.filter { it.recipe.id in favIds }
        }

        // Filter by search
        if (query.isNotBlank()) {
            result = result.filter { match ->
                match.recipe.name.contains(query, ignoreCase = true) ||
                match.ingredients.any { ingDetail -> 
                    ingDetail.ingredient.name.contains(query, ignoreCase = true) 
                }
             }
        }
        
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userInventory: StateFlow<List<Ingredient>> = repository.getUserInventory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleShowFavorites(show: Boolean) {
        _showFavoritesOnly.value = show
    }

    fun toggleFavorite(recipeId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(recipeId)
        }
    }

    fun addMissingIngredientsToShoppingList(ingredients: List<Ingredient>) {
        viewModelScope.launch {
            ingredients.forEach { repository.addToShoppingList(it.id) }
        }
    }
}
