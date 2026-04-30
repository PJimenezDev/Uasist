package com.dev.uasist.model

data class Clase(
    val id: String,
    val nombre: String,
    val profesor: String,
    val horario: String,
    val dias: List<String>,
    var asistencias: Int,
    val totalClases: Int
)