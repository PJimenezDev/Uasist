package com.dev.uasist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UAsistMainScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear QR") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Simulador de visor de cámara
            Surface(
                modifier = Modifier
                    .size(280.dp)
                    .padding(16.dp),
                color = Color.Black,
                shape = MaterialTheme.shapes.medium
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "Apunte al código QR",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* Aquí irá la lógica de marcar asistencia */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Simular Escaneo")
            }
        }
    }
}