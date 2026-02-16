package com.wlaz.brainfood

import android.app.Application
import com.wlaz.brainfood.data.DataInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BrainFoodApp : Application() {

    @Inject lateinit var dataInitializer: DataInitializer

    override fun onCreate() {
        super.onCreate()
        dataInitializer.initializeIfNeeded()
    }
}
