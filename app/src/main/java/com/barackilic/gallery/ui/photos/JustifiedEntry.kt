package com.barackilic.gallery.ui.photos

import androidx.paging.compose.LazyPagingItems
import com.barackilic.gallery.domain.model.MediaItem

// Data structure for the L5 (justified) photo layout. Each entry maps 1:1 to a
// LazyColumn item: either a sticky date header or a horizontal row of photos
// packed at their native aspect ratios.
sealed interface JustifiedEntry {
    val key: String
    data class Header(override val key: String, val label: String) : JustifiedEntry
    data class Row(
        override val key: String,
        val items: List<JustifiedItem>,
    ) : JustifiedEntry
}

data class JustifiedItem(
    val media: MediaItem,
    // Absolute index within the media-only sequence (ignoring headers).
    // Used by the viewer to seek to the tapped photo.
    val mediaIndex: Int,
)

// Tunable: how many aspect-ratio units to pack per row before closing it.
// 2.5 ≈ ~2.5 average-aspect photos per row → row height ~ containerWidth / 2.5.
// On a 390dp wide screen, that's ~155dp tall rows of mixed portrait/landscape.
private const val ROW_TARGET_ASPECT_SUM = 2.5f

// Builds JustifiedEntry list from currently-loaded PhotoGridCell pages. Uses
// `peek` so paging isn't forced to materialize items just for packing.
fun buildJustifiedEntries(items: LazyPagingItems<PhotoGridCell>): List<JustifiedEntry> {
    val result = ArrayList<JustifiedEntry>(items.itemCount / 2)
    val pending = ArrayList<JustifiedItem>(8)
    var pendingAspectSum = 0f
    var mediaIndex = 0
    var rowCounter = 0

    fun flushRow() {
        if (pending.isEmpty()) return
        val rowKey = "row-${pending.first().media.id}-$rowCounter"
        result.add(JustifiedEntry.Row(key = rowKey, items = pending.toList()))
        pending.clear()
        pendingAspectSum = 0f
        rowCounter++
    }

    for (i in 0 until items.itemCount) {
        when (val cell = items.peek(i)) {
            is PhotoGridCell.Header -> {
                flushRow()
                result.add(JustifiedEntry.Header(key = "h-${cell.key}", label = cell.label))
            }
            is PhotoGridCell.Item -> {
                val aspect = cell.media.aspectRatio
                pending.add(JustifiedItem(cell.media, mediaIndex))
                mediaIndex++
                pendingAspectSum += aspect
                if (pendingAspectSum >= ROW_TARGET_ASPECT_SUM) {
                    flushRow()
                }
            }
            null -> {
                // Placeholder / not yet loaded — stop packing here so we don't emit
                // partial rows that would later need to be re-grouped.
                flushRow()
                return result
            }
        }
    }
    flushRow()
    return result
}
