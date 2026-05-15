package com.dev.uasist.data

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import kotlinx.coroutines.flow.map

class AsistenciaRepository {

    companion object {
        private const val TAG = "AsistenciaRepository"
    }

    // --- 1. AUTENTICACIÓN Y PERFILES ---

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

    suspend fun enviarCorreoRecuperacion(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
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
            SupabaseManager.client.auth.updateUser {
                password = nuevaPassword
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al cambiar contraseña: ${e.message}")
            false
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

    // --- 2. GESTIÓN DE CLASES ---

    /**
     * Crea una nueva sesión de clase y devuelve su ID.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun crearClaseInmediata(profesorId: String, materiaNombre: String): String? = withContext(Dispatchers.IO) {
        try {
            val nuevaClase = ClaseDto(
                id = java.util.UUID.randomUUID().toString(),
                nombre = "Clase de $materiaNombre - ${java.time.LocalDate.now()}",
                materia = materiaNombre,
                profesorId = profesorId,
                horario = "Sesión Activa", // Marcador de búsqueda
                dias = listOf("Hoy"),
                totalClases = 1
            )
            SupabaseManager.client.from("clases").insert(nuevaClase)
            nuevaClase.id
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear: ${e.message}")
            null
        }
    }

    /**
     * Busca una clase que no haya sido finalizada para un profesor específico.
     * Esto permite que el Dashboard recupere el estado si la app se cierra.
     */
    suspend fun obtenerClaseActiva(profesorId: String): ClaseDto? = withContext(Dispatchers.IO) {
        try {
            SupabaseManager.client.from("clases")
                .select {
                    filter {
                        eq("profesor_id", profesorId)
                        eq("horario", "Sesión Activa")
                    }
                }
                .decodeList<ClaseDto>()
                .lastOrNull() // Retorna la más reciente creada
        } catch (e: Throwable) {
            Log.e(TAG, "Error al obtener clase activa: ${e.message}")
            null
        }
    }

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

    /**
     * IMPORTANTE: Cambia el estado de la clase para que obtenerClaseActiva
     * ya no la detecte como abierta.
     */
    suspend fun finalizarClase(claseId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseManager.client.from("clases").update(
                { set("horario", "Finalizada") }
            ) {
                filter { eq("id", claseId) }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al finalizar clase: ${e.message}")
            false
        }
    }

    // --- 3. REGISTRO DE ASISTENCIA ---

    // PARA REGISTRAR: Usamos UPSERT para evitar errores por doble escaneo
    suspend fun registrarAsistencia(claseId: String, estudianteId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val tabla = SupabaseManager.client.from("asistencia_estudiante")

            val asistencia = AsistenciaDto(
                estudianteId = estudianteId,
                claseId = claseId,
                asistenciasCount = 1
            )

            // upsert() inserta si no existe, o actualiza si ya existe (basado en PK)
            tabla.upsert(asistencia)
            true
        } catch (e: Exception) {
            Log.e("Repo", "Error crítico al registrar: ${e.message}")
            false
        }
    }

    // --- 4. OBSERVACIÓN EN TIEMPO REAL ---

    /**
     * ESCUCHA EN TIEMPO REAL: Alumnos presentes en una clase específica.
     */
    @OptIn(SupabaseExperimental::class)
    fun observarAsistentesPorClase(claseId: String): Flow<List<EstudianteConAsistencia>> {
        return SupabaseManager.client.from("detalles_asistencia_view")
            .selectAsFlow(
                primaryKey = AsistenciaDetalladaDto::estudianteId,
                filter = FilterOperation("clase_id", FilterOperator.EQ, claseId)
            ).map { lista ->
                lista.map { dto ->
                    val porcentajeCalculado = if (dto.totalClases > 0) {
                        ((dto.asistenciasCount.toDouble() / dto.totalClases) * 100).toInt()
                    } else 0

                    EstudianteConAsistencia(
                        id = dto.estudianteId,
                        nombreCompleto = "${dto.nombre} ${dto.apellidos}",
                        email = dto.email,
                        porcentaje = porcentajeCalculado
                    )
                }
            }
    }

    /**
     * ESCUCHA EN TIEMPO REAL: Historial total de un profesor.
     */
    @OptIn(SupabaseExperimental::class)
    fun observarHistorialAsistentes(profesorId: String): Flow<List<EstudianteConAsistencia>> {
        return SupabaseManager.client.from("detalles_asistencia_view")
            .selectAsFlow(
                primaryKey = AsistenciaDetalladaDto::estudianteId,
                filter = FilterOperation("profesor_id", FilterOperator.EQ, profesorId)
            ).map { lista ->
                lista.map { dto ->
                    val porcentajeCalculado = if (dto.totalClases > 0) {
                        ((dto.asistenciasCount.toDouble() / dto.totalClases.toDouble()) * 100).toInt()
                    } else 0

                    EstudianteConAsistencia(
                        id = dto.estudianteId,
                        nombreCompleto = "${dto.nombre} ${dto.apellidos}",
                        email = dto.email,
                        porcentaje = porcentajeCalculado
                    )
                }
            }
    }

    // --- 5. ESTADÍSTICAS Y RESÚMENES ---

    suspend fun getAsistenciaGeneral(estudianteId: String): Int = withContext(Dispatchers.IO) {
        try {
            val asistencias = SupabaseManager.client.from("asistencia_estudiante")
                .select {
                    filter { eq("estudiante_id", estudianteId) }
                }.decodeList<AsistenciaDto>()

            if (asistencias.isEmpty()) return@withContext 0

            val todasLasClases = SupabaseManager.client.from("clases")
                .select()
                .decodeList<ClaseDto>()

            var totalAsistenciasAlumno = 0
            var totalClasesDictadas = 0

            asistencias.forEach { asis ->
                totalAsistenciasAlumno += asis.asistenciasCount
            }

            todasLasClases.forEach { clase ->
                totalClasesDictadas += clase.totalClases
            }

            if (totalClasesDictadas == 0) return@withContext 0

            ((totalAsistenciasAlumno.toDouble() / totalClasesDictadas) * 100).toInt()

        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular porcentaje: ${e.message}")
            0
        }
    }

    suspend fun getResumenAsistenciaPorMateria(estudianteId: String): List<Clase> = withContext(Dispatchers.IO) {
        try {
            val perfilesProfesores = SupabaseManager.client.from("perfiles")
                .select { filter { eq("rol", "profesor") } }
                .decodeList<PerfilDto>()

            val todasLasClases = SupabaseManager.client.from("clases").select().decodeList<ClaseDto>()
            val asistencias = SupabaseManager.client.from("asistencia_estudiante")
                .select { filter { eq("estudiante_id", estudianteId) } }
                .decodeList<AsistenciaDto>()

            val resumen = todasLasClases.groupBy { it.materia }.map { (materia, sesiones) ->
                val primerProfesorId = sesiones.firstOrNull()?.profesorId ?: ""

                val perfilProfesor = perfilesProfesores.find { it.id == primerProfesorId }
                val nombreCompleto = if (perfilProfesor != null) {
                    "${perfilProfesor.nombre} ${perfilProfesor.apellidos}"
                } else {
                    "Profesor no asignado"
                }

                val totalClasesMateria = sesiones.sumOf { it.totalClases }
                val idSesiones = sesiones.map { it.id }
                val asistenciasEnMateria = asistencias
                    .filter { it.claseId in idSesiones }
                    .sumOf { it.asistenciasCount }

                Clase(
                    id = materia,
                    nombre = materia,
                    profesorId = primerProfesorId,
                    profesor = nombreCompleto,
                    horario = sesiones.firstOrNull()?.horario ?: "Horario variable",
                    dias = sesiones.flatMap { it.dias }.distinct(),
                    asistencias = asistenciasEnMateria,
                    totalClases = totalClasesMateria
                )
            }
            resumen
        } catch (e: Exception) {
            Log.e("Repo", "Error resumen con nombres: ${e.message}")
            emptyList()
        }
    }

    // --- 6. MÉTODOS AUXILIARES ---

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