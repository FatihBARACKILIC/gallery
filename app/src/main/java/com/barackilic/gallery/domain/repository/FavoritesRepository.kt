package com.barackilic.gallery.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavorites(): Flow<Set<Long>>
    suspend fun toggle(mediaId: Long)
}
