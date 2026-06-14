package com.barackilic.gallery.domain.model

data class TrashedItem(
    val mediaId: Long,
    val type: MediaType,
    val expiresAtMillis: Long?,
)
