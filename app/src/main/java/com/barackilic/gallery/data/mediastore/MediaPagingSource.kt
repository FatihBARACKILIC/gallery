package com.barackilic.gallery.data.mediastore

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.barackilic.gallery.domain.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaPagingSource(
    private val source: MediaStoreSource,
    private val bucketId: Long? = null,
) : PagingSource<Int, MediaItem>() {

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            invalidate()
        }
    }

    init {
        source.registerObserver(observer)
        registerInvalidatedCallback {
            source.unregisterObserver(observer)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaItem> {
        val page = params.key ?: 0
        return try {
            val items = withContext(Dispatchers.IO) {
                source.page(
                    offset = page * params.loadSize,
                    limit = params.loadSize,
                    bucketId = bucketId,
                )
            }
            LoadResult.Page(
                data = items,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (items.size < params.loadSize) null else page + 1,
            )
        } catch (t: Throwable) {
            LoadResult.Error(t)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MediaItem>): Int? =
        state.anchorPosition?.let { anchor ->
            val closest = state.closestPageToPosition(anchor)
            closest?.prevKey?.plus(1) ?: closest?.nextKey?.minus(1)
        }
}
