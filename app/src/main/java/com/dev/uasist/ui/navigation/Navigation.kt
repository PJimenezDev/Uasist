package com.dev.uasist.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.uasist.ui.screens.*
import com.dev.uasist.model.Usuario


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
    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }

    NavHost(navController = navController, startDestination = Rutas.Login.ruta) {

        composable(Rutas.Login.ruta) {
            LoginScreen(
                onLoginSuccess = { usuario ->
                    usuarioActual = usuario
                    val rutaDestino = when (usuario) {
                        is Usuario.Profesor -> Rutas.ProfesorDashboard.ruta
                        is Usuario.Estudiante -> Rutas.AlumnoDashboard.ruta
                    }
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

        composable(Rutas.Profile.ruta) {
            usuarioActual?.let { user ->
                ProfileScreen(
                    usuario = user,
                    onLogout = {
                        usuarioActual = null
                        navController.navigate(Rutas.Login.ruta) {
                            popUpTo(0)
                        }
                    }
                )
            }
        }

        composable(Rutas.ScannerQR.ruta) {
            QRScannerScreen(onAsistenciaRegistrada = { resultado ->
                // Al dejar esto vacío (o solo con un log),
                // la pantalla no se cerrará al escanear.
                println("QR Escaneado con éxito: $resultado")

                // Si quieres que después de unos segundos se limpie para otro escaneo,
                // la lógica debe estar dentro de QRScannerScreen, no aquí.
            })
        }

        // --- RUTAS DEL PROFESOR ---
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