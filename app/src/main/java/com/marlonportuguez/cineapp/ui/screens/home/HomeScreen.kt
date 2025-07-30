package com.marlonportuguez.cineapp.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onMovieClick: (String) -> Unit,
    onViewAcquiredMoviesClick: () -> Unit
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A),
                        Color(0xFF0D0D0D)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "CINEAPP",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onViewAcquiredMoviesClick,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Default.List, contentDescription = "Mis Películas")
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (movies.isNotEmpty()) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(6.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cartelera Actual",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp
                                ),
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(movies) { movieItem ->
                                MovieCard(movieItem = movieItem, onMovieClick = onMovieClick)
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.align(Alignment.Center),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.1f)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "?",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay películas disponibles",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieCard(movieItem: MovieListItem, onMovieClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = { onMovieClick(movieItem.movie.id) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = movieItem.movie.posterUrl),
                    contentDescription = "${movieItem.movie.title} Poster",
                    modifier = Modifier
                        .size(100.dp, 140.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movieItem.movie.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(4.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {}
                    Text(
                        text = movieItem.movie.genre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(4.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary
                    ) {}
                    Text(
                        text = "${movieItem.movie.durationMinutes} minutos",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                movieItem.earliestShowtime?.let { showtime ->
                    movieItem.theaterName?.let { theaterName ->
                        val dateTimeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        dateTimeFormat.timeZone = TimeZone.getTimeZone("America/Bogota")
                        val formattedDateTime = dateTimeFormat.format(showtime.startTime.toDate())

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(4.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary
                                ) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$formattedDateTime • $theaterName",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
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