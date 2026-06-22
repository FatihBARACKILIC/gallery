package com.barackilic.gallery.ui.albums

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.barackilic.gallery.R
import com.barackilic.gallery.data.mediastore.mediaContentUri
import com.barackilic.gallery.domain.model.Album
import com.barackilic.gallery.ui.common.MediaThumb
import com.barackilic.gallery.ui.common.PermissionGate
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AlbumsScreen(
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier,
) {
    PermissionGate(modifier = modifier) {
        val viewModel: AlbumsViewModel = koinViewModel()
        val albums by viewModel.albums.collectAsState()
        val sortOrder by viewModel.sortOrder.collectAsState()
        val columns by viewModel.columns.collectAsState()
        AlbumsContent(
            albums = albums,
            sortOrder = sortOrder,
            columns = columns,
            onSortOrderChange = viewModel::setSortOrder,
            onZoomIn = viewModel::zoomIn,
            onZoomOut = viewModel::zoomOut,
            onAlbumClick = onAlbumClick,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumsContent(
    albums: List<Album>,
    sortOrder: AlbumSortOrder,
    columns: AlbumColumns,
    onSortOrderChange: (AlbumSortOrder) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    AlbumsSortMenu(current = sortOrder, onSelect = onSortOrderChange)
                    AlbumsOverflowMenu(
                        columns = columns,
                        onZoomIn = onZoomIn,
                        onZoomOut = onZoomOut,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { innerPadding ->
        AlbumsGrid(
            albums = albums,
            columns = columns,
            onAlbumClick = onAlbumClick,
            onZoomIn = onZoomIn,
            onZoomOut = onZoomOut,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@Composable
private fun AlbumsSortMenu(
    current: AlbumSortOrder,
    onSelect: (AlbumSortOrder) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Sort,
                contentDescription = stringResource(R.string.albums_sort),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AlbumSortOrder.entries.forEach { order ->
                val selected = order == current
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(order.labelRes),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onSelect(order)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun AlbumsOverflowMenu(
    columns: AlbumColumns,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = stringResource(R.string.more_options),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.photos_zoom_in)) },
                enabled = columns.canZoomIn,
                onClick = {
                    onZoomIn()
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.photos_zoom_out)) },
                enabled = columns.canZoomOut,
                onClick = {
                    onZoomOut()
                    expanded = false
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlbumsGrid(
    albums: List<Album>,
    columns: AlbumColumns,
    onAlbumClick: (Album) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zoomInState = rememberUpdatedState(onZoomIn)
    val zoomOutState = rememberUpdatedState(onZoomOut)
    Box(
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
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
                        event.changes.forEach { it.consume() }
                    }
                } while (event.changes.any { it.pressed })
            }
        },
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(columns.count),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(
                items = albums,
                key = { it.id },
            ) { album ->
                AlbumCell(
                    album = album,
                    columns = columns,
                    onClick = { onAlbumClick(album) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun AlbumCell(
    album: Album,
    columns: AlbumColumns,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            // Smooths the text-block resize when the typography ramp changes between
            // L2/L3/L4. Without this the height snaps and the row below visibly jumps.
            .animateContentSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            MediaThumb(
                uri = mediaContentUri(album.coverMediaId),
                modifier = Modifier.fillMaxSize(),
            )
        }
        // At higher column counts the cell width shrinks; drop the name to a smaller
        // type ramp so two cells fit per row without truncation ugliness.
        val nameStyle = when (columns) {
            AlbumColumns.L2 -> MaterialTheme.typography.titleMedium
            AlbumColumns.L3 -> MaterialTheme.typography.titleSmall
            AlbumColumns.L4 -> MaterialTheme.typography.labelMedium
        }
        val nameTopPadding = if (columns == AlbumColumns.L2) 12.dp else 8.dp
        Text(
            text = album.name,
            color = MaterialTheme.colorScheme.onSurface,
            style = nameStyle,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = nameTopPadding),
        )
        if (columns != AlbumColumns.L4) {
            Text(
                text = stringResource(R.string.albums_item_count, formatItemCount(album.count)),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private val TR_NUMBER_FORMAT: NumberFormat = NumberFormat.getInstance(Locale.forLanguageTag("tr"))

private fun formatItemCount(count: Int): String = TR_NUMBER_FORMAT.format(count)

private const val ZOOM_STEP_THRESHOLD = 1.25f
