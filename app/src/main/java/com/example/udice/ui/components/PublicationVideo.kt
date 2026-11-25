package com.example.udice.ui.components

import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.udice.R

@Composable
internal fun PublicationVideo(videoUrl: String, thumbnailUrl: String?) {
    var opened by remember(videoUrl) { mutableStateOf(false) }
    var failed by remember(videoUrl) { mutableStateOf(false) }
    if (!opened) {
        Column {
            PublicationVideoThumbnail(
                thumbnailUrl,
                if (failed) "Reintentar reproducción" else "Reproducir video"
            ) {
                failed = false
                opened = true
            }
            if (failed) Text("No se pudo reproducir el video.")
        }
        return
    }

    PublicationVideoPlayer(videoUrl, onError = {
        failed = true
        opened = false
    }, onPause = { opened = false })
}

@Composable
internal fun FeedPublicationVideo(
    videoUrl: String,
    thumbnailUrl: String?,
    active: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit
) {
    var failed by remember(videoUrl) { mutableStateOf(false) }
    Column {
        if (active) {
            PublicationVideoPlayer(videoUrl, onPause = onStop, onError = {
                failed = true
                onStop()
            })
            TextButton(onClick = onStop) { Text("Cerrar video") }
        } else {
            PublicationVideoThumbnail(
                thumbnailUrl,
                if (failed) "Reintentar reproducción" else "Reproducir video"
            ) {
                failed = false
                onPlay()
            }
            if (failed) Text("No se pudo reproducir el video.")
        }
    }
}

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private fun PublicationVideoPlayer(videoUrl: String, onError: () -> Unit, onPause: () -> Unit = {}) {
    val reportError by rememberUpdatedState(onError)
    val reportPause by rememberUpdatedState(onPause)
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val player = remember(context, lifecycle, videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(AudioAttributes.DEFAULT, true)
            setHandleAudioBecomingNoisy(true)
            setMediaItem(MediaItem.fromUri(videoUrl))
        }
    }
    val playerView = remember(context, player) {
        LayoutInflater.from(context).inflate(R.layout.publication_player, null) as PlayerView
    }
    DisposableEffect(player, lifecycle, playerView) {
        fun detachSurface() {
            playerView.visibility = View.INVISIBLE
            playerView.player = null
        }
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                reportError()
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                detachSurface()
                player.pause()
                reportPause()
            }
        }
        player.addListener(listener)
        lifecycle.addObserver(observer)
        player.prepare()
        player.play()
        onDispose {
            lifecycle.removeObserver(observer)
            player.removeListener(listener)
            detachSurface()
            player.release()
        }
    }
    AndroidView(
        factory = { playerView },
        update = { if (it.visibility == View.VISIBLE) it.player = player },
        onRelease = { it.player = null },
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
    )
}

@Composable
internal fun PublicationVideoThumbnail(thumbnailUrl: String?, action: String, onPlay: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp)).background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        thumbnailUrl?.let { PublicationImage(it, "Miniatura del video") }
        Box(
            modifier = Modifier.size(52.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(interactionSource = null, indication = null, role = Role.Button, onClick = onPlay),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = action, tint = Color.White, modifier = Modifier.size(30.dp))
        }
    }
}
