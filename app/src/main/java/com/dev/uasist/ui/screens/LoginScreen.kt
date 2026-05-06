package com.dev.uasist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.dev.uasist.data.AsistenciaRepository
import com.dev.uasist.model.Usuario

@Composable
fun LoginScreen(
    onLoginSuccess: (usuario: Usuario) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val repository = remember { AsistenciaRepository() }
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    // Fondo con Gradiente (from-blue-500 to-purple-600)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3B82F6), Color(0xFF9333EA))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono circular de cabecera
                Surface(
                    shape = RoundedCornerShape(100),
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Bienvenido", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Inicia sesión para continuar", color = Color.Gray, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(24.dp))

                // Campo de Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Enlace sutil para recuperar contraseña
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { onNavigateToForgotPassword() }) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9333EA)
                        )
                    }
                }

                if (error.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón Iniciar Sesión
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                error = "" // Limpiar errores previos
                                val usuarioLogueado = repository.login(email)
                                if (usuarioLogueado != null) {
                                    onLoginSuccess(usuarioLogueado)
                                } else {
                                    error = "Usuario no encontrado"
                                }
                            } catch (e: Exception) {
                                // Esto hará que el error aparezca en tu pantalla en texto rojo
                                error = "Error de conexión: ${e.localizedMessage}"
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Iniciar Sesión", fontSize = 16.sp)
                }

                TextButton(
                    onClick = { onNavigateToSignUp() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(
                        text = "¿No tienes cuenta? Regístrate aquí",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}