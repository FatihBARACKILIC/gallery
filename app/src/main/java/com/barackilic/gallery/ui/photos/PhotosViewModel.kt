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

    private val _mode = MutableStateFlow(GroupingMode.Day)
    val mode: StateFlow<GroupingMode> = _mode.asStateFlow()

    private val cachedMedia: Flow<PagingData<MediaItem>> =
        (if (bucketId != null) {
            repository.pagedMediaInBucket(bucketId)
        } else {
            repository.pagedMedia()
        }).cachedIn(viewModelScope)

    val gridCells: Flow<PagingData<PhotoGridCell>> =
        _mode
            .flatMapLatest { mode ->
                cachedMedia.map { paging ->
                    paging
                        .map<MediaItem, PhotoGridCell.Item> { PhotoGridCell.Item(it) }
                        .insertSeparators<PhotoGridCell.Item, PhotoGridCell> { before, after ->
                            headerBetween(before?.media, after?.media, mode)
                        }
                }
            }
            .cachedIn(viewModelScope)

    fun setMode(mode: GroupingMode) {
        _mode.value = mode
    }
}
