package com.marlonportuguez.cineapp.ui.screens.acquiredmovies

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.google.firebase.Timestamp
import com.marlonportuguez.cineapp.data.model.UserHistoryEntry
import com.marlonportuguez.cineapp.ui.theme.CineAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.lifecycle.ViewModel as AndroidxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcquiredMoviesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    acquiredMoviesViewModel: AcquiredMoviesViewModel = viewModel(),
    onReviewClick: (UserHistoryEntry) -> Unit
) {
    val acquiredMovies by acquiredMoviesViewModel.acquiredMovies.collectAsState()
    val isLoading by acquiredMoviesViewModel.isLoading.collectAsState()
    val error by acquiredMoviesViewModel.error.collectAsState()
    val cancellationSuccess by acquiredMoviesViewModel.cancellationSuccess.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        acquiredMoviesViewModel.loadAcquiredMovies()
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            acquiredMoviesViewModel.resetError()
        }
    }

    LaunchedEffect(cancellationSuccess) {
        if (cancellationSuccess) {
            Toast.makeText(context, "Boleto cancelado con éxito!", Toast.LENGTH_LONG).show()
            acquiredMoviesViewModel.resetCancellationSuccess()
            acquiredMoviesViewModel.loadAcquiredMovies()
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
                            "Mi Historial",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
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
                } else if (acquiredMovies.isNotEmpty()) {
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
                                text = "Historial de Compras",
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
                            items(acquiredMovies) { entry ->
                                UserHistoryCard(
                                    entry = entry,
                                    onCancelClick = { acquiredMoviesViewModel.cancelPurchasedTicket(entry) },
                                    onReviewClick = onReviewClick,
                                    isProcessing = isLoading
                                )
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
                                        text = "H",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay historial disponible",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tus compras y cancelaciones aparecerán aquí",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.5f),
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
fun UserHistoryCard(
    entry: UserHistoryEntry,
    onCancelClick: () -> Unit,
    onReviewClick: (UserHistoryEntry) -> Unit,
    isProcessing: Boolean
) {
    val dateFormat = SimpleDateFormat("dd 'de' MMMM, hh:mm a", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("America/Bogota")

    val isPurchased = entry.action == "purchased"
    val statusColor = if (isPurchased) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    // Calcular si la película ya terminó para habilitar el botón de opinar
    val isMovieFinished = try {
        entry.showtimeStartTime + (entry.durationMinutes * 60 * 1000L) < System.currentTimeMillis()
    } catch (e: Exception) {
        false // Si hay error en el cálculo, no habilitar el botón
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Imagen de la película
                if (entry.moviePosterUrl.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box {
                            Image(
                                painter = rememberAsyncImagePainter(model = entry.moviePosterUrl),
                                contentDescription = "${entry.details} Poster",
                                modifier = Modifier
                                    .size(80.dp, 110.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )

                            // Badge de estado
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(24.dp),
                                shape = CircleShape,
                                color = statusColor
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPurchased) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = entry.action,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.size(80.dp, 110.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor.copy(alpha = 0.2f)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPurchased) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = entry.action,
                                tint = statusColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Información de la transacción
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.details,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Estado de la transacción
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = statusColor.copy(alpha = 0.2f)
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
                                color = statusColor
                            ) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPurchased) "Comprado" else "Cancelado",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fecha
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(4.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.6f)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dateFormat.format(entry.actionDate.toDate()),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Botones de acción para compras
            if (isPurchased) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Cancelar
                    Button(
                        onClick = onCancelClick,
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
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
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Cancelando...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        } else {
                            Text(
                                "Cancelar",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Botón Opinar
                    OutlinedButton(
                        onClick = { onReviewClick(entry) },
                        enabled = !isProcessing && isMovieFinished,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isMovieFinished) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.3f),
                            disabledContentColor = Color.White.copy(alpha = 0.3f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isMovieFinished) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.RateReview,
                                contentDescription = "Opinar",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isMovieFinished) "Opinar" else "Pendiente",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                // Mensaje informativo para el botón de opinar
                if (!isMovieFinished && isPurchased) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Podrás opinar cuando termine la función",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AcquiredMoviesScreenPreview() {
    CineAppTheme {
        val sampleHistoryEntries = listOf(
            UserHistoryEntry(
                id = "hist1",
                userId = "user1",
                movieId = "movie1",
                showtimeId = "show1",
                action = "purchased",
                actionDate = Timestamp(System.currentTimeMillis() / 1000, 0),
                details = "2 boletos para Superman",
                moviePosterUrl = "https://sacnkprodarcms.blob.core.windows.net/content/posters/HO00010495.jpg",
                showtimeStartTime = System.currentTimeMillis() - 7200000, // 2 horas atrás
                durationMinutes = 143
            ),
            UserHistoryEntry(
                id = "hist2",
                userId = "user1",
                movieId = "movie2",
                showtimeId = "show2",
                action = "cancelled",
                actionDate = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0),
                details = "1 boleto cancelado para Hachiko",
                moviePosterUrl = "https://upload.wikimedia.org/wikipedia/en/5/5a/Hachi_A_Dog%27s_Tale_poster.jpg",
                showtimeStartTime = System.currentTimeMillis() + 3600000, // 1 hora en el futuro
                durationMinutes = 93
            )
        )
        val previewViewModel = object : AndroidxViewModel() {
            val acquiredMovies = MutableStateFlow(sampleHistoryEntries).asStateFlow()
            val isLoading = MutableStateFlow(false).asStateFlow()
            val error = MutableStateFlow<String?>(null).asStateFlow()
            val cancellationSuccess = MutableStateFlow(false).asStateFlow()
            fun loadAcquiredMovies() {}
            fun resetError() {}
            fun resetCancellationSuccess() {}
            fun cancelPurchasedTicket(entry: UserHistoryEntry) {}
        }
        AcquiredMoviesScreen(
            onBack = {},
            acquiredMoviesViewModel = previewViewModel as AcquiredMoviesViewModel,
            onReviewClick = {}
        )
    }
}
