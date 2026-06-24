package com.barackilic.gallery.ui.albums

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.barackilic.gallery.R
import com.barackilic.gallery.domain.model.BucketStats
import com.barackilic.gallery.ui.common.PermissionGate
import com.barackilic.gallery.ui.photos.JustifiedLayout
import com.barackilic.gallery.ui.photos.OnMediaClick
import com.barackilic.gallery.ui.photos.PhotoGrid
import com.barackilic.gallery.ui.photos.RefreshOnResume
import com.barackilic.gallery.ui.photos.cellCornerFor
import com.barackilic.gallery.ui.photos.cellSpacingFor
import com.barackilic.gallery.ui.photos.computeMediaIndex
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketPhotosScreen(
    bucketId: Long,
    title: String,
    onBack: () -> Unit,
    onItemClick: OnMediaClick,
    modifier: Modifier = Modifier,
) {
    val viewModel: BucketPhotosViewModel = koinViewModel { parametersOf(bucketId) }
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val groupByDate by viewModel.groupByDate.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val items = viewModel.gridCells.collectAsLazyPagingItems()
    RefreshOnResume(items)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    BucketSortMenu(
                        current = sortOrder,
                        onSelect = viewModel::setSortOrder,
                    )
                    BucketOverflowMenu(
                        canZoomIn = zoomLevel.canZoomIn,
                        canZoomOut = zoomLevel.canZoomOut,
                        groupByDate = groupByDate,
                        onZoomIn = { viewModel.setZoomLevel(zoomLevel.zoomIn()) },
                        onZoomOut = { viewModel.setZoomLevel(zoomLevel.zoomOut()) },
                        onToggleGroup = viewModel::toggleGroupByDate,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { padding ->
        PermissionGate(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                BucketSubtitle(stats = stats)
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
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    PhotoGrid(
                        items = items,
                        columns = zoomLevel.columns,
                        onCellClick = { cellIndex, mediaId ->
                            val mediaIndex = if (groupByDate) {
                                computeMediaIndex(items, cellIndex)
                            } else {
                                cellIndex
                            }
                            onItemClick(mediaIndex, mediaId)
                        },
                        onZoomIn = onZoomIn,
                        onZoomOut = onZoomOut,
                        cellSpacing = cellSpacing,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        cellCornerRadius = cellCorner,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun BucketSubtitle(stats: BucketStats?) {
    // Single-line subtitle "X öğe • Y GB" rendered below the top bar.
    // While stats are loading (first frame after init) show a non-breaking
    // placeholder height so the grid below doesn't jump when it arrives.
    val text = if (stats != null) {
        stringResource(
            R.string.bucket_subtitle,
            formatItemCount(stats.count),
            formatSize(stats.totalBytes),
        )
    } else {
        " "
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun BucketSortMenu(
    current: BucketPhotosSortOrder,
    onSelect: (BucketPhotosSortOrder) -> Unit,
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
            BucketPhotosSortOrder.entries.forEach { order ->
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
private fun BucketOverflowMenu(
    canZoomIn: Boolean,
    canZoomOut: Boolean,
    groupByDate: Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onToggleGroup: () -> Unit,
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
                enabled = canZoomIn,
                onClick = {
                    onZoomIn()
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.photos_zoom_out)) },
                enabled = canZoomOut,
                onClick = {
                    onZoomOut()
                    expanded = false
                },
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.bucket_group_by_date)) },
                trailingIcon = {
                    if (groupByDate) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                        )
                    }
                },
                onClick = {
                    onToggleGroup()
                    expanded = false
                },
            )
        }
    }
}

private val TR_LOCALE: Locale = Locale.forLanguageTag("tr")
private val TR_INT_FORMAT: NumberFormat = NumberFormat.getInstance(TR_LOCALE)
private val TR_DECIMAL_FORMAT: DecimalFormat =
    DecimalFormat("#,##0.0", DecimalFormatSymbols(TR_LOCALE))

private fun formatItemCount(count: Int): String = TR_INT_FORMAT.format(count)

@Composable
private fun formatSize(bytes: Long): String {
    val kb = 1024L
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        bytes >= gb -> stringResource(
            R.string.bucket_size_gb,
            TR_DECIMAL_FORMAT.format(bytes.toDouble() / gb),
        )
        bytes >= mb -> stringResource(
            R.string.bucket_size_mb,
            TR_DECIMAL_FORMAT.format(bytes.toDouble() / mb),
        )
        else -> stringResource(
            R.string.bucket_size_kb,
            TR_INT_FORMAT.format((bytes / kb).coerceAtLeast(0L)),
        )
    }
}
