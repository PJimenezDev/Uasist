package com.dev.uasist.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AsistenciaDetalladaDto(
    @SerialName("estudiante_id") val estudianteId: String,
    @SerialName("estudiante_nombre") val nombre: String,
    @SerialName("estudiante_apellidos") val apellidos: String,
    @SerialName("estudiante_email") val email: String,
    @SerialName("profesor_id") val profesorId: String,
    @SerialName("clase_id") val claseId: String, // Agregado para que selectAsFlow funcione mejor
    @SerialName("total_asistencias_acumuladas") val asistenciasCount: Int,
    @SerialName("total_clases_materia") val totalClases: Int // <--- OBLIGATORIO: Sin valor por defecto
)