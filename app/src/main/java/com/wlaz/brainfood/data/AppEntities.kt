package com.wlaz.brainfood.data

import androidx.room.Embedded
import androidx.room.Entity // Keep existing
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.Ignore // Added for debug

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // e.g., "Proteins", "Veggies"
    val isBasic: Boolean = false, // True if likely owned (Salt, Oil)
    val imageUrl: String? = null
)

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val instructions: String, // Text blob or JSON
    val imageUrl: String? = null,
    val prepTimeMinutes: Int,
    val mealType: String = "Cualquiera" // "Desayuno", "Almuerzo", "Cena", "Cualquiera"
)

data class RecipeWithIngredients(
    val recipe: Recipe,
    val ingredients: List<Ingredient> = emptyList()
)

@Entity(
    tableName = "substitutions",
    foreignKeys = [
        ForeignKey(entity = Ingredient::class, parentColumns = ["id"], childColumns = ["originalIngredientId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Ingredient::class, parentColumns = ["id"], childColumns = ["substituteIngredientId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("originalIngredientId"),
        Index("substituteIngredientId")
    ]
)
data class Substitution(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalIngredientId: Int,
    val substituteIngredientId: Int,
    val impactDescription: String // Ej: "Sabor más ácido"
)

@Entity(
    tableName = "recipe_ingredients",
    primaryKeys = ["recipeId", "ingredientId"],
    foreignKeys = [
        ForeignKey(entity = Recipe::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Ingredient::class, parentColumns = ["id"], childColumns = ["ingredientId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("ingredientId")]
)
data class RecipeIngredient(
    val recipeId: Int,
    val ingredientId: Int,
    val quantity: String, // e.g., "200g", "1 cup"
    val isOptional: Boolean = false,
    val impact: String? = null
)

// Helper class for UI/Engine to hold Ingredient + Metadata
data class IngredientDetail(
    val ingredient: Ingredient,
    val quantity: String,
    val isOptional: Boolean,
    val impact: String?
)

@Entity(tableName = "user_inventory")
data class InventoryItem(
    @PrimaryKey val ingredientId: Int, // Maps to Ingredient.id
    val addedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_favorites")
data class UserFavorite(
    @PrimaryKey
    val recipeId: Int
)

@Entity(
    tableName = "shopping_list",
    foreignKeys = [
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ingredientId")]
)
data class ShoppingListItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ingredientId: Int,
    val isChecked: Boolean = false
)
