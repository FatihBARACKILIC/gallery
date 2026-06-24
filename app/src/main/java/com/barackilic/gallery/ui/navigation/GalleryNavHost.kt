package com.barackilic.gallery.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.barackilic.gallery.ui.albums.AlbumsScreen
import com.barackilic.gallery.ui.albums.BucketPhotosScreen
import com.barackilic.gallery.ui.permission.PermissionScreen
import com.barackilic.gallery.ui.photos.PhotosScreen
import com.barackilic.gallery.ui.search.SearchScreen
import com.barackilic.gallery.ui.settings.SettingsScreen
import com.barackilic.gallery.ui.trash.TrashScreen
import com.barackilic.gallery.ui.viewer.ViewerScreen

@Composable
fun GalleryNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<Destination.Permission> {
            PermissionScreen(
                onGranted = { navController.goToPhotosFromPermission() },
                onSkip = { navController.goToPhotosFromPermission() },
            )
        }
        composable<Destination.Photos> {
            PhotosScreen(
                onItemClick = { mediaIndex, mediaId ->
                    navController.navigate(
                        Destination.Viewer(
                            initialIndex = mediaIndex,
                            mediaId = mediaId,
                        ),
                    )
                },
            )
        }
        composable<Destination.Albums> {
            AlbumsScreen(
                onAlbumClick = { album ->
                    navController.navigate(Destination.AlbumPhotos(album.id, album.name))
                },
            )
        }
        composable<Destination.Search> { SearchScreen() }
        composable<Destination.Settings> {
            SettingsScreen(
                onTrashClick = { navController.navigate(Destination.Trash) },
            )
        }
        composable<Destination.Trash> {
            TrashScreen(onBack = { navController.popBackStack() })
        }
        composable<Destination.AlbumPhotos> { backStackEntry ->
            val route: Destination.AlbumPhotos = backStackEntry.toRoute()
            BucketPhotosScreen(
                bucketId = route.bucketId,
                title = route.name,
                onBack = { navController.popBackStack() },
                onItemClick = { mediaIndex, mediaId ->
                    navController.navigate(
                        Destination.Viewer(
                            initialIndex = mediaIndex,
                            mediaId = mediaId,
                            bucketId = route.bucketId,
                        ),
                    )
                },
            )
        }
        composable<Destination.Viewer> { backStackEntry ->
            val route: Destination.Viewer = backStackEntry.toRoute()
            val bucketId = if (route.bucketId == Destination.Viewer.NO_BUCKET) null else route.bucketId
            ViewerScreen(
                initialIndex = route.initialIndex,
                bucketId = bucketId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private fun NavHostController.goToPhotosFromPermission() {
    navigate(Destination.Photos) {
        popUpTo(Destination.Permission) { inclusive = true }
        launchSingleTop = true
    }
}
