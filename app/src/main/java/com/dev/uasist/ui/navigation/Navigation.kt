package com.dev.uasist.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dev.uasist.ui.screens.*
import com.dev.uasist.model.Usuario
import com.dev.uasist.data.AsistenciaRepository

sealed class Rutas(val ruta: String) {
    // 1. Autenticación
    object Login : Rutas("login")
    object SignUp : Rutas("signup")
    object ForgotPassword : Rutas("recuperar_clave")
    object ResetPassword : Rutas("nueva_clave")
    
    // 2. Común
    object Profile : Rutas("profile")

    // 3. Alumno
    object AlumnoDashboard : Rutas("alumno_dashboard")
    object Clases : Rutas("clases")
    object ScannerQR : Rutas("scanner_qr")

    // 4. Profesor
    object ProfesorDashboard : Rutas("profesor_dashboard")
    object GenerarQR : Rutas("generar_qr/{claseId}/{materia}")
    object AsistenciaLista : Rutas("asistencia_lista/{profesorId}/{materia}")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    usuarioActual: Usuario?,
    onUsuarioChange: (Usuario?) -> Unit,
    onRoleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Rutas.Login.ruta,
        modifier = modifier
    ) {
        // --- 1. FLUJO DE AUTENTICACIÓN ---

        composable(Rutas.Login.ruta) {
            LoginScreen(
                onLoginSuccess = { usuario ->
                    onUsuarioChange(usuario)
                    val rutaDestino = when (usuario) {
                        is Usuario.Profesor -> {
                            onRoleChange("profesor")
                            Rutas.ProfesorDashboard.ruta
                        }
                        is Usuario.Estudiante -> {
                            onRoleChange("alumno")
                            Rutas.AlumnoDashboard.ruta
                        }
                    }
                    navController.navigate(rutaDestino) {
                        popUpTo(Rutas.Login.ruta) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Rutas.SignUp.ruta)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Rutas.ForgotPassword.ruta)
                }
            )
        }

        composable(Rutas.SignUp.ruta) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    navController.navigate(Rutas.Login.ruta) {
                        popUpTo(Rutas.SignUp.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.ForgotPassword.ruta) {
            ForgotPasswordScreen(
                repository = AsistenciaRepository(),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Rutas.ResetPassword.ruta) {
            ResetPasswordScreen(
                repository = AsistenciaRepository(),
                onFinish = {
                    navController.navigate(Rutas.Login.ruta) {
                        popUpTo(Rutas.ResetPassword.ruta) { inclusive = true }
                    }
                }
            )
        }

        // --- 2. PANTALLAS DEL ALUMNO ---

        composable(Rutas.AlumnoDashboard.ruta) {
            usuarioActual?.let { user ->
                DashboardScreen(
                    usuario = user,
                    onNavigateToClases = { navController.navigate(Rutas.Clases.ruta) }
                )
            } ?: LaunchedEffect(Unit) {
                navController.navigate(Rutas.Login.ruta) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        composable(Rutas.Clases.ruta) {
            usuarioActual?.let { user ->
                ClasesScreen(usuario = user)
            } ?: LaunchedEffect(Unit) {
                navController.navigate(Rutas.Login.ruta) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        composable(Rutas.ScannerQR.ruta) {
            usuarioActual?.let { user ->
                QRScannerScreen(
                    usuario = user,
                    onAsistenciaRegistrada = { resultado ->
                        println("QR Escaneado con éxito: $resultado")
                    }
                )
            } ?: LaunchedEffect(Unit) {
                navController.navigate(Rutas.Login.ruta) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        // --- 3. PANTALLAS DEL PROFESOR ---

        composable(Rutas.ProfesorDashboard.ruta) {
            usuarioActual?.let { user ->
                ProfesorDashboardScreen(
                    usuario = user,
                    onNavigateToQR = { id, mat ->
                        val matEncoded = java.net.URLEncoder.encode(mat, "UTF-8")
                        navController.navigate("generar_qr/$id/$matEncoded") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLista = { id, mat ->
                        val matEncoded = java.net.URLEncoder.encode(mat, "UTF-8")
                        navController.navigate("asistencia_lista/${user.id}/$matEncoded")
                    }
                )
            } ?: LaunchedEffect(Unit) {
                navController.navigate(Rutas.Login.ruta) { popUpTo(0) { inclusive = true } }
            }
        }

        composable(
            route = Rutas.GenerarQR.ruta,
            arguments = listOf(
                navArgument("claseId") { type = NavType.StringType },
                navArgument("materia") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val claseId = backStackEntry.arguments?.getString("claseId") ?: ""
            val materia = backStackEntry.arguments?.getString("materia") ?: "Materia"

            if (usuarioActual != null && claseId.isNotEmpty()) {
                GenerarQRScreen(
                    usuario = usuarioActual,
                    claseId = claseId,
                    materiaNombre = materia
                )
            } else {
                Text("Error: Datos de sesión o clase no encontrados")
            }
        }

        composable(
            route = Rutas.AsistenciaLista.ruta,
            arguments = listOf(
                navArgument("profesorId") { type = NavType.StringType },
                navArgument("materia") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val profesorId = backStackEntry.arguments?.getString("profesorId") ?: ""
            val materia = backStackEntry.arguments?.getString("materia") ?: ""

            AsistenciaAlumnosScreen(profesorId = profesorId, materiaNombre = materia)
        }

        // --- 4. PANTALLAS COMUNES ---

        composable(Rutas.Profile.ruta) {
            usuarioActual?.let { user ->
                ProfileScreen(
                    usuario = user,
                    onLogout = {
                        onUsuarioChange(null)
                        onRoleChange("alumno")
                        navController.navigate(Rutas.Login.ruta) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            } ?: LaunchedEffect(Unit) {
                navController.navigate(Rutas.Login.ruta) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}