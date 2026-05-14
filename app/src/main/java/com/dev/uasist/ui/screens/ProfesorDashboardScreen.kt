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
    val profesor = usuario as? Usuario.Profesor
    val materia = profesor?.materiaImpartida?.nombre ?: "Materia"
    val materiaLimpia = materia.replace(":", "").trim()

    val claseIdActiva by viewModel.claseIdActiva.collectAsState()
    val alumnos by viewModel.asistentesRealtime.collectAsState()
    val scope = rememberCoroutineScope()

    // 1. CARGA INICIAL: Solo busca si ya hay algo en Supabase, NO crea nada.
    LaunchedEffect(profesor?.id) {
        profesor?.id?.let { viewModel.cargarClaseActiva(it) }
    }

    // 2. NAVEGACIÓN DE UN SOLO DISPARO:
    // Solo se activa cuando el ViewModel emite 'navegarAQR' tras presionar el botón.
    LaunchedEffect(Unit) {
        viewModel.navegarAQR.collect { id ->
            onNavigateToQR(id, materiaLimpia)
        }
    }

    if (profesor == null) return

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Hola, ${usuario.nombre} 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Asignatura: $materia", color = Color.Gray)

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstadisticaCard("Presentes", "${alumnos.size}", Modifier.weight(1f), Color(0xFFE3F2FD))
            EstadisticaCard(
                "Estado",
                if (claseIdActiva != null) "Sesión Activa" else "Sin Iniciar",
                Modifier.weight(1f),
                if (claseIdActiva != null) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ACCIÓN: INICIAR O VER QR
        Button(
            onClick = {
                if (claseIdActiva == null) {
                    // AQUÍ es el único lugar donde se crea la clase
                    viewModel.iniciarNuevaClase(profesor.id, materia)
                } else {
                    // Si ya existe, solo vamos a ver el QR, no creamos otro
                    onNavigateToQR(claseIdActiva!!, materiaLimpia)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = if (claseIdActiva != null) ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5077))
            else ButtonDefaults.buttonColors()
        ) {
            Icon(Icons.Default.QrCode, null)
            Spacer(Modifier.width(8.dp))
            Text(if (claseIdActiva == null) "Iniciar Clase y Crear QR" else "Mostrar QR Actual")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // VER LISTA
        OutlinedButton(
            onClick = { claseIdActiva?.let { onNavigateToLista(it, materiaLimpia) } },
            enabled = claseIdActiva != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Groups, null)
            Spacer(Modifier.width(8.dp))
            Text("Ver Lista en Tiempo Real")
        }

        // FINALIZAR SESIÓN
        if (claseIdActiva != null) {
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(
                onClick = { viewModel.finalizarSesion() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Finalizar Sesión de Hoy", color = Color.Red, fontWeight = FontWeight.SemiBold)
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
