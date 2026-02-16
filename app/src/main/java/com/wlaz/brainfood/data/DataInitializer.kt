package com.wlaz.brainfood.data

import android.content.Context
import com.wlaz.brainfood.data.repository.BrainFoodRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads the ingredient catalog and recipes on first launch.
 * Uses SharedPreferences to track if data has already been seeded.
 */
@Singleton
class DataInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: BrainFoodRepository
) {
    private val prefs by lazy {
        context.getSharedPreferences("brainfood_prefs", Context.MODE_PRIVATE)
    }

    fun initializeIfNeeded() {
        val isSeeded = prefs.getBoolean("data_seeded_v8", false)
        if (!isSeeded) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.loadDemoData()
                prefs.edit().putBoolean("data_seeded_v8", true).apply()
            }
        }
    }
}
