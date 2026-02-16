package com.wlaz.brainfood.data.repository

import com.wlaz.brainfood.data.BrainFoodDao
import com.wlaz.brainfood.data.Ingredient
import com.wlaz.brainfood.data.IngredientDetail
import com.wlaz.brainfood.data.InventoryItem
import com.wlaz.brainfood.data.Recipe
import com.wlaz.brainfood.data.RecipeIngredient
import com.wlaz.brainfood.data.RecipeWithIngredients
import com.wlaz.brainfood.data.Substitution
import com.wlaz.brainfood.domain.MatchResult
import com.wlaz.brainfood.domain.MatchingEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

import com.wlaz.brainfood.data.ShoppingListItem
import com.wlaz.brainfood.data.UserFavorite
import com.wlaz.brainfood.data.sync.SyncManager

data class ShoppingListItemDetail(
    val item: ShoppingListItem,
    val ingredient: Ingredient
)

@Singleton
class BrainFoodRepository @Inject constructor(
    private val dao: BrainFoodDao,
    private val matchingEngine: MatchingEngine,
    val syncManager: SyncManager
) {

    val allIngredients: Flow<List<Ingredient>> = dao.getAllIngredients()

    // ════════════ FAVOURITES ════════════

    val favoriteRecipeIds: Flow<Set<Int>> = dao.getAllFavorites().map { list -> 
        list.map { it.recipeId }.toSet() 
    }

    suspend fun toggleFavorite(recipeId: Int) {
        val isFav = dao.isFavorite(recipeId) > 0
        if (isFav) {
            dao.removeFavorite(recipeId)
        } else {
            dao.addFavorite(UserFavorite(recipeId))
        }
        syncManager.scheduleSyncAfterIdle()
    }

    // ════════════ SHOPPING LIST ════════════

    fun getShoppingList(): Flow<List<ShoppingListItemDetail>> {
        return combine(
            dao.getShoppingList(),
            dao.getAllIngredients()
        ) { items, allIngredients ->
            val ingredientMap = allIngredients.associateBy { it.id }
            items.mapNotNull { item ->
                ingredientMap[item.ingredientId]?.let { ingredient ->
                    ShoppingListItemDetail(item, ingredient)
                }
            }
        }
    }

    suspend fun addToShoppingList(ingredientId: Int) {
        dao.addToShoppingList(ShoppingListItem(ingredientId = ingredientId))
        syncManager.scheduleSyncAfterIdle()
    }

    suspend fun removeFromShoppingList(id: Int) {
        dao.removeFromShoppingList(id)
        syncManager.scheduleSyncAfterIdle()
    }

    suspend fun updateShoppingItemStatus(id: Int, isChecked: Boolean) {
        dao.updateShoppingItemStatus(itemId = id, status = isChecked)
        syncManager.scheduleSyncAfterIdle()
    }

    suspend fun clearCheckedShoppingItems() {
        dao.clearCheckedShoppingItems()
        syncManager.scheduleSyncAfterIdle()
    }

    // ════════════ INVENTORY ════════════

    fun getUserInventory(): Flow<List<Ingredient>> {
        return combine(
            dao.getUserInventory(),
            dao.getAllIngredients()
        ) { inventoryItems, allIngredients ->
             val inventoryIds = inventoryItems.map { it.ingredientId }.toSet()
             allIngredients.filter { it.id in inventoryIds }
        }
    }

    fun getInventoryIds(): Flow<Set<Int>> {
        return dao.getUserInventory().map { items ->
            items.map { it.ingredientId }.toSet()
        }
    }

    suspend fun addToInventory(item: InventoryItem) {
        dao.addToInventory(item)
        syncManager.scheduleSyncAfterIdle()
    }

    suspend fun removeFromInventory(ingredientId: Int) {
        dao.removeFromInventory(ingredientId)
        syncManager.scheduleSyncAfterIdle()
    }

    suspend fun toggleInventory(ingredientId: Int, isCurrentlyInInventory: Boolean) {
        if (isCurrentlyInInventory) {
            dao.removeFromInventory(ingredientId)
        } else {
            dao.addToInventory(InventoryItem(ingredientId = ingredientId))
        }
        syncManager.scheduleSyncAfterIdle()
    }

    // ═══════════════════════════════════════════════
    // SEED DATA — Clear + Insert (sin duplicados)
    // ═══════════════════════════════════════════════

    suspend fun loadDemoData() {
        // Limpiar tablas primero para evitar duplicados
        dao.clearRecipeIngredients()
        dao.clearSubstitutions()
        dao.clearRecipes()
        dao.clearIngredients()

        // ═══ CATÁLOGO DE INGREDIENTES (52 únicos) ═══
        val ingredients = listOf(
            // Proteínas (10)
            Ingredient(name = "Pollo", category = "Proteínas"),
            Ingredient(name = "Huevo", category = "Proteínas"),
            Ingredient(name = "Carne Molida", category = "Proteínas"),
            Ingredient(name = "Atún en Lata", category = "Proteínas"),
            Ingredient(name = "Salchicha", category = "Proteínas"),
            Ingredient(name = "Hot Dog", category = "Proteínas"),
            Ingredient(name = "Lomo de Res", category = "Proteínas"),
            Ingredient(name = "Pescado", category = "Proteínas"),
            Ingredient(name = "Camarón", category = "Proteínas"),
            Ingredient(name = "Cerdo", category = "Proteínas"),
            Ingredient(name = "Tocino", category = "Proteínas"),
            // Granos (7)
            Ingredient(name = "Arroz", category = "Granos", isBasic = true),
            Ingredient(name = "Pasta", category = "Granos"),
            Ingredient(name = "Pan", category = "Granos"),
            Ingredient(name = "Avena", category = "Granos"),
            Ingredient(name = "Quinua", category = "Granos"),
            Ingredient(name = "Fideos", category = "Granos"),
            Ingredient(name = "Harina", category = "Granos", isBasic = true),
            // Verduras (14)
            Ingredient(name = "Tomate", category = "Verduras"),
            Ingredient(name = "Cebolla", category = "Verduras", isBasic = true),
            Ingredient(name = "Zanahoria", category = "Verduras"),
            Ingredient(name = "Pimiento", category = "Verduras"),
            Ingredient(name = "Papa", category = "Verduras"),
            Ingredient(name = "Ajo", category = "Verduras", isBasic = true),
            Ingredient(name = "Lechuga", category = "Verduras"),
            Ingredient(name = "Camote", category = "Verduras"),
            Ingredient(name = "Choclo", category = "Verduras"),
            Ingredient(name = "Cebolla Roja", category = "Verduras"),
            Ingredient(name = "Pepino", category = "Verduras"),
            Ingredient(name = "Brócoli", category = "Verduras"),
            Ingredient(name = "Palta", category = "Verduras"),
            Ingredient(name = "Espinaca", category = "Verduras"),
            Ingredient(name = "Kion", category = "Verduras", isBasic = true),
            Ingredient(name = "Apio", category = "Verduras"),
            Ingredient(name = "Yuca", category = "Verduras"),
            // Lácteos (5)
            Ingredient(name = "Leche", category = "Lácteos"),
            Ingredient(name = "Queso", category = "Lácteos"),
            Ingredient(name = "Mantequilla", category = "Lácteos"),
            Ingredient(name = "Leche Evaporada", category = "Lácteos"),
            Ingredient(name = "Crema de Leche", category = "Lácteos"),
            // Condimentos (11)
            Ingredient(name = "Sal", category = "Condimentos", isBasic = true),
            Ingredient(name = "Aceite", category = "Condimentos", isBasic = true),
            Ingredient(name = "Sillao", category = "Condimentos"),
            Ingredient(name = "Limón", category = "Condimentos"),
            Ingredient(name = "Lima", category = "Condimentos"),
            Ingredient(name = "Ají Amarillo", category = "Condimentos"),
            Ingredient(name = "Ají Panca", category = "Condimentos"),
            Ingredient(name = "Rocoto", category = "Condimentos"),
            Ingredient(name = "Vinagre", category = "Condimentos"),
            Ingredient(name = "Pimienta", category = "Condimentos"),
            Ingredient(name = "Comino", category = "Condimentos"),
            Ingredient(name = "Aceituna", category = "Condimentos"),
            Ingredient(name = "Mayonesa", category = "Condimentos", isBasic = true),
            Ingredient(name = "Ketchup", category = "Condimentos"),
            Ingredient(name = "Mostaza", category = "Condimentos"),
            Ingredient(name = "Maicena", category = "Condimentos"),
            // Hierbas (5)
            Ingredient(name = "Cilantro", category = "Hierbas"),
            Ingredient(name = "Perejil", category = "Hierbas"),
            Ingredient(name = "Culantro", category = "Hierbas"),
            Ingredient(name = "Huacatay", category = "Hierbas"),
            Ingredient(name = "Orégano", category = "Hierbas"),
            // Legumbres / Menestras (6) - NUEVO
            Ingredient(name = "Lentejas", category = "Granos"),
            Ingredient(name = "Frijoles", category = "Granos"),
            Ingredient(name = "Garbanzos", category = "Granos"),
            Ingredient(name = "Pallares", category = "Granos"),
            Ingredient(name = "Habas", category = "Verduras"),
            Ingredient(name = "Arvejas", category = "Verduras")
        )
        val ids = dao.insertIngredients(ingredients)
        val idMap = ingredients.mapIndexed { index, ing -> ing.name to ids[index].toInt() }.toMap()

        // Helper
        suspend fun addRI(recipeId: Int, name: String, qty: String, optional: Boolean = false, impact: String? = null) {
            dao.insertRecipeIngredient(RecipeIngredient(
                recipeId = recipeId,
                ingredientId = idMap[name]!!,
                quantity = qty,
                isOptional = optional,
                impact = impact
            ))
        }

        // ════════════════════════════════════════
        // DESAYUNOS 🌅
        // ════════════════════════════════════════

        val d1 = dao.insertRecipe(Recipe(
            name = "Avena con Leche",
            description = "Avena cremosa con leche, canela y miel. Desayuno nutritivo y rápido.",
            instructions = "[BOIL] 1. **Calentar la leche**: En una olla pequeña, vierte la leche y agrega la canela y el clavo de olor. Calienta a fuego medio hasta que empiece a humear.\n[MIX] 2. **Incorporar avena**: Agrega la avena en hojuelas y baja el fuego al mínimo. Remueve constantemente para evitar que se pegue al fondo.\n[COOK] 3. **Cocción lenta**: Cocina por unos 5-7 minutos hasta que la avena espese y tenga una textura cremosa. Si prefieres más líquida, agrega un chorrito de agua o leche extra.\n[SERVE] 4. **Servir**: Retira la canela y sirve caliente. Endulza con miel al gusto y decora con fruta fresca si deseas.",
            prepTimeMinutes = 10,
            mealType = "Desayuno"
        )).toInt()
        addRI(d1, "Avena", "1/2 taza")
        addRI(d1, "Leche", "1 taza")
        addRI(d1, "Sal", "Pizca")

        val d2 = dao.insertRecipe(Recipe(
            name = "Huevos Revueltos Cremosos",
            description = "Huevos revueltos con mantequilla, leche y queso. Suaves y cremosos.",
            instructions = "[MIX] 1. **Batido perfecto**: En un bowl, bate los huevos con la leche y una pizca de sal hasta que la mezcla esté homogénea y aireada.\n[COOK] 2. **Sartén a punto**: Calienta la mantequilla en una sartén antiadherente a fuego medio-bajo. No dejes que la mantequilla se queme.\n[COOK] 3. **Cocción suave**: Vierte los huevos y espera 10 segundos. Luego, con una espátula, empuja los bordes hacia el centro suavemente. Repite hasta que estén casi cuajados pero húmedos.\n[SERVE] 4. **Toque final**: Apaga el fuego, agrega el queso rallado para que se derrita con el calor residual y sirve inmediatamente sobre tostadas.",
            prepTimeMinutes = 8,
            mealType = "Desayuno"
        )).toInt()
        addRI(d2, "Huevo", "3")
        addRI(d2, "Leche", "2 cdas")
        addRI(d2, "Mantequilla", "1 cda")
        addRI(d2, "Queso", "30g")
        addRI(d2, "Sal", "Pizca")

        val d3 = dao.insertRecipe(Recipe(
            name = "Pan con Palta",
            description = "Tostada con palta aplastada, sal y limón. Simple y delicioso.",
            instructions = "[COOK] 1. **Tostar**: Tuesta las rebanadas de pan hasta que estén doradas y crujientes.\n[CHOP] 2. **Chancar la palta**: Corta la palta por la mitad, retira la pepa y saca la pulpa. Aplástala con un tenedor dejando algunos trozos para textura.\n[MIX] 3. **Sazonar**: Agrega sal, unas gotas de limón y pimienta negra a la palta. Mezcla suavemente.\n[SERVE] 4. **Montaje**: Unta una capa generosa sobre el pan tostado. Opcional: agrega huevo duro laminado encima.",
            prepTimeMinutes = 5,
            mealType = "Desayuno"
        )).toInt()
        addRI(d3, "Pan", "2 rebanadas")
        addRI(d3, "Palta", "1")
        addRI(d3, "Limón", "1/2")
        addRI(d3, "Sal", "Pizca")
        addRI(d3, "Huevo", "1 cocido", optional = true, impact = "Sin proteína extra")

        val d4 = dao.insertRecipe(Recipe(
            name = "Quinua con Leche",
            description = "Quinua hervida con leche evaporada y canela. Desayuno andino energético.",
            instructions = "[BOIL] 1. **Lavar la quinua**: Lava la quinua varias veces bajo el chorro de agua hasta que deje de salir espuma (saponina).\n[BOIL] 2. **Cocción base**: Hierve la quinua con agua, canela y clavo por 15 minutos o hasta que el grano reviente y esté suave.\n[MIX] 3. **Dar cremosidad**: Baja el fuego, agrega la leche evaporada y el azúcar/endulzante. Remueve bien.\n[SERVE] 4. **Reposo**: Deja reposar 2 minutos tapado para que los sabores se integren. Sirve caliente espolvoreando canela en polvo.",
            prepTimeMinutes = 20,
            mealType = "Desayuno"
        )).toInt()
        addRI(d4, "Quinua", "1/2 taza")
        addRI(d4, "Leche Evaporada", "1/2 taza")
        addRI(d4, "Leche", "1/2 taza", optional = true, impact = "Menos líquido")

        val d5 = dao.insertRecipe(Recipe(
            name = "Tortilla de Huevo con Tocino",
            description = "Tortilla esponjosa con tocino crocante. Desayuno contundente.",
            instructions = "[COOK] 1. **Crocante**: Corta el tocino en cuadrados pequeños y fríelo en su propia grasa hasta que esté dorado y crujiente. Retira el exceso de grasa.\n[MIX] 2. **Batir**: Bate los huevos con una pizca de sal y pimienta. Incorpora el tocino frito a la mezcla.\n[COOK] 3. **Cuajar**: Vierte la mezcla en la sartén caliente. Cocina a fuego medio, moviendo un poco para que el huevo crudo llegue al fondo.\n[COOK] 4. **Voltear**: Cuando la base esté firme, voltea la tortilla (con ayuda de un plato si es necesario) y cocina 1 minuto más.",
            prepTimeMinutes = 12,
            mealType = "Desayuno"
        )).toInt()
        addRI(d5, "Huevo", "3")
        addRI(d5, "Tocino", "3 tiras")
        addRI(d5, "Aceite", "1 cda")
        addRI(d5, "Sal", "Pizca")
        addRI(d5, "Queso", "30g", optional = true, impact = "Sin extra cremosidad")

        val d6 = dao.insertRecipe(Recipe(
            name = "Pan con Chicharrón",
            description = "Sánguche peruano de cerdo frito con camote y salsa criolla.",
            instructions = "[COOK] 1. **Chicharrón**: Corta el cerdo en trozos. Hiérvelo con agua y sal hasta que el agua se evapore, luego deja que se fría en su propia manteca hasta dorar.\n[CHOP] 2. **Salsa Criolla**: Corta la cebolla roja en pluma muy fina. Lava con agua fría. Mezcla con ají amarillo picado, limón, sal y cilantro.\n[COOK] 3. **Camote**: Fríe rodajas de camote hasta que estén doradas.\n[SERVE] 4. **Armar**: En un pan francés, coloca una cama de camote, luego bastante chicharrón y corona con la salsa criolla.",
            prepTimeMinutes = 25,
            mealType = "Desayuno"
        )).toInt()
        addRI(d6, "Cerdo", "200g")
        addRI(d6, "Pan", "2")
        addRI(d6, "Cebolla Roja", "1/2")
        addRI(d6, "Limón", "1")
        addRI(d6, "Ají Amarillo", "1/2 cda", optional = true, impact = "Sin picante")
        addRI(d6, "Camote", "1 pequeño", optional = true, impact = "Sin guarnición")
        addRI(d6, "Sal", "Al gusto")

        // ════════════════════════════════════════
        // ALMUERZOS 🌞
        // ════════════════════════════════════════

        // NUEVAS RECETAS CON MENESTRAS
        val a12 = dao.insertRecipe(Recipe(
            name = "Lentejas con Arroz",
            description = "Guiso clásico de lunes: lentejas sabrosas con arroz blanco y huevo frito.",
            instructions = "[COOK] 1. **Aderezo base**: En una olla, dora la cebolla picada y el ajo a fuego lento hasta que estén transparentes. Agrega una pizca de comino.\n[BOIL] 2. **Cocción**: Agrega las lentejas (previamente remojadas 2 horas) y cubre con agua o caldo. Cocina por 30-40 minutos hasta que estén suaves pero no deshechas.\n[MIX] 3. **Espesar**: Aplasta unas pocas lentejas contra la pared de la olla para espesar el guiso. Agrega la papa picada si deseas y cocina 10 min más.\n[SERVE] 4. **Servir**: Acompaña con arroz blanco graneado y un huevo frito encima.",
            prepTimeMinutes = 40,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a12, "Lentejas", "250g")
        addRI(a12, "Arroz", "1 taza")
        addRI(a12, "Cebolla Roja", "1")
        addRI(a12, "Ajo", "2 dientes")
        addRI(a12, "Huevo", "2 fritos")
        addRI(a12, "Papa", "1", optional = true)

        val a13 = dao.insertRecipe(Recipe(
            name = "Seco de Res con Frijoles",
            description = "Guiso de carne en salsa de cilantro acompañado de frijoles cremosos.",
            instructions = "[CHOP] 1. **La salsa verde**: Licúa las hojas de cilantro y espinaca con un chorrito de agua o cerveza hasta obtener una pasta suave.\n[COOK] 2. **Sellar y aderezar**: Dora los trozos de carne y retíralos. En la misma olla, haz un aderezo con cebolla, ajo y ají amarillo. Cocina bien hasta que el aceite se separe.\n[BOIL] 3. **Guisar**: Regresa la carne, agrega el licuado verde, zanahoria y arvejas. Tapa y cocina a fuego lento por 45-60 min hasta que la carne esté suave.\n[SERVE] 4. **Emplatado**: Sirve una porción de frijoles (previamente cocidos y aderezados), arroz blanco y el seco jugoso encima.",
            prepTimeMinutes = 60,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a13, "Carne Molida", "O Lomo (300g)", optional = false, impact = "Principal")
        addRI(a13, "Frijoles", "200g (cocidos)")
        addRI(a13, "Cilantro", "1 atado")
        addRI(a13, "Espinaca", "1 puñado")
        addRI(a13, "Cebolla Roja", "1")
        addRI(a13, "Ají Amarillo", "1")
        addRI(a13, "Arvejas", "1/2 taza")
        addRI(a13, "Zanahoria", "1")

        val a14 = dao.insertRecipe(Recipe(
            name = "Guiso de Garbanzos",
            description = "Garbanzos estofados con acelga o espinaca. Muy nutritivo.",
            instructions = "[BOIL] 1. **Ablandar**: Cocina los garbanzos (remojados desde la noche anterior) en agua hirviendo hasta que estén tiernos (aprox 45 min o usa olla a presión).\n[COOK] 2. **Saborizar**: En otra olla, sofreír cebolla, ajo y tomate picado hasta formar una pasta.\n[MIX] 3. **Unir**: Vierte los garbanzos con un poco de su agua al aderezo. Agrega la espinaca picada y deja cocinar 5 minutos hasta que la verdura reduzca.\n[SERVE] 4. **Finalizar**: Corrige la sal y sirve caliente, idealmente con arroz o pan.",
            prepTimeMinutes = 50,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a14, "Garbanzos", "250g")
        addRI(a14, "Espinaca", "200g")
        addRI(a14, "Cebolla Roja", "1")
        addRI(a14, "Tomate", "1")
        addRI(a14, "Ajo", "2 dientes")

        val a15 = dao.insertRecipe(Recipe(
            name = "Tacu Tacu con Huevo",
            description = "Mezcla criolla de frijoles y arroz dorados en sartén.",
            instructions = "[MIX] 1. **La mezcla**: En un bowl, mezcla el arroz cocido y los frijoles del día anterior. Deben integrarse bien, aplastando un poco los frijoles.\n[COOK] 2. **Aderezo rápido**: En una sartén, dora cebolla picada con ajo y pasta de ají amarillo.\n[COOK] 3. **Dorar el Tacu Tacu**: Agrega la mezcla de arroz/frijoles a la sartén. Fríe a fuego alto, moviendo y dando forma ovalada hasta que se forme una costra dorada por abajo. Voltea con cuidado.\n[SERVE] 4. **Acompañar**: Sirve inmediatamente con un huevo frito encima y salsa criolla.",
            prepTimeMinutes = 20,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a15, "Frijoles", "2 tazas (cocidos)")
        addRI(a15, "Arroz", "2 tazas (cocido)")
        addRI(a15, "Cebolla Roja", "1")
        addRI(a15, "Ají Amarillo", "1 cda")
        addRI(a15, "Huevo", "2")

        val a1 = dao.insertRecipe(Recipe(
            name = "Arroz Chaufa de Pollo",
            description = "Clásico peruano-chino: arroz salteado al wok con pollo, huevo y sillao.",
            instructions = "[COOK] 1. **Huevo y Pollo**: Haz una tortilla de huevo, pícala en cuadritos y reserva. En la misma sartén/wok bien caliente, saltea el pollo en cubos hasta dorar y reserva.\n[COOK] 2. **La base**: Saltea el jengibre (kion) y la parte blanca de la cebolla china (si tienes) con ajo picado.\n[MIX] 3. **El Chaufa**: Sube el fuego al máximo. Agrega el arroz frío, el pollo y el huevo. Vierte el sillao y aceite de ajonjolí.\n[COOK] 4. **Salteado final**: Mueve el wok/sartén vigorosamente para que el arroz se fría y tome sabor ahumado. Termina con la cebolla china verde.",
            prepTimeMinutes = 20,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a1, "Pollo", "150g")
        addRI(a1, "Arroz", "2 tazas cocido")
        addRI(a1, "Huevo", "2")
        addRI(a1, "Sillao", "3 cdas")
        addRI(a1, "Cebolla", "1/2")
        addRI(a1, "Ajo", "1 diente")
        addRI(a1, "Aceite", "2 cdas")

        val a2 = dao.insertRecipe(Recipe(
            name = "Lomo Saltado",
            description = "Saltado peruano clásico con lomo, tomate, cebolla y papas fritas.",
            instructions = "[CHOP] 1. **Mise en place**: Corta el lomo en tiras gruesas (3cm). Corta cebolla y tomate en gajos gruesos. Ten el vinagre y sillao a la mano.\n[COOK] 2. **Wok humeante**: Calienta el aceite hasta que humee. Dora la carne en tandas pequeñas (1 min) para sellarla sin sancocharla. Retira.\n[COOK] 3. **Salteado de verduras**: En el mismo wok, saltea la cebolla y ají amarillo por 30 seg. Agrega el tomate y vinagre.\n[MIX] 4. **Unión**: Regresa la carne, añade el sillao y un poco de caldo. Mezcla rápido. Apaga el fuego y agrega cilantro.\n[SERVE] 5. **Servir**: Sirve jugoso acompañado de arroz blanco y papas fritas crujientes.",
            prepTimeMinutes = 30,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a2, "Lomo de Res", "300g")
        addRI(a2, "Cebolla Roja", "1 grande")
        addRI(a2, "Tomate", "2")
        addRI(a2, "Ají Amarillo", "1 cda pasta")
        addRI(a2, "Sillao", "3 cdas")
        addRI(a2, "Vinagre", "1 cda")
        addRI(a2, "Papa", "2 medianas")
        addRI(a2, "Arroz", "2 tazas")
        addRI(a2, "Aceite", "3 cdas")
        addRI(a2, "Cilantro", "Al gusto", optional = true, impact = "Solo decoración")

        val a3 = dao.insertRecipe(Recipe(
            name = "Ceviche Clásico",
            description = "Pescado fresco marinado en limón con cebolla roja, ají y cilantro.",
            instructions = "[CHOP] 1. **Corte preciso**: Corta el pescado fresco en cubos de 2cm. Mantenerlo siempre frío. Corta la cebolla en pluma y lávala con agua helada.\n[MIX] 2. **Marinado**: En un bowl frío, coloca el pescado, sal y el ají limo picado. Mezcla. Exprime los limones al momento (sin apretar demasiado para que no amargue).\n[MIX] 3. **Leche de Tigre**: Agrega el jugo de limón al pescado y mezcla por 1-2 minutos. El pescado cambiará a un color blanco opaco.\n[SERVE] 4. **Emplatado**: Agrega la cebolla y el cilantro al final. Sirve inmediatamente acompañado de choclo, camote y lechuga.",
            prepTimeMinutes = 15,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a3, "Pescado", "400g filete fresco")
        addRI(a3, "Limón", "8-10")
        addRI(a3, "Cebolla Roja", "1 grande")
        addRI(a3, "Ají Amarillo", "1 cda")
        addRI(a3, "Cilantro", "Un puñado")
        addRI(a3, "Sal", "Al gusto")
        addRI(a3, "Camote", "1 mediano", optional = true, impact = "Sin acompañamiento clásico")
        addRI(a3, "Choclo", "1", optional = true, impact = "Sin guarnición tradicional")

        val a4 = dao.insertRecipe(Recipe(
            name = "Papa a la Huancaína",
            description = "Papas con crema de ají amarillo, queso y leche evaporada.",
            instructions = "[BOIL] 1. **Papas**: Sancocha las papas en agua con sal hasta que estén suaves al hincar con un tenedor. Pélalas aún tibias.\n[MIX] 2. **La Salsa**: En una licuadora, pon el ají amarillo (sin venas), el queso fresco trozado, un chorrito de leche y una galleta de soda (o pan). Licúa agregando aceite en hilo hasta lograr una crema espesa.\n[SERVE] 3. **Presentación**: Coloca una cama de lechuga, rodajas de papa y cubre generosamente con la salsa huancaína. Decora con huevo duro y aceituna.",
            prepTimeMinutes = 20,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a4, "Papa", "4 medianas")
        addRI(a4, "Ají Amarillo", "2 cdas pasta")
        addRI(a4, "Queso", "100g fresco")
        addRI(a4, "Leche Evaporada", "1/2 taza")
        addRI(a4, "Aceite", "3 cdas")
        addRI(a4, "Sal", "Al gusto")
        addRI(a4, "Huevo", "2 cocidos", optional = true, impact = "Sin decoración")
        addRI(a4, "Aceituna", "4", optional = true, impact = "Pierde toque clásico")

        val a5 = dao.insertRecipe(Recipe(
            name = "Arroz con Pollo",
            description = "Arroz verde peruano con pollo, cilantro y cerveza negra.",
            instructions = "[COOK] 1. **Presas**: Sazona el pollo y séllalo en aceite caliente. Retira. En el mismo aceite, haz un aderezo con cebolla, ajo y ají amarillo.\n[CHOP] 2. **Verde**: Licúa el cilantro (y espinaca si deseas color intenso) con un poco de agua o cerveza negra.\n[COOK] 3. **Base**: Agrega el licuado al aderezo y cocina hasta que reduzca un poco. Regresa el pollo y agrega el líquido (agua/cerveza), zanahoria y pimiento.\n[BOIL] 4. **Arroz**: Cuando hierva, retira el pollo (para que no se deshaga), echa el arroz y cocina a fuego lento 20 min. Al final, pon el pollo encima para calentar.",
            prepTimeMinutes = 40,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a5, "Pollo", "1/2 pollo presas")
        addRI(a5, "Arroz", "2 tazas")
        addRI(a5, "Cilantro", "1 atado grande")
        addRI(a5, "Cebolla", "1")
        addRI(a5, "Ajo", "3 dientes")
        addRI(a5, "Ají Amarillo", "1 cda")
        addRI(a5, "Aceite", "3 cdas")
        addRI(a5, "Sal", "Al gusto")
        addRI(a5, "Comino", "1/2 cdta")
        addRI(a5, "Zanahoria", "1", optional = true, impact = "Pierde color extra")
        addRI(a5, "Choclo", "1 desgranado", optional = true, impact = "Sin guarnición")

        val a6 = dao.insertRecipe(Recipe(
            name = "Pollo al Limón y Ajo",
            description = "Pollo jugoso marinado con limón y ajo. Fácil y rápido.",
            instructions = "[MIX] 1. **Marinado**: En un bowl, mezcla el jugo de limón, ajo picado, sal, pimienta y orégano. Baña el pollo y deja reposar 15 minutos.\n[COOK] 2. **Cocción**: Calienta aceite en una sartén. Coloca el pollo (sin el jugo del marinado al principio) y dora por ambos lados.\n[BOIL] 3. **Reducción**: Cuando esté casi listo, agrega el jugo del marinado a la sartén y deja que reduzca y glasee el pollo por 2 minutos.\n[SERVE] 4. **Servir**: Acompaña con arroz blanco o ensalada.",
            prepTimeMinutes = 30,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a6, "Pollo", "200g")
        addRI(a6, "Limón", "1")
        addRI(a6, "Ajo", "2 dientes")
        addRI(a6, "Aceite", "2 cdas")
        addRI(a6, "Cilantro", "Al gusto", optional = true, impact = "Sin frescura extra")

        val a7 = dao.insertRecipe(Recipe(
            name = "Pasta con Salsa de Tomate",
            description = "Pasta sencilla con salsa casera de tomate y ajo.",
            instructions = "[BOIL] 1. **Pasta**: Hierve agua con abundante sal. Cocina la pasta hasta que esté al dente (según empaque).\n[CHOP] 2. **Salsa**: Pica los tomates y el ajo finamente.\n[COOK] 3. **Sofreír**: En una sartén con aceite de oliva, dora el ajo (cuidado que no se queme) y agrega el tomate. Cocina 10 min aplastando los tomates.\n[MIX] 4. **Juntar**: Vuelca la pasta escurrida directamente a la sartén con salsa. Mezcla bien y sirve con queso.",
            prepTimeMinutes = 15,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a7, "Pasta", "200g")
        addRI(a7, "Tomate", "3")
        addRI(a7, "Ajo", "2 dientes")
        addRI(a7, "Aceite", "2 cdas")
        addRI(a7, "Sal", "Al gusto")
        addRI(a7, "Queso", "Al gusto", optional = true, impact = "Menos cremosa")

        val a8 = dao.insertRecipe(Recipe(
            name = "Tallarín Saltado",
            description = "Fideos salteados al wok con carne, tomate, cebolla y sillao.",
            instructions = "[BOIL] 1. **Fideos**: Sancocha los fideos pero déjalos un poco duros (se terminarán de cocinar en el wok).\n[COOK] 2. **Carnes**: Sella la carne sazonada en aceite muy caliente. Retira.\n[COOK] 3. **Vegetales**: Saltea la cebolla y tomate en gajos por pocos segundos.\n[MIX] 4. **Todo junto**: Regresa la carne, agrega los fideos, el sillao y un chorrito de vinagre. Saltea todo para que se mezclen los sabores.",
            prepTimeMinutes = 25,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a8, "Fideos", "250g")
        addRI(a8, "Lomo de Res", "200g")
        addRI(a8, "Cebolla Roja", "1")
        addRI(a8, "Tomate", "2")
        addRI(a8, "Sillao", "3 cdas")
        addRI(a8, "Ajo", "2 dientes")
        addRI(a8, "Aceite", "2 cdas")
        addRI(a8, "Ají Amarillo", "1 cda", optional = true, impact = "Sin picante")

        val a9 = dao.insertRecipe(Recipe(
            name = "Arroz con Atún",
            description = "Arroz con atún, cebolla y limón. Almuerzo rápido y económico.",
            instructions = "[BOIL] 1. **Arroz**: Prepara un arroz blanco básico (o usa el que sobró de ayer).\n[CHOP] 2. **Frescura**: Pica la cebolla en cuadritos y el tomate. Lava la cebolla.\n[MIX] 3. **Mezcla**: En la olla con el arroz caliente, agrega la lata de atún (con o sin aceite, al gusto), la cebolla, tomate y jugo de limón.\n[SERVE] 4. **Servir**: Mezcla todo suavemente con un tenedor. Sirve caliente o frío como ensalada.",
            prepTimeMinutes = 15,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a9, "Arroz", "2 tazas")
        addRI(a9, "Atún en Lata", "1 lata")
        addRI(a9, "Cebolla", "1/2")
        addRI(a9, "Limón", "1")
        addRI(a9, "Aceite", "1 cda")
        addRI(a9, "Sal", "Al gusto")
        addRI(a9, "Tomate", "1", optional = true, impact = "Menos frescura")

        val a10 = dao.insertRecipe(Recipe(
            name = "Carapulcra",
            description = "Guiso peruano de papa seca con cerdo y ají panca.",
            instructions = "[BOIL] 1. **Papa Seca**: Tuesta ligeramente la papa seca y luego remójala en agua por 2 horas min.\n[COOK] 2. **Cerdo**: En una olla, dora los trozos de cerdo. Retira. Haz un aderezo con cebolla, ajos y bastante ají panca.\n[BOIL] 3. **Guiso**: Regresa el cerdo, agrega la papa seca escurrida y caldo de cerdo o agua. Cocina a fuego bajo, moviendo para que no se pegue.\n[SERVE] 4. **Listo**: Cuando la papa esté suave y el guiso espeso, rectifica la sal y sirve. Tradicionalmente con yuca o arroz.",
            prepTimeMinutes = 90,
            mealType = "Almuerzo"
        )).toInt()
        addRI(a10, "Papa", "200g (seca)")
        addRI(a10, "Cerdo", "300g")
        addRI(a10, "Ají Panca", "2 cdas pasta")
        addRI(a10, "Cebolla", "1")
        addRI(a10, "Ajo", "3 dientes")
        addRI(a10, "Comino", "1/2 cdta")
        addRI(a10, "Aceite", "3 cdas")
        addRI(a10, "Sal", "Al gusto")

        // ════════════════════════════════════════
        // CENAS 🌙
        // ════════════════════════════════════════

        val c1 = dao.insertRecipe(Recipe(
            name = "Tortilla de Espinaca",
            description = "Cena ligera y proteica. Espinaca, huevos y un toque de queso.",
            instructions = "[CHOP] 1. **Preparar**: Lava bien la espinaca y pícala finamente. Si deseas, saltéala unos segundos para reducir volumen.\n[MIX] 2. **Mezcla**: Bate los huevos en un bowl. Agrega la espinaca, sal, pimienta y nuez moscada (opcional).\n[COOK] 3. **Sartén**: Calienta una sartén pequeña con poco aceite. Vierte la mezcla.\n[COOK] 4. **Cocción**: Cocina a fuego bajo tapado. Cuando la base esté firme, voltea y cocina 2 minutos más.",
            prepTimeMinutes = 15,
            mealType = "Cena"
        )).toInt()
        addRI(c1, "Huevo", "2")
        addRI(c1, "Espinaca", "1 puñado")
        addRI(c1, "Sal", "Al gusto")
        addRI(c1, "Aceite", "1 cdta")
        addRI(c1, "Queso", "30g", optional = true)

        val c2 = dao.insertRecipe(Recipe(
            name = "Ensalada de Pollo y Verduras",
            description = "Pollo deshilachado con lechuga, tomate, pepino y palta. Fresco y saludable.",
            instructions = "[COOK] 1. **Pollo**: Sancocha la pechuga de pollo en agua con sal. Una vez lista, deshiláchala con dos tenedores.\n[CHOP] 2. **Vegetales**: Lava y corta la lechuga con la mano. Corta tomate y pepino en rodajas. Pica la palta en cubos.\n[MIX] 3. **Vinagreta**: Mezcla limón, aceite de oliva, sal y orégano en un vasito.\n[SERVE] 4. **Servir**: Coloca todos los ingredientes en un plato hondo, baña con la vinagreta y mezcla justo antes de comer.",
            prepTimeMinutes = 20,
            mealType = "Cena"
        )).toInt()
        addRI(c2, "Pollo", "1 pechuga cocida")
        addRI(c2, "Lechuga", "4 hojas")
        addRI(c2, "Tomate", "1")
        addRI(c2, "Pepino", "1/2")
        addRI(c2, "Palta", "1/2")
        addRI(c2, "Limón", "1")
        addRI(c2, "Aceite", "1 cda")

        val c3 = dao.insertRecipe(Recipe(
            name = "Sopa de Pollo (Dieta)",
            description = "Caldo reconfortante con pollo, fideos cabello de ángel y papa amarilla.",
            instructions = "[CHOP] 1. **Corte**: Pela y corta la papa amarilla en mitades. Corta el apio en trozos grandes (para sabor) o pequeños (para comer).\n[BOIL] 2. **Caldo**: Hierve agua con los huesos del pollo, apio, kion (jengibre) y sal. Cuando rompa hervor, agrega las presas de pollo.\n[BOIL] 3. **Sustancia**: Agrega la papa amarilla y cocina 10 minutos. Luego agrega los fideos cabello de ángel y cocina 3 minutos más.\n[SERVE] 4. **Final**: Sirve caliente con un toque de orégano seco frotado con las manos.",
            prepTimeMinutes = 30,
            mealType = "Cena"
        )).toInt()
        addRI(c3, "Pollo", "1 presa o huesos")
        addRI(c3, "Papa", "2 amarillas")
        addRI(c3, "Fideos", "1 puñado cabello ángel")
        addRI(c3, "Apio", "1 rama")
        addRI(c3, "Kion", "1 trozo", optional = true)
        addRI(c3, "Orégano", "Pizca")

        val c4 = dao.insertRecipe(Recipe(
            name = "Saltado de Brócoli con Pollo",
            description = "Salteado tipo oriental con brócoli crocante y pollo en cubos.",
            instructions = "[BOIL] 1. **Blanquear**: Pasa los árboles de brócoli por agua hirviendo 2 minutos y luego a agua fría con hielo (para que queden verdes).\n[COOK] 2. **Pollo**: En un wok/sartén, saltea el pollo en cubos con sal y pimienta hasta dorar. Retira.\n[COOK] 3. **Vegetales**: Saltea ajo, kion y cebolla en tiras gruesas. Agrega el brócoli y pimiento.\n[MIX] 4. **Salsa**: Regresa el pollo, agrega el sillao diluido con un poquito de agua y maicena (opcional para espesar). Saltea todo 1 minuto.",
            prepTimeMinutes = 20,
            mealType = "Cena"
        )).toInt()
        addRI(c4, "Brócoli", "1 cabeza chica")
        addRI(c4, "Pollo", "150g")
        addRI(c4, "Sillao", "2 cdas")
        addRI(c4, "Ajo", "1 diente")
        addRI(c4, "Kion", "1 trozo")
        addRI(c4, "Cebolla", "1/2")
        addRI(c4, "Pimiento", "1/2", optional = true)

        val c5 = dao.insertRecipe(Recipe(
            name = "Sánguche de Atún",
            description = "Clásico salvavidas: atún con mayonesa y cebolla en pan de molde.",
            instructions = "[CHOP] 1. **Cebolla**: Pica la cebolla roja en cuadritos muy finos y lávala bien (pásala por agua con sal si está muy fuerte).\n[MIX] 2. **Relleno**: Escurre el atún. Mézclalo en un bowl con la cebolla, mayonesa, limón y pimienta.\n[SERVE] 3. **Armar**: Coloca la mezcla generosamente entre dos rebanadas de pan de molde o pan francés. Puedes tostar el pan si prefieres.",
            prepTimeMinutes = 10,
            mealType = "Cena"
        )).toInt()
        addRI(c5, "Atún en Lata", "1 lata")
        addRI(c5, "Pan", "2 rebanadas")
        addRI(c5, "Mayonesa", "1 cda")
        addRI(c5, "Cebolla Roja", "1/4 pequeña")
        addRI(c5, "Limón", "Gotitas")

        val c6 = dao.insertRecipe(Recipe(
            name = "Crema de Espinaca con Huevo",
            description = "Crema suave de espinacas servida con huevo pochado o duro.",
            instructions = "[COOK] 1. **Base**: Sofreír cebolla y ajo en mantequilla hasta transparente.\n[COOK] 2. **Verde**: Agregar la espinaca lavada y cocinar hasta que reduzca su tamaño. Dejar enfriar un poco.\n[MIX] 3. **Licuar**: Licuar el sofrito de espinaca con la leche evaporada hasta tener una crema homogénea.\n[COOK] 4. **Calentar**: Regresar a la olla, sazonar con sal/pimienta y calentar. Servir con crutones y huevo.",
            prepTimeMinutes = 20,
            mealType = "Cena"
        )).toInt()
        addRI(c6, "Espinaca", "300g")
        addRI(c6, "Leche Evaporada", "1 taza")
        addRI(c6, "Cebolla", "1/2")
        addRI(c6, "Mantequilla", "1 cda")
        addRI(c6, "Huevo", "1", optional = true)
        addRI(c6, "Pan", "Croutones", optional = true)

        val c7 = dao.insertRecipe(Recipe(
            name = "Salchipapa Casera",
            description = "Papas fritas con hot dog. Un gusto culposo de fin de semana.",
            instructions = "[CHOP] 1. **Corte**: Pela las papas y córtalas en bastones. Corta los hot dogs en rodajas o sesgados.\n[COOK] 2. **Papas**: Fríe las papas en abundante aceite caliente. Tip: fríelas una vez, saca, espera 5 min y vuelve a freír para que queden crocantes.\n[COOK] 3. **Salchichas**: Fríe los hot dogs hasta que doren.\n[SERVE] 4. **Mezclar**: Sirve todo junto en un plato grande con todas las cremas que tengas (mayonesa, ketchup, mostaza, ají).",
            prepTimeMinutes = 25,
            mealType = "Cena"
        )).toInt()
        addRI(c7, "Papa", "3 grandes")
        addRI(c7, "Hot Dog", "3 unidades")
        addRI(c7, "Aceite", "Abundante")
        addRI(c7, "Sal", "Al gusto")
        addRI(c7, "Ají Amarillo", "1 cda", optional = true, impact = "Sin salsa picante")

        // ═══ SUSTITUCIONES ═══
        dao.insertSubstitution(Substitution(originalIngredientId = idMap["Limón"]!!, substituteIngredientId = idMap["Lima"]!!, impactDescription = "Sabor ligeramente más ácido"))
        dao.insertSubstitution(Substitution(originalIngredientId = idMap["Lima"]!!, substituteIngredientId = idMap["Limón"]!!, impactDescription = "Sabor más suave y aromático"))
        dao.insertSubstitution(Substitution(originalIngredientId = idMap["Mantequilla"]!!, substituteIngredientId = idMap["Aceite"]!!, impactDescription = "Menos cremoso pero más ligero"))
        dao.insertSubstitution(Substitution(originalIngredientId = idMap["Cebolla"]!!, substituteIngredientId = idMap["Cebolla Roja"]!!, impactDescription = "Sabor más fuerte y color morado"))
        dao.insertSubstitution(Substitution(originalIngredientId = idMap["Cebolla Roja"]!!, substituteIngredientId = idMap["Cebolla"]!!, impactDescription = "Sabor más suave, menos color"))
        dao.insertSubstitution(Substitution(originalIngredientId = idMap["Leche"]!!, substituteIngredientId = idMap["Leche Evaporada"]!!, impactDescription = "Más cremoso y concentrado"))
        dao.insertSubstitution(Substitution(originalIngredientId = idMap["Cilantro"]!!, substituteIngredientId = idMap["Culantro"]!!, impactDescription = "Sabor más intenso, típico peruano"))
        dao.insertSubstitution(Substitution(originalIngredientId = idMap["Culantro"]!!, substituteIngredientId = idMap["Cilantro"]!!, impactDescription = "Sabor más fresco y suave"))
        dao.insertSubstitution(Substitution(originalIngredientId = idMap["Pasta"]!!, substituteIngredientId = idMap["Fideos"]!!, impactDescription = "Textura similar, más fino"))
        dao.insertSubstitution(Substitution(originalIngredientId = idMap["Fideos"]!!, substituteIngredientId = idMap["Pasta"]!!, impactDescription = "Textura más gruesa"))
    }

    fun getRecommendedRecipes(): Flow<List<MatchResult>> {
        return combine(
            dao.getRecipesWithIngredients().map { recipes ->
                recipes.map { RecipeWithIngredients(it) }
            },
            dao.getAllRecipeIngredients(),
            dao.getAllSubstitutions(),
            dao.getAllIngredients(),
            getUserInventory()
        ) { recipesWithIng, allRecipeIngredients, allSubstitutions, allIngredients, inventory ->

            val ingredientMap = allIngredients.associateBy { it.id }
            val inventoryIds = inventory.map { it.id }.toSet()

            recipesWithIng.map { recipeStructure ->
                val metadataList = allRecipeIngredients.filter { it.recipeId == recipeStructure.recipe.id }

                val fullIngredients = metadataList.mapNotNull { meta ->
                    ingredientMap[meta.ingredientId]?.let { ingredient ->
                        IngredientDetail(
                            ingredient = ingredient,
                            quantity = meta.quantity,
                            isOptional = meta.isOptional,
                            impact = meta.impact
                        )
                    }
                }

                matchingEngine.calculateMatch(
                    recipe = recipeStructure.recipe,
                    ingredients = fullIngredients,
                    userInventoryIds = inventoryIds,
                    availableSubstitutions = allSubstitutions,
                    allIngredientsMap = ingredientMap
                )
            }.sortedByDescending { it.matchPercentage }
        }
    }
}
