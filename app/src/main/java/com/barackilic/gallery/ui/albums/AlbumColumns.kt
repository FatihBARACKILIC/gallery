package com.barackilic.gallery.ui.albums

// Albums grid column count, controllable via pinch + overflow menu.
// L2 default = SS design (large cards, name/count readable).
enum class AlbumColumns(val count: Int) {
    L2(2),
    L3(3),
    L4(4);

    fun zoomIn(): AlbumColumns = when (this) {
        L2 -> L2
        L3 -> L2
        L4 -> L3
    }

    fun zoomOut(): AlbumColumns = when (this) {
        L2 -> L3
        L3 -> L4
        L4 -> L4
    }

    val canZoomIn: Boolean get() = this != L2
    val canZoomOut: Boolean get() = this != L4
}
