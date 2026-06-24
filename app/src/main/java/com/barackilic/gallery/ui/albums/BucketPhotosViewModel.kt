package com.barackilic.gallery.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.barackilic.gallery.domain.model.BucketStats
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.repository.MediaRepository
import com.barackilic.gallery.ui.photos.PhotoGridCell
import com.barackilic.gallery.ui.photos.ZoomLevel
import com.barackilic.gallery.ui.photos.headerBetween
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BucketPhotosViewModel(
    private val repository: MediaRepository,
    private val bucketId: Long,
) : ViewModel() {

    private val _zoomLevel = MutableStateFlow(ZoomLevel.L3)
    val zoomLevel: StateFlow<ZoomLevel> = _zoomLevel.asStateFlow()

    private val _sortOrder = MutableStateFlow(BucketPhotosSortOrder.CreatedDesc)
    val sortOrder: StateFlow<BucketPhotosSortOrder> = _sortOrder.asStateFlow()

    private val _groupByDate = MutableStateFlow(false)
    val groupByDate: StateFlow<Boolean> = _groupByDate.asStateFlow()

    private val _stats = MutableStateFlow<BucketStats?>(null)
    val stats: StateFlow<BucketStats?> = _stats.asStateFlow()

    // Captured once; relative buckets (Bugün/Dün) drift past midnight if the app
    // stays open. Acceptable v0.2 — RefreshOnResume rebuilds on next foreground.
    private val nowMillis: Long = System.currentTimeMillis()

    private val pagedMedia: Flow<PagingData<MediaItem>> =
        _sortOrder
            .flatMapLatest { order -> repository.pagedMediaInBucket(bucketId, order.sqlOrder) }
            .cachedIn(viewModelScope)

    val gridCells: Flow<PagingData<PhotoGridCell>> =
        combine(_zoomLevel, _groupByDate, ::Pair)
            .flatMapLatest { (zoom, group) ->
                pagedMedia.map { paging ->
                    val itemsAsCell = paging.map { PhotoGridCell.Item(it) }
                    if (group) {
                        itemsAsCell.insertSeparators<PhotoGridCell.Item, PhotoGridCell> { before, after ->
                            headerBetween(
                                before?.media,
                                after?.media,
                                nowMillis,
                                zoom.headerGranularity,
                            )
                        }
                    } else {
                        // Safe upcast — no separators inserted, just widening the type.
                        itemsAsCell.map { it as PhotoGridCell }
                    }
                }
            }
            .cachedIn(viewModelScope)

    init {
        loadStats()
    }

    fun setZoomLevel(level: ZoomLevel) {
        _zoomLevel.value = level
    }

    fun setSortOrder(order: BucketPhotosSortOrder) {
        _sortOrder.value = order
    }

    fun toggleGroupByDate() {
        _groupByDate.value = !_groupByDate.value
    }

    fun refreshStats() {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _stats.value = repository.bucketStats(bucketId)
        }
    }
}
