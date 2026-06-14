package com.barackilic.gallery.domain.repository

import com.barackilic.gallery.domain.model.Album
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun observeAlbums(): Flow<List<Album>>
}
