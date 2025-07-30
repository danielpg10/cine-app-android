package com.marlonportuguez.cineapp.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.marlonportuguez.cineapp.data.model.Movie
import com.marlonportuguez.cineapp.data.model.MovieListItem
import com.marlonportuguez.cineapp.data.model.Showtime
import com.marlonportuguez.cineapp.data.model.Theater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class MovieRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Obtiene películas para la lista principal con su horario y sala
    fun getMoviesForList(): Flow<List<MovieListItem>> = flow {
        try {
            val currentTime = Timestamp.now()

            val moviesSnapshot = firestore.collection("movies")
                .whereEqualTo("available", true)
                .get()
                .await()

            val movies = moviesSnapshot.documents.mapNotNull { it.toObject(Movie::class.java) }

            val movieItems = movies.mapNotNull { movie ->
                val showtimeSnapshot = firestore.collection("showtimes")
                    .whereEqualTo("movieId", movie.id)
                    .whereGreaterThanOrEqualTo("startTime", currentTime)
                    .orderBy("startTime")
                    .limit(1)
                    .get()
                    .await()

                val earliestShowtime = showtimeSnapshot.documents.firstOrNull()?.toObject(Showtime::class.java)

                val theaterName = if (earliestShowtime != null) {
                    val theaterSnapshot = firestore.collection("theaters")
                        .document(earliestShowtime.theaterId)
                        .get()
                        .await()
                    theaterSnapshot.toObject(Theater::class.java)?.name
                } else {
                    null
                }

                if (earliestShowtime != null && theaterName != null) {
                    MovieListItem(movie, earliestShowtime, theaterName)
                } else {
                    null // No incluir en la lista si falta info de horario/sala
                }
            }
            emit(movieItems)

        } catch (e: Exception) {
            emit(emptyList()) // Emite lista vacía en caso de error
        }
    }
}