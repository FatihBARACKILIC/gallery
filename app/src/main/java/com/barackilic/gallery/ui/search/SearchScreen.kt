package com.barackilic.gallery.ui.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.barackilic.gallery.R
import com.barackilic.gallery.ui.common.EmptyState

// Placeholder for the Search tab. Real implementation arrives in v0.2 Adım 11/12
// (recent searches + filter chips + results grid).
@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.Search,
        title = stringResource(R.string.search_placeholder_title),
        description = stringResource(R.string.search_placeholder_description),
        modifier = modifier,
    )
}
