package com.marlonportuguez.cineapp.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.marlonportuguez.cineapp.data.model.Movie
import com.marlonportuguez.cineapp.data.model.Showtime
import com.marlonportuguez.cineapp.data.model.Theater
import com.marlonportuguez.cineapp.data.model.UserHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DetailViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _movie = MutableStateFlow<Movie?>(null)
    val movie: StateFlow<Movie?> = _movie.asStateFlow()

    private val _showtimes = MutableStateFlow<List<Pair<Showtime, Theater>>>(emptyList())
    val showtimes: StateFlow<List<Pair<Showtime, Theater>>> = _showtimes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _purchaseSuccess = MutableStateFlow(false)
    val purchaseSuccess: StateFlow<Boolean> = _purchaseSuccess.asStateFlow()

    private val _cancellationSuccess = MutableStateFlow(false)
    val cancellationSuccess: StateFlow<Boolean> = _cancellationSuccess.asStateFlow()

    fun loadMovieDetails(movieId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val movieDoc = firestore.collection("movies").document(movieId).get().await()
                val fetchedMovie = movieDoc.toObject(Movie::class.java)
                _movie.value = fetchedMovie

                if (fetchedMovie != null) {
                    val showtimeSnapshots = firestore.collection("showtimes")
                        .whereEqualTo("movieId", fetchedMovie.id)
                        .whereGreaterThanOrEqualTo("startTime", Timestamp.now())
                        .orderBy("startTime")
                        .get()
                        .await()

                    val fetchedShowtimes = showtimeSnapshots.documents.mapNotNull { it.toObject(Showtime::class.java) }

                    val showtimesWithTheaters = mutableListOf<Pair<Showtime, Theater>>()
                    for (showtime in fetchedShowtimes) {
                        val theaterDoc = firestore.collection("theaters").document(showtime.theaterId).get().await()
                        val fetchedTheater = theaterDoc.toObject(Theater::class.java)
                        if (fetchedTheater != null) {
                            showtimesWithTheaters.add(Pair(showtime, fetchedTheater))
                        }
                    }
                    _showtimes.value = showtimesWithTheaters
                } else {
                    _error.value = "Película no encontrada."
                }

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al cargar detalles de la película."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun buyTickets(showtime: Showtime, numberOfTickets: Int) {
        val currentUserId = auth.currentUser?.uid
        val currentMovie = movie.value
        if (currentUserId == null) {
            _error.value = "Debe iniciar sesión para comprar boletos."
            return
        }
        if (numberOfTickets <= 0) {
            _error.value = "El número de boletos debe ser al menos 1."
            return
        }
        if (showtime.availableSeats < numberOfTickets) {
            _error.value = "No hay suficientes asientos disponibles."
            return
        }
        if (currentMovie == null) {
            _error.value = "Detalles de película no disponibles."
            return
        }

        _isLoading.value = true
        _error.value = null
        _purchaseSuccess.value = false
        _cancellationSuccess.value = false

        viewModelScope.launch {
            try {
                val showtimeRef = firestore.collection("showtimes").document(showtime.id)
                firestore.runTransaction { transaction ->
                    val freshShowtime = transaction.get(showtimeRef).toObject(Showtime::class.java)
                    if (freshShowtime == null || freshShowtime.availableSeats < numberOfTickets) {
                        throw Exception("Asientos no disponibles o horario no encontrado.")
                    }
                    transaction.update(showtimeRef, "availableSeats", FieldValue.increment(-numberOfTickets.toLong()))
                    null
                }.await()

                val transactionData = hashMapOf(
                    "userId" to currentUserId,
                    "showtimeId" to showtime.id,
                    "movieId" to showtime.movieId,
                    "numberOfTickets" to numberOfTickets,
                    "totalAmount" to showtime.price * numberOfTickets,
                    "transactionDate" to Timestamp.now(),
                    "transactionType" to "purchase",
                    "status" to "completed"
                )
                firestore.collection("transactions").add(transactionData).await()

                val userHistoryData = hashMapOf(
                    "userId" to currentUserId,
                    "movieId" to showtime.movieId,
                    "showtimeId" to showtime.id,
                    "action" to "purchased",
                    "actionDate" to Timestamp.now(),
                    "details" to "$numberOfTickets boletos para ${currentMovie.title}",
                    "moviePosterUrl" to currentMovie.posterUrl,
                    "numberOfTickets" to numberOfTickets,
                    "totalAmount" to showtime.price * numberOfTickets,
                    "showtimeStartTime" to showtime.startTime.toDate().time,
                    "durationMinutes" to currentMovie.durationMinutes
                )
                firestore.collection("userHistory").add(userHistoryData).await()

                _purchaseSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al procesar la compra."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelTickets(entry: UserHistoryEntry) {
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
                // Obtener el Showtime original para actualizar los asientos
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

                // Registrar la transacción de cancelación
                val transactionData = hashMapOf(
                    "userId" to currentUserId,
                    "showtimeId" to entry.showtimeId,
                    "movieId" to entry.movieId,
                    "numberOfTickets" to entry.numberOfTickets,
                    "totalAmount" to -entry.totalAmount,
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
                    "moviePosterUrl" to movie.posterUrl,
                    "numberOfTickets" to entry.numberOfTickets,
                    "totalAmount" to -entry.totalAmount,
                    "showtimeStartTime" to entry.showtimeStartTime,
                    "durationMinutes" to entry.durationMinutes
                )
                firestore.collection("userHistory").add(userHistoryData).await()

                _cancellationSuccess.value = true
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

    fun resetPurchaseSuccess() {
        _purchaseSuccess.value = false
    }

    fun resetCancellationSuccess() {
        _cancellationSuccess.value = false
    }
}