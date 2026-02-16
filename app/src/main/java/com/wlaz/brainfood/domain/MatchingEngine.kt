package com.wlaz.brainfood.domain

import com.wlaz.brainfood.data.Ingredient
import com.wlaz.brainfood.data.IngredientDetail
import com.wlaz.brainfood.data.Recipe
import com.wlaz.brainfood.data.Substitution
import javax.inject.Inject

enum class MatchType {
    EXACT,      // 100% match
    ALMOST,     // > 80% or missing 1-2 items
    POSSIBLE,   // < 80% but has key items (future logic)
    NONE
}

data class MatchResult(
    val recipe: Recipe,
    val matchPercentage: Int,
    val ingredients: List<IngredientDetail>,
    val missingIngredients: List<Ingredient>,
    val warnings: List<String> = emptyList(),
    val substitutions: List<String> = emptyList()
)

class MatchingEngine @Inject constructor() {

    fun calculateMatch(
        recipe: Recipe,
        ingredients: List<IngredientDetail>,
        userInventoryIds: Set<Int>,
        availableSubstitutions: List<Substitution>,
        allIngredientsMap: Map<Int, Ingredient>
    ): MatchResult {

        var currentScore = 0.0
        var totalWeight = 0.0
        val missingList = mutableListOf<Ingredient>()
        val warnings = mutableListOf<String>()
        val activeSubstitutions = mutableListOf<String>()

        ingredients.forEach { detail ->
            totalWeight += 1.0

            if (userInventoryIds.contains(detail.ingredient.id)) {
                // User has this ingredient
                currentScore += 1.0
            } else {
                // User does NOT have this ingredient — check options

                // 1. Check substitutions first
                val sub = availableSubstitutions.firstOrNull { s ->
                    s.originalIngredientId == detail.ingredient.id &&
                    userInventoryIds.contains(s.substituteIngredientId)
                }

                if (sub != null) {
                    // Substitution found! Almost full credit
                    currentScore += 0.95
                    val subName = allIngredientsMap[sub.substituteIngredientId]?.name ?: "?"
                    activeSubstitutions.add(
                        "Usa $subName en vez de ${detail.ingredient.name}: ${sub.impactDescription}"
                    )
                } else if (detail.isOptional) {
                    // 2. Optional ingredient — minor penalty
                    currentScore += 0.9
                    val impactMsg = detail.impact?.let { ": $it" } ?: ""
                    warnings.add("Opcional: ${detail.ingredient.name}$impactMsg")
                } else {
                    // 3. Truly missing — no credit
                    missingList.add(detail.ingredient)
                }
            }
        }

        val percentage = if (totalWeight > 0) (currentScore * 100 / totalWeight).toInt() else 0

        return MatchResult(
            recipe = recipe,
            matchPercentage = percentage,
            ingredients = ingredients,
            missingIngredients = missingList,
            warnings = warnings,
            substitutions = activeSubstitutions
        )
    }
}
