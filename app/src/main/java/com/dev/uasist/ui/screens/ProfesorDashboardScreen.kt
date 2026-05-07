package com.dev.uasist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.QrCode
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
fun ProfesorDashboardScreen(
    usuario: Usuario,
    onNavigateToQR: (String, String) -> Unit,
    onNavigateToLista: (claseId: String, materia: String) -> Unit,
    viewModel: AsistenciaViewModel = viewModel()
) {
    // 1. Casting seguro del usuario
    val profesor = usuario as? Usuario.Profesor
    val materia = profesor?.materiaImpartida?.nombre ?: "Materia"

    // 1. Limpiamos SIEMPRE los dos puntos aquí para evitar errores de navegación
    val materiaLimpia = materia.replace(":", "").trim()

    val claseIdActiva by viewModel.claseIdActiva.collectAsState()
    val alumnos by viewModel.asistentesRealtime.collectAsState()

    // Control para evitar bucles de navegación al regresar con una clase ya activa
    var yaNavegadoPorCambio by remember { mutableStateOf(false) }

    // Si por alguna razón no es profesor, mostramos un error y salimos
    if (profesor == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: Acceso denegado. No eres profesor.", color = Color.Red)
        }
        return
    }

    // --- NAVEGACIÓN REACTIVA ---
    // Solo navegamos automáticamente cuando se crea una NUEVA clase (claseIdActiva pasa de null a ID)
    LaunchedEffect(claseIdActiva) {
        // Solo navegamos si hay una clase y NO estamos ya en la pantalla de destino
        if (claseIdActiva != null && !yaNavegadoPorCambio) {
            yaNavegadoPorCambio = true // Marcamos primero para evitar disparos dobles
            onNavigateToQR(claseIdActiva!!, materiaLimpia)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Hola, ${usuario.nombre} 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Asignatura: $materia", color = Color.Gray)

        Spacer(modifier = Modifier.height(20.dp))

        // Métricas Reales
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstadisticaCard("Presentes", "${alumnos.size}", Modifier.weight(1f), Color(0xFFE3F2FD))
            EstadisticaCard(
                "Estado",
                if (claseIdActiva != null) "En curso" else "Cerrada",
                Modifier.weight(1f),
                if (claseIdActiva != null) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ACCIÓN 1: GENERAR / VER QR
        Button(
            onClick = {
                if (claseIdActiva == null) {
                    // Al iniciar clase, el LaunchedEffect se encargará de la navegación
                    viewModel.iniciarNuevaClase(profesor.id, materia)
                } else {
                    // Si ya existe, navegamos manualmente usando la versión limpia
                    onNavigateToQR(claseIdActiva!!, materiaLimpia)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.QrCode, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (claseIdActiva == null) "Iniciar Clase y Crear QR" else "Mostrar QR Actual")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ACCIÓN 2: VER LISTA
        OutlinedButton(
            onClick = { claseIdActiva?.let { onNavigateToLista(it, materiaLimpia) } },
            enabled = claseIdActiva != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Groups, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ver Lista en Tiempo Real")
        }

        if (claseIdActiva != null) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { 
                viewModel.finalizarSesion()
                yaNavegadoPorCambio = false
            }) {
                Text("Finalizar Sesión de Hoy", color = Color.Red)
            }
        }
    }
}

@Composable
fun EstadisticaCard(titulo: String, valor: String, modifier: Modifier, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, fontSize = 12.sp, color = Color.DarkGray)
            Text(valor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
