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
import com.example.dsy1105_006d_lc7.data.model.UserRole
import com.example.dsy1105_006d_lc7.ui.dashboard.DashboardScreen
import com.example.dsy1105_006d_lc7.ui.locations.LocationsScreen
import com.example.dsy1105_006d_lc7.ui.login.AuthViewModel
import com.example.dsy1105_006d_lc7.ui.login.HomeScreen
import com.example.dsy1105_006d_lc7.ui.movements.MovementFormScreen
import com.example.dsy1105_006d_lc7.ui.movements.MovementsScreen
import com.example.dsy1105_006d_lc7.ui.products.ProductFormScreen
import com.example.dsy1105_006d_lc7.ui.products.ProductsScreen
import com.example.dsy1105_006d_lc7.ui.reposition.RepositionScreen
import com.example.dsy1105_006d_lc7.ui.suppliers.SuppliersScreen

/**
 * Grafo de navegación principal con control de acceso por roles.
 * Login → Dashboard → Módulos (Productos, Movimientos, Reposición, Proveedores, Ubicaciones).
 *
 * Reglas de navegación:
 * - El rol del usuario autenticado se observa desde [AuthViewModel] mediante StateFlow.
 * - Si el usuario es SELLER, los módulos de Proveedores y Reposición NO son ruteables
 *   y redirigen de inmediato al Dashboard para prevenir acceso no autorizado.
 */
@Composable
fun AppNav(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()
    val currentRole = authState.role ?: UserRole.ADMIN

    NavHost(navController = navController, startDestination = "login") {

        // ── Login ──
        composable("login") {
            HomeScreen(navController = navController, vm = authViewModel)
        }

        // ── Dashboard principal ──
        composable(
            route = "muestraDatos/{username}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username").orEmpty()
            DashboardScreen(
                username = username,
                role = currentRole,
                navController = navController
            )
        }

        // ── Módulo de Productos ──
        composable("products") {
            ProductsScreen(
                navController = navController,
                role = currentRole
            )
        }

        // ── Formulario de Producto (crear/editar) ──
        composable("productForm") {
            ProductFormScreen(
                navController = navController,
                role = currentRole
            )
        }

        // ── Módulo de Movimientos ──
        composable("movements") {
            MovementsScreen(
                navController = navController,
                role = currentRole
            )
        }

        // ── Formulario de Movimiento ──
        composable("movementForm") {
            MovementFormScreen(
                navController = navController,
                role = currentRole
            )
        }

        // ── Módulo de Reposición (RESTRICCIÓN 5: No ruteable para SELLER) ──
        composable("reposition") {
            if (currentRole == UserRole.ADMIN) {
                RepositionScreen(navController = navController)
            } else {
                // Bloqueo total: el módulo de Reposición no es ruteable para el rol SELLER
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        // ── Módulo de Proveedores (RESTRICCIÓN 5: No ruteable para SELLER) ──
        composable("suppliers") {
            if (currentRole == UserRole.ADMIN) {
                SuppliersScreen(navController = navController)
            } else {
                // Bloqueo total: el módulo de Proveedores no es ruteable para el rol SELLER
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        // ── Módulo de Ubicaciones ──
        composable("locations") {
            LocationsScreen(navController = navController)
        }
    }
}