package com.wlaz.brainfood.data

enum class ChefEmotion {
    HAPPY,
    THINKING,
    SURPRISED,
    NEUTRAL
}

data class ChefTip(
    val id: String,
    val text: String,
    val emotion: ChefEmotion = ChefEmotion.HAPPY,
    val condition: (List<Ingredient>) -> Boolean = { true } // Por defecto, siempre puede salir
)
