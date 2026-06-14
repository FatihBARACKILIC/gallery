package com.barackilic.gallery.ui.common

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest

@Composable
fun MediaThumb(
    uri: Uri,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(Color.Black.copy(alpha = 0.06f)),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(MediaThumbRequest(uri))
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
