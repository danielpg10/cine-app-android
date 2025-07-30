package com.marlonportuguez.cineapp.data.model

import com.google.firebase.Timestamp

data class MovieListItem(
    val movie: Movie,
    val earliestShowtime: Showtime? = null,
    val theaterName: String? = null
)