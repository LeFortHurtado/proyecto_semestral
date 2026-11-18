package com.example.dsy1105_006d_lc7.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dsy1105_006d_lc7.model.UserRole
import com.example.dsy1105_006d_lc7.ui.screen.*
import com.example.dsy1105_006d_lc7.viewmodel.LoginViewModel

/**
 * Grafo de navegación principal con control de acceso por roles.
 * Login → Menú Principal → Módulos (Productos, Movimientos, Reposición, Proveedores, Ubicaciones).
 *
 * Restricciones por rol:
 * - ADMIN: Acceso total a todos los módulos y opciones de edición/eliminación.
 * - SELLER: Acceso restringido. Reposición y Proveedores no son navegables.
 */
@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by loginViewModel.uiState.collectAsState()
    val currentRole = authState.role ?: UserRole.ADMIN

    NavHost(navController = navController, startDestination = "login") {

        // ── Inicio de Sesión ──
        composable("login") {
            PantallaLogin(navController = navController, vm = loginViewModel)
        }

        // ── Menú Principal (Dashboard) ──
        composable(
            route = "muestraDatos/{username}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username").orEmpty()
            PantallaMenuPrincipal(
                username = username,
                role = currentRole,
                navController = navController
            )
        }

        // ── Módulo de Productos ──
        composable("products") {
            PantallaProductos(
                navController = navController,
                role = currentRole
            )
        }

        // ── Formulario de Producto (crear/editar) ──
        composable(
            route = "productForm?productId={productId}",
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            if (currentRole == UserRole.ADMIN) {
                val productId = backStackEntry.arguments?.getLong("productId") ?: -1L
                PantallaFormularioProducto(
                    navController = navController,
                    role = currentRole,
                    productId = productId
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        // ── Módulo de Movimientos ──
        composable("movements") {
            PantallaMovimientos(
                navController = navController,
                role = currentRole
            )
        }

        // ── Formulario de Movimiento (crear/editar) ──
        composable(
            route = "movementForm?movementId={movementId}",
            arguments = listOf(
                navArgument("movementId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val movementId = backStackEntry.arguments?.getLong("movementId") ?: -1L
            PantallaFormularioMovimiento(
                navController = navController,
                role = currentRole,
                movementId = movementId
            )
        }

        // ── Módulo de Reposición (Restringido para SELLER) ──
        composable("reposition") {
            if (currentRole == UserRole.ADMIN) {
                PantallaReposicion(navController = navController)
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        // ── Módulo de Proveedores (Restringido para SELLER) ──
        composable("suppliers") {
            if (currentRole == UserRole.ADMIN) {
                PantallaProveedores(navController = navController)
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        // ── Módulo de Ubicaciones ──
        composable("locations") {
            PantallaUbicaciones(navController = navController)
        }
    }
}

// Alias para compatibilidad con llamadas existentes a AppNav()
@Composable
fun AppNav(loginViewModel: LoginViewModel = viewModel()) = AppNavigation(loginViewModel)
