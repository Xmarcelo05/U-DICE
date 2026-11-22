package com.example.udice.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.udice.data.local.dao.PostDao
import com.example.udice.data.local.dao.PreferenceDao
import com.example.udice.data.local.dao.SavedPostDao
import com.example.udice.data.local.dao.UserDao
import com.example.udice.data.local.entity.PostEntity
import com.example.udice.data.local.entity.PreferenceEntity
import com.example.udice.data.local.entity.SavedPostEntity
import com.example.udice.data.local.entity.UserEntity

@Database(
    entities = [
        PostEntity::class,
        UserEntity::class,
        PreferenceEntity::class,
        SavedPostEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UDiceDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun savedPostDao(): SavedPostDao
}
