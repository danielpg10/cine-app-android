package com.marlonportuguez.cineapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Showtime(
    @DocumentId val id: String = "",
    val movieId: String = "",
    val theaterId: String = "",
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp = Timestamp.now(),
    val price: Double = 0.0,
    val availableSeats: Int = 0
)