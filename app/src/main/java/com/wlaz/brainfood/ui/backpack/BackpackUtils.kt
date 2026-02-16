package com.wlaz.brainfood.ui.backpack

import androidx.compose.ui.graphics.Color
import com.wlaz.brainfood.ui.theme.CategoryDairy
import com.wlaz.brainfood.ui.theme.CategoryGrain
import com.wlaz.brainfood.ui.theme.CategoryOther
import com.wlaz.brainfood.ui.theme.CategoryProtein
import com.wlaz.brainfood.ui.theme.CategorySpice
import com.wlaz.brainfood.ui.theme.CategoryVeggie

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "proteínas", "proteinas", "proteins" -> CategoryProtein
        "verduras", "vegetales", "veggies", "frutas y verduras" -> CategoryVeggie
        "granos", "cereales", "grains" -> CategoryGrain
        "lácteos", "lacteos", "dairy" -> CategoryDairy
        "condimentos", "especias", "spices" -> CategorySpice
        "hierbas" -> CategorySpice
        else -> CategoryOther
    }
}

/** Emoji de categoría — para los filter chips */
fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "proteínas", "proteinas", "proteins" -> "🍖"
        "verduras", "vegetales", "veggies", "frutas y verduras" -> "🥬"
        "granos", "cereales", "grains" -> "🌾"
        "lácteos", "lacteos", "dairy" -> "🧀"
        "condimentos", "especias", "spices" -> "🧂"
        "hierbas" -> "🌿"
        else -> "🥘"
    }
}

/** Emoji individual por ingrediente — cada uno tiene su propio icono */
fun getIngredientEmoji(ingredientName: String): String {
    return when (ingredientName.lowercase()) {
        // Proteínas
        "pollo" -> "🍗"
        "huevo" -> "🥚"
        "carne molida" -> "🥩"
        "atún en lata", "atún", "atun" -> "🐟"
        "salchicha" -> "🌭"
        "cerdo" -> "🥩"
        "pescado" -> "🐠"
        "camarón", "camarones" -> "🦐"
        "tocino" -> "🥓"
        "lomo de res", "lomo" -> "🥩"
        // Granos y Legumbres
        "arroz" -> "🍚"
        "pasta" -> "🍝"
        "pan" -> "🍞"
        "avena" -> "🥣"
        "quinoa", "quinua" -> "🌾"
        "fideos" -> "🍜"
        "harina" -> "🌾"
        "lenteja", "lentejas" -> "🫘"
        "frijol", "frijoles", "frejol", "frejoles" -> "🫘"
        "garbanzo", "garbanzos" -> "🫘"
        "pallar", "pallares" -> "🫘"
        "haba", "habas" -> "🫛"
        "arveja", "arvejas" -> "🫛"
        // Verduras y Frutas
        "tomate" -> "🍅"
        "cebolla" -> "🧅"
        "zanahoria" -> "🥕"
        "pimiento" -> "🫑"
        "papa" -> "🥔"
        "ajo" -> "🧄"
        "lechuga" -> "🥬"
        "brócoli", "brocoli" -> "🥦"
        "pepino" -> "🥒"
        "maíz", "choclo" -> "🌽"
        "espinaca" -> "🥬"
        "camote" -> "🍠"
        "cebolla roja" -> "🧅"
        "champiñón", "champiñones" -> "🍄"
        "palta", "aguacate" -> "🥑"
        "plátano", "banana" -> "🍌"
        "manzana" -> "🍎"
        "naranja" -> "🍊"
        "fresa" -> "🍓"
        // Lácteos
        "leche" -> "🥛"
        "queso" -> "🧀"
        "mantequilla" -> "🧈"
        "yogurt" -> "🥛"
        "crema", "crema de leche" -> "🥛"
        "leche evaporada" -> "🥫"
        // Condimentos
        "sal" -> "🧂"
        "aceite" -> "🫒"
        "sillao" -> "🥫"
        "limón" -> "🍋"
        "lima" -> "🍋"
        "vinagre" -> "🫙"
        "azúcar", "azucar" -> "🍬"
        "pimienta" -> "🫚"
        "comino" -> "🫙"
        "ketchup" -> "🍅"
        "mayonesa" -> "🥄"
        "mostaza" -> "🟡"
        "ají amarillo" -> "🌶️"
        "ají panca" -> "🌶️"
        "rocoto" -> "🌶️"
        "aceituna", "aceitunas" -> "🫒"
        // Hierbas
        "cilantro" -> "🌿"
        "perejil" -> "🌿"
        "orégano", "oregano" -> "🍃"
        "romero" -> "🌱"
        "albahaca" -> "🌿"
        "culantro" -> "🌿"
        "huacatay" -> "🍃"
        // Fallback
        else -> "🥘"
    }
}

/** Categorías disponibles para los filter chips */
val INGREDIENT_CATEGORIES = listOf(
    "Proteínas",
    "Granos",
    "Verduras",
    "Lácteos",
    "Condimentos",
    "Hierbas"
)
