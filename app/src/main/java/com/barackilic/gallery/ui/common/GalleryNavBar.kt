package com.barackilic.gallery.ui.common

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.barackilic.gallery.ui.navigation.Destination
import com.barackilic.gallery.ui.navigation.TopLevelTab

// M3 NavigationBar already renders the selected item with a pill indicator
// (secondary-container by default), matching design.md. We only need to wire up
// destination matching + tint mapping; no custom layout required.
@Composable
fun GalleryNavBar(
    currentDestination: NavDestination?,
    onTabSelected: (TopLevelTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val albumsSubroute =
        currentDestination?.hasRoute(Destination.AlbumPhotos::class) == true
    val settingsSubroute =
        currentDestination?.hasRoute(Destination.Trash::class) == true
    NavigationBar(modifier = modifier) {
        TopLevelTab.entries.forEach { tab ->
            val selected = when (tab) {
                TopLevelTab.Albums ->
                    currentDestination?.hasRoute(tab.destination::class) == true ||
                        albumsSubroute
                TopLevelTab.Settings ->
                    currentDestination?.hasRoute(tab.destination::class) == true ||
                        settingsSubroute
                else -> currentDestination?.hasRoute(tab.destination::class) == true
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
