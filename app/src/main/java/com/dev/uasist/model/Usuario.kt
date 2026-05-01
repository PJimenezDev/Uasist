package com.dev.uasist.model

sealed class Usuario {
    abstract val id: String
    abstract val nombre: String
    abstract val apellidos: String
    abstract val email: String

    data class Estudiante(
        override val id: String,
        override val nombre: String,
        override val apellidos: String,
        override val email: String
    ) : Usuario()

    data class Profesor(
        override val id: String,
        override val nombre: String,
        override val apellidos: String,
        override val email: String,
        val materiaImpartida: Materia, // Aquí obligamos a que tenga una materia
        val salaAsignada: String
    ) : Usuario()
}