package com.example.udice.data.model

enum class Categoria(val displayName: String) {
    EVENTO("Evento"),
    CONVOCATORIA("Convocatoria"),
    CONFERENCIA("Conferencia"),
    AVISO("Aviso"),
    OTRO("Otro");

    companion object {
        fun fromDisplayName(name: String): Categoria =
            entries.find { it.displayName.equals(name, ignoreCase = true) } ?: OTRO
    }
}
