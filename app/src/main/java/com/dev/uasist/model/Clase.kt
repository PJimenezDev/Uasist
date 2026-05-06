package com.dev.uasist.model

data class Clase(
    val id: String,
    val nombre: String,
    val profesorId: String,
    val profesor: String, // Nombre para mostrar en UI
    val horario: String,
    val dias: List<String>,
    val asistencias: Int,
    val totalClases: Int
)