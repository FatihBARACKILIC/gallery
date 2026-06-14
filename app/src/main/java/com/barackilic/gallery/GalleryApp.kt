package com.barackilic.gallery

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.barackilic.gallery.core.di.appModule
import com.barackilic.gallery.ui.common.MediaThumbnailFetcher
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GalleryApp : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GalleryApp)
            modules(appModule)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(MediaThumbnailFetcher.Factory(contentResolver))
            }
            .crossfade(true)
            .build()
}
