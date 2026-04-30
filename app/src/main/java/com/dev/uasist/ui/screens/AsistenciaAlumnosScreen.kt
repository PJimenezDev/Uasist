package com.dev.uasist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AsistenciaAlumnosScreen() {
    var busqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("Todos") } // "Todos", "Riesgo", "Excelente"

    // Datos de ejemplo basados en tu lógica de React
    val alumnos = listOf(
        AlumnoEstado("1", "Carlos", "Ramírez", 92),
        AlumnoEstado("2", "María", "González", 65), // Riesgo
        AlumnoEstado("3", "Pedro", "Martínez", 88),
        AlumnoEstado("4", "Ana", "Fernández", 40)   // Riesgo
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Asistencia de Alumnos", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Matemáticas - Sección 001", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de Búsqueda
        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar alumno...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Chips de Filtro
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

        // Lista de Alumnos
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(alumnosFiltrados) { alumno ->
                AlumnoCard(alumno)
            }
        }

        // Botón Exportar
        Button(
            onClick = { /* Lógica de exportar CSV */ },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${alumno.nombre} ${alumno.apellido}", fontWeight = FontWeight.Bold)
                Text(if (alumno.porcentaje < 70) "Estado: En Riesgo" else "Estado: Regular",
                    fontSize = 12.sp, color = Color.Gray)
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