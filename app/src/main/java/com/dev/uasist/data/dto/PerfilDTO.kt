package com.dev.uasist.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PerfilDto(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val email: String = "",
    val rol: String = "",
    @SerialName("materia_impartida") val materiaImpartida: String? = null,
    @SerialName("sala_asignada") val salaAsignada: String? = null
)