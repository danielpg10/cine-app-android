package com.marlonportuguez.cineapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class UserHistoryEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val movieId: String = "",
    val showtimeId: String = "",
    val action: String = "",
    val actionDate: Timestamp = Timestamp.now(),
    val details: String = "",
    val moviePosterUrl: String = "",
    val numberOfTickets: Int = 0,
    val totalAmount: Double = 0.0
)