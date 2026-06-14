package com.barackilic.gallery.ui.photos

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.barackilic.gallery.R
import com.barackilic.gallery.data.mediastore.contentUri
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.model.MediaType
import com.barackilic.gallery.ui.common.DurationBadge
import com.barackilic.gallery.ui.common.MediaThumb
import com.barackilic.gallery.ui.common.PermissionGate
import com.barackilic.gallery.ui.common.SectionHeader
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

typealias OnMediaClick = (mediaIndex: Int, mediaId: Long) -> Unit

@Composable
fun PhotosScreen(
    onItemClick: OnMediaClick,
    modifier: Modifier = Modifier,
) {
    PermissionGate(modifier = modifier) {
        val viewModel: PhotosViewModel = koinViewModel()
        PhotoGridContent(viewModel = viewModel, onItemClick = onItemClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketPhotosScreen(
    bucketId: Long,
    title: String,
    onBack: () -> Unit,
    onItemClick: OnMediaClick,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        PermissionGate(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val viewModel: PhotosViewModel = koinViewModel { parametersOf(bucketId) }
            PhotoGridContent(viewModel = viewModel, onItemClick = onItemClick)
        }
    }
}

@Composable
private fun PhotoGridContent(
    viewModel: PhotosViewModel,
    onItemClick: OnMediaClick,
) {
    val mode by viewModel.mode.collectAsState()
    val items = viewModel.gridCells.collectAsLazyPagingItems()
    RefreshOnResume(items)
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
            onCellClick = { cellIndex, mediaId ->
                val mediaIndex = computeMediaIndex(items, cellIndex)
                onItemClick(mediaIndex, mediaId)
            },
            modifier = Modifier.fillMaxSize(),
        )
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
    onCellClick: (cellIndex: Int, mediaId: Long) -> Unit,
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
                is PhotoGridCell.Item -> MediaCell(
                    item = cell.media,
                    onClick = { onCellClick(index, cell.media.id) },
                )
                null -> Box(Modifier.aspectRatio(1f))
            }
        }
    }
}

@Composable
private fun MediaCell(
    item: MediaItem,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
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

private fun computeMediaIndex(
    items: LazyPagingItems<PhotoGridCell>,
    cellIndex: Int,
): Int {
    var headerCount = 0
    for (i in 0 until cellIndex) {
        if (items.peek(i) is PhotoGridCell.Header) headerCount++
    }
    return cellIndex - headerCount
}

private fun PhotoGridCell.cellKey(): Any = when (this) {
    is PhotoGridCell.Header -> "h:$key"
    is PhotoGridCell.Item -> media.id
}

private fun PhotoGridCell.cellContentType(): Any = when (this) {
    is PhotoGridCell.Header -> "header"
    is PhotoGridCell.Item -> media.type
}

@Composable
private fun RefreshOnResume(items: LazyPagingItems<*>) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentItems by rememberUpdatedState(items)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentItems.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
