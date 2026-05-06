package com.dev.uasist.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dev.uasist.ui.screens.*
import com.dev.uasist.model.Usuario
import com.dev.uasist.data.AsistenciaRepository

sealed class Rutas(val ruta: String) {
    object Login : Rutas("login")
    object SignUp : Rutas("signup")
    object Profile : Rutas("profile")
    object ResetPassword : Rutas("nueva_clave")
    object ForgotPassword : Rutas("recuperar_clave")

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
        // --- PANTALLA DE LOGIN ---
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

        composable(Rutas.ForgotPassword.ruta) {
            ForgotPasswordScreen(
                repository = AsistenciaRepository(),
                onBack = { navController.popBackStack() }
            )
        }

        // --- PANTALLAS DEL ALUMNO ---
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

        composable(Rutas.Clases.ruta) {
            usuarioActual?.let { user ->
                ClasesScreen(usuario = user)
            } ?: LaunchedEffect(Unit) {
                navController.navigate(Rutas.Login.ruta) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        // --- PANTALLAS DEL PROFESOR ---
        composable(Rutas.ProfesorDashboard.ruta) {
            usuarioActual?.let { user ->
                ProfesorDashboardScreen(
                    usuario = user,
                    onNavigateToQR = { navController.navigate(Rutas.GenerarQR.ruta) },
                    onNavigateToLista = { navController.navigate(Rutas.AsistenciaLista.ruta) }
                )
            } ?: LaunchedEffect(Unit) {
                navController.navigate(Rutas.Login.ruta) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        composable(Rutas.GenerarQR.ruta) {
            usuarioActual?.let { user ->
                GenerarQRScreen(usuario = user)
            } ?: LaunchedEffect(Unit) {
                navController.navigate(Rutas.Login.ruta) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        composable(Rutas.AsistenciaLista.ruta) {
            usuarioActual?.let { user ->
                AsistenciaAlumnosScreen(usuario = user)
            } ?: LaunchedEffect(Unit) {
                navController.navigate(Rutas.Login.ruta) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        composable(Rutas.SignUp.ruta) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    // Después del registro, intentamos obtener el perfil para setear el usuarioActual
                    // O simplemente redirigimos al login para que el usuario inicie sesión.
                    // Para una mejor UX, intentaremos loguear automáticamente o pedir login.
                    // Por ahora, redirigimos al Login con un mensaje de éxito sería ideal, 
                    // pero aquí lo mandamos al dashboard si logramos reconstruir el objeto.
                    // Como simplificación para este flujo, lo mandaremos al Login.
                    navController.navigate(Rutas.Login.ruta) {
                        popUpTo(Rutas.SignUp.ruta) { inclusive = true }
                    }
                }
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
    }
}