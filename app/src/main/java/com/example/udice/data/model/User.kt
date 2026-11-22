package com.example.udice.data.model

data class User(
    val id: String,
    val nombre: String,
    val email: String,
    val rol: Rol,
    val facultad: Facultad? = null,
    val carrera: String? = null
)
