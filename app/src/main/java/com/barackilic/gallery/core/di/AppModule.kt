package com.barackilic.gallery.core.di

import com.barackilic.gallery.data.mediastore.MediaStoreSource
import com.barackilic.gallery.data.repository.MediaRepositoryImpl
import com.barackilic.gallery.domain.repository.MediaRepository
import com.barackilic.gallery.ui.photos.PhotosViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Keep this module "Hilt-shaped": only `single { ... }` for graph nodes and `viewModel { ... }`
// for ViewModels. No `KoinComponent`, no `by inject()`, no dynamic `get<T>()` in screens —
// constructor injection everywhere. This is the contract that makes a later migration to
// Hilt mechanical (see PLAN.md "Ertelenmiş Kararlar").
val appModule = module {
    single { MediaStoreSource(androidContext().contentResolver) }
    single<MediaRepository> { MediaRepositoryImpl(get()) }
    viewModel { PhotosViewModel(get()) }
}
