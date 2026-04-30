package com.dev.uasist.data

import com.dev.uasist.model.*
import java.util.Calendar

class AsistenciaRepository {

    private val estudiantes = listOf(
        Estudiante("est1", "Carlos", "Ramírez López", "carlos@mail.com"),
        Estudiante("est2", "María", "González Silva", "maria@mail.com"),
        Estudiante("est3", "Pedro", "Martínez Rojas", "pedro.martinez@mail.com"),
        Estudiante("est4", "Ana", "Fernández Torres", "ana.fernandez@mail.com")
    )

    private val clases = listOf(
        Clase("1", "Matemáticas", "Prof. González", "08:00 - 10:00", listOf("Lunes", "Miércoles", "Viernes"), 28, 30),
        Clase("2", "Física", "Prof. Martínez", "10:00 - 12:00", listOf("Martes", "Jueves"), 18, 20),
        Clase("3", "Química", "Prof. Rodríguez", "14:00 - 16:00", listOf("Lunes", "Miércoles"), 15, 20),
        Clase("4", "Lenguaje", "Prof. Silva", "16:00 - 18:00", listOf("Martes", "Jueves"), 19, 20),
        Clase("5", "Historia", "Prof. Pérez", "08:00 - 10:00", listOf("Martes", "Jueves"), 16, 20),
        Clase("6", "Biología", "Prof. Torres", "12:00 - 14:00", listOf("Lunes", "Viernes"), 13, 16)
    )

    private var asistenciasCache = mutableListOf<AsistenciaEstudiante>()

    init {
        generarAsistenciasIniciales()
    }

    private fun generarAsistenciasIniciales() {
        estudiantes.forEach { estudiante ->
            clases.forEach { clase ->
                asistenciasCache.add(
                    AsistenciaEstudiante(estudiante.id, clase.id, (0..clase.totalClases).random(), clase.totalClases)
                )
            }
        }
    }

    // --- NUEVO MÉTODO ---
    fun getTodasLasClases(): List<Clase> {
        return clases
    }

    fun getClasesHoy(): List<Clase> {
        val diasSemana = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        val hoy = diasSemana[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]
        return clases.filter { it.dias.contains(hoy) }
    }

    fun getAsistenciaGeneral(): Int {
        val totalAsist = clases.sumOf { it.asistencias }
        val totalClases = clases.sumOf { it.totalClases }
        return if (totalClases > 0) Math.round((totalAsist.toDouble() / totalClases) * 100).toInt() else 0
    }

    fun marcarAsistenciaEstudiante(estudianteId: String, claseId: String): Boolean {
        val asistencia = asistenciasCache.find { it.estudianteId == estudianteId && it.claseId == claseId }
        if (asistencia != null && asistencia.asistencias < asistencia.totalClases) {
            asistencia.asistencias += 1
            return true
        }
        return false
    }
}