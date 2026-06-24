package com.barackilic.gallery.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.barackilic.gallery.data.mediastore.MediaPagingSource
import com.barackilic.gallery.data.mediastore.MediaStoreSource
import com.barackilic.gallery.domain.model.BucketStats
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MediaRepositoryImpl(
    private val source: MediaStoreSource,
) : MediaRepository {

    override fun pagedMedia(): Flow<PagingData<MediaItem>> = pager(bucketId = null, sortOrder = null)

    override fun pagedMediaInBucket(
        bucketId: Long,
        sortOrder: String?,
    ): Flow<PagingData<MediaItem>> = pager(bucketId = bucketId, sortOrder = sortOrder)

    override suspend fun bucketStats(bucketId: Long): BucketStats =
        withContext(Dispatchers.IO) { source.bucketStats(bucketId) }

    private fun pager(bucketId: Long?, sortOrder: String?): Flow<PagingData<MediaItem>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE * 2,
                initialLoadSize = PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { MediaPagingSource(source, bucketId, sortOrder) },
        ).flow

    private companion object {
        const val PAGE_SIZE = 300
    }
}
