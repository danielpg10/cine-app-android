package com.marlonportuguez.cineapp.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.marlonportuguez.cineapp.data.model.Movie
import com.marlonportuguez.cineapp.data.model.MovieListItem
import com.marlonportuguez.cineapp.data.model.Showtime
import com.marlonportuguez.cineapp.data.repository.MovieRepository
import com.marlonportuguez.cineapp.ui.theme.CineAppTheme
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(),
    onMovieClick: (String) -> Unit // Parámetro para manejar el clic en la película
) {
    val movies by homeViewModel.movies.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val error by homeViewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            homeViewModel.resetError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CineApp - Cartelera") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (movies.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(movies) { movieItem ->
                        MovieCard(movieItem = movieItem, onMovieClick = onMovieClick)
                    }
                }
            } else {
                Text(
                    text = "No hay películas disponibles en este momento.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieCard(movieItem: MovieListItem, onMovieClick: (String) -> Unit) { // Parámetro para el clic
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { onMovieClick(movieItem.movie.id) } // Llama al callback al hacer clic
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = movieItem.movie.posterUrl),
                contentDescription = "${movieItem.movie.title} Poster",
                modifier = Modifier
                    .size(90.dp, 120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movieItem.movie.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = movieItem.movie.genre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${movieItem.movie.durationMinutes} minutos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                movieItem.earliestShowtime?.let { showtime ->
                    movieItem.theaterName?.let { theaterName ->
                        val dateTimeFormat = SimpleDateFormat("dd 'de' MMMM, hh:mm a", Locale.getDefault())
                        dateTimeFormat.timeZone = TimeZone.getTimeZone("America/Bogota")
                        val formattedDateTime = dateTimeFormat.format(showtime.startTime.toDate())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$formattedDateTime en $theaterName",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CineAppTheme {
        val sampleMovies = listOf(
            Movie(
                title = "Superman",
                description = "Un héroe con superpoderes...",
                genre = "Acción",
                posterUrl = "https://sacnkprodarcms.blob.core.windows.net/content/posters/HO00010495.jpg",
                durationMinutes = 143,
                available = true
            ),
            Movie(
                title = "Siempre a tu lado, Hachiko",
                description = "La historia de un perro fiel...",
                genre = "Drama",
                posterUrl = "https://upload.wikimedia.org/wikipedia/en/5/5a/Hachi_A_Dog%27s_Tale_poster.jpg",
                durationMinutes = 93,
                available = true
            )
        )
        val sampleShowtime = Showtime(
            movieId = "someMovieId",
            theaterId = "someTheaterId",
            startTime = Timestamp(System.currentTimeMillis() / 1000 + 3600, 0),
            price = 25000.0,
            availableSeats = 100
        )
        val sampleMovieItems = listOf(
            MovieListItem(sampleMovies[0], sampleShowtime, "Sala Principal"),
            MovieListItem(sampleMovies[1], sampleShowtime, "Sala VIP")
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(sampleMovieItems) { movieItem ->
                MovieCard(movieItem = movieItem, onMovieClick = {})
            }
        }
    }
}