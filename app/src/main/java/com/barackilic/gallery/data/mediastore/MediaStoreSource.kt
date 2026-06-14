package com.barackilic.gallery.data.mediastore

import android.content.ContentResolver
import android.content.ContentUris
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import com.barackilic.gallery.domain.model.Album
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.model.MediaType
import com.barackilic.gallery.domain.model.TrashedItem

class MediaStoreSource(private val resolver: ContentResolver) {

    private val collection: Uri =
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

    private val itemProjection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.Files.FileColumns.DATE_TAKEN,
        MediaStore.Files.FileColumns.DATE_MODIFIED,
        MediaStore.Files.FileColumns.DURATION,
        MediaStore.Files.FileColumns.BUCKET_ID,
        MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
    )

    private val trashProjection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.Files.FileColumns.DATE_EXPIRES,
    )

    private val albumProjection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.BUCKET_ID,
        MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
        MediaStore.Files.FileColumns.DATE_TAKEN,
        MediaStore.Files.FileColumns.DATE_MODIFIED,
    )

    private val sortOrder =
        "COALESCE(${MediaStore.Files.FileColumns.DATE_TAKEN}, " +
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} * 1000) DESC, " +
            "${MediaStore.Files.FileColumns._ID} DESC"

    private fun selection(bucketId: Long?): String {
        val base = "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?) AND " +
            "${MediaStore.Files.FileColumns.IS_TRASHED} = 0"
        return if (bucketId != null) {
            "$base AND ${MediaStore.Files.FileColumns.BUCKET_ID} = ?"
        } else {
            base
        }
    }

    private fun selectionArgs(bucketId: Long?): Array<String> {
        val base = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
        )
        return if (bucketId != null) base + bucketId.toString() else base
    }

    fun registerObserver(observer: ContentObserver) {
        resolver.registerContentObserver(collection, /* notifyForDescendants = */ true, observer)
    }

    fun unregisterObserver(observer: ContentObserver) {
        resolver.unregisterContentObserver(observer)
    }

    fun page(offset: Int, limit: Int, bucketId: Long? = null): List<MediaItem> {
        val args = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection(bucketId))
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs(bucketId))
            putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
            putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
            putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
        }
        val result = ArrayList<MediaItem>(limit.coerceAtMost(1024))
        resolver.query(collection, itemProjection, args, null)?.use { c ->
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

    fun queryTrashed(): List<TrashedItem> {
        val args = Bundle().apply {
            putString(
                ContentResolver.QUERY_ARG_SQL_SELECTION,
                "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?)",
            )
            putStringArray(
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                ),
            )
            putString(
                ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                "${MediaStore.Files.FileColumns.DATE_EXPIRES} DESC, " +
                    "${MediaStore.Files.FileColumns._ID} DESC",
            )
            // Trashed and pending rows are hidden by default; opt into trashed-only here.
            putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_ONLY)
        }
        val results = ArrayList<TrashedItem>()
        resolver.query(collection, trashProjection, args, null)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val typeCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val expiresCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_EXPIRES)
            while (c.moveToNext()) {
                val mediaType = when (c.getInt(typeCol)) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> MediaType.Video
                    else -> MediaType.Image
                }
                // DATE_EXPIRES is in seconds (epoch); convert to ms for consistency.
                val expires = c.getLong(expiresCol).takeIf { it > 0 }?.let { it * 1000L }
                results += TrashedItem(
                    mediaId = c.getLong(idCol),
                    type = mediaType,
                    expiresAtMillis = expires,
                )
            }
        }
        return results
    }

    fun queryAlbums(): List<Album> {
        // MediaProvider GROUP BY support has been inconsistent across OEMs since Android 11,
        // so we group in memory. A single cursor pass over (id, bucket_id, name, date) is
        // bounded by the device's media count.
        val args = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection(bucketId = null))
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs(bucketId = null))
        }
        val buckets = HashMap<Long, AlbumAccumulator>()
        resolver.query(collection, albumProjection, args, null)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val bucketIdCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID)
            val bucketNameCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val dateTakenCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
            val dateModCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            while (c.moveToNext()) {
                val bucketId = c.getLong(bucketIdCol)
                val id = c.getLong(idCol)
                val name = c.getString(bucketNameCol).orEmpty()
                val date = c.getLong(dateTakenCol).takeIf { it > 0 }
                    ?: (c.getLong(dateModCol) * 1000L)
                val acc = buckets.getOrPut(bucketId) { AlbumAccumulator(bucketId) }
                if (acc.name.isEmpty() && name.isNotEmpty()) acc.name = name
                acc.count++
                if (date > acc.latestDate) {
                    acc.latestDate = date
                    acc.latestId = id
                }
            }
        }
        return buckets.values
            .map { acc ->
                Album(
                    id = acc.bucketId,
                    name = acc.name.ifEmpty { "Unnamed" },
                    count = acc.count,
                    coverMediaId = acc.latestId,
                )
            }
            .sortedByDescending { it.count }
    }

    private class AlbumAccumulator(val bucketId: Long) {
        var name: String = ""
        var count: Int = 0
        var latestId: Long = 0L
        var latestDate: Long = 0L
    }
}

fun MediaItem.contentUri(): Uri = mediaContentUri(id)

fun mediaContentUri(id: Long): Uri =
    ContentUris.withAppendedId(
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
        id,
    )
