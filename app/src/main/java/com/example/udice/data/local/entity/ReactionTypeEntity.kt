package com.example.udice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reaction_types")
data class ReactionTypeEntity(
    @PrimaryKey val id: String,
    val label: String,
    val emoji: String
)
