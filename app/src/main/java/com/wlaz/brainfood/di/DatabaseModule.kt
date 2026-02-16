package com.wlaz.brainfood.di

import android.content.Context
import androidx.room.Room
import com.wlaz.brainfood.data.AppDatabase
import com.wlaz.brainfood.data.BrainFoodDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "brainfood_db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideBrainFoodDao(database: AppDatabase): BrainFoodDao {
        return database.brainFoodDao()
    }
}
