package com.dev.uasist.data

import android.util.Log
import com.dev.uasist.data.dto.*
import com.dev.uasist.data.network.SupabaseManager
import com.dev.uasist.model.*
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Calendar

class AsistenciaRepository {

    private val TAG = "AsistenciaRepository"

    suspend fun signUp(email: String, pass: String, nombre: String, apellidos: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Crear usuario en Auth
            // Usamos la sintaxis más reciente del SDK
            val authResponse = SupabaseManager.client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }

            // 2. Intentar obtener el ID del usuario recién creado
            // Si authResponse es null o no tiene ID, lo buscamos en el estado actual de la sesión
            val userId = authResponse?.id

            if (userId == null) {
                Log.e(TAG, "Auth exitoso pero no se pudo obtener el ID del usuario")
                return@withContext false
            }

            Log.d(TAG, "Usuario creado en Auth con ID: $userId. Procediendo a crear perfil...")

            // 3. Insertar perfil en la tabla 'perfiles'
            // IMPORTANTE: Asegúrate que PerfilDto acepte nulos en materia y sala
            val nuevoPerfil = PerfilDto(
                id = userId,
                nombre = nombre,
                apellidos = apellidos,
                email = email,
                rol = "estudiante",
                materiaImpartida = null,
                salaAsignada = null
            )

            SupabaseManager.client.from("perfiles").insert(nuevoPerfil)

            Log.d(TAG, "Perfil insertado correctamente en la tabla")
            true

        } catch (e: Exception) {
            // Log detallado para saber si el error es de Auth o de la Tabla
            Log.e(TAG, "Error detallado en registro: ${e.localizedMessage}")
            e.printStackTrace()
            false
        }
    }
    suspend fun login(email: String): Usuario? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Intentando login para: $email")

            val result = SupabaseManager.client.from("perfiles")
                .select {
                    filter { eq("email", email) }
                }
                .decodeSingleOrNull<PerfilDto>()

            result?.let { dto ->
                Log.d(TAG, "Perfil encontrado: ${dto.rol}")

                if (dto.rol.equals("profesor", ignoreCase = true)) {
                    // MAPEÓ SEGURO DE MATERIA: Evita que la app muera si el texto no coincide exactamente
                    val materiaMapeada = Materia.entries.find {
                        it.nombre.equals(dto.materiaImpartida, ignoreCase = true)
                    } ?: Materia.MATEMATICAS // Valor por defecto si hay error en DB

                    Usuario.Profesor(
                        id = dto.id,
                        nombre = dto.nombre,
                        apellidos = dto.apellidos,
                        email = dto.email,
                        materiaImpartida = materiaMapeada,
                        salaAsignada = dto.salaAsignada ?: "No asignada"
                    )
                } else {
                    Usuario.Estudiante(
                        id = dto.id,
                        nombre = dto.nombre,
                        apellidos = dto.apellidos,
                        email = dto.email
                    )
                }
            }
        } catch (t: Throwable) {
            // CAPTURA FATAL: Esto capturará errores de librerías, serialización y red.
            Log.e(TAG, "!!! ERROR CRÍTICO EN LOGIN !!!")
            Log.e(TAG, "Mensaje: ${t.localizedMessage}")
            Log.e(TAG, "Causa: ${t.cause}")
            t.printStackTrace() // Esto imprime el stacktrace completo en el log
            null
        }
    }

    // --- CLASES ---
    suspend fun getTodasLasClases(): List<Clase> = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseManager.client.from("clases")
                .select()
                .decodeList<ClaseDto>()

            result.map { it.toDomain() }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener clases: ${e.message}")
            emptyList()
        }
    }

    suspend fun getClasesHoy(): List<Clase> = withContext(Dispatchers.IO) {
        val diasSemana = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        val calendar = Calendar.getInstance()
        val hoy = diasSemana[calendar.get(Calendar.DAY_OF_WEEK) - 1]

        try {
            getTodasLasClases().filter { clase ->
                clase.dias.any { dia -> dia.equals(hoy, ignoreCase = true) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- ASISTENCIA ---
    suspend fun marcarAsistenciaEstudiante(estudianteId: String, claseId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Intentar obtener registro actual
            val asistenciaActual = SupabaseManager.client.from("asistencia_estudiante")
                .select {
                    filter {
                        eq("estudiante_id", estudianteId)
                        eq("clase_id", claseId)
                    }
                }.decodeSingleOrNull<AsistenciaDto>()

            if (asistenciaActual != null) {
                // 2. Incrementar contador
                SupabaseManager.client.from("asistencia_estudiante")
                    .update({
                        set("asistencias_count", asistenciaActual.asistenciasCount + 1)
                    }) {
                        filter {
                            eq("estudiante_id", estudianteId)
                            eq("clase_id", claseId)
                        }
                    }
                true
            } else {
                // 3. Si no existe, crear el primer registro
                SupabaseManager.client.from("asistencia_estudiante").insert(
                    AsistenciaDto(estudianteId, claseId, 1)
                )
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al marcar asistencia: ${e.message}")
            false
        }
    }

    suspend fun getAsistenciaGeneral(estudianteId: String): Int = withContext(Dispatchers.IO) {
        try {
            val asistencias = SupabaseManager.client.from("asistencia_estudiante")
                .select { filter { eq("estudiante_id", estudianteId) } }
                .decodeList<AsistenciaDto>()

            val totalAsist = asistencias.sumOf { it.asistenciasCount }
            // Simulación: total clases dadas (esto debería venir de una tabla de registros)
            val totalClasesDadas = 20

            if (totalClasesDadas > 0) {
                ((totalAsist.toDouble() / totalClasesDadas) * 100).toInt().coerceAtMost(100)
            } else 0
        } catch (e: Exception) {
            0
        }
    }

    // --- HELPER MAPPERS ---
    private fun ClaseDto.toDomain() = Clase(
        id = this.id,
        nombre = this.nombre,
        profesorId = this.profesorId,
        profesor = "Profesor Titular",
        horario = this.horario,
        dias = this.dias,
        asistencias = 0,
        totalClases = this.totalClases
    )
}

@Serializable
data class AsistenciaDto(
    @SerialName("estudiante_id") val estudianteId: String,
    @SerialName("clase_id") val claseId: String,
    @SerialName("asistencias_count") val asistenciasCount: Int
)