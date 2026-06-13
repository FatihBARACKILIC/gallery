package com.barackilic.gallery.domain.model

data class MediaItem(
    val id: Long,
    val type: MediaType,
    val dateTakenMillis: Long,
    val durationMs: Long?,
    val bucketId: Long,
    val bucketName: String,
)
