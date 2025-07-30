package com.marlonportuguez.cineapp.ui.screens.review

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.marlonportuguez.cineapp.data.model.UserHistoryEntry
import com.marlonportuguez.cineapp.ui.screens.reviews.ReviewViewModel
import com.marlonportuguez.cineapp.ui.theme.CineAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReviewScreen(
    userHistoryEntryJson: String?,
    onReviewSubmitted: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val comment by reviewViewModel.comment.collectAsState()
    val rating by reviewViewModel.rating.collectAsState()
    val isSubmitting by reviewViewModel.isSubmitting.collectAsState()
    val submissionSuccess by reviewViewModel.submissionSuccess.collectAsState()
    val error by reviewViewModel.error.collectAsState()
    val selectedMediaUri by reviewViewModel.selectedMediaUri.collectAsState()
    val selectedMediaType by reviewViewModel.selectedMediaType.collectAsState()
    val context = LocalContext.current

    val userHistoryEntry = remember(userHistoryEntryJson) {
        userHistoryEntryJson?.let { Gson().fromJson(it, UserHistoryEntry::class.java) }
    }

    val cameraAndAudioPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    )

    // URIs temporales para archivos multimedia
    val tempPhotoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
        )
    }
    val tempVideoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(context.cacheDir, "temp_video_${System.currentTimeMillis()}.mp4")
        )
    }
    val tempAudioUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}.m4a")
        )
    }

    // Launchers para captura de medios
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) reviewViewModel.setSelectedMedia(tempPhotoUri, "photo")
        else reviewViewModel.setSelectedMedia(null, null)
    }
    val takeVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) reviewViewModel.setSelectedMedia(tempVideoUri, "video")
        else reviewViewModel.setSelectedMedia(null, null)
    }
    val recordAudioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) reviewViewModel.setSelectedMedia(tempAudioUri, "audio")
        else reviewViewModel.setSelectedMedia(null, null)
    }

    // Manejo del estado del ViewModel
    LaunchedEffect(userHistoryEntry) {
        if (userHistoryEntry != null) reviewViewModel.setHistoryEntry(userHistoryEntry)
        else {
            Toast.makeText(context, "Información de película no disponible para opinión.", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }
    LaunchedEffect(submissionSuccess) {
        if (submissionSuccess) {
            Toast.makeText(context, "Opinión enviada con éxito!", Toast.LENGTH_LONG).show()
            reviewViewModel.resetSubmissionSuccess()
            onReviewSubmitted()
        }
    }
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            reviewViewModel.resetError()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A),
                        Color(0xFF0D0D0D)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Escribir Reseña",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (userHistoryEntry != null) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(6.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tu Opinión",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp
                                ),
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            MovieInfoCard(userHistoryEntry = userHistoryEntry)

                            RatingSection(
                                rating = rating,
                                onRatingChange = { newRating -> reviewViewModel.onRatingChange(newRating) },
                                isEnabled = !isSubmitting
                            )

                            CommentSection(
                                comment = comment,
                                onCommentChange = { newComment -> reviewViewModel.onCommentChange(newComment) },
                                isEnabled = !isSubmitting
                            )

                            MediaSection(
                                selectedMediaUri = selectedMediaUri,
                                selectedMediaType = selectedMediaType,
                                isSubmitting = isSubmitting,
                                onPhotoClick = {
                                    cameraAndAudioPermissionsState.launchMultiplePermissionRequest()
                                    if (cameraAndAudioPermissionsState.allPermissionsGranted) {
                                        reviewViewModel.setSelectedMedia(tempPhotoUri, "photo")
                                        takePictureLauncher.launch(tempPhotoUri)
                                    } else {
                                        Toast.makeText(context, "Permiso de cámara necesario", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onVideoClick = {
                                    cameraAndAudioPermissionsState.launchMultiplePermissionRequest()
                                    if (cameraAndAudioPermissionsState.allPermissionsGranted) {
                                        reviewViewModel.setSelectedMedia(tempVideoUri, "video")
                                        takeVideoLauncher.launch(tempVideoUri)
                                    } else {
                                        Toast.makeText(context, "Permiso de cámara/micrófono necesario", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onAudioClick = {
                                    cameraAndAudioPermissionsState.launchMultiplePermissionRequest()
                                    if (cameraAndAudioPermissionsState.allPermissionsGranted) {
                                        reviewViewModel.setSelectedMedia(tempAudioUri, "audio")
                                        recordAudioLauncher.launch(tempAudioUri)
                                    } else {
                                        Toast.makeText(context, "Permiso de micrófono necesario", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )

                            SubmitSection(
                                isSubmitting = isSubmitting,
                                rating = rating,
                                onSubmit = { reviewViewModel.submitReview() }
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun MovieInfoCard(userHistoryEntry: UserHistoryEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = userHistoryEntry.moviePosterUrl),
                    contentDescription = "${userHistoryEntry.details} Poster",
                    modifier = Modifier
                        .size(80.dp, 110.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userHistoryEntry.details,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(4.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Comprado",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun RatingSection(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(6.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calificación",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }

            RatingBar(
                currentRating = rating,
                onRatingChange = onRatingChange,
                isEnabled = isEnabled,
                starSize = 40.dp
            )

            if (rating > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (rating) {
                        1 -> "Muy malo"
                        2 -> "Malo"
                        3 -> "Regular"
                        4 -> "Bueno"
                        5 -> "Excelente"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CommentSection(
    comment: String,
    onCommentChange: (String) -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(6.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Comentario",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                label = { Text("Comparte tu experiencia...", color = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                enabled = isEnabled,
                maxLines = 4
            )
        }
    }
}

@Composable
fun MediaSection(
    selectedMediaUri: Uri?,
    selectedMediaType: String?,
    isSubmitting: Boolean,
    onPhotoClick: () -> Unit,
    onVideoClick: () -> Unit,
    onAudioClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(6.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Multimedia (Opcional)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MediaButton(
                    icon = Icons.Default.CameraAlt,
                    text = "Foto",
                    onClick = onPhotoClick,
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f)
                )

                MediaButton(
                    icon = Icons.Default.Videocam,
                    text = "Video",
                    onClick = onVideoClick,
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f)
                )

                MediaButton(
                    icon = Icons.Default.Mic,
                    text = "Audio",
                    onClick = onAudioClick,
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f)
                )
            }

            selectedMediaUri?.let { uri ->
                Spacer(modifier = Modifier.height(16.dp))
                MediaPreview(uri = uri, mediaType = selectedMediaType)
            }
        }
    }
}

@Composable
fun MediaButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (enabled) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = text,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun MediaPreview(
    uri: Uri,
    mediaType: String?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        when (mediaType) {
            "photo", "video" -> {
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = "Multimedia seleccionada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            "audio" -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.AudioFile,
                        contentDescription = "Audio",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Audio adjunto",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SubmitSection(
    isSubmitting: Boolean,
    rating: Int,
    onSubmit: () -> Unit
) {
    Button(
        onClick = onSubmit,
        enabled = !isSubmitting && rating > 0,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isSubmitting) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Enviando...",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        } else {
            Text(
                "Enviar Reseña",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
    }
}

@Composable
fun RatingBar(
    currentRating: Int,
    onRatingChange: (Int) -> Unit,
    isEnabled: Boolean = true,
    starCount: Int = 5,
    starSize: Dp = 32.dp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..starCount) {
            IconButton(
                onClick = { if (isEnabled) onRatingChange(i) },
                enabled = isEnabled,
            ) {
                Icon(
                    imageVector = if (i <= currentRating) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Estrella $i",
                    tint = if (i <= currentRating) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(starSize) 
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewScreenPreview() {
    CineAppTheme {
        val sampleEntry = UserHistoryEntry(
            id = "hist1",
            userId = "user1",
            movieId = "movie1",
            showtimeId = "show1",
            action = "purchased",
            actionDate = Timestamp(System.currentTimeMillis() / 1000, 0),
            details = "2 boletos para Superman",
            moviePosterUrl = "https://sacnkprodarcms.blob.core.windows.net/content/posters/HO00010495.jpg",
            showtimeStartTime = System.currentTimeMillis() - 7200000,
            durationMinutes = 143
        )
        val previewViewModel = object : ViewModel() {
            val comment = MutableStateFlow("").asStateFlow()
            val rating = MutableStateFlow(0).asStateFlow()
            val isSubmitting = MutableStateFlow(false).asStateFlow()
            val submissionSuccess = MutableStateFlow(false).asStateFlow()
            val error = MutableStateFlow<String?>(null).asStateFlow()
            val selectedMediaUri = MutableStateFlow<Uri?>(null).asStateFlow()
            val selectedMediaType = MutableStateFlow<String?>(null).asStateFlow()
            fun onRatingChange(newRating: Int) {}
            fun onCommentChange(newComment: String) {}
            fun setSelectedMedia(uri: Uri?, type: String?) {}
            fun setHistoryEntry(entry: UserHistoryEntry) {}
            fun submitReview() {}
            fun resetSubmissionSuccess() {}
            fun resetError() {}
        }
        ReviewScreen(
            userHistoryEntryJson = Gson().toJson(sampleEntry),
            onReviewSubmitted = {},
            onBack = {},
            reviewViewModel = previewViewModel as ReviewViewModel
        )
    }
}