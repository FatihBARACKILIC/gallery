package com.barackilic.gallery.ui.photos

import com.barackilic.gallery.domain.model.MediaItem
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

sealed interface PhotoGridCell {
    data class Header(val key: String, val label: String) : PhotoGridCell
    data class Item(val media: MediaItem) : PhotoGridCell
}

enum class ZoomLevel(val columns: Int) {
    L24(24),
    L12(12),
    L6(6),
    L3(3);

    fun zoomIn(): ZoomLevel = when (this) {
        L24 -> L12
        L12 -> L6
        L6 -> L3
        L3 -> L3
    }

    fun zoomOut(): ZoomLevel = when (this) {
        L24 -> L24
        L12 -> L24
        L6 -> L12
        L3 -> L6
    }

    val canZoomIn: Boolean get() = this != L3
    val canZoomOut: Boolean get() = this != L24

    val headerGranularity: HeaderGranularity get() = when (this) {
        L3 -> HeaderGranularity.Relative
        L6, L12 -> HeaderGranularity.Monthly
        L24 -> HeaderGranularity.Yearly
    }
}

enum class HeaderGranularity { Relative, Monthly, Yearly }

private sealed interface DateBucket {
    val key: String
    data object Today : DateBucket { override val key = "today" }
    data object Yesterday : DateBucket { override val key = "yesterday" }
    data object ThisWeek : DateBucket { override val key = "this-week" }
    data object ThisMonth : DateBucket { override val key = "this-month" }
    data class YearMonth(val year: Int, val month: Int) : DateBucket {
        override val key = "ym-%04d-%02d".format(year, month)
    }
    data class Year(val year: Int) : DateBucket {
        override val key = "y-$year"
    }
}

// Hardcoded TR month names — design.md disiplini "tek dil TR".
private val TURKISH_MONTHS = arrayOf(
    "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
    "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık",
)

fun headerBetween(
    before: MediaItem?,
    after: MediaItem?,
    nowMillis: Long,
    granularity: HeaderGranularity,
): PhotoGridCell.Header? {
    if (after == null) return null
    val today = toLocalDate(nowMillis)
    val afterBucket = bucketFor(after.dateTakenMillis, today, granularity)
    if (before == null) {
        return PhotoGridCell.Header(
            key = afterBucket.key,
            label = bucketLabel(afterBucket),
        )
    }
    val beforeBucket = bucketFor(before.dateTakenMillis, today, granularity)
    return if (beforeBucket.key != afterBucket.key) {
        PhotoGridCell.Header(
            key = afterBucket.key,
            label = bucketLabel(afterBucket),
        )
    } else {
        null
    }
}

private fun bucketFor(
    dateMillis: Long,
    today: LocalDate,
    granularity: HeaderGranularity,
): DateBucket {
    val date = toLocalDate(dateMillis)
    return when (granularity) {
        HeaderGranularity.Yearly -> DateBucket.Year(date.year)
        HeaderGranularity.Monthly -> DateBucket.YearMonth(date.year, date.monthValue)
        HeaderGranularity.Relative -> relativeBucket(date, today)
    }
}

private fun relativeBucket(date: LocalDate, today: LocalDate): DateBucket {
    val yesterday = today.minusDays(1)
    val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val startOfMonth = today.withDayOfMonth(1)
    return when {
        date == today -> DateBucket.Today
        date == yesterday -> DateBucket.Yesterday
        date >= startOfWeek -> DateBucket.ThisWeek
        date >= startOfMonth -> DateBucket.ThisMonth
        date.year == today.year -> DateBucket.YearMonth(date.year, date.monthValue)
        else -> DateBucket.Year(date.year)
    }
}

private fun bucketLabel(bucket: DateBucket): String = when (bucket) {
    DateBucket.Today -> "Bugün"
    DateBucket.Yesterday -> "Dün"
    DateBucket.ThisWeek -> "Bu Hafta"
    DateBucket.ThisMonth -> "Bu Ay"
    is DateBucket.YearMonth -> "${TURKISH_MONTHS[bucket.month - 1]} ${bucket.year}"
    is DateBucket.Year -> bucket.year.toString()
}

private fun toLocalDate(dateMillis: Long): LocalDate =
    Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
