package com.barackilic.gallery.core.di

import com.barackilic.gallery.data.mediastore.MediaStoreSource
import com.barackilic.gallery.data.mediastore.TrashMediaActions
import com.barackilic.gallery.data.repository.AlbumRepositoryImpl
import com.barackilic.gallery.data.repository.MediaRepositoryImpl
import com.barackilic.gallery.data.repository.TrashRepositoryImpl
import com.barackilic.gallery.domain.repository.AlbumRepository
import com.barackilic.gallery.domain.repository.MediaRepository
import com.barackilic.gallery.domain.repository.TrashRepository
import com.barackilic.gallery.ui.albums.AlbumsViewModel
import com.barackilic.gallery.ui.photos.PhotosViewModel
import com.barackilic.gallery.ui.trash.TrashViewModel
import com.barackilic.gallery.ui.viewer.ViewerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Keep this module "Hilt-shaped": only `single { ... }` for graph nodes and `viewModel { ... }`
// for ViewModels. No `KoinComponent`, no `by inject()`, no dynamic `get<T>()` in screens —
// constructor injection everywhere. This is the contract that makes a later migration to
// Hilt mechanical (see PLAN.md "Ertelenmiş Kararlar").
val appModule = module {
    single { MediaStoreSource(androidContext().contentResolver) }
    single { TrashMediaActions(androidContext().contentResolver) }
    single<MediaRepository> { MediaRepositoryImpl(get()) }
    single<AlbumRepository> { AlbumRepositoryImpl(get()) }
    single<TrashRepository> { TrashRepositoryImpl(get()) }
    viewModel { params -> PhotosViewModel(get(), params.getOrNull<Long>()) }
    viewModel { AlbumsViewModel(get()) }
    viewModel { params -> ViewerViewModel(get(), get(), params.getOrNull<Long>()) }
    viewModel { TrashViewModel(get(), get()) }
}
