package com.marlonportuguez.cineapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marlonportuguez.cineapp.ui.screens.auth.AuthScreen
import com.marlonportuguez.cineapp.ui.screens.detail.DetailScreen // NUEVA IMPORTACIÓN
import com.marlonportuguez.cineapp.ui.screens.home.HomeScreen
import com.marlonportuguez.cineapp.ui.theme.CineAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CineAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CineAppNavigation()
                }
            }
        }
    }
}

object Routes {
    const val AUTH_SCREEN = "auth_screen"
    const val HOME_SCREEN = "home_screen"
    // Definición de la ruta de detalle con un argumento
    const val DETAIL_SCREEN = "detail_screen/{movieId}"
    const val DETAIL_SCREEN_BASE = "detail_screen" // Ruta base sin argumento
}

@Composable
fun CineAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.AUTH_SCREEN) {
        composable(Routes.AUTH_SCREEN) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Routes.HOME_SCREEN) {
                        popUpTo(Routes.AUTH_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME_SCREEN) {
            HomeScreen(
                // Al hacer clic en una tarjeta de película, navegar a DetailScreen
                onMovieClick = { movieId ->
                    navController.navigate("${Routes.DETAIL_SCREEN_BASE}/$movieId")
                }
            )
        }

        // Definición del destino para DetailScreen, esperando un argumento 'movieId'
        composable(
            route = Routes.DETAIL_SCREEN,
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")
            DetailScreen(
                movieId = movieId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}