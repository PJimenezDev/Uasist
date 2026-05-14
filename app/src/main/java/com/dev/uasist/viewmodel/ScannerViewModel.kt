package com.dev.uasist.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.uasist.data.AsistenciaRepository
import com.dev.uasist.model.AsistenciaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScannerViewModel : ViewModel() {
    private val repository = AsistenciaRepository()
    private val _uiState = MutableStateFlow<AsistenciaUiState>(AsistenciaUiState.Escaneando)
    val uiState = _uiState.asStateFlow()

    fun onQrDetected(estudianteId: String, contenidoQr: String) {
        // Bloqueo de seguridad: Si ya no estamos "Escaneando", no procesamos más
        if (_uiState.value !is AsistenciaUiState.Escaneando) return

        viewModelScope.launch {
            try {
                // 1. Extraer el UUID de la URL uasist://asistencia?claseId=428b1aa9...
                // Usamos una lógica más robusta por si Uri.parse falla con el esquema custom
                val claseId = if (contenidoQr.contains("claseId=")) {
                    contenidoQr.substringAfter("claseId=").substringBefore("&")
                } else {
                    contenidoQr // Fallback si el QR ya es solo el UUID
                }

                // Log para verificar en el Logcat que ahora sí es un UUID limpio
                Log.d("ScannerVM", "Contenido original: $contenidoQr")
                Log.d("ScannerVM", "UUID extraído: $claseId")

                if (claseId.isEmpty() || claseId.length < 32) {
                    _uiState.value = AsistenciaUiState.Error("Formato de QR no reconocido")
                    return@launch
                }

                // Cambiamos el estado a "Cargando" o algo similar si tuvieras, 
                // para que el UI detenga el escaneo inmediatamente.
                // Como no tienes "Cargando", simplemente procedemos, pero el check de arriba nos protege.

                // 2. Enviar el UUID LIMPIO al repositorio
                val exito = repository.registrarAsistencia(claseId, estudianteId)

                if (exito) {
                    _uiState.value = AsistenciaUiState.Exito("¡Asistencia registrada con éxito!")
                } else {
                    _uiState.value = AsistenciaUiState.Error("Error al registrar: Verifica tu conexión")
                }
            } catch (e: Exception) {
                Log.e("ScannerVM", "Error al procesar QR: ${e.message}")
                _uiState.value = AsistenciaUiState.Error("Error al leer el código")
            }
        }
    }

    fun resetScanner() {
        _uiState.value = AsistenciaUiState.Escaneando
    }
}