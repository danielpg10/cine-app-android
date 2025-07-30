package com.marlonportuguez.cineapp.ui.screens.detail

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.marlonportuguez.cineapp.data.model.Movie
import com.marlonportuguez.cineapp.data.model.Showtime
import com.marlonportuguez.cineapp.data.model.Theater
import com.marlonportuguez.cineapp.data.repository.MovieRepository
import com.marlonportuguez.cineapp.ui.theme.CineAppTheme
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.Currency

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(movie?.title ?: "Detalles de Película") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
            } else if (movie != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MovieDetailHeader(movie = movie!!)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Horarios Disponibles:",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        textAlign = TextAlign.Start
                    )

                    if (showtimes.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Text(
                            text = "No hay horarios disponibles para esta película en este momento.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                Text(
                    text = "Película no encontrada.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun MovieDetailHeader(movie: Movie) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = rememberAsyncImagePainter(model = movie.posterUrl),
            contentDescription = "${movie.title} Poster",
            modifier = Modifier
                .size(180.dp, 240.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = movie.title,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${movie.genre} | ${movie.durationMinutes} minutos",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = movie.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowtimeCard(showtime: Showtime, theater: Theater, onBuyClick: (Int) -> Unit, isProcessing: Boolean) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    currencyFormat.currency = Currency.getInstance("COP")

    val dateFormat = SimpleDateFormat("dd 'de' MMMM, hh:mm a", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("America/Bogota")

    var numberOfTickets by remember { mutableIntStateOf(1) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sala: ${theater.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Hora: ${dateFormat.format(showtime.startTime.toDate())}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Precio: ${currencyFormat.format(showtime.price)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Asientos disponibles: ${showtime.availableSeats}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                // Selector de cantidad de boletos
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (numberOfTickets > 1) numberOfTickets-- },
                        enabled = numberOfTickets > 1 && !isProcessing
                    ) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        text = numberOfTickets.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { if (numberOfTickets < showtime.availableSeats) numberOfTickets++ },
                        enabled = numberOfTickets < showtime.availableSeats && !isProcessing
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onBuyClick(numberOfTickets) },
                enabled = !isProcessing && showtime.availableSeats > 0 && numberOfTickets > 0,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Comprar (${numberOfTickets} boletos)")
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
            title = "Superman",
            description = "Un bebé alienígena es enviado a la Tierra desde un planeta moribundo y se convierte en el héroe más grande de la humanidad.",
            genre = "Superhéroes, Acción",
            posterUrl = "https://sacnkprodarcms.blob.core.windows.net/content/posters/HO00010495.jpg",
            durationMinutes = 143,
            available = true
        )
        val sampleShowtime = Showtime(
            id = "sampleShowtimeId",
            movieId = "sampleMovieId",
            theaterId = "sampleTheaterId",
            startTime = Timestamp(System.currentTimeMillis() / 1000 + 7200, 0),
            endTime = Timestamp(System.currentTimeMillis() / 1000 + 7200 + (143 * 60), 0),
            price = 28000.0,
            availableSeats = 140
        )
        val sampleTheater = Theater(
            id = "sampleTheaterId",
            name = "Sala Principal",
            capacity = 150
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