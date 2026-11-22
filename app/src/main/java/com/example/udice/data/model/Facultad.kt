package com.example.udice.data.model

enum class Facultad(val displayName: String) {
    CIENCIAS_VIDA_TECNOLOGIAS("Ciencias de la Vida y Tecnologías"),
    CIENCIAS_SALUD("Ciencias de la Salud"),
    INGENIERIA_INDUSTRIA_CONSTRUCCION("Ingeniería, Industria y Construcción"),
    CIENCIAS_ADMINISTRATIVAS_CONTABLES_COMERCIO("Facultad de Ciencias Administrativas, Contables y Comercio"),
    EDUCACION_TURISMO_ARTES_HUMANIDADES("Facultad de Educación, Turismo, Artes y Humanidades"),
    CIENCIAS_SOCIALES_DERECHO_BIENESTAR("Facultad de Ciencias Sociales, Derecho y Bienestar"),
    BIENESTAR_UNIVERSITARIO("Bienestar Universitario"),
    INSTITUTO_IDIOMAS("Instituto de Idiomas");

    companion object {
        fun fromDisplayName(name: String): Facultad? =
            entries.find { it.displayName.equals(name, ignoreCase = true) }
    }
}
