package com.barackilic.gallery.ui.viewer

import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.barackilic.gallery.data.mediastore.TrashMediaActions
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.repository.FavoritesRepository
import com.barackilic.gallery.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ViewerViewModel(
    repository: MediaRepository,
    private val trashActions: TrashMediaActions,
    private val favoritesRepository: FavoritesRepository,
    bucketId: Long? = null,
) : ViewModel() {

    val items: Flow<PagingData<MediaItem>> =
        (if (bucketId != null) {
            repository.pagedMediaInBucket(bucketId)
        } else {
            repository.pagedMedia()
        }).cachedIn(viewModelScope)

    private val _currentMediaId = MutableStateFlow<Long?>(null)
    val currentMediaId: StateFlow<Long?> = _currentMediaId.asStateFlow()

    val isCurrentFavorite: StateFlow<Boolean> =
        combine(_currentMediaId, favoritesRepository.observeFavorites()) { id, favorites ->
            id != null && id in favorites
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    fun setCurrentMediaId(id: Long?) {
        _currentMediaId.value = id
    }

    fun toggleCurrentFavorite() {
        val id = _currentMediaId.value ?: return
        viewModelScope.launch {
            favoritesRepository.toggle(id)
        }
    }

    fun buildTrashRequest(item: MediaItem): IntentSenderRequest =
        IntentSenderRequest.Builder(
            trashActions.buildTrashRequest(listOf(item)).intentSender,
        ).build()
}
