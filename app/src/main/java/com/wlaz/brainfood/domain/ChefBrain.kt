package com.wlaz.brainfood.domain

import com.wlaz.brainfood.data.ChefEmotion
import com.wlaz.brainfood.data.ChefTip
import com.wlaz.brainfood.data.Ingredient
import kotlin.random.Random

object ChefBrain {

    private val allTips = listOf(
        ChefTip(
            id = "empty_backpack",
            text = "¡Tu mochila está muy vacía! ¿Vamos al mercado?",
            emotion = ChefEmotion.SURPRISED,
            condition = { it.isEmpty() }
        ),
        ChefTip(
            id = "chicken_lover",
            text = "Veo que tienes pollo. ¡El pollo al limón es un clásico!",
            emotion = ChefEmotion.HAPPY,
            condition = { list -> list.any { it.name.contains("Pollo", ignoreCase = true) } }
        ),
        ChefTip(
            id = "garlic_fan",
            text = "El ajo es el secreto de la vida. ¡Nunca es suficiente!",
            emotion = ChefEmotion.HAPPY,
            condition = { list -> list.any { it.name.contains("Ajo", ignoreCase = true) } }
        ),
        ChefTip(
            id = "generic_1",
            text = "¿Qué cocinaremos hoy? ¡Estoy emocionado!",
            emotion = ChefEmotion.HAPPY
        ),
        ChefTip(
            id = "generic_2",
            text = "Recuerda: la creatividad es el mejor ingrediente.",
            emotion = ChefEmotion.THINKING
        ),
        ChefTip(
            id = "lot_of_items",
            text = "¡Wow! Cuantas cosas. Hoy sale banquete seguro.",
            emotion = ChefEmotion.SURPRISED,
            condition = { it.size > 5 }
        )
    )

    fun getTipForInventory(inventory: List<Ingredient>): ChefTip {
        // 1. Filtrar tips que cumplan la condición actual
        val candidates = allTips.filter { it.condition(inventory) }
        
        // 2. Si no hay candidatos (raro), devolver uno genérico
        if (candidates.isEmpty()) return allTips.first { it.id == "generic_1" }
        
        // 3. Elegir uno al azar
        return candidates.random()
    }
}
