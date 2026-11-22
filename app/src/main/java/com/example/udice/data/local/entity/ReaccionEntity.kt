package com.example.udice.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "reactions",
    primaryKeys = ["postId", "userId"]
)
data class ReaccionEntity(
    val postId: String,
    val userId: String,
    val tipo: String,
    val createdAt: Long = System.currentTimeMillis()
)
