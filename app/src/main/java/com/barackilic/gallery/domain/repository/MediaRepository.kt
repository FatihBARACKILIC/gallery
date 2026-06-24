package com.barackilic.gallery.domain.repository

import androidx.paging.PagingData
import com.barackilic.gallery.domain.model.BucketStats
import com.barackilic.gallery.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun pagedMedia(): Flow<PagingData<MediaItem>>
    fun pagedMediaInBucket(bucketId: Long, sortOrder: String? = null): Flow<PagingData<MediaItem>>
    suspend fun bucketStats(bucketId: Long): BucketStats
}
