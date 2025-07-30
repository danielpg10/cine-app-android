package com.marlonportuguez.cineapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Review(
    @DocumentId val id: String = "",
    val userId: String = "",
    val movieId: String = "",
    val showtimeId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val reviewDate: Timestamp = Timestamp.now(),
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val movieTitle: String = "",
    val moviePosterUrl: String = ""
)