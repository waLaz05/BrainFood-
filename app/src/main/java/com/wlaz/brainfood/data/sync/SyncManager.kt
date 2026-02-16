package com.wlaz.brainfood.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.wlaz.brainfood.data.BrainFoodDao
import com.wlaz.brainfood.data.InventoryItem
import com.wlaz.brainfood.data.ShoppingListItem
import com.wlaz.brainfood.data.UserFavorite
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles deferred cloud sync with Firestore.
 * 
 * Key principles:
 * - Room is ALWAYS the source of truth
 * - Cloud writes are debounced (3 sec idle)
 * - Pull on app open, push on idle
 * - No real-time listeners to avoid Brish-like conflicts
 */
@Singleton
class SyncManager @Inject constructor(
    private val dao: BrainFoodDao
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pendingSync: Job? = null

    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_DELAY_MS = 3000L // 3 seconds idle before push
    }

    private val uid: String? get() = auth.currentUser?.uid

    /**
     * Schedule a deferred push to cloud.
     * Each new call resets the timer — only syncs after 3 sec of inactivity.
     */
    fun scheduleSyncAfterIdle() {
        if (uid == null) return // Not logged in, skip
        
        pendingSync?.cancel()
        pendingSync = scope.launch {
            delay(SYNC_DELAY_MS)
            try {
                pushToCloud()
                Log.d(TAG, "✅ Deferred sync pushed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Deferred sync failed: ${e.message}")
            }
        }
    }

    /**
     * Push current Room state to Firestore as a full snapshot.
     * Called after idle delay or when app goes to background.
     */
    suspend fun pushToCloud() {
        val userId = uid ?: return

        try {
            // Read current Room state
            val inventory = dao.getUserInventory().first()
            val favorites = dao.getAllFavorites().first()
            val shoppingList = dao.getShoppingList().first()

            val userDoc = firestore.collection("users").document(userId)

            // Inventory: just the ingredient IDs
            userDoc.collection("data").document("inventory").set(
                mapOf("items" to inventory.map { it.ingredientId }),
                SetOptions.merge()
            ).await()

            // Favorites: just the recipe IDs
            userDoc.collection("data").document("favorites").set(
                mapOf("items" to favorites.map { it.recipeId }),
                SetOptions.merge()
            ).await()

            // Shopping list: ingredient IDs + checked status
            userDoc.collection("data").document("shopping_list").set(
                mapOf("items" to shoppingList.map { item ->
                    mapOf(
                        "ingredientId" to item.ingredientId,
                        "isChecked" to item.isChecked
                    )
                }),
                SetOptions.merge()
            ).await()

            // Update last sync timestamp
            userDoc.set(
                mapOf(
                    "lastSync" to System.currentTimeMillis(),
                    "displayName" to (auth.currentUser?.displayName ?: ""),
                    "email" to (auth.currentUser?.email ?: "")
                ),
                SetOptions.merge()
            ).await()

            Log.d(TAG, "Push complete: ${inventory.size} inv, ${favorites.size} favs, ${shoppingList.size} shop")
        } catch (e: Exception) {
            Log.e(TAG, "Push failed: ${e.message}", e)
            throw e
        }
    }

    /**
     * Pull cloud data and replace local Room state.
     * Called on app open (if logged in).
     */
    suspend fun pullFromCloud() {
        val userId = uid ?: return

        try {
            val userDoc = firestore.collection("users").document(userId)

            // Pull inventory
            val invSnapshot = userDoc.collection("data").document("inventory").get().await()
            val cloudInventoryIds = (invSnapshot.get("items") as? List<*>)
                ?.mapNotNull { (it as? Number)?.toInt() }
                ?: emptyList()

            // Pull favorites
            val favSnapshot = userDoc.collection("data").document("favorites").get().await()
            val cloudFavoriteIds = (favSnapshot.get("items") as? List<*>)
                ?.mapNotNull { (it as? Number)?.toInt() }
                ?: emptyList()

            // Pull shopping list
            val shopSnapshot = userDoc.collection("data").document("shopping_list").get().await()
            val cloudShoppingItems = (shopSnapshot.get("items") as? List<*>)
                ?.mapNotNull { item ->
                    (item as? Map<*, *>)?.let { map ->
                        val ingId = (map["ingredientId"] as? Number)?.toInt() ?: return@mapNotNull null
                        val checked = (map["isChecked"] as? Boolean) ?: false
                        ShoppingListItem(ingredientId = ingId, isChecked = checked)
                    }
                }
                ?: emptyList()

            // Replace local data
            // Inventory
            dao.clearInventory()
            cloudInventoryIds.forEach { id ->
                dao.addToInventory(InventoryItem(ingredientId = id))
            }

            // Favorites
            dao.clearFavorites()
            cloudFavoriteIds.forEach { id ->
                dao.addFavorite(UserFavorite(recipeId = id))
            }

            // Shopping list
            dao.clearShoppingList()
            cloudShoppingItems.forEach { item ->
                dao.addToShoppingList(item)
            }

            Log.d(TAG, "Pull complete: ${cloudInventoryIds.size} inv, ${cloudFavoriteIds.size} favs, ${cloudShoppingItems.size} shop")
        } catch (e: Exception) {
            Log.e(TAG, "Pull failed: ${e.message}", e)
            // Don't throw — keep using local data if pull fails
        }
    }

    /**
     * Final flush when app goes to background.
     */
    fun flushSync() {
        if (uid == null) return
        pendingSync?.cancel()
        scope.launch {
            try {
                pushToCloud()
            } catch (e: Exception) {
                Log.e(TAG, "Flush sync failed: ${e.message}")
            }
        }
    }
}
