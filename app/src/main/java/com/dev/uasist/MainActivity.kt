package com.dev.uasist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dev.uasist.ui.theme.UasistTheme
import com.dev.uasist.model.Usuario
import com.dev.uasist.ui.navigation.AppNavigation
import com.dev.uasist.ui.navigation.Rutas

// Definición de los items del menú vinculados a las rutas oficiales del paquete navigation
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    // Alumno
    object Inicio : BottomNavItem(Rutas.AlumnoDashboard.ruta, Icons.Default.Home, "Inicio")
    object QR : BottomNavItem(Rutas.ScannerQR.ruta, Icons.Default.QrCodeScanner, "QR")
    object Clases : BottomNavItem(Rutas.Clases.ruta, Icons.Default.DateRange, "Clases")

    // Profesor
    object InicioProfesor : BottomNavItem(Rutas.ProfesorDashboard.ruta, Icons.Default.Home, "Inicio")
    object GenerarQR : BottomNavItem(Rutas.GenerarQR.ruta, Icons.Default.QrCode, "Generar QR")
    object Asistencia : BottomNavItem(Rutas.AsistenciaLista.ruta, Icons.Default.Groups, "Alumnos")

    // Común
    object Perfil : BottomNavItem(Rutas.Profile.ruta, Icons.Default.Person, "Perfil")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UasistTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // ESTADO CENTRALIZADO: Única fuente de verdad para el usuario y su rol
                var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
                var userRole by remember { mutableStateOf("alumno") }

                // Solo mostrar menú si no estamos en el Login y tenemos un usuario
                val showBottomBar = currentDestination?.route != Rutas.Login.ruta && usuarioActual != null

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                val items = if (userRole == "alumno") {
                                    listOf(
                                        BottomNavItem.Inicio,
                                        BottomNavItem.QR,
                                        BottomNavItem.Clases,
                                        BottomNavItem.Perfil
                                    )
                                } else {
                                    listOf(
                                        BottomNavItem.InicioProfesor,
                                        BottomNavItem.GenerarQR,
                                        BottomNavItem.Asistencia,
                                        BottomNavItem.Perfil
                                    )
                                }
                                items.forEach { item ->
                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) },
                                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    // Delegamos la navegación al archivo Navigation.kt pasándole los estados necesarios
                    AppNavigation(
                        navController = navController,
                        usuarioActual = usuarioActual,
                        onUsuarioChange = { usuarioActual = it },
                        onRoleChange = { userRole = it },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
