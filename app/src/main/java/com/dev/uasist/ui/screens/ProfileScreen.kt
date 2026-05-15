package com.dev.uasist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.uasist.model.Usuario
import com.dev.uasist.ui.theme.BlueBrand
import com.dev.uasist.ui.theme.BlueLight
import com.dev.uasist.ui.theme.PinkBrand
import com.dev.uasist.ui.theme.PurpleBrand

@Composable
fun ProfileScreen(usuario: Usuario, onLogout: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    // Estados inicializados con los datos reales del usuario pasado por parámetro
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var apellidos by remember { mutableStateOf(usuario.apellidos) }
    var email by remember { mutableStateOf(usuario.email) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text("Mi Perfil", style = MaterialTheme.typography.bodySmall)

        Text(
            if (usuario is Usuario.Profesor) "Docente" else "Estudiante",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta de Usuario con color según el Rol
        val gradienteColor = if (usuario is Usuario.Profesor)
            listOf(BlueBrand, BlueLight)
        else
            listOf(PurpleBrand, PinkBrand)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .background(Brush.horizontalGradient(gradienteColor))
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
                        val iconoPrincipal = if (usuario is Usuario.Profesor) Icons.Default.School else Icons.Default.Person
                        Icon(iconoPrincipal, null, tint = Color.White, modifier = Modifier.padding(16.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("$nombre $apellidos", color = Color.White, style = MaterialTheme.typography.titleLarge)

                    Text(email, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Campos básicos comunes
        ProfileField(label = "Nombre", value = nombre, isEditing = isEditing, onValueChange = { nombre = it }, icon = Icons.Default.Person)
        ProfileField(label = "Apellidos", value = apellidos, isEditing = isEditing, onValueChange = { apellidos = it }, icon = Icons.Default.Badge)
        ProfileField(label = "Email", value = email, isEditing = isEditing, onValueChange = { email = it }, icon = Icons.Default.Email)

        // CAMPOS DINÁMICOS (Solo si es Profesor)
        if (usuario is Usuario.Profesor) {
            ProfileField(
                label = "Materia Impartida",
                value = usuario.materiaImpartida.nombre,
                isEditing = false, // La materia suele ser asignada administrativamente
                onValueChange = {},
                icon = Icons.Default.Book
            )
            ProfileField(
                label = "Sala Asignada",
                value = usuario.salaAsignada,
                isEditing = isEditing,
                onValueChange = {},
                icon = Icons.Default.MeetingRoom
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Lógica de botones (Editar / Guardar)
        if (!isEditing) {
            OutlinedButton(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
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
                    Text("Guardar Cambios")
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

        // Cerrar sesión (Rojo estándar de error)
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión", color = MaterialTheme.colorScheme.onError)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
@Composable
fun ProfileField(label: String, value: String, isEditing: Boolean, onValueChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = isEditing,
            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}