package com.barackilic.gallery.data.repository

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import com.barackilic.gallery.data.mediastore.MediaStoreSource
import com.barackilic.gallery.domain.model.TrashedItem
import com.barackilic.gallery.domain.repository.TrashRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TrashRepositoryImpl(
    private val source: MediaStoreSource,
) : TrashRepository {

    override fun observeTrashed(): Flow<List<TrashedItem>> = ticks()
        .conflate()
        .map { withContext(Dispatchers.IO) { source.queryTrashed() } }

    private fun ticks(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        source.registerObserver(observer)
        trySend(Unit)
        awaitClose { source.unregisterObserver(observer) }
    }
}
