package com.example.udice.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.udice.BuildConfig
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

    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS reaction_types (
                    id TEXT NOT NULL PRIMARY KEY,
                    label TEXT NOT NULL,
                    emoji TEXT NOT NULL
                )
            """.trimIndent())
        }
    }

    private val migration4To5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE posts ADD COLUMN allowedReactionIds TEXT")
        }
    }

    private val migration5To6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE posts ADD COLUMN videoUrl TEXT")
        }
    }

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): UDiceDatabase =
        Room.databaseBuilder(
            context,
            UDiceDatabase::class.java,
            if (BuildConfig.UDICE_SUPABASE) "udice-cloud.db" else "udice.db"
        )
            .fallbackToDestructiveMigration(true)
            .addMigrations(migration2To3, migration3To4, migration4To5, migration5To6)
            .apply { if (!BuildConfig.UDICE_SUPABASE) addCallback(SeedDataCallback()) }
            .build()

    @Provides fun providePostDao(db: UDiceDatabase): PostDao = db.postDao()
    @Provides fun provideUserDao(db: UDiceDatabase): UserDao = db.userDao()
    @Provides fun providePreferenceDao(db: UDiceDatabase): PreferenceDao = db.preferenceDao()
    @Provides fun provideSavedPostDao(db: UDiceDatabase): SavedPostDao = db.savedPostDao()
    @Provides fun provideReaccionDao(db: UDiceDatabase): ReaccionDao = db.reaccionDao()
}
