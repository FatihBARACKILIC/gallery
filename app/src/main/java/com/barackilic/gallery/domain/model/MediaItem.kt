package com.barackilic.gallery.domain.model

data class MediaItem(
    val id: Long,
    val type: MediaType,
    val dateTakenMillis: Long,
    val durationMs: Long?,
    val bucketId: Long,
    val bucketName: String,
    // 0 when MediaStore doesn't expose dimensions (corrupt metadata, some HEIC/RAW).
    // UI must guard against zero — aspectRatio falls back to 1f below.
    val width: Int = 0,
    val height: Int = 0,
) {
    val aspectRatio: Float
        get() = if (width > 0 && height > 0) width.toFloat() / height else 1f
}
