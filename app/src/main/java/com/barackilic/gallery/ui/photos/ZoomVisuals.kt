package com.barackilic.gallery.ui.photos

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Corner radius scales with cell size — at L12/L24 a fixed 8dp would turn
// sub-50dp cells into circles. Spacing shrinks too so dense grids don't
// drown in gaps. Same values used by Photos and BucketPhotos so zooming
// produces identical visuals across screens.

internal fun cellCornerFor(zoom: ZoomLevel): Dp = when (zoom) {
    ZoomLevel.L3 -> 8.dp
    ZoomLevel.L5 -> 6.dp
    ZoomLevel.L6 -> 4.dp
    ZoomLevel.L12 -> 2.dp
    ZoomLevel.L24 -> 0.dp
}

internal fun cellSpacingFor(zoom: ZoomLevel): Dp = when (zoom) {
    ZoomLevel.L3 -> 4.dp
    ZoomLevel.L5 -> 3.dp
    ZoomLevel.L6 -> 3.dp
    ZoomLevel.L12 -> 2.dp
    ZoomLevel.L24 -> 1.dp
}
