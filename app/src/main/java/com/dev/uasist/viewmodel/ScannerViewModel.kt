package com.dev.uasist.viewmodel

import androidx.lifecycle.ViewModel
import com.dev.uasist.data.AsistenciaRepository
import com.dev.uasist.model.AsistenciaUiState // Importamos nuestro estado en español
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScannerViewModel(
    private val repository: AsistenciaRepository = AsistenciaRepository() // Inyección simple
) : ViewModel() {

    // Inicializamos con el estado "Escaneando"
    private val _uiState = MutableStateFlow<AsistenciaUiState>(AsistenciaUiState.Escaneando)
    val uiState: StateFlow<AsistenciaUiState> = _uiState

    // Supongamos que el alumno logueado es est1
    private val currentAlumnoId = "est1"

    fun onQrDetected(codigoClaseQr: String) {
        if (codigoClaseQr.isNotBlank()) {
            // Llama a la función del repositorio
            val exito = repository.marcarAsistenciaEstudiante(currentAlumnoId, codigoClaseQr)

            if (exito) {
                // Pasamos al estado de Éxito
                _uiState.value = AsistenciaUiState.Exito("Clase: $codigoClaseQr")
            } else {
                // Pasamos al estado de Error (Lógica de negocio)
                _uiState.value = AsistenciaUiState.Error("Ya estás registrado o código inválido")
            }
        } else {
            // Pasamos al estado de Error (Lectura fallida)
            _uiState.value = AsistenciaUiState.Error("Código QR no legible")
        }
    }

    fun resetScanner() {
        // Volvemos a activar la cámara
        _uiState.value = AsistenciaUiState.Escaneando
    }
}