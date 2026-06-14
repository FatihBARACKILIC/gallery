package com.barackilic.gallery.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barackilic.gallery.domain.model.Album
import com.barackilic.gallery.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AlbumsViewModel(
    repository: AlbumRepository,
) : ViewModel() {
    val albums: StateFlow<List<Album>> = repository.observeAlbums()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
