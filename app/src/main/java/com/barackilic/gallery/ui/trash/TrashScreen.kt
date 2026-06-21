package com.barackilic.gallery.ui.trash

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barackilic.gallery.R
import com.barackilic.gallery.data.mediastore.mediaContentUri
import com.barackilic.gallery.domain.model.TrashedItem
import com.barackilic.gallery.ui.common.MediaThumb
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrashScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: TrashViewModel = koinViewModel()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val selection by viewModel.selection.collectAsStateWithLifecycle()

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.clearSelection()
    }
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.clearSelection()
    }

    BackHandler(enabled = selection.isNotEmpty()) { viewModel.clearSelection() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TrashTopBar(
                selectionCount = selection.size,
                onBack = onBack,
                onClearSelection = viewModel::clearSelection,
                onRestore = {
                    viewModel.buildRestoreRequest()?.let(restoreLauncher::launch)
                },
                onDelete = {
                    viewModel.buildDeleteRequest()?.let(deleteLauncher::launch)
                },
            )
        },
    ) { innerPadding ->
        if (items.isEmpty()) {
            EmptyState(modifier = Modifier.fillMaxSize().padding(innerPadding))
        } else {
            TrashGrid(
                items = items,
                selection = selection,
                onToggle = viewModel::toggle,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrashTopBar(
    selectionCount: Int,
    onBack: () -> Unit,
    onClearSelection: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    if (selectionCount > 0) {
        TopAppBar(
            title = {
                Text(
                    text = pluralStringResource(
                        R.plurals.trash_selected_count,
                        selectionCount,
                        selectionCount,
                    ),
                )
            },
            navigationIcon = {
                IconButton(onClick = onClearSelection) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.trash_clear_selection),
                    )
                }
            },
            actions = {
                IconButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Outlined.Restore,
                        contentDescription = stringResource(R.string.trash_restore),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteForever,
                        contentDescription = stringResource(R.string.trash_delete_forever),
                    )
                }
            },
        )
    } else {
        TopAppBar(
            title = { Text(text = stringResource(R.string.trash_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            },
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.trash_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
    }
}

@Composable
private fun TrashGrid(
    items: List<TrashedItem>,
    selection: Set<Long>,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(2.dp),
    ) {
        items(items = items, key = { it.mediaId }) { item ->
            TrashCell(
                item = item,
                selected = item.mediaId in selection,
                onToggle = { onToggle(item.mediaId) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrashCell(
    item: TrashedItem,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    val uri = mediaContentUri(item.mediaId)
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onToggle,
            ),
    ) {
        MediaThumb(uri = uri, modifier = Modifier.fillMaxSize())
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            )
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
            )
        }
    }
}
