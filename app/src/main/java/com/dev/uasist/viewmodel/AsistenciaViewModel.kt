package com.dev.uasist.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.uasist.model.AsistenciaUiState
import com.dev.uasist.model.Usuario
import com.dev.uasist.data.AsistenciaRepository
import com.dev.uasist.model.EstudianteConAsistencia
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AsistenciaViewModel : ViewModel() {

    private val repository = AsistenciaRepository()

    // --- 1. ESTADOS DE LA UI ---

    // Estado para el Escáner del Alumno
    private val _uiState = MutableStateFlow<AsistenciaUiState>(AsistenciaUiState.Escaneando)
    val uiState: StateFlow<AsistenciaUiState> = _uiState.asStateFlow()

    // ID de la sesión activa del profesor
    private val _claseIdActiva = MutableStateFlow<String?>(null)
    val claseIdActiva = _claseIdActiva.asStateFlow()

    // Lista de alumnos presentes (Realtime)
    private val _asistentesRealtime = MutableStateFlow<List<EstudianteConAsistencia>>(emptyList())
    val asistentesRealtime: StateFlow<List<EstudianteConAsistencia>> = _asistentesRealtime.asStateFlow()

    // Evento para navegar al QR (SharedFlow para disparar eventos una sola vez)
    private val _navegarAQR = MutableSharedFlow<String>()
    val navegarAQR = _navegarAQR.asSharedFlow()

    private var monitoreoJob: Job? = null

    // --- 2. FLUJO DEL PROFESOR (GESTIÓN DE SESIÓN) ---

    /**
     * Recupera una sesión activa si el profesor salió de la app.
     */
    fun cargarClaseActiva(profesorId: String) {
        if (_claseIdActiva.value != null) return

        viewModelScope.launch {
            try {
                val claseExistente = repository.obtenerClaseActiva(profesorId)
                if (claseExistente != null) {
                    _claseIdActiva.value = claseExistente.id
                    escucharAsistenciaClaseActual(claseExistente.id)
                }
            } catch (e: Exception) {
                Log.e("AsistenciaVM", "Error al recuperar sesión: ${e.message}")
            }
        }
    }

    /**
     * Inicia una nueva clase y emite el evento de navegación al QR.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun iniciarNuevaClase(profesorId: String, materia: String) {
        if (profesorId.isEmpty()) {
            Log.e("AsistenciaVM", "Error: profesorId vacío")
            return
        }

        viewModelScope.launch {
            try {
                val idGenerado = repository.crearClaseInmediata(profesorId, materia)
                if (idGenerado != null) {
                    escucharAsistenciaClaseActual(idGenerado)
                    _claseIdActiva.value = idGenerado
                    _navegarAQR.emit(idGenerado)
                }
            } catch (e: Exception) {
                Log.e("AsistenciaVM", "Error al iniciar clase: ${e.message}")
            }
        }
    }

    /**
     * Escucha en tiempo real a los alumnos de la clase actual.
     */
    private fun escucharAsistenciaClaseActual(claseId: String) {
        monitoreoJob?.cancel()
        monitoreoJob = viewModelScope.launch {
            try {
                repository.observarAsistentesPorClase(claseId).collect { lista ->
                    _asistentesRealtime.value = lista
                    Log.d("AsistenciaVM", "Actualización Realtime: ${lista.size} alumnos")
                }
            } catch (e: Exception) {
                Log.e("AsistenciaVM", "Error en monitoreo Realtime: ${e.message}")
            }
        }
    }

    /**
     * Finaliza la sesión actual y limpia los estados locales.
     */
    fun finalizarSesion() {
        val idClase = _claseIdActiva.value

        if (idClase != null) {
            viewModelScope.launch {
                try {
                    val exito = repository.finalizarClase(idClase)
                    if (exito) {
                        _claseIdActiva.value = null
                        monitoreoJob?.cancel()
                        _asistentesRealtime.value = emptyList()
                    }
                } catch (e: Exception) {
                    Log.e("AsistenciaVM", "Error al cerrar sesión: ${e.message}")
                }
            }
        } else {
            _claseIdActiva.value = null
            _asistentesRealtime.value = emptyList()
        }
    }

    /**
     * Monitorea el historial completo para la pantalla de lista de alumnos.
     */
    fun monitorearAsistencia(profesorId: String) {
        viewModelScope.launch {
            repository.observarHistorialAsistentes(profesorId).collect { lista ->
                _asistentesRealtime.value = lista
            }
        }
    }

    // --- 3. FLUJO DEL ALUMNO ---

    /**
     * Maneja el código QR detectado por la cámara.
     */
    fun onQrDetectado(codigoQr: String) {
        if (codigoQr.isNotEmpty()) {
            _uiState.value = AsistenciaUiState.Exito(codigoQr)
        } else {
            _uiState.value = AsistenciaUiState.Error("Código QR no válido")
        }
    }
}