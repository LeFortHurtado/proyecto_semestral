package com.example.dsy1105_006d_lc7.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dsy1105_006d_lc7.ui.dashboard.DashboardScreen
import com.example.dsy1105_006d_lc7.ui.locations.LocationsScreen
import com.example.dsy1105_006d_lc7.ui.login.HomeScreen
import com.example.dsy1105_006d_lc7.ui.movements.MovementFormScreen
import com.example.dsy1105_006d_lc7.ui.movements.MovementsScreen
import com.example.dsy1105_006d_lc7.ui.products.ProductFormScreen
import com.example.dsy1105_006d_lc7.ui.products.ProductsScreen
import com.example.dsy1105_006d_lc7.ui.reposition.RepositionScreen
import com.example.dsy1105_006d_lc7.ui.suppliers.SuppliersScreen

/**
 * Grafo de navegación principal de la aplicación.
 * Login → Dashboard → Módulos (Productos, Movimientos, Reposición, Proveedores, Ubicaciones)
 */
@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        // ── Login ──
        composable("login") {
            HomeScreen(navController = navController)
        }

        // ── Dashboard principal (reemplaza MuestraDatosScreen) ──
        composable(
            route = "muestraDatos/{username}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username").orEmpty()
            DashboardScreen(username = username, navController = navController)
        }

        // ── Módulo de Productos ──
        composable("products") {
            ProductsScreen(navController = navController)
        }

        // ── Formulario de Producto (crear/editar) ──
        composable("productForm") {
            ProductFormScreen(navController = navController)
        }

        // ── Módulo de Movimientos ──
        composable("movements") {
            MovementsScreen(navController = navController)
        }

        // ── Formulario de Movimiento ──
        composable("movementForm") {
            MovementFormScreen(navController = navController)
        }

        // ── Módulo de Reposición ──
        composable("reposition") {
            RepositionScreen(navController = navController)
        }

        // ── Módulo de Proveedores ──
        composable("suppliers") {
            SuppliersScreen(navController = navController)
        }

        // ── Módulo de Ubicaciones ──
        composable("locations") {
            LocationsScreen(navController = navController)
        }
    }
}