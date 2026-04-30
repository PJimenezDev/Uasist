package com.dev.uasist.model

data class AsistenciaEstudiante(
    val estudianteId: String,
    val claseId: String,
    var asistencias: Int,
    val totalClases: Int
)