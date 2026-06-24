package com.barackilic.gallery.ui.viewer

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.collection.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

// Pulls scaled video frames at arbitrary timestamps for the scrubber strip.
// MediaMetadataRetriever is NOT thread-safe; all access is serialized through
// `mutex`. The retriever is created lazily on the first frame() call so a
// freshly-constructed source that the user never expands doesn't pay the
// setDataSource cost (~50-100ms + IO).
class VideoFrameSource(
    private val context: Context,
    private val uri: Uri,
) {

    private var retriever: MediaMetadataRetriever? = null

    // Key = (timeMs, sizePx) so the same frame can be cached at multiple
    // resolutions (e.g. big poster + 40 small strip thumbs).
    // 64 entries ≈ ~3MB worst case at the bigger size.
    private val cache = LruCache<Pair<Long, Int>, Bitmap>(FRAME_CACHE_SIZE)

    private val mutex = Mutex()
    private var released = false

    suspend fun frame(timeMs: Long, sizePx: Int): Bitmap? = withContext(Dispatchers.IO) {
        val key = timeMs to sizePx
        cache.get(key)?.let { return@withContext it }
        mutex.withLock {
            cache.get(key)?.let { return@withLock it }
            if (released) return@withLock null
            val r = retriever ?: runCatching {
                MediaMetadataRetriever().apply { setDataSource(context, uri) }
            }.getOrNull()?.also { retriever = it }
                ?: return@withLock null
            runCatching {
                r.getScaledFrameAtTime(
                    timeMs * 1_000L,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    sizePx,
                    sizePx,
                )
            }.getOrNull()?.also { cache.put(key, it) }
        }
    }

    fun release() {
        if (released) return
        released = true
        runCatching { retriever?.release() }
        retriever = null
        cache.evictAll()
    }

    private companion object {
        const val FRAME_CACHE_SIZE = 64
    }
}
