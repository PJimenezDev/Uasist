package com.dev.uasist.viewmodel

import androidx.lifecycle.ViewModel
import com.dev.uasist.model.AsistenciaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AsistenciaViewModel : ViewModel() {

    // El estado interno que podemos modificar
    private val _uiState = MutableStateFlow<AsistenciaUiState>(AsistenciaUiState.Escaneando)
    // El estado público que la vista (Compose) va a escuchar
    val uiState: StateFlow<AsistenciaUiState> = _uiState.asStateFlow()

    /**
     * Esta función se llamará desde la UI nativa cuando la cámara detecte un QR
     */
    fun onQrDetectado(codigoQr: String) {
        // Aquí iría la lógica para procesar la asistencia.
        // Como indicaste que por ahora es "Sin conexión a APIs",
        // simplemente cambiaremos el estado a "Éxito".

        if (codigoQr.isNotEmpty()) {
            _uiState.value = AsistenciaUiState.Exito(codigoQr)
        } else {
            _uiState.value = AsistenciaUiState.Error("Código QR no válido")
        }
    }

    /**
     * Permite al usuario volver a escanear
     */
    fun reiniciarEscaner() {
        _uiState.value = AsistenciaUiState.Escaneando
    }

    // Aquí podrías agregar más funciones relacionadas con la asistencia,
    // como validar el código QR, manejar errores específicos, etc.
}