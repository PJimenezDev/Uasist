package com.dev.uasist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // Importante
import androidx.compose.foundation.verticalScroll // Importante
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState() // Estado del scroll

    var nombre by remember { mutableStateOf("Carlos") }
    var apellidos by remember { mutableStateOf("Ramírez López") }
    var email by remember { mutableStateOf("carlos.ramirez@mail.com") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(scrollState) // AGREGADO: Esto permite bajar hasta el botón
            .padding(16.dp)
    ) {
        Text("Mi Perfil", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Gestiona tu información personal", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta de Usuario
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .background(Brush.horizontalGradient(listOf(Color(0xFFA855F7), Color(0xFFEC4899))))
                    .padding(24.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.padding(16.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("$nombre $apellidos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(email, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Campos de información
        ProfileField(label = "Nombre", value = nombre, isEditing = isEditing, onValueChange = { nombre = it }, icon = Icons.Default.Person)
        ProfileField(label = "Apellidos", value = apellidos, isEditing = isEditing, onValueChange = { apellidos = it }, icon = Icons.Default.Badge)
        ProfileField(label = "Email", value = email, isEditing = isEditing, onValueChange = { email = it }, icon = Icons.Default.Email)

        // Usamos un Spacer con peso para empujar los botones abajo,
        // pero en un scroll se recomienda un height fijo para evitar conflictos
        Spacer(modifier = Modifier.height(32.dp))

        // Botones de Acción
        if (!isEditing) {
            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                border = ButtonDefaults.outlinedButtonBorder,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar Perfil")
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { isEditing = false },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar")
                }
                OutlinedButton(
                    onClick = { isEditing = false },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BOTÓN CERRAR SESIÓN
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(12.dp)
        ) {
            // NOTA: Asegúrate de usar Icons.Default.LogOut (con O mayúscula)
            // ya que Logout a veces no resuelve según la versión de iconos.
            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión", color = Color.White)
        }

        // Padding extra al final para que el menú inferior no lo tape
        Spacer(modifier = Modifier.height(80.dp))
    }
}
@Composable
fun ProfileField(label: String, value: String, isEditing: Boolean, onValueChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = isEditing,
            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                disabledContainerColor = Color(0xFFF3F4F6),
                disabledTextColor = Color.Black
            )
        )
    }
}