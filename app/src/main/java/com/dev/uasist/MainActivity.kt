package com.dev.uasist

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dev.uasist.ui.theme.UasistTheme
import com.dev.uasist.model.Usuario
import com.dev.uasist.ui.navigation.AppNavigation
import com.dev.uasist.ui.navigation.Rutas
import kotlinx.coroutines.launch

// --- Mantenemos los BottomNavItem como los tienes ---
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Inicio : BottomNavItem(Rutas.AlumnoDashboard.ruta, Icons.Default.Home, "Inicio")
    object QR : BottomNavItem(Rutas.ScannerQR.ruta, Icons.Default.QrCodeScanner, "QR")
    object Clases : BottomNavItem(Rutas.Clases.ruta, Icons.Default.DateRange, "Clases")
    object InicioProfesor : BottomNavItem(Rutas.ProfesorDashboard.ruta, Icons.Default.Home, "Inicio")
    object GenerarQR : BottomNavItem(Rutas.GenerarQR.ruta, Icons.Default.QrCode, "Generar QR")
    object Asistencia : BottomNavItem(Rutas.AsistenciaLista.ruta, Icons.Default.Groups, "Alumnos")
    object Perfil : BottomNavItem(Rutas.Profile.ruta, Icons.Default.Person, "Perfil")
}

class MainActivity : ComponentActivity() {

    // Estado para disparar el Snackbar desde fuera de la UI
    private var snackbarMessage = mutableStateOf<String?>(null)
    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UasistTheme {
                val controller = rememberNavController()
                navController = controller

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                val navBackStackEntry by controller.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
                var userRole by remember { mutableStateOf("alumno") }

                // Escuchar cambios en el mensaje del snackbar
                LaunchedEffect(snackbarMessage.value) {
                    snackbarMessage.value?.let { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = message,
                                duration = SnackbarDuration.Long,
                                withDismissAction = true
                            )
                            snackbarMessage.value = null // Limpiar después de mostrar
                        }
                    }
                }

                val pantallasSinBarra = listOf(
                    Rutas.Login.ruta, Rutas.SignUp.ruta,
                    Rutas.ForgotPassword.ruta, Rutas.ResetPassword.ruta
                )
                val showBottomBar = !pantallasSinBarra.contains(currentDestination?.route) && usuarioActual != null

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }, // <--- EL HOST DEL SNACKBAR
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                val items = if (userRole == "alumno") {
                                    listOf(BottomNavItem.Inicio, BottomNavItem.QR, BottomNavItem.Clases, BottomNavItem.Perfil)
                                } else {
                                    listOf(BottomNavItem.InicioProfesor, BottomNavItem.GenerarQR, BottomNavItem.Asistencia, BottomNavItem.Perfil)
                                }
                                items.forEach { item ->
                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) },
                                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                        onClick = {
                                            // LÓGICA DINÁMICA: Si la ruta necesita parámetros, los inyectamos aquí
                                            val rutaReal = when (item.route) {
                                                Rutas.AsistenciaLista.ruta -> {
                                                    val profesor = usuarioActual as? Usuario.Profesor
                                                    "asistencia_lista/${profesor?.id ?: "error"}/${profesor?.materiaImpartida?.nombre ?: "Materia"}"
                                                }
                                                Rutas.GenerarQR.ruta -> {
                                                    // Si el profesor intenta ir al QR desde la barra pero no ha iniciado clase
                                                    // mejor lo mandamos al Dashboard para que le de al botón de "Iniciar"
                                                    Rutas.ProfesorDashboard.ruta
                                                }
                                                else -> item.route
                                            }

                                            controller.navigate(rutaReal) {
                                                popUpTo(controller.graph.findStartDestination().id) { saveState = true }
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
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation(
                            navController = controller,
                            usuarioActual = usuarioActual,
                            onUsuarioChange = { usuarioActual = it },
                            onRoleChange = { userRole = it }
                        )
                    }
                }

                LaunchedEffect(Unit) {
                    intent?.let { handleIntent(it) }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val data: Uri? = intent.data
        if (data != null && data.scheme == "uasist") {
            when (data.host) {
                "reset-password" -> {
                    navController?.navigate(Rutas.ResetPassword.ruta)
                }
                "login" -> {
                    // Actualizamos el estado para que el LaunchedEffect lo dispare
                    snackbarMessage.value = "¡Cuenta verificada! Ya puedes iniciar sesión."
                    navController?.navigate(Rutas.Login.ruta) {
                        popUpTo(0)
                    }
                }
            }
        }
    }
}