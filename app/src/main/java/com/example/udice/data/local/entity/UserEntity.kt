package com.example.udice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val email: String,
    val rol: String,
    val facultad: String?,
    val carrera: String?,
    val isCurrentSession: Boolean = false
)
