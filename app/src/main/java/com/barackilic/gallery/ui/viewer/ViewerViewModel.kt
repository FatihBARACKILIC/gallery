package com.barackilic.gallery.ui.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class ViewerViewModel(
    repository: MediaRepository,
    bucketId: Long? = null,
) : ViewModel() {

    val items: Flow<PagingData<MediaItem>> =
        (if (bucketId != null) {
            repository.pagedMediaInBucket(bucketId)
        } else {
            repository.pagedMedia()
        }).cachedIn(viewModelScope)
}
