package com.dev.uasist.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.dev.uasist.utils.QrGenerator
import com.dev.uasist.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun GenerarQRScreen(
    usuario: Usuario,
    claseId: String,
    materiaNombre: String
) {
    var bitmapQR by remember { mutableStateOf<Bitmap?>(null) }
    var segundosRestantes by remember { mutableIntStateOf(300) }
    var cargando by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val materiaLegible = remember(materiaNombre) {
        try {
            java.net.URLDecoder.decode(materiaNombre, "UTF-8")
        } catch (e: Exception) {
            materiaNombre
        }
    }

    // Efecto para generar el QR automáticamente al entrar
    LaunchedEffect(claseId) {
        if (claseId.isNotEmpty() && claseId != "error") {
            try {
                cargando = true
                val contenidoQR = "uasist://asistencia?claseId=$claseId"

                // Generación en hilo secundario
                val result = withContext(Dispatchers.Default) {
                    QrGenerator.generarBitmapQR(contenidoQR, 512)
                }
                
                if (result != null) {
                    bitmapQR = result
                } else {
                    errorMsg = "No se pudo generar el código QR"
                }
                cargando = false
            } catch (e: Exception) {
                errorMsg = "Error: ${e.localizedMessage}"
                cargando = false
            }
        } else {
            errorMsg = "ID de clase no válido"
            cargando = false
        }
    }

    // Timer optimizado: una sola corrutina que descuenta cada segundo
    LaunchedEffect(bitmapQR) {
        if (bitmapQR != null) {
            while (segundosRestantes > 0) {
                delay(1000L)
                segundosRestantes--
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Asistencia: $materiaLegible",
            fontSize = 20.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            cargando -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Generando código seguro...", fontSize = 16.sp)
            }

            errorMsg != null -> {
                Text("⚠️", fontSize = 40.sp)
                Text(errorMsg!!, color = Color.Red, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* Reintento manual si se desea */ }) {
                    Text("Reintentar")
                }
            }

            bitmapQR != null && segundosRestantes > 0 -> {
                val imageBitmap = remember(bitmapQR) { bitmapQR!!.asImageBitmap() }

                Text(
                    text = "Código QR Activo",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50),
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Código QR",
                        modifier = Modifier
                            .size(280.dp)
                            .padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                val minutos = segundosRestantes / 60
                val segundos = segundosRestantes % 60
                val tiempoTexto = "${minutos.toString().padStart(2, '0')}:${segundos.toString().padStart(2, '0')}"

                Text(
                    text = "Válido por: $tiempoTexto",
                    color = if (segundosRestantes < 60) Color.Red else Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        segundosRestantes = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Finalizar Registro QR", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            else -> {
                Text("El código ha expirado", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { 
                        segundosRestantes = 300 
                        // Forzamos regeneración si es necesario o simplemente reseteamos el tiempo
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generar nuevo código")
                }
            }
        }
    }
}