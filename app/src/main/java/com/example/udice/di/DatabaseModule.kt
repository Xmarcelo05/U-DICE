package com.example.udice.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE users ADD COLUMN passwordSalt BLOB")
            db.execSQL("ALTER TABLE users ADD COLUMN passwordHash BLOB")
            db.execSQL("UPDATE users SET isCurrentSession = 0")
        }
    }

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): UDiceDatabase =
        Room.databaseBuilder(context, UDiceDatabase::class.java, "udice.db")
            .fallbackToDestructiveMigration(true)
            .addMigrations(migration2To3)
            .addCallback(SeedDataCallback())
            .build()

    @Provides fun providePostDao(db: UDiceDatabase): PostDao = db.postDao()
    @Provides fun provideUserDao(db: UDiceDatabase): UserDao = db.userDao()
    @Provides fun providePreferenceDao(db: UDiceDatabase): PreferenceDao = db.preferenceDao()
    @Provides fun provideSavedPostDao(db: UDiceDatabase): SavedPostDao = db.savedPostDao()
    @Provides fun provideReaccionDao(db: UDiceDatabase): ReaccionDao = db.reaccionDao()
}
