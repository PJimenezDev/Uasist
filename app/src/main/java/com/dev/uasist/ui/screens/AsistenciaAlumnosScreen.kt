package com.dev.uasist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dev.uasist.model.Usuario
import com.dev.uasist.viewmodel.AsistenciaViewModel

@Composable
fun AsistenciaAlumnosScreen(
    profesorId: String, // Cambiado: Ahora recibimos el ID del profesor para el historial total
    materiaNombre: String,
    viewModel: AsistenciaViewModel = viewModel()
) {
    var busqueda by remember { mutableStateOf("") }

    // Recolectamos el historial acumulado de todas las clases de este profesor
    // Asegúrate de tener este StateFlow en tu ViewModel que apunte a la nueva Vista SQL
    val alumnosHistorial by viewModel.asistentesRealtime.collectAsState()

    // Suscripción al historial total del profesor
    LaunchedEffect(profesorId) {
        viewModel.monitorearAsistencia(profesorId)
    }

    // Validación de seguridad para el ID
    if (profesorId.isEmpty() || profesorId == "{profesorId}") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: ID de profesor no válido", color = Color.Red)
        }
        return
    }

    val alumnosFiltrados = alumnosHistorial.filter {
        "${it.nombre} ${it.apellidos}".contains(busqueda, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Asistencia Acumulada", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(materiaNombre, color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar alumno en historial...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Total alumnos registrados: ${alumnosHistorial.size}",
            fontSize = 14.sp,
            color = Color(0xFF3F51B5),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(alumnosFiltrados) { alumno ->
                AlumnoCardRealtime(alumno)
            }
        }

        Button(
            onClick = { /* Lógica de exportar CSV */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp, top = 8.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Exportar Reporte Total (CSV)")
        }
    }
}

@Composable
fun AlumnoCardRealtime(alumno: Usuario.Estudiante) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${alumno.nombre} ${alumno.apellidos}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(alumno.email, fontSize = 12.sp, color = Color.Gray)
                // Opcional: Mostrar contador si el DTO lo trae
                // Text("Asistencias: ${alumno.total}", fontSize = 11.sp, color = Color(0xFF3F51B5))
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50)
            )
        }
    }
}