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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dev.uasist.ui.theme.UasistTheme

// Importamos las pantallas que creamos anteriormente
import com.dev.uasist.ui.screens.LoginScreen
import com.dev.uasist.ui.screens.DashboardScreen
import com.dev.uasist.ui.screens.ProfileScreen
import com.dev.uasist.ui.screens.UAsistMainScreen

import com.dev.uasist.ui.screens.ClasesScreen
import com.dev.uasist.ui.screens.GenerarQRScreen
import com.dev.uasist.ui.screens.ProfesorDashboardScreen
import com.dev.uasist.ui.screens.AsistenciaAlumnosScreen

// 1. Definimos las rutas de la aplicación
sealed class Rutas(val ruta: String) {
    object Login : Rutas("login")
    object AlumnoDashboard : Rutas("alumno_dashboard")
    object Profile : Rutas("profile")
    object ScannerQR : Rutas("scanner_qr")
    object Clases : Rutas("clases")

    // Rutas del Profesor
    object ProfesorDashboard : Rutas("profesor_dashboard")
    object GenerarQR : Rutas("generar_qr")
    object AsistenciaLista : Rutas("asistencia_lista")
}

// Definición de los items del menú
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

                // Estado para el rol del usuario
                var userRole by remember { mutableStateOf("alumno") }

                // Solo mostrar menú si no estamos en el Login
                val showBottomBar = currentDestination?.route != Rutas.Login.ruta

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
                    AppNavigation(
                        navController = navController,
                        onRoleChange = { userRole = it },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// 4. El Componente que maneja qué pantalla se muestra
@Composable
fun AppNavigation(
    navController: androidx.navigation.NavHostController,
    onRoleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Rutas.Login.ruta,
        modifier = modifier
    ) {
        // --- PANTALLA DE LOGIN ---
        composable("login") {
            LoginScreen(onLoginSuccess = { esProfe: Boolean ->
                val rutaDestino = if(esProfe) "profesor_dashboard" else "alumno_dashboard"
                onRoleChange(if(esProfe) "profesor" else "alumno")

                navController.navigate(rutaDestino) {
                    // Esto elimina la pantalla de Login del historial
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        // --- PANTALLAS DEL ALUMNO ---
        composable(Rutas.AlumnoDashboard.ruta) {
            DashboardScreen(
                onNavigateToScanner = { navController.navigate(Rutas.ScannerQR.ruta) },
                onNavigateToClases = { navController.navigate(Rutas.Clases.ruta) }
            )
        }

        composable(Rutas.Profile.ruta) {
            ProfileScreen(onLogout = {
                navController.navigate(Rutas.Login.ruta) {
                    popUpTo(0) // Borra el historial para no volver atrás con el botón de retroceso
                }
            })
        }

        composable(Rutas.ScannerQR.ruta) {
            UAsistMainScreen(onBack = { navController.popBackStack() })
        }

        composable(Rutas.Clases.ruta) {
            ClasesScreen()
        }

        // --- PANTALLAS DEL PROFESOR ---
        composable(Rutas.ProfesorDashboard.ruta) {
            ProfesorDashboardScreen(
                onNavigateToQR = { navController.navigate(Rutas.GenerarQR.ruta) },
                onNavigateToLista = { navController.navigate(Rutas.AsistenciaLista.ruta) }
            )
        }

        composable(Rutas.GenerarQR.ruta) {
            GenerarQRScreen()
        }

        composable(Rutas.AsistenciaLista.ruta) {
            AsistenciaAlumnosScreen()
        }
    }
}