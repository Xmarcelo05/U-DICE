package com.example.udice.data.local.mapper

import com.example.udice.data.local.entity.PostEntity
import com.example.udice.data.model.Alcance
import com.example.udice.data.model.Categoria
import com.example.udice.data.model.Facultad
import com.example.udice.data.model.Post

fun PostEntity.toDomain(): Post = Post(
    id = id,
    autorId = autorId,
    autorNombre = autorNombre,
    titulo = titulo,
    descripcion = descripcion,
    categoria = runCatching { Categoria.valueOf(categoria) }.getOrDefault(Categoria.fromDisplayName(categoria)),
    facultad = runCatching { Facultad.valueOf(facultad) }.getOrDefault(Facultad.fromDisplayName(facultad) ?: Facultad.CIENCIAS_VIDA_TECNOLOGIAS),
    alcance = runCatching { Alcance.valueOf(alcance) }.getOrDefault(Alcance.GENERAL),
    carrera = carrera,
    fecha = fecha,
    imageUrl = imageUrl,
    allowedReactionIds = allowedReactionIds?.split(',')
)

fun Post.toEntity(): PostEntity = PostEntity(
    id = id,
    autorId = autorId,
    autorNombre = autorNombre,
    titulo = titulo,
    descripcion = descripcion,
    categoria = categoria.name,
    facultad = facultad.name,
    alcance = alcance.name,
    carrera = carrera,
    fecha = fecha,
    imageUrl = imageUrl,
    allowedReactionIds = allowedReactionIds?.joinToString(",")
)
