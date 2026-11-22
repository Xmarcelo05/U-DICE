package com.example.udice.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "user_preferences",
    primaryKeys = ["userId", "preferenceKey"]
)
data class PreferenceEntity(
    val userId: String,
    val preferenceKey: String,
    val isEnabled: Boolean
)
