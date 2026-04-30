package com.dev.uasist.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dev.uasist.model.AsistenciaUiState
import com.dev.uasist.viewmodel.AsistenciaViewModel

// Colores extraídos de tu default_shadcn_theme.css
val ShadcnBackground = Color(0xFFFFFFFF)
val ShadcnPrimary = Color(0xFF030213)
val ShadcnMutedForeground = Color(0xFF717182)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaScreen(
    // Inyectamos el ViewModel
    viewModel: AsistenciaViewModel = viewModel()
) {
    // Observamos el estado del ViewModel para que la UI reaccione automáticamente
    val estadoActual by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asistencia", color = ShadcnBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ShadcnPrimary)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ShadcnBackground)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {

            // Reaccionamos según el estado actual
            when (estadoActual) {
                is AsistenciaUiState.Escaneando -> {
                    VistaEscanerActivo { qrLeido ->
                        // Cuando la cámara lea algo, avisamos al ViewModel
                        viewModel.onQrDetectado(qrLeido)
                    }
                }
                is AsistenciaUiState.Exito -> {
                    val codigo = (estadoActual as AsistenciaUiState.Exito).mensajeExito
                    VistaExito(codigo) {
                        viewModel.reiniciarEscaner()
                    }
                }
                is AsistenciaUiState.Error -> {
                    val error = (estadoActual as AsistenciaUiState.Error).mensaje
                    Text(text = "Error: $error", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun VistaEscanerActivo(onQrScanned: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Aquí en un futuro irá tu AndroidView con CameraX para mostrar la cámara
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black)
        )

        // Marco central
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.Center)
                .border(3.dp, ShadcnBackground, RoundedCornerShape(16.dp))
        )

        // Textos de instrucciones
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Apunta al QR del Profesor", color = ShadcnBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Centra el código en el recuadro", color = Color.LightGray, fontSize = 14.sp)

            // Botón simulador (Para que puedas probar sin cámara)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onQrScanned("SIMULACION_QR_12345") },
                colors = ButtonDefaults.buttonColors(containerColor = ShadcnPrimary)
            ) {
                Text("Simular Lectura QR")
            }
        }
    }
}

@Composable
fun VistaExito(codigo: String, onVolver: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("¡Asistencia Registrada!", color = ShadcnPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Código: $codigo", color = ShadcnMutedForeground, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onVolver,
            colors = ButtonDefaults.buttonColors(containerColor = ShadcnPrimary)
        ) {
            Text("Escanear otro", color = ShadcnBackground)
        }
    }
}