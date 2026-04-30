package com.dev.uasist.model

sealed class AsistenciaUiState {
    object Escaneando : AsistenciaUiState()
    data class Exito(val mensajeExito: String) : AsistenciaUiState()
    data class Error(val mensaje: String) : AsistenciaUiState()
}