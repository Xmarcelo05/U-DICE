package com.example.udice.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage

@Composable
internal fun PublicationImageGrid(images: List<Any>, onRemove: ((Int) -> Unit)? = null) {
    if (images.isEmpty()) return

    var openedImage by remember(images) { mutableStateOf<Int?>(null) }
    openedImage?.let { page ->
        PublicationImageViewer(images, initialPage = page, onClose = { openedImage = null })
    }

    Column(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        images.chunked(2).forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                row.forEachIndexed { columnIndex, image ->
                    val index = rowIndex * 2 + columnIndex
                    Box(Modifier.weight(1f).fillMaxSize()) {
                        AsyncImage(
                            model = image,
                            contentDescription = "Imagen ${index + 1} de ${images.size}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clickable(
                                onClickLabel = "Ampliar imagen"
                            ) { openedImage = index }
                        )
                        if (onRemove != null) {
                            FilledIconButton(
                                onClick = { onRemove(index) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar imagen ${index + 1}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicationImageViewer(images: List<Any>, initialPage: Int, onClose: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { images.size })
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black).safeDrawingPadding()
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = images[page],
                    contentDescription = "Imagen ${page + 1} de ${images.size}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(vertical = 56.dp)
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar visor de imágenes", tint = Color.White)
            }
            if (images.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${images.size}",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
                )
            }
        }
    }
}
