package com.marlonportuguez.cineapp.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.marlonportuguez.cineapp.data.model.Movie
import com.marlonportuguez.cineapp.data.model.Showtime
import com.marlonportuguez.cineapp.data.model.Theater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DetailViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    // Estados de UI
    private val _movie = MutableStateFlow<Movie?>(null)
    val movie: StateFlow<Movie?> = _movie.asStateFlow()

    private val _showtimes = MutableStateFlow<List<Pair<Showtime, Theater>>>(emptyList())
    val showtimes: StateFlow<List<Pair<Showtime, Theater>>> = _showtimes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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

    fun resetError() {
        _error.value = null
    }
    // Aca me falta agregar la logica para compra/cancelación de entrddas
}