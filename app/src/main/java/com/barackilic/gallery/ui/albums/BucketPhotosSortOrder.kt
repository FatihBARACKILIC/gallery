package com.barackilic.gallery.ui.albums

import android.provider.MediaStore.Files.FileColumns as Cols
import androidx.annotation.StringRes
import com.barackilic.gallery.R

// SQL ORDER BY clauses sent through MediaProvider's QUERY_ARG_SQL_SORT_ORDER.
// Name sort uses NOCASE (binary unicode after lowercasing) — TR ç/ğ/ı/ş/ü ordering
// won't match Collator.PRIMARY, but cursor-level locale collation isn't portable
// across OEMs. UI-side TR collation isn't possible here because data is paged.
enum class BucketPhotosSortOrder(
    @StringRes val labelRes: Int,
    val sqlOrder: String,
) {
    CreatedDesc(
        R.string.bucket_sort_created_desc,
        "COALESCE(${Cols.DATE_TAKEN}, ${Cols.DATE_MODIFIED} * 1000) DESC, ${Cols._ID} DESC",
    ),
    CreatedAsc(
        R.string.bucket_sort_created_asc,
        "COALESCE(${Cols.DATE_TAKEN}, ${Cols.DATE_MODIFIED} * 1000) ASC, ${Cols._ID} ASC",
    ),
    ModifiedDesc(
        R.string.bucket_sort_modified_desc,
        "${Cols.DATE_MODIFIED} DESC, ${Cols._ID} DESC",
    ),
    ModifiedAsc(
        R.string.bucket_sort_modified_asc,
        "${Cols.DATE_MODIFIED} ASC, ${Cols._ID} ASC",
    ),
    NameAsc(
        R.string.bucket_sort_name_asc,
        "${Cols.DISPLAY_NAME} COLLATE NOCASE ASC, ${Cols._ID} ASC",
    ),
    NameDesc(
        R.string.bucket_sort_name_desc,
        "${Cols.DISPLAY_NAME} COLLATE NOCASE DESC, ${Cols._ID} DESC",
    );

    val isChronological: Boolean get() = this == CreatedDesc || this == CreatedAsc
}
