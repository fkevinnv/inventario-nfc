package com.inventarionfc.app.modelo

/** Equipo tal y como lo envía y lo recibe la API. */
data class Equipo(
    val id: Long? = null,
    val codigoNfc: String? = null,
    val nombre: String,
    val tipo: String,
    val marca: String? = null,
    val modelo: String? = null,
    val numeroSerie: String? = null,
    val ubicacion: String? = null,
    val estado: String? = null,
    val fechaAlta: String? = null
)

/** Problema reportado sobre un equipo. */
data class Incidencia(
    val id: Long? = null,
    val descripcion: String,
    val fecha: String? = null,
    val resuelta: Boolean = false
)

/** Convierte el estado que devuelve la API (OPERATIVO, EN_REPARACION...) en un texto legible. */
fun String?.comoTextoEstado(): String = when (this) {
    "OPERATIVO" -> "Operativo"
    "AVERIADO" -> "Averiado"
    "EN_REPARACION" -> "En reparación"
    "BAJA" -> "Baja"
    else -> this ?: "-"
}
