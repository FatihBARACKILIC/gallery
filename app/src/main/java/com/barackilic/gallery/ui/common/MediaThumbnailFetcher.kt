package com.barackilic.gallery.ui.common

import android.content.ContentResolver
import android.net.Uri
import android.util.Size
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import coil3.size.pxOrElse

@JvmInline
value class MediaThumbRequest(val uri: Uri)

class MediaThumbnailFetcher(
    private val resolver: ContentResolver,
    private val uri: Uri,
    private val size: Size,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val bitmap = resolver.loadThumbnail(uri, size, null)
        return ImageFetchResult(
            image = bitmap.asImage(),
            isSampled = true,
            dataSource = DataSource.DISK,
        )
    }

    class Factory(
        private val resolver: ContentResolver,
    ) : Fetcher.Factory<MediaThumbRequest> {

        override fun create(
            data: MediaThumbRequest,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher {
            val width = options.size.width.pxOrElse { DEFAULT_THUMB_PX }
            val height = options.size.height.pxOrElse { DEFAULT_THUMB_PX }
            return MediaThumbnailFetcher(
                resolver = resolver,
                uri = data.uri,
                size = Size(width, height),
            )
        }

        private companion object {
            const val DEFAULT_THUMB_PX = 512
        }
    }
}
