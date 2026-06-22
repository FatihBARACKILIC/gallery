package com.barackilic.gallery.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barackilic.gallery.domain.model.Album
import com.barackilic.gallery.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AlbumsViewModel(
    repository: AlbumRepository,
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(AlbumSortOrder.CountDesc)
    val sortOrder: StateFlow<AlbumSortOrder> = _sortOrder.asStateFlow()

    private val _columns = MutableStateFlow(AlbumColumns.L2)
    val columns: StateFlow<AlbumColumns> = _columns.asStateFlow()

    val albums: StateFlow<List<Album>> =
        combine(repository.observeAlbums(), _sortOrder) { list, order ->
            list.sortedByOrder(order)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun setSortOrder(order: AlbumSortOrder) {
        _sortOrder.value = order
    }

    fun zoomIn() {
        _columns.value = _columns.value.zoomIn()
    }

    fun zoomOut() {
        _columns.value = _columns.value.zoomOut()
    }
}
