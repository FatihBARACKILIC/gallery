package com.barackilic.gallery.domain.repository

import com.barackilic.gallery.domain.model.TrashedItem
import kotlinx.coroutines.flow.Flow

interface TrashRepository {
    fun observeTrashed(): Flow<List<TrashedItem>>
}
