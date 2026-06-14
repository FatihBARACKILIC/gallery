package com.barackilic.gallery.ui.photos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.barackilic.gallery.ui.common.SectionHeader
import org.koin.androidx.compose.koinViewModel

@Composable
fun PhotosScreen(modifier: Modifier = Modifier) {
    PermissionGate(modifier = modifier) {
        val viewModel: PhotosViewModel = koinViewModel()
        val mode by viewModel.mode.collectAsState()
        val items = viewModel.gridCells.collectAsLazyPagingItems()
        Column(modifier = Modifier.fillMaxSize()) {
            GroupingTabs(
                selected = mode,
                onSelect = viewModel::setMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
            PhotoGrid(
                items = items,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun GroupingTabs(
    selected: GroupingMode,
    onSelect: (GroupingMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = GroupingMode.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
            ) {
                Text(stringResource(mode.labelRes))
            }
        }
    }
}

@Composable
private fun PhotoGrid(
    items: LazyPagingItems<PhotoGridCell>,
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
            key = items.itemKey { it.cellKey() },
            contentType = items.itemContentType { it.cellContentType() },
            span = { index ->
                when (items.peek(index)) {
                    is PhotoGridCell.Header -> GridItemSpan(maxLineSpan)
                    else -> GridItemSpan(1)
                }
            },
        ) { index ->
            when (val cell = items[index]) {
                is PhotoGridCell.Header -> SectionHeader(cell.label)
                is PhotoGridCell.Item -> MediaCell(cell.media)
                null -> Box(Modifier.aspectRatio(1f))
            }
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

private fun PhotoGridCell.cellKey(): Any = when (this) {
    is PhotoGridCell.Header -> "h:$key"
    is PhotoGridCell.Item -> media.id
}

private fun PhotoGridCell.cellContentType(): Any = when (this) {
    is PhotoGridCell.Header -> "header"
    is PhotoGridCell.Item -> media.type
}
