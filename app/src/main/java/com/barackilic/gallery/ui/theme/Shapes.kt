package com.barackilic.gallery.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Rounded scale from design.md (xs/sm/md/lg/xl/full).
// `full` use-case (butonlar, search-bar, nav pill) Compose'ta CircleShape veya
// component-local RoundedCornerShape(50) ile karşılanır — Material 3 Shapes API'sinde
// dört slot var, full ölçeği bu yüzden burada yok; ihtiyaç noktasında inline kullan.
object GalleryShapes {
    val xs = RoundedCornerShape(4.dp)
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
    val xl = RoundedCornerShape(28.dp)
}

val Shapes = Shapes(
    extraSmall = GalleryShapes.xs,
    small = GalleryShapes.sm,
    medium = GalleryShapes.md,
    large = GalleryShapes.lg,
    extraLarge = GalleryShapes.xl,
)
