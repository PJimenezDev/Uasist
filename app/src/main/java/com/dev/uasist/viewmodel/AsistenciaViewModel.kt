package com.dev.uasist.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.uasist.model.AsistenciaUiState
import com.dev.uasist.model.Usuario
import com.dev.uasist.data.AsistenciaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AsistenciaViewModel : ViewModel() {

    private val repository = AsistenciaRepository()

    // Estado de la UI para el Alumno (Escáner)
    private val _uiState = MutableStateFlow<AsistenciaUiState>(AsistenciaUiState.Escaneando)
    val uiState: StateFlow<AsistenciaUiState> = _uiState.asStateFlow()

    // Lista de alumnos que se muestra en el Dashboard/Lista
    private val _asistentesRealtime = MutableStateFlow<List<Usuario.Estudiante>>(emptyList())
    val asistentesRealtime: StateFlow<List<Usuario.Estudiante>> = _asistentesRealtime.asStateFlow()

    // ID de la sesión activa
    private val _claseIdActiva = MutableStateFlow<String?>(null)
    val claseIdActiva = _claseIdActiva.asStateFlow()
    private var monitoreoJob: Job? = null

    /**
     * Inicia una nueva sesión de clase.
     */
    fun iniciarNuevaClase(profesorId: String, materia: String) {
        if (profesorId.isEmpty()) {
            Log.e("AsistenciaVM", "Error: El profesorId está vacío. No se puede iniciar clase.")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("AsistenciaVM", "Intentando crear clase para: $profesorId en $materia")

                val idGenerado = repository.crearClaseInmediata(profesorId, materia)

                if (idGenerado != null) {
                    Log.d("AsistenciaVM", "¡Clase creada con éxito! ID: $idGenerado")
                    
                    // Al iniciar clase, empezamos a escuchar solo a los que entren a ESTA clase
                    // Lo hacemos ANTES de actualizar el ID activa para que el monitoreo esté listo
                    escucharAsistenciaClaseActual(idGenerado)
                    
                    _claseIdActiva.value = idGenerado
                } else {
                    Log.e("AsistenciaVM", "El repositorio devolvió un ID nulo. Revisa la conexión o la tabla 'clases'.")
                }
            } catch (e: Exception) {
                Log.e("AsistenciaVM", "Error fatal al iniciar clase: ${e.message}")
            }
        }
    }

    /**
     * Escucha en tiempo real solo a los alumnos que se registran en la clase que acaba de abrirse.
     */
    private fun escucharAsistenciaClaseActual(claseId: String) {
        monitoreoJob?.cancel() // Cancelamos monitoreos previos
        monitoreoJob = viewModelScope.launch {
            try {
                repository.observarAsistentesPorClase(claseId).collect { lista ->
                    val estudiantes = lista.map { dto ->
                        Usuario.Estudiante(
                            id = dto.estudianteId,
                            nombre = dto.nombre,
                            apellidos = dto.apellidos,
                            email = dto.email
                        )
                    }
                    _asistentesRealtime.value = estudiantes
                    Log.d("AsistenciaVM", "Asistentes actualizados: ${estudiantes.size} alumnos presentes.")
                }
            } catch (e: Exception) {
                Log.e("AsistenciaVM", "Error en el monitoreo Realtime: ${e.message}")
            }
        }
    }

    /**
     * Monitorea el HISTORIAL TOTAL de un profesor (todas sus clases pasadas).
     * Se usa para la pantalla de AsistenciaAlumnosScreen.
     */
    fun monitorearAsistencia(profesorId: String) {
        viewModelScope.launch {
            repository.observarHistorialAsistentes(profesorId).collect { lista ->
                _asistentesRealtime.value = lista.map { dto ->
                    Usuario.Estudiante(dto.estudianteId, dto.nombre, dto.apellidos, dto.email)
                }
            }
        }
    }
    /**
     * Maneja la detección de QR del lado del alumno
     */
    fun onQrDetectado(codigoQr: String) {
        if (codigoQr.isNotEmpty()) {
            _uiState.value = AsistenciaUiState.Exito(codigoQr)
        } else {
            _uiState.value = AsistenciaUiState.Error("Código QR no válido")
        }
    }

    fun finalizarSesion() {
        Log.d("AsistenciaVM", "Finalizando sesión: ${_claseIdActiva.value}")
        _claseIdActiva.value = null
        monitoreoJob?.cancel()
        _asistentesRealtime.value = emptyList() // Limpiamos la lista para la siguiente clase
    }


    fun reiniciarEscaner() {
        _uiState.value = AsistenciaUiState.Escaneando
    }
}