package com.marlonportuguez.cineapp.ui.screens.reviews

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.marlonportuguez.cineapp.data.model.Review
import com.marlonportuguez.cineapp.data.model.UserHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ReviewViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : ViewModel() {

    private val _historyEntry = MutableStateFlow<UserHistoryEntry?>(null)
    private val historyEntry: StateFlow<UserHistoryEntry?> = _historyEntry.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()

    private val _rating = MutableStateFlow(0)
    val rating: StateFlow<Int> = _rating.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submissionSuccess = MutableStateFlow(false)
    val submissionSuccess: StateFlow<Boolean> = _submissionSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedMediaUri = MutableStateFlow<Uri?>(null)
    val selectedMediaUri: StateFlow<Uri?> = _selectedMediaUri.asStateFlow()

    private val _selectedMediaType = MutableStateFlow<String?>(null)
    val selectedMediaType: StateFlow<String?> = _selectedMediaType.asStateFlow()

    fun setHistoryEntry(entry: UserHistoryEntry) {
        _historyEntry.value = entry
    }

    fun onCommentChange(newComment: String) {
        _comment.value = newComment
    }

    fun onRatingChange(newRating: Int) {
        _rating.value = newRating
    }

    fun setSelectedMedia(uri: Uri?, type: String?) {
        _selectedMediaUri.value = uri
        _selectedMediaType.value = type
    }

    fun submitReview() {
        val currentUserId = auth.currentUser?.uid
        val entry = historyEntry.value

        if (currentUserId == null || entry == null) {
            _error.value = "Error: Datos de usuario o película no disponibles."
            return
        }
        if (rating.value == 0) {
            _error.value = "Por favor, selecciona una calificación."
            return
        }
        if (comment.value.isBlank()) {
            _error.value = "Por favor, escribe un comentario."
            return
        }

        _isSubmitting.value = true
        _error.value = null
        _submissionSuccess.value = false

        viewModelScope.launch {
            try {
                var mediaUrl: String? = null
                if (_selectedMediaUri.value != null && _selectedMediaType.value != null) {
                    mediaUrl = uploadMediaToFirebaseStorage(_selectedMediaUri.value!!, _selectedMediaType.value!!)
                }

                val reviewData = hashMapOf(
                    "userId" to currentUserId,
                    "movieId" to entry.movieId,
                    "showtimeId" to entry.showtimeId,
                    "rating" to rating.value,
                    "comment" to comment.value,
                    "reviewDate" to Timestamp.now(),
                    "mediaUrl" to mediaUrl,
                    "mediaType" to _selectedMediaType.value,
                    "movieTitle" to entry.details.substringAfter("para "),
                    "moviePosterUrl" to entry.moviePosterUrl
                )
                firestore.collection("reviews").add(reviewData).await()
                _submissionSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al enviar la opinión."
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    private suspend fun uploadMediaToFirebaseStorage(mediaUri: Uri, mediaType: String): String {
        val fileExtension = when (mediaType) {
            "photo" -> "jpg"
            "video" -> "mp4"
            "audio" -> "m4a"
            else -> "bin"
        }
        val fileName = "${mediaType}_${UUID.randomUUID()}.$fileExtension"
        val storageRef = storage.reference.child("review_media/$fileName")
        return storageRef.putFile(mediaUri).await().storage.downloadUrl.await().toString()
    }

    fun resetSubmissionSuccess() {
        _submissionSuccess.value = false
        _comment.value = ""
        _rating.value = 0
        _selectedMediaUri.value = null
        _selectedMediaType.value = null
    }

    fun resetError() {
        _error.value = null
    }
}