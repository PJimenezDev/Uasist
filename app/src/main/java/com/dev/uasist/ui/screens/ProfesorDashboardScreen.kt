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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.dev.uasist.model.Usuario
import com.dev.uasist.model.Materia

@Composable
fun ProfesorDashboardScreen(
    usuario: Usuario,
    onNavigateToQR: () -> Unit,
    onNavigateToLista: () -> Unit
) {
    val profesor = usuario as? Usuario.Profesor
    val materia = profesor?.materiaImpartida?.nombre ?: "Materia"
    val sala = profesor?.salaAsignada ?: "No asignada"
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Hola, ${usuario.nombre} 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Gestión de la asignatura: $materia", color = Color.Gray)
        Text("Sala: $sala", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(20.dp))

        // Tarjetas de Estadísticas (Fila)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstadisticaCard("Total Alumnos", "30", Modifier.weight(1f), Color(0xFFE3F2FD))
            EstadisticaCard("Asistencia Prom.", "85%", Modifier.weight(1f), Color(0xFFE8F5E9))
            EstadisticaCard("En Riesgo", "3", Modifier.weight(1f), Color(0xFFFFEBEE))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Acciones Rápidas", fontWeight = FontWeight.SemiBold)

        // Botón Generar QR
        Button(
            onClick = onNavigateToQR,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.QrCode, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Generar Código QR Hoy")
        }

        // Botón Ver Lista
        OutlinedButton(
            onClick = onNavigateToLista,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Groups, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ver Lista de Asistencia")
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