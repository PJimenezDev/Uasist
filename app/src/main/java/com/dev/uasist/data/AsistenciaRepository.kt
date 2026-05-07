package com.dev.uasist.data

import android.util.Log
import com.dev.uasist.data.dto.*
import com.dev.uasist.data.network.SupabaseManager
import com.dev.uasist.model.*
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import com.dev.uasist.data.dto.AsistenciaDetalladaDto

class AsistenciaRepository {

    companion object {
        private const val TAG = "AsistenciaRepository"
    }

    /**
     * ESCUCHA EN TIEMPO REAL: Alumnos presentes en una clase específica.
     * Esta es la función que faltaba.
     */
    @OptIn(SupabaseExperimental::class)
    fun observarAsistentesPorClase(claseId: String): Flow<List<AsistenciaDetalladaDto>> {
        return SupabaseManager.client.from("detalles_asistencia_view")
            .selectAsFlow(
                primaryKey = AsistenciaDetalladaDto::estudianteId,
                filter = FilterOperation("clase_id", FilterOperator.EQ, claseId)
            )
    }

    /**
     * ESCUCHA EN TIEMPO REAL: Historial total de un profesor.
     */
    @OptIn(SupabaseExperimental::class)
    fun observarHistorialAsistentes(profesorId: String): Flow<List<AsistenciaDetalladaDto>> {
        return SupabaseManager.client.from("detalles_asistencia_view")
            .selectAsFlow(
                primaryKey = AsistenciaDetalladaDto::estudianteId,
                filter = FilterOperation("profesor_id", FilterOperator.EQ, profesorId)
            )
    }

    /**
     * Crea una nueva sesión de clase y devuelve su ID.
     */
    suspend fun crearClaseInmediata(profesorId: String, materiaNombre: String): String? = withContext(Dispatchers.IO) {
        try {
            val nuevaClase = ClaseDto(
                id = java.util.UUID.randomUUID().toString(),
                nombre = "Clase de Hoy",
                materia = materiaNombre,
                profesorId = profesorId,
                horario = "Sesión Activa",
                dias = listOf("Hoy"),
                totalClases = 1
            )

            Log.d(TAG, "Insertando en Supabase: $nuevaClase")

            // Insertamos y esperamos confirmación
            SupabaseManager.client.from("clases").insert(nuevaClase)

            Log.d(TAG, "¡Inserción exitosa en Postgres!")
            nuevaClase.id

        } catch (e: Throwable) {
            Log.e(TAG, "Error CRÍTICO en Supabase: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtiene la información de un perfil por ID (para el nombre del alumno)
     */
    suspend fun obtenerPerfilPorId(id: String): PerfilDto? = withContext(Dispatchers.IO) {
        try {
            SupabaseManager.client.from("perfiles")
                .select { filter { eq("id", id) } }
                .decodeSingleOrNull<PerfilDto>()
        } catch (e: Exception) {
            null
        }
    }

    // --- AUTENTICACIÓN Y PERFILES ---

    suspend fun login(email: String): Usuario? = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseManager.client.from("perfiles")
                .select { filter { eq("email", email) } }
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
                    Usuario.Estudiante(dto.id, dto.nombre, dto.apellidos, dto.email)
                }
            }
        } catch (t: Throwable) {
            null
        }
    }

    suspend fun signUp(email: String, pass: String, nombre: String, apellidos: String): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseManager.client.auth.signOut()
            val authResponse = SupabaseManager.client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }
            val userId = authResponse?.id ?: SupabaseManager.client.auth.currentUserOrNull()?.id ?: return@withContext false

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
            true
        } catch (e: Exception) {
            false
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
            // updateUser en la versión 2.x del SDK
            SupabaseManager.client.auth.updateUser {
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

    // --- MÉTODOS DE ASISTENCIA ---

    suspend fun marcarAsistenciaEstudiante(estudianteId: String, claseId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Lógica para insertar o actualizar contador de asistencia
            val asistenciaActual = SupabaseManager.client.from("asistencia_estudiante")
                .select {
                    filter {
                        eq("estudiante_id", estudianteId)
                        eq("clase_id", claseId)
                    }
                }.decodeSingleOrNull<AsistenciaDto>()

            if (asistenciaActual != null) {
                SupabaseManager.client.from("asistencia_estudiante").update(
                    { set("asistencias_count", asistenciaActual.asistenciasCount + 1) }
                ) {
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