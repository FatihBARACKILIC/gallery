package com.barackilic.gallery.ui.theme

import androidx.compose.ui.graphics.Color

// Tokens from design.md (Dark — varsayılan)
private val DarkPrimary = Color(0xFF9FCAFF)
private val DarkOnPrimary = Color(0xFF00315C)
private val DarkPrimaryContainer = Color(0xFF194A7A)
private val DarkOnPrimaryContainer = Color(0xFFD2E4FF)
private val DarkSecondary = Color(0xFFBBC7DB)
private val DarkOnSecondary = Color(0xFF253140)
private val DarkSecondaryContainer = Color(0xFF3B4858)
private val DarkOnSecondaryContainer = Color(0xFFD7E3F8)
private val DarkTertiary = Color(0xFFD7BDE2)
private val DarkOnTertiary = Color(0xFF3B2948)
private val DarkBackground = Color(0xFF0E0F11)
private val DarkOnBackground = Color(0xFFE4E2E6)
private val DarkSurface = Color(0xFF0E0F11)
private val DarkSurfaceContainer = Color(0xFF1A1B1E)
private val DarkSurfaceContainerHigh = Color(0xFF242528)
private val DarkSurfaceContainerHighest = Color(0xFF2E3033)
private val DarkOnSurface = Color(0xFFE4E2E6)
private val DarkOnSurfaceVariant = Color(0xFFC5C6D0)
private val DarkOutline = Color(0xFF8F9099)
private val DarkOutlineVariant = Color(0xFF44474E)
private val DarkError = Color(0xFFFFB4AB)
private val DarkOnError = Color(0xFF690005)
private val Scrim = Color(0xFF000000)

// AMOLED — saf siyah arka plan; container kademeleri Dark'ın bir altına çekilir
private val AmoledBackground = Color(0xFF000000)
private val AmoledSurface = Color(0xFF000000)
private val AmoledSurfaceContainer = DarkBackground            // #0E0F11
private val AmoledSurfaceContainerHigh = DarkSurfaceContainer  // #1A1B1E
private val AmoledSurfaceContainerHighest = DarkSurfaceContainerHigh // #242528

// Light — design.md verilen değerler + M3 baseline'dan türetilen tamamlayıcılar
private val LightPrimary = Color(0xFF415E91)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFD2E4FF)
private val LightOnPrimaryContainer = Color(0xFF001B3D)
private val LightSecondary = Color(0xFF565F71)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFDAE2F9)
private val LightOnSecondaryContainer = Color(0xFF131C2B)
private val LightTertiary = Color(0xFF715573)
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightBackground = Color(0xFFFAF8FC)
private val LightOnBackground = Color(0xFF1A1C1E)
private val LightSurface = Color(0xFFFAF8FC)
private val LightSurfaceContainer = Color(0xFFEEEDF1)
private val LightSurfaceContainerHigh = Color(0xFFE8E7EB)
private val LightSurfaceContainerHighest = Color(0xFFE2E2E5)
private val LightOnSurface = Color(0xFF1A1C1E)
private val LightOnSurfaceVariant = Color(0xFF44474E)
private val LightOutline = Color(0xFF74777F)
private val LightOutlineVariant = Color(0xFFC4C6CF)
private val LightError = Color(0xFFBA1A1A)
private val LightOnError = Color(0xFFFFFFFF)

internal object GalleryColors {
    val darkPrimary = DarkPrimary
    val darkOnPrimary = DarkOnPrimary
    val darkPrimaryContainer = DarkPrimaryContainer
    val darkOnPrimaryContainer = DarkOnPrimaryContainer
    val darkSecondary = DarkSecondary
    val darkOnSecondary = DarkOnSecondary
    val darkSecondaryContainer = DarkSecondaryContainer
    val darkOnSecondaryContainer = DarkOnSecondaryContainer
    val darkTertiary = DarkTertiary
    val darkOnTertiary = DarkOnTertiary
    val darkBackground = DarkBackground
    val darkOnBackground = DarkOnBackground
    val darkSurface = DarkSurface
    val darkSurfaceContainer = DarkSurfaceContainer
    val darkSurfaceContainerHigh = DarkSurfaceContainerHigh
    val darkSurfaceContainerHighest = DarkSurfaceContainerHighest
    val darkOnSurface = DarkOnSurface
    val darkOnSurfaceVariant = DarkOnSurfaceVariant
    val darkOutline = DarkOutline
    val darkOutlineVariant = DarkOutlineVariant
    val darkError = DarkError
    val darkOnError = DarkOnError
    val scrim = Scrim

    val amoledBackground = AmoledBackground
    val amoledSurface = AmoledSurface
    val amoledSurfaceContainer = AmoledSurfaceContainer
    val amoledSurfaceContainerHigh = AmoledSurfaceContainerHigh
    val amoledSurfaceContainerHighest = AmoledSurfaceContainerHighest

    val lightPrimary = LightPrimary
    val lightOnPrimary = LightOnPrimary
    val lightPrimaryContainer = LightPrimaryContainer
    val lightOnPrimaryContainer = LightOnPrimaryContainer
    val lightSecondary = LightSecondary
    val lightOnSecondary = LightOnSecondary
    val lightSecondaryContainer = LightSecondaryContainer
    val lightOnSecondaryContainer = LightOnSecondaryContainer
    val lightTertiary = LightTertiary
    val lightOnTertiary = LightOnTertiary
    val lightBackground = LightBackground
    val lightOnBackground = LightOnBackground
    val lightSurface = LightSurface
    val lightSurfaceContainer = LightSurfaceContainer
    val lightSurfaceContainerHigh = LightSurfaceContainerHigh
    val lightSurfaceContainerHighest = LightSurfaceContainerHighest
    val lightOnSurface = LightOnSurface
    val lightOnSurfaceVariant = LightOnSurfaceVariant
    val lightOutline = LightOutline
    val lightOutlineVariant = LightOutlineVariant
    val lightError = LightError
    val lightOnError = LightOnError
}
