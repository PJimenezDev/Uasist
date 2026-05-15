package com.dev.uasist.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dev.uasist.model.AsistenciaUiState
import com.dev.uasist.model.Usuario
import com.dev.uasist.viewmodel.ScannerViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dev.uasist.ui.theme.AttendError
import com.dev.uasist.ui.theme.AttendSuccess
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun QRScannerScreen(
    usuario: Usuario,
    viewModel: ScannerViewModel = viewModel(),
    onAsistenciaRegistrada: (String) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }

    // Lanzador para solicitar permisos
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            when (val state = uiState) {
                is AsistenciaUiState.Escaneando -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Vista de la cámara
                        AndroidView(
                            factory = { context ->
                                val previewView = PreviewView(context)
                                val executor = Executors.newSingleThreadExecutor()
                                val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)

                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()

                                    // Configurar Preview
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    // Configurar Analizador de ML Kit
                                    val scanner = BarcodeScanning.getClient(
                                        BarcodeScannerOptions.Builder()
                                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                                            .build()
                                    )

                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()

                                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null && viewModel.uiState.value is AsistenciaUiState.Escaneando) {
                                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                            scanner.process(image)
                                                .addOnSuccessListener { barcodes ->
                                                    for (barcode in barcodes) {
                                                        barcode.rawValue?.let { value ->
                                                            // Verificamos de nuevo el estado para no llamar a la DB varias veces
                                                            if (viewModel.uiState.value is AsistenciaUiState.Escaneando) {
                                                                viewModel.onQrDetected(usuario.id, value)
                                                            }
                                                        }
                                                    }
                                                }
                                                .addOnCompleteListener { imageProxy.close() }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalysis
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }, ContextCompat.getMainExecutor(context))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Overlay adaptativo
                        Surface(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = CircleShape
                        ) {
                            Text(
                                "Apunta al código QR del profesor",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                color = Color.White
                            )
                        }
                    }
                }
                is AsistenciaUiState.Exito -> {
                    StatusView(
                        icon = Icons.Default.CheckCircle,
                        title = "¡Asistencia Registrada!",
                        message = state.mensajeExito,
                        iconColor = AttendSuccess,
                        buttonText = "Escanear de nuevo",
                        onAction = { viewModel.resetScanner() }
                    )
                }
                is AsistenciaUiState.Error -> {
                    StatusView(
                        icon = Icons.Default.Error,
                        title = "Error",
                        message = state.mensaje,
                        iconColor = AttendError,
                        buttonText = "Reintentar",
                        onAction = { viewModel.resetScanner() }
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Se requiere permiso de cámara para escanear.")
            }
        }
    }
}

@Composable
fun StatusView(icon: ImageVector, title: String, message: String, iconColor: Color, buttonText: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)

        Button(
            onClick = onAction,
            modifier = Modifier.padding(top = 32.dp).fillMaxWidth().height(50.dp)
        ) {
            Text(buttonText)
        }
    }
}