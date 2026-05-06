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
import java.util.Calendar

class AsistenciaRepository {

    companion object {
        private const val TAG = "AsistenciaRepository"
    }

    /**
     * REGISTRO: Crea usuario en Auth e inserta perfil en la tabla pública.
     */
    suspend fun signUp(email: String, pass: String, nombre: String, apellidos: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Aseguramos que no haya sesiones activas previas
            SupabaseManager.client.auth.signOut()

            // 1. Crear usuario en Auth
            val authResponse = SupabaseManager.client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }

            // En versiones nuevas del SDK, el ID está en authResponse o en el usuario creado
            val userId = authResponse?.id ?: SupabaseManager.client.auth.currentUserOrNull()?.id

            if (userId == null) {
                Log.e(TAG, "Auth exitoso pero no se pudo obtener el ID del usuario")
                return@withContext false
            }

            Log.d(TAG, "Usuario en Auth: $userId. Creando perfil...")

            // 2. Insertar perfil
            // Nota: "estudiante" debe coincidir con el valor de tu ENUM en Postgres
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

            Log.d(TAG, "Registro completo exitoso")
            true

        } catch (e: Exception) {
            Log.e(TAG, "ERROR EN REGISTRO: ${e.localizedMessage}")
            // Si falla el insert de perfil pero el auth se creó, aquí podrías manejarlo
            false
        }
    }

    /**
     * LOGIN: Obtiene los datos del perfil según el email
     */
    suspend fun login(email: String): Usuario? = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseManager.client.from("perfiles")
                .select {
                    filter { eq("email", email) }
                }
                .decodeSingleOrNull<PerfilDto>()

            result?.let { dto ->
                if (dto.rol.equals("profesor", ignoreCase = true)) {
                    val materiaMapeada = Materia.entries.find {
                        it.nombre.equals(dto.materiaImpartida, ignoreCase = true)
                    } ?: Materia.MATEMATICAS

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
                        id = dto.id, nombre = dto.nombre,
                        apellidos = dto.apellidos, email = dto.email
                    )
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error crítico en login: ${t.localizedMessage}")
            null
        }
    }

    // --- RECUPERACIÓN DE CONTRASEÑA ---
    suspend fun enviarCorreoRecuperacion(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // El SDK usa resetPasswordForEmail
            SupabaseManager.client.auth.resetPasswordForEmail(
                email = email,
                redirectUrl = "uasist://reset-password"
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando recuperación: ${e.message}")
            false
        }
    }

    suspend fun actualizarPassword(nuevaPassword: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // modifyUser en la versión 2.x del SDK
            SupabaseManager.client.auth.modifyUser {
                password = nuevaPassword
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al cambiar contraseña: ${e.message}")
            false
        }
    }

    // --- CLASES ---

    suspend fun getTodasLasClases(estudianteId: String? = null): List<Clase> = withContext(Dispatchers.IO) {
        try {
            val clasesDto = SupabaseManager.client.from("clases")
                .select()
                .decodeList<ClaseDto>()

            val asistencias = if (estudianteId != null) {
                SupabaseManager.client.from("asistencia_estudiante")
                    .select {
                        filter { eq("estudiante_id", estudianteId) }
                    }.decodeList<AsistenciaDto>()
            } else {
                emptyList()
            }

            clasesDto.map { dto ->
                val asistencia = asistencias.find { it.claseId == dto.id }
                dto.toDomain(asistencia?.asistenciasCount ?: 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clases: ${e.message}")
            emptyList()
        }
    }

    suspend fun getClasesHoy(estudianteId: String? = null): List<Clase> = withContext(Dispatchers.IO) {
        val diasSemana = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        val calendar = Calendar.getInstance()
        val hoy = diasSemana[calendar.get(Calendar.DAY_OF_WEEK) - 1]

        getTodasLasClases(estudianteId).filter { clase ->
            clase.dias.any { it.equals(hoy, ignoreCase = true) }
        }
    }

    // --- ASISTENCIA ---

    suspend fun getAsistenciaGeneral(estudianteId: String): Int = withContext(Dispatchers.IO) {
        try {
            val asistencias = SupabaseManager.client.from("asistencia_estudiante")
                .select {
                    filter { eq("estudiante_id", estudianteId) }
                }.decodeList<AsistenciaDto>()

            if (asistencias.isEmpty()) return@withContext 0

            val clases = getTodasLasClases()
            var totalAsistencias = 0
            var totalPosibles = 0

            asistencias.forEach { asis ->
                val clase = clases.find { it.id == asis.claseId }
                if (clase != null) {
                    totalAsistencias += asis.asistenciasCount
                    totalPosibles += clase.totalClases
                }
            }

            if (totalPosibles == 0) 0 else (totalAsistencias * 100) / totalPosibles
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener asistencia general: ${e.message}")
            0
        }
    }

    suspend fun marcarAsistenciaEstudiante(estudianteId: String, claseId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val asistenciaActual = SupabaseManager.client.from("asistencia_estudiante")
                .select {
                    filter {
                        eq("estudiante_id", estudianteId)
                        eq("clase_id", claseId)
                    }
                }.decodeSingleOrNull<AsistenciaDto>()

            if (asistenciaActual != null) {
                SupabaseManager.client.from("asistencia_estudiante")
                    .update({
                        set("asistencias_count", asistenciaActual.asistenciasCount + 1)
                    }) {
                        filter {
                            eq("estudiante_id", estudianteId)
                            eq("clase_id", claseId)
                        }
                    }
            } else {
                SupabaseManager.client.from("asistencia_estudiante").insert(
                    AsistenciaDto(estudianteId, claseId, 1)
                )
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error asistencia: ${e.message}")
            false
        }
    }

    private fun ClaseDto.toDomain(asistenciasCount: Int = 0) = Clase(
        id = this.id,
        nombre = this.nombre,
        profesorId = this.profesorId,
        profesor = "Profesor Titular",
        horario = this.horario,
        dias = this.dias,
        asistencias = asistenciasCount,
        totalClases = this.totalClases
    )
}