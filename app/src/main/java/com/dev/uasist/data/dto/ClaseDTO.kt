package com.dev.uasist.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaseDto(
    val id: String = "",
    val nombre: String = "",
    val materia: String = "",
    @SerialName("profesor_id") val profesorId: String = "",
    val horario: String = "",
    val dias: List<String> = emptyList(), // Muy importante para evitar fallos en listas
    @SerialName("total_clases") val totalClases: Int = 0
)