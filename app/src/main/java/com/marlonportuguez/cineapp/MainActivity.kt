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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
// import com.marlonportuguez.cineapp.data.local.db.AppDatabase // Desactivado: Base de datos Room
// import com.marlonportuguez.cineapp.data.repository.LocalMovieRepository // Desactivado: Repositorio Room
import com.marlonportuguez.cineapp.ui.screens.acquiredmovies.AcquiredMoviesScreen
import com.marlonportuguez.cineapp.ui.screens.acquiredmovies.AcquiredMoviesViewModel
import com.marlonportuguez.cineapp.ui.screens.auth.AuthScreen
import com.marlonportuguez.cineapp.ui.screens.detail.DetailScreen
import com.marlonportuguez.cineapp.ui.screens.detail.DetailViewModel
import com.marlonportuguez.cineapp.ui.screens.home.HomeScreen
import com.marlonportuguez.cineapp.ui.theme.CineAppTheme
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.ViewModel as AndroidxViewModel

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
    const val DETAIL_SCREEN = "detail_screen/{movieId}"
    const val DETAIL_SCREEN_BASE = "detail_screen"
    const val ACQUIRED_MOVIES_SCREEN = "acquired_movies_screen"
}

@Composable
fun CineAppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Inicialización de Room Database - Desactivado
    // val appDatabase = AppDatabase.getDatabase(context)
    // val acquiredMovieDao = appDatabase.acquiredMovieDao()
    // val localMovieRepository = LocalMovieRepository(acquiredMovieDao)

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
                onMovieClick = { movieId ->
                    navController.navigate("${Routes.DETAIL_SCREEN_BASE}/$movieId")
                },
                onViewAcquiredMoviesClick = {
                    navController.navigate(Routes.ACQUIRED_MOVIES_SCREEN)
                }
            )
        }

        composable(
            route = Routes.DETAIL_SCREEN,
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")
            val detailViewModel: DetailViewModel = viewModel()
            DetailScreen(
                movieId = movieId,
                onBack = { navController.popBackStack() },
                detailViewModel = detailViewModel
            )
        }

        composable(Routes.ACQUIRED_MOVIES_SCREEN) {
            // AcquiredMoviesViewModel se crea sin factory
            val acquiredMoviesViewModel: AcquiredMoviesViewModel = viewModel()
            AcquiredMoviesScreen(
                onBack = { navController.popBackStack() },
                acquiredMoviesViewModel = acquiredMoviesViewModel
            )
        }
    }
}

// Factories de ViewModel - Desactivados
/*
class DetailViewModelFactory(private val localMovieRepository: LocalMovieRepository) : ViewModelProvider.Factory {
    override fun <T : AndroidxViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(localMovieRepository = localMovieRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AcquiredMoviesViewModelFactory(private val localMovieRepository: LocalMovieRepository) : ViewModelProvider.Factory {
    override fun <T : AndroidxViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(AcquiredMoviesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AcquiredMoviesViewModel(localMovieRepository = localMovieRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
*/