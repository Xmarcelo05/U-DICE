package com.example.udice.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "saved_posts",
    primaryKeys = ["userId", "postId"]
)
data class SavedPostEntity(
    val userId: String,
    val postId: String,
    val savedAt: Long = System.currentTimeMillis()
)
