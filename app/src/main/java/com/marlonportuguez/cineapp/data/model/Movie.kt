package com.marlonportuguez.cineapp.data.model

import com.google.firebase.firestore.DocumentId

data class Movie(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val genre: String = "",
    val posterUrl: String = "",
    val durationMinutes: Int = 0,
    val available: Boolean = false
)