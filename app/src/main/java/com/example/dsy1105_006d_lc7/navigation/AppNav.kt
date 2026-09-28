package com.example.dsy1105_006d_lc7.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dsy1105_006d_lc7.ui.home.MuestraDatosScreen
import com.example.dsy1105_006d_lc7.ui.login.HomeScreen

@Composable
fun AppNav(){
    val navController= rememberNavController()
    NavHost(navController=navController, startDestination = "login") {
        composable("login") {
            HomeScreen(navController = navController)
        }// fin composable

        composable(
            route = "muestraDatos/{username}",
            arguments = listOf(
                navArgument("username") {
                    type = NavType.StringType
                }
            )// fin list Of

        )// fin composable

        { //inicio back
                backStackEntry ->
            val username = backStackEntry.arguments?.getString("username").orEmpty()
            MuestraDatosScreen(username = username, navController = navController)

        }// termino back

    }// Fin Nav
}//fin AppNav