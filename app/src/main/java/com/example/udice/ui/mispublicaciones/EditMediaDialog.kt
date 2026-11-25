package com.example.udice.ui.mispublicaciones

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.udice.data.model.MAX_PUBLICATION_IMAGES
import com.example.udice.data.model.Post
import com.example.udice.data.repository.PostMediaEdit
import com.example.udice.ui.components.PublicationImage
import com.example.udice.ui.components.PublicationImageGrid

internal fun initialMedia(post: Post) = PostMediaEdit(
    retainedPhotos = if (post.videoUrl == null) post.imageUrls else emptyList(),
    newPhotos = emptyList(),
    keepVideo = post.videoUrl != null,
    newVideo = null
)

@Composable
internal fun EditMediaFields(
    post: Post,
    media: PostMediaEdit,
    running: Boolean,
    onChange: (PostMediaEdit) -> Unit
) {
    val (retained, photos, keepVideo, video) = media
    var pickerError by remember(post.id) { mutableStateOf<String?>(null) }
    val remaining = MAX_PUBLICATION_IMAGES - retained.size - photos.size
    val selectPhotos: (List<Uri>) -> Unit = { picked ->
        if (picked.isNotEmpty()) {
            val combined = (photos + picked).distinct()
            if (retained.size + combined.size <= MAX_PUBLICATION_IMAGES) {
                onChange(media.copy(newPhotos = combined, keepVideo = false, newVideo = null))
                pickerError = null
            } else {
                pickerError = "Puedes agregar hasta cuatro imágenes."
            }
        }
    }
    val singlePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        if (it != null) selectPhotos(listOf(it))
    }
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxOf(2, remaining)), selectPhotos
    )
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { picked ->
        if (picked != null) {
            onChange(PostMediaEdit(emptyList(), emptyList(), false, picked))
            pickerError = null
        }
    }

    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Hasta cuatro fotos o un video MP4 de hasta 20 MB.")
        if (keepVideo || video != null) {
            if (keepVideo) post.imageUrl?.let { PublicationImage(it, "Miniatura del video actual") }
            Text(if (keepVideo) "Video actual" else "Video nuevo seleccionado")
            TextButton(onClick = { onChange(media.copy(keepVideo = false, newVideo = null)) }, enabled = !running) {
                Text("Quitar video")
            }
        }
        PublicationImageGrid(retained + photos, onRemove = if (running) null else { index ->
            val updated = if (index < retained.size) {
                media.copy(retainedPhotos = retained.filterIndexed { i, _ -> i != index })
            } else {
                media.copy(newPhotos = photos.filterIndexed { i, _ -> i != index - retained.size })
            }
            onChange(updated)
            pickerError = null
        })
        OutlinedButton(onClick = {
            val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            if (remaining == 1) singlePicker.launch(request) else imagePicker.launch(request)
        }, enabled = !running && remaining > 0) {
            Text(if (keepVideo || video != null) "Cambiar a fotos" else "Agregar fotos")
        }
        OutlinedButton(onClick = {
            videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType("video/mp4")))
        }, enabled = !running) {
            Text(if (keepVideo || video != null) "Reemplazar video"
                else if (retained.isNotEmpty() || photos.isNotEmpty()) "Cambiar a video" else "Agregar video")
        }
        pickerError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
