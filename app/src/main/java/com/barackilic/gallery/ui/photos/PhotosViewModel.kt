package com.barackilic.gallery.ui.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.repository.MediaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosViewModel(
    repository: MediaRepository,
    bucketId: Long? = null,
) : ViewModel() {

    private val _zoomLevel = MutableStateFlow(ZoomLevel.L3)
    val zoomLevel: StateFlow<ZoomLevel> = _zoomLevel.asStateFlow()

    // Captured once; relative buckets (Bugün/Dün/Bu Hafta) drift past midnight if the
    // app stays open, but that's acceptable for v0.2 — refresh-on-resume rebuilds.
    private val nowMillis: Long = System.currentTimeMillis()

    private val cachedMedia: Flow<PagingData<MediaItem>> =
        (if (bucketId != null) {
            repository.pagedMediaInBucket(bucketId)
        } else {
            repository.pagedMedia()
        }).cachedIn(viewModelScope)

    val gridCells: Flow<PagingData<PhotoGridCell>> =
        _zoomLevel
            .flatMapLatest { level ->
                cachedMedia.map { paging ->
                    paging
                        .map { PhotoGridCell.Item(it) }
                        .insertSeparators<PhotoGridCell.Item, PhotoGridCell> { before, after ->
                            headerBetween(
                                before?.media,
                                after?.media,
                                nowMillis,
                                level.headerGranularity,
                            )
                        }
                }
            }
            .cachedIn(viewModelScope)

    fun setZoomLevel(level: ZoomLevel) {
        _zoomLevel.value = level
    }
}
