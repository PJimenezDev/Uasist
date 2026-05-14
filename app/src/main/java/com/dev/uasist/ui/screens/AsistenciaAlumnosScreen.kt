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
import com.dev.uasist.model.EstudianteConAsistencia
import com.dev.uasist.model.Usuario
import com.dev.uasist.viewmodel.AsistenciaViewModel

@Composable
fun AsistenciaAlumnosScreen(
    profesorId: String,
    materiaNombre: String,
    viewModel: AsistenciaViewModel = viewModel()
) {
    var busqueda by remember { mutableStateOf("") }
    val alumnosHistorial by viewModel.asistentesRealtime.collectAsState()

    LaunchedEffect(profesorId) {
        viewModel.monitorearAsistencia(profesorId)
    }

    val alumnosFiltrados = alumnosHistorial.filter {
        it.nombreCompleto.contains(busqueda, ignoreCase = true)
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
            placeholder = { Text("Buscar alumno...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(alumnosFiltrados) { alumno ->
                AlumnoCardRealtime(alumno)
            }
        }

    }
}

@Composable
fun AlumnoCardRealtime(alumno: EstudianteConAsistencia) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(alumno.nombreCompleto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(alumno.email, fontSize = 12.sp, color = Color.Gray)
            }

            // BADGE DE PORCENTAJE
            Surface(
                color = when {
                    alumno.porcentaje >= 75 -> Color(0xFFE8F5E9)
                    alumno.porcentaje >= 50 -> Color(0xFFFFF3E0)
                    else -> Color(0xFFFFEBEE)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${alumno.porcentaje}%",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = when {
                        alumno.porcentaje >= 75 -> Color(0xFF2E7D32)
                        alumno.porcentaje >= 50 -> Color(0xFFEF6C00)
                        else -> Color(0xFFC62828)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}