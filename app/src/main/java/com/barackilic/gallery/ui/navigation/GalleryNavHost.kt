package com.barackilic.gallery.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.barackilic.gallery.ui.albums.AlbumsScreen
import com.barackilic.gallery.ui.photos.BucketPhotosScreen
import com.barackilic.gallery.ui.photos.PhotosScreen
import com.barackilic.gallery.ui.trash.TrashScreen

@Composable
fun GalleryNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Photos,
        modifier = modifier,
    ) {
        composable<Destination.Photos> { PhotosScreen() }
        composable<Destination.Albums> {
            AlbumsScreen(
                onAlbumClick = { album ->
                    navController.navigate(Destination.AlbumPhotos(album.id, album.name))
                },
            )
        }
        composable<Destination.Trash> { TrashScreen() }
        composable<Destination.AlbumPhotos> { backStackEntry ->
            val route: Destination.AlbumPhotos = backStackEntry.toRoute()
            BucketPhotosScreen(
                bucketId = route.bucketId,
                title = route.name,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
