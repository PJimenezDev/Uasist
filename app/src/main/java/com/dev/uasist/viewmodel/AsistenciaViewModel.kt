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

    // Estado de la UI para el Alumno (Escáner)
    private val _uiState = MutableStateFlow<AsistenciaUiState>(AsistenciaUiState.Escaneando)
    val uiState: StateFlow<AsistenciaUiState> = _uiState.asStateFlow()

    // Lista de alumnos que se muestra en el Dashboard/Lista
    private val _asistentesRealtime = MutableStateFlow<List<EstudianteConAsistencia>>(emptyList())
    val asistentesRealtime: StateFlow<List<EstudianteConAsistencia>> = _asistentesRealtime.asStateFlow()
    // ID de la sesión activa
    private val _claseIdActiva = MutableStateFlow<String?>(null)
    val claseIdActiva = _claseIdActiva.asStateFlow()
    private var monitoreoJob: Job? = null

    // Evento de navegación para ir al QR solo al crear la clase
    private val _navegarAQR = MutableSharedFlow<String>()
    val navegarAQR = _navegarAQR.asSharedFlow()

    /**
     * IMPORTANTE: Llama a esto en el Dashboard del profesor para recuperar la sesión
     * si el profesor salió de la app o cambió de pantalla.
     */
    fun cargarClaseActiva(profesorId: String) {
        if (_claseIdActiva.value != null) return // Ya está cargada

        viewModelScope.launch {
            try {
                // Debes implementar 'obtenerClaseActiva' en tu repositorio
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
     * Inicia una nueva sesión de clase.
     */
    @RequiresApi(Build.VERSION_CODES.O)
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
                    _navegarAQR.emit(idGenerado) // Notificamos que debe navegar
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
                // El repositorio ahora devuelve Flow<List<EstudianteConAsistencia>>
                repository.observarAsistentesPorClase(claseId).collect { lista ->
                    // Simplemente asignamos la lista, ya que _asistentesRealtime
                    // ahora debe ser del tipo List<EstudianteConAsistencia>
                    _asistentesRealtime.value = lista
                    Log.d("AsistenciaVM", "Asistentes actualizados: ${lista.size} alumnos con su porcentaje.")
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
                _asistentesRealtime.value = lista
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
        val idAByPass = _claseIdActiva.value // Obtenemos el ID antes de limpiar

        if (idAByPass != null) {
            viewModelScope.launch {
                try {
                    Log.d("AsistenciaVM", "Finalizando sesión en BD: $idAByPass")

                    // 1. LLAMADA AL REPOSITORIO (La función que me pasaste)
                    val exito = repository.finalizarClase(idAByPass)

                    if (exito) {
                        // 2. Solo si se actualizó en la nube, limpiamos el estado local
                        _claseIdActiva.value = null
                        monitoreoJob?.cancel()
                        _asistentesRealtime.value = emptyList()
                        Log.d("AsistenciaVM", "Sesión cerrada exitosamente.")
                    }
                } catch (e: Exception) {
                    Log.e("AsistenciaVM", "Error al cerrar sesión: ${e.message}")
                }
            }
        } else {
            // Caso borde: Si por alguna razón el ID local era null, nos aseguramos de limpiar
            _claseIdActiva.value = null
            _asistentesRealtime.value = emptyList()
        }
    }


    fun reiniciarEscaner() {
        _uiState.value = AsistenciaUiState.Escaneando
    }

    // Aquí podrías agregar más funciones relacionadas con la asistencia,
    // como validar el código QR, manejar errores específicos, etc.
}