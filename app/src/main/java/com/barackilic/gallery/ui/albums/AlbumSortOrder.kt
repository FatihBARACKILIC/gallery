package com.barackilic.gallery.ui.albums

import androidx.annotation.StringRes
import com.barackilic.gallery.R
import com.barackilic.gallery.domain.model.Album
import java.text.Collator
import java.util.Locale

enum class AlbumSortOrder(@StringRes val labelRes: Int) {
    CountDesc(R.string.albums_sort_count_desc),
    NameAsc(R.string.albums_sort_name_asc),
    LatestUpdated(R.string.albums_sort_latest_updated),
    LatestCreated(R.string.albums_sort_latest_created),
}

// Locale.forLanguageTag("tr") for ç/ğ/ı/ö/ş/ü ordering; PRIMARY ignores case + accent variants.
private val TR_COLLATOR: Collator = Collator.getInstance(Locale.forLanguageTag("tr")).apply {
    strength = Collator.PRIMARY
}

fun List<Album>.sortedByOrder(order: AlbumSortOrder): List<Album> = when (order) {
    AlbumSortOrder.CountDesc -> sortedByDescending { it.count }
    AlbumSortOrder.NameAsc -> sortedWith(compareBy(TR_COLLATOR) { it.name })
    AlbumSortOrder.LatestUpdated -> sortedByDescending { it.latestDateMillis }
    AlbumSortOrder.LatestCreated -> sortedByDescending { it.earliestDateMillis }
}
