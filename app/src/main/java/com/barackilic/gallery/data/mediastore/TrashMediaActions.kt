package com.barackilic.gallery.data.mediastore

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.model.MediaType
import com.barackilic.gallery.domain.model.TrashedItem

class TrashMediaActions(private val resolver: ContentResolver) {

    fun buildTrashRequest(items: List<MediaItem>): PendingIntent =
        MediaStore.createTrashRequest(
            resolver,
            items.map { typedUri(it.id, it.type) },
            /* value = */ true,
        )

    fun buildRestoreRequest(items: List<TrashedItem>): PendingIntent =
        MediaStore.createTrashRequest(
            resolver,
            items.map { typedUri(it.mediaId, it.type) },
            /* value = */ false,
        )

    fun buildDeleteForeverRequest(items: List<TrashedItem>): PendingIntent =
        MediaStore.createDeleteRequest(
            resolver,
            items.map { typedUri(it.mediaId, it.type) },
        )

    // createTrashRequest / createDeleteRequest reject the generic `Files` collection URI
    // (content://media/external/file/<id>) — they require the typed Images or Video
    // collection URIs. Generic `Files` URIs are still fine for Coil, ExoPlayer, and
    // ACTION_SEND share, so `mediaContentUri()` stays Files-based.
    private fun typedUri(id: Long, type: MediaType): Uri {
        val base = when (type) {
            MediaType.Image -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            MediaType.Video -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }
        return ContentUris.withAppendedId(base, id)
    }
}
