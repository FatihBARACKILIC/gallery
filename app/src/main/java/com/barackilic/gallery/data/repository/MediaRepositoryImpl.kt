package com.barackilic.gallery.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.barackilic.gallery.data.mediastore.MediaPagingSource
import com.barackilic.gallery.data.mediastore.MediaStoreSource
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class MediaRepositoryImpl(
    private val source: MediaStoreSource,
) : MediaRepository {

    override fun pagedMedia(): Flow<PagingData<MediaItem>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE * 2,
                initialLoadSize = PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { MediaPagingSource(source) },
        ).flow

    private companion object {
        const val PAGE_SIZE = 300
    }
}
