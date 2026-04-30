package com.dev.uasist.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.uasist.ui.screens.ClasesScreen
import com.dev.uasist.ui.screens.DashboardScreen
import com.dev.uasist.ui.screens.LoginScreen
import com.dev.uasist.ui.screens.ProfileScreen
import com.dev.uasist.ui.screens.UAsistMainScreen


sealed class Rutas(val ruta: String) {
    object Login : Rutas("login")
    object Profile : Rutas("profile")

    // Alumno
    object AlumnoDashboard : Rutas("alumno_dashboard")
    object Clases : Rutas("clases")
    object ScannerQR : Rutas("scanner_qr")

    // Profesor
    object ProfesorDashboard : Rutas("profesor_dashboard")
    object GenerarQR : Rutas("generar_qr")
    object AsistenciaLista : Rutas("asistencia_lista")
}



@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Rutas.Login.ruta) {

        composable(Rutas.Login.ruta) {
            LoginScreen(
                onLoginSuccess = { esProfe ->
                    val rutaDestino = if(esProfe) Rutas.ProfesorDashboard.ruta else Rutas.AlumnoDashboard.ruta
                    navController.navigate(rutaDestino) {
                        popUpTo(Rutas.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        // --- RUTAS DEL ALUMNO ---
        composable(Rutas.AlumnoDashboard.ruta) {
            DashboardScreen(
                onNavigateToScanner = { navController.navigate(Rutas.ScannerQR.ruta) },
                onNavigateToClases = { navController.navigate(Rutas.Clases.ruta) }
            )
        }

        composable(Rutas.Clases.ruta) {
            ClasesScreen()
        }

        composable(Rutas.Profile.ruta) { // Añade esta ruta a tu sealed class Rutas
            ProfileScreen(onLogout = {
                navController.navigate(Rutas.Login.ruta) {
                    popUpTo(0) // Limpiar historial para que no pueda volver atrás
                }
            })
        }

        composable(Rutas.ScannerQR.ruta) {
            UAsistMainScreen(onBack = { navController.popBackStack() })
        }
    }
}