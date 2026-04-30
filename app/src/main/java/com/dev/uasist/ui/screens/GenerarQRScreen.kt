package com.dev.uasist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GenerarQRScreen() {
    var qrGenerado by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!qrGenerado) {
            Text("Lista para iniciar clase?", fontSize = 20.sp)
            Button(onClick = { qrGenerado = true }) {
                Text("Generar QR de Asistencia")
            }
        } else {
            Text("Código QR Activo", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            Spacer(modifier = Modifier.height(16.dp))

            // Aquí iría la imagen del QR. Usamos un Icono grande como placeholder
            Icon(
                Icons.Default.QrCode,
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Expira en: 04:59", color = Color.Red)

            Button(
                onClick = { qrGenerado = false },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text("Desactivar QR")
            }
        }
    }
}