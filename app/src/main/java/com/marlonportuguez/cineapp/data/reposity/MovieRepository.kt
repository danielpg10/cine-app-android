package com.marlonportuguez.cineapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.marlonportuguez.cineapp.data.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class MovieRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Obtiene todas las películas disponibles de Firestore
    fun getAvailableMovies(): Flow<List<Movie>> = flow {
        try {
            val querySnapshot = firestore.collection("movies")
                .whereEqualTo("available", true)
                .get()
                .await()
            val movies = querySnapshot.documents.mapNotNull { it.toObject(Movie::class.java) }
            emit(movies)
        } catch (e: Exception) {
            // Manejo de errres
            emit(emptyList()) // Pa lista vacía en caso de error
        }
    }
}