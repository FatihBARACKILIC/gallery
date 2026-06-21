package com.barackilic.gallery.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.barackilic.gallery.R
import com.barackilic.gallery.ui.common.GalleryTopBar

// Minimal placeholder until v0.3 designs the full Settings surface. Only entry
// for now is the Trash, which moved out of the top-level nav.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onTrashClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            GalleryTopBar(title = stringResource(R.string.settings_title))
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.settings_trash_item))
                },
                supportingContent = {
                    Text(stringResource(R.string.settings_trash_item_description))
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable(onClick = onTrashClick),
            )
        }
    }
}
