package com.wlaz.brainfood.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BrainFoodDao {
    // Ingredients
    @Query("SELECT * FROM ingredients ORDER BY category, name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIngredient(ingredient: Ingredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<Ingredient>): List<Long>

    // Inventory
    @Query("SELECT * FROM user_inventory")
    fun getUserInventory(): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToInventory(item: InventoryItem)

    @Query("DELETE FROM user_inventory WHERE ingredientId = :ingredientId")
    suspend fun removeFromInventory(ingredientId: Int)

    // Recipes
    @Query("SELECT * FROM recipes")
    fun getRecipesWithIngredients(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE mealType = :mealType OR mealType = 'Cualquiera'")
    fun getRecipesByMealType(mealType: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipe_ingredients")
    fun getAllRecipeIngredients(): Flow<List<RecipeIngredient>>

    // Substitutions
    @Query("SELECT * FROM substitutions")
    fun getAllSubstitutions(): Flow<List<Substitution>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubstitution(sub: Substitution)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(crossRef: RecipeIngredient)

    // Clear all data (for re-seeding without duplicates)
    @Query("DELETE FROM recipe_ingredients")
    suspend fun clearRecipeIngredients()

    @Query("DELETE FROM recipes")
    suspend fun clearRecipes()

    @Query("DELETE FROM substitutions")
    suspend fun clearSubstitutions()

    @Query("DELETE FROM ingredients")
    suspend fun clearIngredients()

    // Favorites
    @Query("SELECT * FROM user_favorites")
    fun getAllFavorites(): Flow<List<UserFavorite>>

    @Query("SELECT COUNT(*) FROM user_favorites WHERE recipeId = :recipeId")
    suspend fun isFavorite(recipeId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: UserFavorite)

    @Query("DELETE FROM user_favorites WHERE recipeId = :recipeId")
    suspend fun removeFavorite(recipeId: Int)

    // Shopping List
    @Query("SELECT * FROM shopping_list")
    fun getShoppingList(): Flow<List<ShoppingListItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToShoppingList(item: ShoppingListItem)
    
    @Query("UPDATE shopping_list SET isChecked = :status WHERE id = :itemId")
    suspend fun updateShoppingItemStatus(itemId: Int, status: Boolean)

    @Query("DELETE FROM shopping_list WHERE id = :id")
    suspend fun removeFromShoppingList(id: Int)

    @Query("DELETE FROM shopping_list WHERE isChecked = 1")
    suspend fun clearCheckedShoppingItems()

    // Sync clear methods (for cloud pull)
    @Query("DELETE FROM user_inventory")
    suspend fun clearInventory()

    @Query("DELETE FROM user_favorites")
    suspend fun clearFavorites()

    @Query("DELETE FROM shopping_list")
    suspend fun clearShoppingList()
}
