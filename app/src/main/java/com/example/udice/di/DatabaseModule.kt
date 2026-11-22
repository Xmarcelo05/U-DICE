package com.example.udice.di

import android.content.Context
import androidx.room.Room
import com.example.udice.data.local.UDiceDatabase
import com.example.udice.data.local.SeedDataCallback
import com.example.udice.data.local.dao.PostDao
import com.example.udice.data.local.dao.PreferenceDao
import com.example.udice.data.local.dao.ReaccionDao
import com.example.udice.data.local.dao.SavedPostDao
import com.example.udice.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): UDiceDatabase =
        Room.databaseBuilder(context, UDiceDatabase::class.java, "udice.db")
            .fallbackToDestructiveMigration(true)
            .addCallback(SeedDataCallback())
            .build()

    @Provides fun providePostDao(db: UDiceDatabase): PostDao = db.postDao()
    @Provides fun provideUserDao(db: UDiceDatabase): UserDao = db.userDao()
    @Provides fun providePreferenceDao(db: UDiceDatabase): PreferenceDao = db.preferenceDao()
    @Provides fun provideSavedPostDao(db: UDiceDatabase): SavedPostDao = db.savedPostDao()
    @Provides fun provideReaccionDao(db: UDiceDatabase): ReaccionDao = db.reaccionDao()
}
