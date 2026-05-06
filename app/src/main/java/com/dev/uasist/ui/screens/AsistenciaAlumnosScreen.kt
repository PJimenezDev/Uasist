package com.dev.uasist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.dev.uasist.model.Usuario

@Composable
fun AsistenciaAlumnosScreen(usuario: Usuario) {
    var busqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("Todos") }

    val alumnos = listOf(
        AlumnoEstado("1", "Carlos", "Ramírez", 92),
        AlumnoEstado("2", "María", "González", 65),
        AlumnoEstado("3", "Pedro", "Martínez", 88),
        AlumnoEstado("4", "Ana", "Fernández", 40)
    )

    val alumnosFiltrados = alumnos.filter {
        val coincideNombre = "${it.nombre} ${it.apellido}".contains(busqueda, ignoreCase = true)
        val coincideFiltro = when (filtroSeleccionado) {
            "Riesgo" -> it.porcentaje < 70
            "Excelente" -> it.porcentaje >= 85
            else -> true
        }
        coincideNombre && coincideFiltro
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        // Título fijo arriba
        Spacer(modifier = Modifier.height(16.dp))
        Text("Asistencia de Alumnos", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Matemáticas - Sección 001", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de Búsqueda fija
        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar alumno...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Chips de Filtro fijos
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todos", "Riesgo", "Excelente").forEach { filtro ->
                FilterChip(
                    selected = filtroSeleccionado == filtro,
                    onClick = { filtroSeleccionado = filtro },
                    label = { Text(filtro) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LISTA CON SCROLL
        LazyColumn(
            modifier = Modifier.weight(1f), // Toma todo el espacio disponible entre el buscador y el botón
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp) // Espacio al final de la lista
        ) {
            items(alumnosFiltrados) { alumno ->
                AlumnoCard(alumno)
            }
        }

        // BOTÓN DE ACCIÓN FINAL
        Button(
            onClick = { /* Lógica de exportar CSV */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp, top = 8.dp) // 80.dp de margen inferior para no quedar oculto por el menú
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Exportar Lista (CSV)")
        }
    }
}

@Composable
fun AlumnoCard(alumno: AlumnoEstado) {
    val colorPorcentaje = when {
        alumno.porcentaje < 70 -> Color.Red
        alumno.porcentaje >= 85 -> Color(0xFF4CAF50)
        else -> Color(0xFFFFA000)
    }

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
                Text("${alumno.nombre} ${alumno.apellido}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = if (alumno.porcentaje < 70) "Estado: En Riesgo" else "Estado: Regular",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = "${alumno.porcentaje}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorPorcentaje
            )
        }
    }
}

data class AlumnoEstado(val id: String, val nombre: String, val apellido: String, val porcentaje: Int)