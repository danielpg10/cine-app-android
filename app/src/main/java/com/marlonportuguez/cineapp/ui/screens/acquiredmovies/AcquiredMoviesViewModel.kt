package com.marlonportuguez.cineapp.ui.screens.acquiredmovies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.marlonportuguez.cineapp.data.model.Movie
import com.marlonportuguez.cineapp.data.model.Showtime
import com.marlonportuguez.cineapp.data.model.UserHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AcquiredMoviesViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _acquiredMovies = MutableStateFlow<List<UserHistoryEntry>>(emptyList())
    val acquiredMovies: StateFlow<List<UserHistoryEntry>> = _acquiredMovies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _cancellationSuccess = MutableStateFlow(false)
    val cancellationSuccess: StateFlow<Boolean> = _cancellationSuccess.asStateFlow()

    fun loadAcquiredMovies() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _error.value = "Debe iniciar sesión para ver su historial."
            return
        }

        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val historySnapshot = firestore.collection("userHistory")
                    .whereEqualTo("userId", currentUserId)
                    .orderBy("actionDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val historyEntries = historySnapshot.documents.mapNotNull { it.toObject(UserHistoryEntry::class.java) }
                _acquiredMovies.value = historyEntries
                _isLoading.value = false

                if (historyEntries.isEmpty() && _error.value == null) {
                    _error.value = "No hay historial de películas adquirido/cancelado aún."
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al cargar historial de películas."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Cancela un boleto adquirido por el usuario
    fun cancelPurchasedTicket(entry: UserHistoryEntry) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _error.value = "Debe iniciar sesión para cancelar boletos."
            return
        }
        if (entry.action != "purchased") {
            _error.value = "Solo se pueden cancelar boletos comprados."
            return
        }

        _isLoading.value = true
        _error.value = null
        _cancellationSuccess.value = false

        viewModelScope.launch {
            try {
                // Obtener el horario de función para actualizar asientos disponibles
                val showtimeDoc = firestore.collection("showtimes").document(entry.showtimeId).get().await()
                val showtime = showtimeDoc.toObject(Showtime::class.java)
                if (showtime == null) {
                    throw Exception("Horario de función no encontrado para la cancelación.")
                }

                // Obtener la película para los detalles del historial
                val movieDoc = firestore.collection("movies").document(entry.movieId).get().await()
                val movie = movieDoc.toObject(Movie::class.java)
                if (movie == null) {
                    throw Exception("Película no encontrada para la cancelación.")
                }

                // Actualizar asientos disponibles en el horario (Firestore Transaction)
                val showtimeRef = firestore.collection("showtimes").document(showtime.id)
                firestore.runTransaction { transaction ->
                    val freshShowtime = transaction.get(showtimeRef).toObject(Showtime::class.java)
                    if (freshShowtime == null) {
                        throw Exception("Horario de función no encontrado para la cancelación.")
                    }
                    transaction.update(showtimeRef, "availableSeats", FieldValue.increment(entry.numberOfTickets.toLong()))
                    null
                }.await()

                // Registrdr la transacción de cancelación
                val transactionData = hashMapOf(
                    "userId" to currentUserId,
                    "showtimeId" to entry.showtimeId,
                    "movieId" to entry.movieId,
                    "numberOfTickets" to entry.numberOfTickets,
                    "totalAmount" to -entry.totalAmount, // Monto negativo para reembolso
                    "transactionDate" to Timestamp.now(),
                    "transactionType" to "cancellation",
                    "status" to "completed"
                )
                firestore.collection("transactions").add(transactionData).await()

                // Registrar en el historial del usuario como cancelación
                val userHistoryData = hashMapOf(
                    "userId" to currentUserId,
                    "movieId" to entry.movieId,
                    "showtimeId" to entry.showtimeId,
                    "action" to "cancelled",
                    "actionDate" to Timestamp.now(),
                    "details" to "${entry.numberOfTickets} boletos cancelados para ${movie.title}",
                    "moviePosterUrl" to movie.posterUrl
                )
                firestore.collection("userHistory").add(userHistoryData).await()

                _cancellationSuccess.value = true
                loadAcquiredMovies()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al procesar la cancelación."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetError() {
        _error.value = null
    }

    fun resetCancellationSuccess() {
        _cancellationSuccess.value = false
    }
}