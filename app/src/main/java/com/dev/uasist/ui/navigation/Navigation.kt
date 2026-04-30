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
                onLoginAlumno = { navController.navigate(Rutas.AlumnoDashboard.ruta) },
                onLoginProfesor = { navController.navigate(Rutas.ProfesorDashboard.ruta) }
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
            // Aquí llamas a la pantalla de Scanner que te programé en el mensaje anterior
            UAsistMainScreen(onBack = { navController.popBackStack() })
        }

//        // --- RUTAS DEL PROFESOR ---
//        composable(Rutas.ProfesorDashboard.ruta) {
//            ProfesorDashboardScreen()
//        }
    }
}