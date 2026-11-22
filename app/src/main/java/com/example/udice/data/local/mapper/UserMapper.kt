package com.example.udice.data.local.mapper

import com.example.udice.data.local.entity.UserEntity
import com.example.udice.data.model.Facultad
import com.example.udice.data.model.Rol
import com.example.udice.data.model.User

fun UserEntity.toDomain(): User = User(
    id = id,
    nombre = nombre,
    email = email,
    rol = runCatching { Rol.valueOf(rol) }.getOrDefault(Rol.LECTOR),
    facultad = facultad?.let { f -> runCatching { Facultad.valueOf(f) }.getOrDefault(Facultad.fromDisplayName(f)) },
    carrera = carrera
)

fun User.toEntity(isCurrentSession: Boolean = false): UserEntity = UserEntity(
    id = id,
    nombre = nombre,
    email = email,
    rol = rol.name,
    facultad = facultad?.name,
    carrera = carrera,
    isCurrentSession = isCurrentSession
)
