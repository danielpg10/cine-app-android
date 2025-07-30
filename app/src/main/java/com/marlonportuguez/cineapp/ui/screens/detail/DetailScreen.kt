package com.marlonportuguez.cineapp.ui.screens.detail

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.marlonportuguez.cineapp.data.model.Movie
import com.marlonportuguez.cineapp.data.model.Showtime
import com.marlonportuguez.cineapp.data.model.Theater
import com.marlonportuguez.cineapp.ui.theme.CineAppTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    movieId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    detailViewModel: DetailViewModel = viewModel()
) {
    val movie by detailViewModel.movie.collectAsState()
    val showtimes by detailViewModel.showtimes.collectAsState()
    val isLoading by detailViewModel.isLoading.collectAsState()
    val error by detailViewModel.error.collectAsState()
    val purchaseSuccess by detailViewModel.purchaseSuccess.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(movieId) {
        if (movieId != null) {
            detailViewModel.loadMovieDetails(movieId)
        } else {
            Toast.makeText(context, "ID de película no proporcionado", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            detailViewModel.resetError()
        }
    }

    LaunchedEffect(purchaseSuccess) {
        if (purchaseSuccess) {
            Toast.makeText(context, "Compra realizada con éxito!", Toast.LENGTH_LONG).show()
            detailViewModel.resetPurchaseSuccess()
            movieId?.let { detailViewModel.loadMovieDetails(it) }
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
                            text = movie?.title ?: "Detalles",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (movie != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        MovieDetailHeader(movie = movie!!)
                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(6.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Horarios Disponibles",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = Color.White
                            )
                        }

                        if (showtimes.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                showtimes.forEach { (showtime, theater) ->
                                    ShowtimeCard(
                                        showtime = showtime,
                                        theater = theater,
                                        onBuyClick = { numberOfTickets ->
                                            detailViewModel.buyTickets(showtime, numberOfTickets)
                                        },
                                        isProcessing = isLoading
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "No hay horarios disponibles en este momento",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.align(Alignment.Center),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = "Película no encontrada",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovieDetailHeader(movie: Movie) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = movie.posterUrl),
                    contentDescription = "${movie.title} Poster",
                    modifier = Modifier
                        .size(200.dp, 280.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(4.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.6f)
                ) {}
                Text(
                    text = movie.genre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Surface(
                    modifier = Modifier.size(4.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.6f)
                ) {}
                Text(
                    text = "${movie.durationMinutes} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = movie.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Justify,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun ShowtimeCard(
    showtime: Showtime,
    theater: Theater,
    onBuyClick: (Int) -> Unit,
    isProcessing: Boolean
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    currencyFormat.currency = Currency.getInstance("COP")
    val dateFormat = SimpleDateFormat("dd 'de' MMMM, hh:mm a", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("America/Bogota")
    var numberOfTickets by remember { mutableIntStateOf(1) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "VIP",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = theater.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = dateFormat.format(showtime.startTime.toDate()),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currencyFormat.format(showtime.price),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${showtime.availableSeats} disponibles",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = if (numberOfTickets > 1) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
                        ) {
                            IconButton(
                                onClick = { if (numberOfTickets > 1) numberOfTickets-- },
                                enabled = numberOfTickets > 1 && !isProcessing
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Reducir",
                                    tint = if (numberOfTickets > 1) Color.White else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = numberOfTickets.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = if (numberOfTickets < showtime.availableSeats) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
                        ) {
                            IconButton(
                                onClick = { if (numberOfTickets < showtime.availableSeats) numberOfTickets++ },
                                enabled = numberOfTickets < showtime.availableSeats && !isProcessing
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Aumentar",
                                    tint = if (numberOfTickets < showtime.availableSeats) Color.White else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onBuyClick(numberOfTickets) },
                enabled = !isProcessing && showtime.availableSeats > 0 && numberOfTickets > 0,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Procesando...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                } else {
                    Text(
                        "Comprar $numberOfTickets ${if (numberOfTickets == 1) "boleto" else "boletos"}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    CineAppTheme {
        val sampleMovie = Movie(
            id = "sampleMovieId",
            title = "Siempre a tu lado, Hachiko",
            description = "La historia real de un perro fiel que espera a su dueño en una estación de tren todos los días, incluso después de su muerte",
            genre = "Drama, Familiar",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/5/5a/Hachi_A_Dog%27s_Tale_poster.jpg",
            durationMinutes = 93,
            available = true
        )
        val sampleShowtime = Showtime(
            id = "sampleShowtimeId",
            movieId = "sampleMovieId",
            theaterId = "sampleTheaterId",
            startTime = Timestamp(System.currentTimeMillis() / 1000 + 7200, 0),
            endTime = Timestamp(System.currentTimeMillis() / 1000 + 7200 + (93 * 60), 0),
            price = 25000.0,
            availableSeats = 45
        )
        val sampleTheater = Theater(
            id = "sampleTheaterId",
            name = "Sala VIP",
            capacity = 50
        )
        val previewViewModel = object : ViewModel() {
            val movie = MutableStateFlow(sampleMovie).asStateFlow()
            val showtimes = MutableStateFlow(listOf(Pair(sampleShowtime, sampleTheater))).asStateFlow()
            val isLoading = MutableStateFlow(false).asStateFlow()
            val error = MutableStateFlow<String?>(null).asStateFlow()
            val purchaseSuccess = MutableStateFlow(false).asStateFlow()
            fun loadMovieDetails(movieId: String) {}
            fun resetError() {}
            fun resetPurchaseSuccess() {}
            fun buyTickets(showtime: Showtime, numberOfTickets: Int) {}
        }
        DetailScreen(movieId = "sampleMovieId", onBack = {}, detailViewModel = previewViewModel as DetailViewModel)
    }
}
