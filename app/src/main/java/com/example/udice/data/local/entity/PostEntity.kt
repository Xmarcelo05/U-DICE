package com.example.udice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val autorId: String,
    val autorNombre: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val facultad: String,
    val alcance: String,
    val carrera: String?,
    val fecha: Long,
    val imageUrl: String?,
    val allowedReactionIds: String?,
    val videoUrl: String?,
    val additionalImageUrls: String?,
    val authorAvatarUrl: String?
)
