package com.barackilic.gallery.ui.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class PhotosViewModel(
    repository: MediaRepository,
) : ViewModel() {
    val photos: Flow<PagingData<MediaItem>> =
        repository.pagedMedia().cachedIn(viewModelScope)
}
