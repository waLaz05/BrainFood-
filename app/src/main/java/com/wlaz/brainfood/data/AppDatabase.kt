package com.wlaz.brainfood.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Ingredient::class, 
        Recipe::class, 
        RecipeIngredient::class, 
        InventoryItem::class,
        Substitution::class,
        UserFavorite::class,
        ShoppingListItem::class
    ],
    version = 8, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun brainFoodDao(): BrainFoodDao
}
