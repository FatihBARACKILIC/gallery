package com.barackilic.gallery.data.mediastore

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.model.MediaType

class MediaStoreSource(private val resolver: ContentResolver) {

    private val collection: Uri =
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

    private val projection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.Files.FileColumns.DATE_TAKEN,
        MediaStore.Files.FileColumns.DATE_MODIFIED,
        MediaStore.Files.FileColumns.DURATION,
        MediaStore.Files.FileColumns.BUCKET_ID,
        MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
    )

    private val selection =
        "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?) AND " +
            "${MediaStore.Files.FileColumns.IS_TRASHED} = 0"

    private val selectionArgs = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
    )

    private val sortOrder =
        "COALESCE(${MediaStore.Files.FileColumns.DATE_TAKEN}, " +
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} * 1000) DESC, " +
            "${MediaStore.Files.FileColumns._ID} DESC"

    fun count(): Int {
        val args = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
        }
        resolver.query(
            collection,
            arrayOf(MediaStore.Files.FileColumns._ID),
            args,
            null,
        )?.use { cursor ->
            return cursor.count
        }
        return 0
    }

    fun page(offset: Int, limit: Int): List<MediaItem> {
        val args = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
            putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
            putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
        }
        val result = ArrayList<MediaItem>(limit.coerceAtMost(1024))
        resolver.query(collection, projection, args, null)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val typeCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val dateTakenCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
            val dateModCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val durationCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)
            val bucketIdCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID)
            val bucketNameCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)

            while (c.moveToNext()) {
                val mediaType = when (c.getInt(typeCol)) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> MediaType.Video
                    else -> MediaType.Image
                }
                val dateTaken = c.getLong(dateTakenCol).takeIf { it > 0 }
                    ?: (c.getLong(dateModCol) * 1000L)
                val duration = if (mediaType == MediaType.Video) {
                    c.getLong(durationCol).takeIf { it > 0 }
                } else {
                    null
                }
                result += MediaItem(
                    id = c.getLong(idCol),
                    type = mediaType,
                    dateTakenMillis = dateTaken,
                    durationMs = duration,
                    bucketId = c.getLong(bucketIdCol),
                    bucketName = c.getString(bucketNameCol).orEmpty(),
                )
            }
        }
        return result
    }
}

fun MediaItem.contentUri(): Uri =
    ContentUris.withAppendedId(
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
        id,
    )
