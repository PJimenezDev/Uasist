package com.dev.uasist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.uasist.data.AsistenciaRepository

@Composable
fun ClasesScreen() {
    val repository = remember { AsistenciaRepository() }
    val clases = remember { repository.getTodasLasClases() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Mis Clases", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Gestiona tu asistencia por asignatura", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)) {
            items(clases) { clase ->
                val porcentaje = (clase.asistencias.toFloat() / clase.totalClases.toFloat() * 100).toInt()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = clase.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = clase.profesor, color = Color.Gray, fontSize = 14.sp)
                            }
                            Text(
                                text = "$porcentaje%",
                                color = if (porcentaje > 80) Color(0xFF4CAF50) else Color(0xFFF44336),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Barra de progreso
                        LinearProgressIndicator(
                            progress = { clase.asistencias.toFloat() / clase.totalClases.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = if (porcentaje > 80) Color(0xFF4CAF50) else Color(0xFFF44336),
                            trackColor = Color(0xFFE0E0E0)
                        )

                        Text(
                            text = "${clase.asistencias}/${clase.totalClases} asistencias",
                            modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}