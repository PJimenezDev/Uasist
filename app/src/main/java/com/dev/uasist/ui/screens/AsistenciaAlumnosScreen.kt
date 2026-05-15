package com.dev.uasist.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.dev.uasist.ui.theme.*
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
    val isDark = isSystemInDarkTheme()
    val (contentColor, backgroundColor) = when {
        alumno.porcentaje >= 75 -> AttendSuccess to BadgeGreenBg
        alumno.porcentaje >= 50 -> AttendWarning to BadgeOrangeBg
        else -> AttendError to BadgeRedBg
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(alumno.nombreCompleto, style = MaterialTheme.typography.titleLarge)
                Text(alumno.email, style = MaterialTheme.typography.bodySmall)
            }

            // BADGE DE PORCENTAJE
            Surface(
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant
                else if (alumno.porcentaje >= 75) BadgeGreenBg else BadgeRedBg,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${alumno.porcentaje}%",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}