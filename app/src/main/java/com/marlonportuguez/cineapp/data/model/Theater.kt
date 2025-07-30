package com.marlonportuguez.cineapp.data.model

import com.google.firebase.firestore.DocumentId

data class Theater(
    @DocumentId val id: String = "",
    val name: String = "",
    val capacity: Int = 0
)