package com.dev.uasist.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AsistenciaDto(
    @SerialName("estudiante_id") val estudianteId: String,
    @SerialName("clase_id") val claseId: String,
    @SerialName("asistencias_count") val asistenciasCount: Int
)
