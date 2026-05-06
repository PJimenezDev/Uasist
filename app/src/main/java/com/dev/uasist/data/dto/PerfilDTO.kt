package com.dev.uasist.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PerfilDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val nombre: String,
    @SerialName("apellidos") val apellidos: String,
    @SerialName("email") val email: String,
    @SerialName("rol") val rol: String,
    // Importante: Estos deben ser nulables para que el insert de un alumno no falle
    @SerialName("materia_impartida") val materiaImpartida: String? = null,
    @SerialName("sala_asignada") val salaAsignada: String? = null
)