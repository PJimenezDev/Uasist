package com.dev.uasist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.uasist.model.Usuario
import com.dev.uasist.model.Clase
import com.dev.uasist.data.AsistenciaRepository

@Composable
fun DashboardScreen(
    usuario: Usuario,
    onNavigateToClases: () -> Unit,
    // Inyectamos el repositorio temporalmente para obtener datos (simulando los estados de React)
    repository: AsistenciaRepository = remember { AsistenciaRepository() },
) {
    // Usamos produceState para manejar llamadas suspendidas
    val clasesHoy by produceState(initialValue = emptyList<Clase>(), repository, usuario.id) {
        value = repository.getClasesHoy(usuario.id)
    }
    val asistenciaGeneral by produceState(initialValue = 0, repository, usuario.id) {
        value = repository.getAsistenciaGeneral(usuario.id)
    }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // bg-gray-50
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text("Hola, ${usuario.nombre} 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Aquí está tu resumen de hoy", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta de Asistencia General (Gradiente en React, aquí usamos un color primario)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3B82F6)), // bg-blue-500
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Asistencia General", color = Color.White, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("$asistenciaGeneral%", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { asistenciaGeneral / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF2563EB))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clases de Hoy", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (clasesHoy.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No tienes clases programadas hoy", color = Color.Gray)
            }
        } else {
            clasesHoy.forEach { clase ->
                // TARJETA CLICABLE
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToClases() }, // Navega al hacer clic
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(clase.nombre, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            Text(clase.profesor, fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(clase.horario, fontSize = 14.sp, color = Color.Gray)
                        }
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

    }
}