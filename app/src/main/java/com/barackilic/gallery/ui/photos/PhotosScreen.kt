package com.barackilic.gallery.ui.photos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
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
import com.barackilic.gallery.ui.common.GalleryTopBar
import com.barackilic.gallery.ui.common.MediaThumb
import com.barackilic.gallery.ui.common.PermissionGate
import com.barackilic.gallery.ui.common.SectionHeader
import org.koin.androidx.compose.koinViewModel

typealias OnMediaClick = (mediaIndex: Int, mediaId: Long) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(
    onItemClick: OnMediaClick,
    modifier: Modifier = Modifier,
) {
    PermissionGate(modifier = modifier) {
        val viewModel: PhotosViewModel = koinViewModel()
        val zoomLevel by viewModel.zoomLevel.collectAsState()
        Scaffold(
            topBar = {
                GalleryTopBar(
                    actions = {
                        PhotoOverflowMenu(
                            current = zoomLevel,
                            onZoomIn = { viewModel.setZoomLevel(zoomLevel.zoomIn()) },
                            onZoomOut = { viewModel.setZoomLevel(zoomLevel.zoomOut()) },
                        )
                    },
                )
            },
        ) { padding ->
            PhotoGridContent(
                viewModel = viewModel,
                onItemClick = onItemClick,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun PhotoOverflowMenu(
    current: ZoomLevel,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = stringResource(R.string.more_options),
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.photos_zoom_in)) },
            enabled = current.canZoomIn,
            onClick = {
                onZoomIn()
                expanded = false
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.photos_zoom_out)) },
            enabled = current.canZoomOut,
            onClick = {
                onZoomOut()
                expanded = false
            },
        )
    }
}

@Composable
private fun PhotoGridContent(
    viewModel: PhotosViewModel,
    onItemClick: OnMediaClick,
    modifier: Modifier = Modifier,
) {
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    val items = viewModel.gridCells.collectAsLazyPagingItems()
    RefreshOnResume(items)
    val onZoomIn: () -> Unit = { viewModel.setZoomLevel(zoomLevel.zoomIn()) }
    val onZoomOut: () -> Unit = { viewModel.setZoomLevel(zoomLevel.zoomOut()) }
    val cellCorner = cellCornerFor(zoomLevel)
    val cellSpacing = cellSpacingFor(zoomLevel)
    if (zoomLevel.isJustified) {
        JustifiedLayout(
            items = items,
            onItemClick = onItemClick,
            onZoomIn = onZoomIn,
            onZoomOut = onZoomOut,
            rowSpacing = cellSpacing,
            itemSpacing = cellSpacing,
            cellCornerRadius = cellCorner,
            modifier = modifier.fillMaxSize(),
        )
    } else {
        PhotoGrid(
            items = items,
            columns = zoomLevel.columns,
            onCellClick = { cellIndex, mediaId ->
                val mediaIndex = computeMediaIndex(items, cellIndex)
                onItemClick(mediaIndex, mediaId)
            },
            onZoomIn = onZoomIn,
            onZoomOut = onZoomOut,
            cellSpacing = cellSpacing,
            cellCornerRadius = cellCorner,
            modifier = modifier.fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PhotoGrid(
    items: LazyPagingItems<PhotoGridCell>,
    columns: Int,
    onCellClick: (cellIndex: Int, mediaId: Long) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier,
    cellSpacing: Dp = 2.dp,
    contentPadding: PaddingValues = PaddingValues(2.dp),
    cellCornerRadius: Dp = 0.dp,
) {
    val zoomInState = rememberUpdatedState(onZoomIn)
    val zoomOutState = rememberUpdatedState(onZoomOut)
    Box(
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                // localZoom resets every time fingers lift, so each new pinch starts
                // fresh — no asymmetric accumulation between gestures.
                var localZoom = 1f
                awaitFirstDown(requireUnconsumed = false)
                do {
                    val event = awaitPointerEvent()
                    val pressedCount = event.changes.count { it.pressed }
                    if (pressedCount >= 2) {
                        val zoomChange = event.calculateZoom()
                        if (zoomChange.isFinite() && zoomChange > 0f && zoomChange != 1f) {
                            localZoom *= zoomChange
                            when {
                                localZoom > ZOOM_STEP_THRESHOLD -> {
                                    zoomInState.value()
                                    localZoom = 1f
                                }
                                localZoom < 1f / ZOOM_STEP_THRESHOLD -> {
                                    zoomOutState.value()
                                    localZoom = 1f
                                }
                            }
                        }
                        // Consume so LazyVerticalGrid's scrollable doesn't also react.
                        event.changes.forEach { it.consume() }
                    }
                } while (event.changes.any { it.pressed })
            }
        },
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(columns),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(cellSpacing),
            verticalArrangement = Arrangement.spacedBy(cellSpacing),
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
                    is PhotoGridCell.Header ->
                        SectionHeader(cell.label, modifier = Modifier.animateItem())
                    is PhotoGridCell.Item -> MediaCell(
                        item = cell.media,
                        onClick = { onCellClick(index, cell.media.id) },
                        cornerRadius = cellCornerRadius,
                        modifier = Modifier.animateItem(),
                    )
                    null -> Box(Modifier.aspectRatio(1f))
                }
            }
        }
    }
}

internal const val ZOOM_STEP_THRESHOLD = 1.25f

@Composable
internal fun MediaCell(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 0.dp,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clip(if (cornerRadius > 0.dp) RoundedCornerShape(cornerRadius) else RectangleShape)
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

internal fun computeMediaIndex(
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
internal fun RefreshOnResume(items: LazyPagingItems<*>) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun JustifiedLayout(
    items: LazyPagingItems<PhotoGridCell>,
    onItemClick: OnMediaClick,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier,
    rowSpacing: Dp = 2.dp,
    itemSpacing: Dp = 2.dp,
    cellCornerRadius: Dp = 0.dp,
) {
    val entries by remember(items) {
        derivedStateOf { buildJustifiedEntries(items) }
    }
    val zoomInState = rememberUpdatedState(onZoomIn)
    val zoomOutState = rememberUpdatedState(onZoomOut)
    Box(
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                var localZoom = 1f
                awaitFirstDown(requireUnconsumed = false)
                do {
                    val event = awaitPointerEvent()
                    if (event.changes.count { it.pressed } >= 2) {
                        val zoomChange = event.calculateZoom()
                        if (zoomChange.isFinite() && zoomChange > 0f && zoomChange != 1f) {
                            localZoom *= zoomChange
                            when {
                                localZoom > ZOOM_STEP_THRESHOLD -> {
                                    zoomInState.value()
                                    localZoom = 1f
                                }
                                localZoom < 1f / ZOOM_STEP_THRESHOLD -> {
                                    zoomOutState.value()
                                    localZoom = 1f
                                }
                            }
                        }
                        event.changes.forEach { it.consume() }
                    }
                } while (event.changes.any { it.pressed })
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(rowSpacing),
        ) {
            items(
                items = entries,
                key = { it.key },
                contentType = { if (it is JustifiedEntry.Header) "header" else "row" },
            ) { entry ->
                when (entry) {
                    is JustifiedEntry.Header -> SectionHeader(
                        label = entry.label,
                        modifier = Modifier.animateItem(),
                    )
                    is JustifiedEntry.Row -> JustifiedRowContent(
                        entry = entry,
                        onItemClick = onItemClick,
                        itemSpacing = itemSpacing,
                        cellCornerRadius = cellCornerRadius,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun JustifiedRowContent(
    entry: JustifiedEntry.Row,
    onItemClick: OnMediaClick,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 2.dp,
    cellCornerRadius: Dp = 0.dp,
) {
    if (entry.items.isEmpty()) return
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gapDp = itemSpacing.value
        val gapTotal = (entry.items.size - 1) * gapDp
        val sumAspect = entry.items
            .sumOf { it.media.aspectRatio.toDouble() }
            .toFloat()
            .coerceAtLeast(0.01f)
        val rowHeightDp = ((maxWidth.value - gapTotal) / sumAspect).coerceAtLeast(40f)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            entry.items.forEach { item ->
                val cellWidthDp = item.media.aspectRatio * rowHeightDp
                Box(
                    modifier = Modifier
                        .width(cellWidthDp.dp)
                        .height(rowHeightDp.dp)
                        .clip(
                            if (cellCornerRadius > 0.dp) RoundedCornerShape(cellCornerRadius)
                            else RectangleShape,
                        )
                        .clickable { onItemClick(item.mediaIndex, item.media.id) },
                ) {
                    MediaThumb(
                        uri = item.media.contentUri(),
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (item.media.type == MediaType.Video &&
                        item.media.durationMs != null
                    ) {
                        DurationBadge(
                            durationMs = item.media.durationMs,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp),
                        )
                    }
                }
            }
        }
    }
}
