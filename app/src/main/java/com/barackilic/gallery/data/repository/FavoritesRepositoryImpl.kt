package com.barackilic.gallery.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.barackilic.gallery.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// File-level delegate: DataStore<Preferences> singleton scoped to applicationContext.
// Name "favorites" becomes the .preferences_pb file in /data/data/.../files/datastore/.
private val Context.favoritesDataStore by preferencesDataStore(name = "favorites")

class FavoritesRepositoryImpl(context: Context) : FavoritesRepository {

    private val dataStore = context.applicationContext.favoritesDataStore

    // Set<String> of mediaIds (Long.toString) — DataStore preferences doesn't support
    // a typed Long set, so we serialize. Filter blanks defensively for parser noise.
    private val key = stringSetPreferencesKey("favorite_media_ids")

    override fun observeFavorites(): Flow<Set<Long>> =
        dataStore.data.map { prefs ->
            prefs[key].orEmpty().mapNotNull { it.toLongOrNull() }.toSet()
        }

    override suspend fun toggle(mediaId: Long) {
        dataStore.edit { prefs ->
            val current = prefs[key].orEmpty().toMutableSet()
            val asString = mediaId.toString()
            if (asString in current) current.remove(asString) else current.add(asString)
            prefs[key] = current
        }
    }
}
