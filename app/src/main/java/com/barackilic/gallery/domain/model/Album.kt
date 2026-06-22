package com.barackilic.gallery.domain.model

data class Album(
    val id: Long,
    val name: String,
    val count: Int,
    val coverMediaId: Long,
    val latestDateMillis: Long,
    val earliestDateMillis: Long,
)
