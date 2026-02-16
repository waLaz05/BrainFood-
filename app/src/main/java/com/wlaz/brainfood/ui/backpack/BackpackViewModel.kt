package com.wlaz.brainfood.ui.backpack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlaz.brainfood.data.Ingredient
import com.wlaz.brainfood.data.InventoryItem
import com.wlaz.brainfood.data.repository.BrainFoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackpackViewModel @Inject constructor(
    private val repository: BrainFoodRepository
) : ViewModel() {

    /** User's inventory — only ingredients they've added */
    val userInventory: StateFlow<List<Ingredient>> = repository.getUserInventory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** IDs in inventory — for checking in picker */
    val inventoryIds: StateFlow<Set<Int>> = repository.getInventoryIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    /** Full catalog — used only in the picker */
    val allIngredients: StateFlow<List<Ingredient>> = repository.allIngredients
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Shopping List */
    val shoppingList = repository.getShoppingList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Chef tip reactive to inventory changes */
    val chefTip = repository.getUserInventory().map { userIngredients ->
        com.wlaz.brainfood.domain.ChefBrain.getTipForInventory(userIngredients)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = com.wlaz.brainfood.domain.ChefBrain.getTipForInventory(emptyList())
    )

    /** Toggle an ingredient in/out of user's inventory */
    fun toggleIngredient(ingredientId: Int, isCurrentlyInInventory: Boolean) {
        viewModelScope.launch {
            repository.toggleInventory(ingredientId, isCurrentlyInInventory)
        }
    }

    /** Add ingredient to user's inventory */
    fun addToInventory(ingredientId: Int) {
        viewModelScope.launch {
            repository.addToInventory(InventoryItem(ingredientId = ingredientId))
        }
    }

    /** Remove ingredient from user's inventory */
    fun removeFromInventory(ingredientId: Int) {
        viewModelScope.launch {
            repository.removeFromInventory(ingredientId)
        }
    }

    // Shopping List Actions
    fun limitShoppingListAction(action: () -> Unit) {
        viewModelScope.launch { action() }
    }

    fun updateShoppingItemStatus(id: Int, isChecked: Boolean) {
        viewModelScope.launch {
            repository.updateShoppingItemStatus(id, isChecked)
        }
    }

    fun removeFromShoppingList(id: Int) {
        viewModelScope.launch {
            repository.removeFromShoppingList(id)
        }
    }

    fun clearCheckedShoppingItems() {
        viewModelScope.launch {
            repository.clearCheckedShoppingItems()
        }
    }

    /** Move checked items to inventory */
    fun moveCheckedToInventory() {
        viewModelScope.launch {
            val checkedItems = shoppingList.value.filter { it.item.isChecked }
            checkedItems.forEach { detail ->
                repository.addToInventory(InventoryItem(ingredientId = detail.ingredient.id))
                repository.removeFromShoppingList(detail.item.id)
            }
        }
    }
}
