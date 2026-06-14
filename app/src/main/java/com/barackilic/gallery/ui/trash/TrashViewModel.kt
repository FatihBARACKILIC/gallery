package com.barackilic.gallery.ui.trash

import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barackilic.gallery.data.mediastore.TrashMediaActions
import com.barackilic.gallery.domain.model.TrashedItem
import com.barackilic.gallery.domain.repository.TrashRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class TrashViewModel(
    repository: TrashRepository,
    private val actions: TrashMediaActions,
) : ViewModel() {

    val items: StateFlow<List<TrashedItem>> = repository.observeTrashed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selection = MutableStateFlow<Set<Long>>(emptySet())
    val selection: StateFlow<Set<Long>> = _selection.asStateFlow()

    fun toggle(mediaId: Long) {
        _selection.update { sel -> if (mediaId in sel) sel - mediaId else sel + mediaId }
    }

    fun clearSelection() {
        _selection.value = emptySet()
    }

    fun buildRestoreRequest(): IntentSenderRequest? {
        val selected = selectedItems() ?: return null
        return IntentSenderRequest.Builder(
            actions.buildRestoreRequest(selected).intentSender,
        ).build()
    }

    fun buildDeleteRequest(): IntentSenderRequest? {
        val selected = selectedItems() ?: return null
        return IntentSenderRequest.Builder(
            actions.buildDeleteForeverRequest(selected).intentSender,
        ).build()
    }

    private fun selectedItems(): List<TrashedItem>? {
        val ids = _selection.value
        if (ids.isEmpty()) return null
        val selected = items.value.filter { it.mediaId in ids }
        return selected.ifEmpty { null }
    }
}
