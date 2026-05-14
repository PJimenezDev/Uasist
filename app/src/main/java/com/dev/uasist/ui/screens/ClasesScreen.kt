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
import com.dev.uasist.model.Clase
import com.dev.uasist.model.Usuario
import com.dev.uasist.data.AsistenciaRepository
import androidx.compose.runtime.produceState

@Composable
fun ClasesScreen(usuario: Usuario) {
    val repository = remember { AsistenciaRepository() }

    // Cambiamos a la nueva función de resumen
    val clases by produceState(initialValue = emptyList<Clase>(), repository, usuario.id) {
        value = repository.getResumenAsistenciaPorMateria(usuario.id)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Mis Clases", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Gestiona tu asistencia por asignatura", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(clases) { clase ->
                // Cálculo de porcentaje seguro
                val porcentaje = if (clase.totalClases > 0) {
                    (clase.asistencias.toFloat() / clase.totalClases.toFloat() * 100).toInt()
                } else 0

                // Lógica de colores del modelo original
                val colorEstado = when {
                    porcentaje >= 85 -> Color(0xFF4CAF50) // Verde
                    porcentaje >= 70 -> Color(0xFFFBC02D) // Amarillo/Naranja
                    else -> Color(0xFFF44336) // Rojo
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = clase.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = "Prof. ${clase.profesor}", color = Color.Gray, fontSize = 12.sp)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$porcentaje%",
                                    color = colorEstado,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                                Text(
                                    text = "${clase.asistencias}/${clase.totalClases}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Barra de progreso estilizada
                        LinearProgressIndicator(
                            progress = { if (clase.totalClases > 0) clase.asistencias.toFloat() / clase.totalClases.toFloat() else 0f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = colorEstado,
                            trackColor = Color(0xFFEEEEEE),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}