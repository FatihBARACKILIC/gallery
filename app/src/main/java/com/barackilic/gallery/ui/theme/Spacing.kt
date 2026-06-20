package com.barackilic.gallery.ui.theme

import androidx.compose.ui.unit.dp

// Spacing scale from design.md — 4dp grid.
// Use case'e göre `Modifier.padding(GallerySpacing.lg)` gibi inline kullan;
// CompositionLocal'a sarmaya v0.2'de ihtiyaç doğarsa eklenir.
// @Suppress: v0.2 Adım 1'de UI component'lar tüketmeye başlayınca kaldırılacak.
@Suppress("unused")
object GallerySpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}
