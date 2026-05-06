package com.dev.uasist.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
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
import com.dev.uasist.utils.generarBitmapQR

import com.dev.uasist.model.Usuario

@Composable
fun GenerarQRScreen(usuario: Usuario) {
    var qrGenerado by remember { mutableStateOf(false) }
    var bitmapQR by remember { mutableStateOf<Bitmap?>(null) }

    // Estado para el tiempo restante en segundos (5 minutos = 300 segundos)
    var segundosRestantes by remember { mutableIntStateOf(300) }

    // Efecto que maneja la cuenta regresiva
    LaunchedEffect(key1 = qrGenerado, key2 = segundosRestantes) {
        if (qrGenerado && segundosRestantes > 0) {
            delay(1000L) // Espera 1 segundo
            segundosRestantes -= 1
        } else if (segundosRestantes == 0) {
            qrGenerado = false // Desactiva el QR automáticamente al llegar a cero
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!qrGenerado) {
            Text("¿Lista para iniciar clase?", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val contenidoQR = "UASIST_CLASE_123_${System.currentTimeMillis()}"
                bitmapQR = generarBitmapQR(contenidoQR, 512)
                segundosRestantes = 300 // Reiniciamos a 5 minutos
                qrGenerado = true
            }) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generar QR de Asistencia")
            }
        } else {
            Text("Código QR Activo", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 22.sp)
            Spacer(modifier = Modifier.height(24.dp))

            bitmapQR?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Código QR Real",
                    modifier = Modifier.size(250.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Formatear segundos a formato MM:SS
            val minutos = segundosRestantes / 60
            val segundos = segundosRestantes % 60
            val tiempoTexto = String.format("%02d:%02d", minutos, segundos)

            Text(
                text = "Expira en: $tiempoTexto",
                color = if (segundosRestantes < 30) Color.Red else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            // Botón para desactivar el QR
            Button(
                onClick = {
                    qrGenerado = false
                    bitmapQR = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text("Desactivar QR", color = Color.White)
            }
        }
    }
}