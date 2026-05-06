package com.dev.uasist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.uasist.data.AsistenciaRepository
import com.dev.uasist.model.AsistenciaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScannerViewModel(
    private val repository: AsistenciaRepository = AsistenciaRepository(), // Inyección simple
) : ViewModel() {

    // Inicializamos con el estado "Escaneando"
    private val _uiState = MutableStateFlow<AsistenciaUiState>(AsistenciaUiState.Escaneando)
    val uiState: StateFlow<AsistenciaUiState> = _uiState

    fun onQrDetected(estudianteId: String, codigoClaseQr: String) {
        if (codigoClaseQr.isNotBlank()) {
            viewModelScope.launch {
                // Llama a la función del repositorio (que es suspend)
                val exito = repository.marcarAsistenciaEstudiante(estudianteId, codigoClaseQr)

                if (exito) {
                    // Pasamos al estado de Éxito
                    _uiState.value = AsistenciaUiState.Exito("Clase: $codigoClaseQr")
                } else {
                    // Pasamos al estado de Error (Lógica de negocio)
                    _uiState.value = AsistenciaUiState.Error("Ya estás registrado o código inválido")
                }
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