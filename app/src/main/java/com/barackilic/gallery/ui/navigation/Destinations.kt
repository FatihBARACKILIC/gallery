package com.barackilic.gallery.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import com.barackilic.gallery.R
import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data object Photos : Destination

    @Serializable
    data object Albums : Destination

    @Serializable
    data object Trash : Destination

    @Serializable
    data class AlbumPhotos(val bucketId: Long, val name: String) : Destination

    @Serializable
    data class Viewer(
        val initialIndex: Int,
        val mediaId: Long,
        val bucketId: Long = NO_BUCKET,
    ) : Destination {
        companion object {
            const val NO_BUCKET: Long = -1L
        }
    }
}

enum class TopLevelTab(
    val destination: Destination,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Photos(Destination.Photos, R.string.tab_photos, Icons.Outlined.PhotoLibrary),
    Albums(Destination.Albums, R.string.tab_albums, Icons.Outlined.Folder),
    Trash(Destination.Trash, R.string.tab_trash, Icons.Outlined.Delete),
}
