package com.barackilic.gallery.ui.photos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.barackilic.gallery.data.mediastore.contentUri
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.model.MediaType
import com.barackilic.gallery.ui.common.DurationBadge
import com.barackilic.gallery.ui.common.MediaThumb
import com.barackilic.gallery.ui.common.PermissionGate
import org.koin.androidx.compose.koinViewModel

@Composable
fun PhotosScreen(modifier: Modifier = Modifier) {
    PermissionGate(modifier = modifier) {
        val viewModel: PhotosViewModel = koinViewModel()
        val items: LazyPagingItems<MediaItem> = viewModel.photos.collectAsLazyPagingItems()
        PhotoGrid(
            items = items,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun PhotoGrid(
    items: LazyPagingItems<MediaItem>,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(
            count = items.itemCount,
            key = items.itemKey { it.id },
            contentType = items.itemContentType { it.type },
        ) { index ->
            val item = items[index] ?: return@items
            MediaCell(item = item)
        }
    }
}

@Composable
private fun MediaCell(item: MediaItem) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f),
    ) {
        MediaThumb(
            uri = item.contentUri(),
            modifier = Modifier.fillMaxSize(),
        )
        if (item.type == MediaType.Video && item.durationMs != null) {
            DurationBadge(
                durationMs = item.durationMs,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
            )
        }
    }
}
