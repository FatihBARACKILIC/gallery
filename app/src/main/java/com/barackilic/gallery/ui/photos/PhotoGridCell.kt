package com.barackilic.gallery.ui.photos

import androidx.annotation.StringRes
import com.barackilic.gallery.R
import com.barackilic.gallery.domain.model.MediaItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed interface PhotoGridCell {
    data class Header(val key: String, val label: String) : PhotoGridCell
    data class Item(val media: MediaItem) : PhotoGridCell
}

enum class GroupingMode(@StringRes val labelRes: Int) {
    Day(R.string.grouping_day),
    Month(R.string.grouping_month),
    Year(R.string.grouping_year),
}

fun headerBetween(
    before: MediaItem?,
    after: MediaItem?,
    mode: GroupingMode,
): PhotoGridCell.Header? {
    if (after == null) return null
    val afterKey = bucketKey(after.dateTakenMillis, mode)
    if (before == null) {
        return PhotoGridCell.Header(
            key = afterKey,
            label = bucketLabel(after.dateTakenMillis, mode),
        )
    }
    val beforeKey = bucketKey(before.dateTakenMillis, mode)
    return if (beforeKey != afterKey) {
        PhotoGridCell.Header(
            key = afterKey,
            label = bucketLabel(after.dateTakenMillis, mode),
        )
    } else {
        null
    }
}

private fun bucketKey(dateMillis: Long, mode: GroupingMode): String {
    val date = toLocalDate(dateMillis)
    return when (mode) {
        GroupingMode.Day -> date.toString()
        GroupingMode.Month -> "%04d-%02d".format(date.year, date.monthValue)
        GroupingMode.Year -> date.year.toString()
    }
}

private fun bucketLabel(dateMillis: Long, mode: GroupingMode): String {
    val date = toLocalDate(dateMillis)
    val locale = Locale.getDefault()
    return when (mode) {
        GroupingMode.Day -> date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale))
        GroupingMode.Month -> date.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
        GroupingMode.Year -> date.year.toString()
    }
}

private fun toLocalDate(dateMillis: Long): LocalDate =
    Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
