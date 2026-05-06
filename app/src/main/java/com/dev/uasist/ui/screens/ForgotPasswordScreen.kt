package com.dev.uasist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.uasist.data.AsistenciaRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(repository: AsistenciaRepository, onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val gradientColors = listOf(Color(0xFFA855F7), Color(0xFFDB2777))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(gradientColors))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono decorativo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3E8FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = Color(0xFF9333EA),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Recuperar Acceso", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Ingresa tu correo para recibir las instrucciones",
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (mensaje.isNotEmpty()) {
                    Text(
                        text = mensaje,
                        color = if (esError) MaterialTheme.colorScheme.error else Color(0xFF059669),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val ok = repository.enviarCorreoRecuperacion(email)
                            mensaje = if (ok) "¡Correo enviado! Revisa tu bandeja." else "No se pudo enviar el correo."
                            esError = !ok
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Enviar instrucciones", fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onBack) {
                    Text("Volver al inicio de sesión", color = Color(0xFF9333EA))
                }
            }
        }
    }
}