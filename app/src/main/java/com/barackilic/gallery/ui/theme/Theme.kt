package com.barackilic.gallery.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode { System, Light, Dark, Amoled }

private val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = GalleryColors.darkPrimary,
    onPrimary = GalleryColors.darkOnPrimary,
    primaryContainer = GalleryColors.darkPrimaryContainer,
    onPrimaryContainer = GalleryColors.darkOnPrimaryContainer,
    secondary = GalleryColors.darkSecondary,
    onSecondary = GalleryColors.darkOnSecondary,
    secondaryContainer = GalleryColors.darkSecondaryContainer,
    onSecondaryContainer = GalleryColors.darkOnSecondaryContainer,
    tertiary = GalleryColors.darkTertiary,
    onTertiary = GalleryColors.darkOnTertiary,
    background = GalleryColors.darkBackground,
    onBackground = GalleryColors.darkOnBackground,
    surface = GalleryColors.darkSurface,
    onSurface = GalleryColors.darkOnSurface,
    onSurfaceVariant = GalleryColors.darkOnSurfaceVariant,
    surfaceContainer = GalleryColors.darkSurfaceContainer,
    surfaceContainerHigh = GalleryColors.darkSurfaceContainerHigh,
    surfaceContainerHighest = GalleryColors.darkSurfaceContainerHighest,
    outline = GalleryColors.darkOutline,
    outlineVariant = GalleryColors.darkOutlineVariant,
    error = GalleryColors.darkError,
    onError = GalleryColors.darkOnError,
    scrim = GalleryColors.scrim,
)

// AMOLED: Dark scheme + saf siyah surface/background; container kademeleri bir tık aşağı kayar.
private val AmoledColorScheme: ColorScheme = DarkColorScheme.copy(
    background = GalleryColors.amoledBackground,
    surface = GalleryColors.amoledSurface,
    surfaceContainer = GalleryColors.amoledSurfaceContainer,
    surfaceContainerHigh = GalleryColors.amoledSurfaceContainerHigh,
    surfaceContainerHighest = GalleryColors.amoledSurfaceContainerHighest,
)

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = GalleryColors.lightPrimary,
    onPrimary = GalleryColors.lightOnPrimary,
    primaryContainer = GalleryColors.lightPrimaryContainer,
    onPrimaryContainer = GalleryColors.lightOnPrimaryContainer,
    secondary = GalleryColors.lightSecondary,
    onSecondary = GalleryColors.lightOnSecondary,
    secondaryContainer = GalleryColors.lightSecondaryContainer,
    onSecondaryContainer = GalleryColors.lightOnSecondaryContainer,
    tertiary = GalleryColors.lightTertiary,
    onTertiary = GalleryColors.lightOnTertiary,
    background = GalleryColors.lightBackground,
    onBackground = GalleryColors.lightOnBackground,
    surface = GalleryColors.lightSurface,
    onSurface = GalleryColors.lightOnSurface,
    onSurfaceVariant = GalleryColors.lightOnSurfaceVariant,
    surfaceContainer = GalleryColors.lightSurfaceContainer,
    surfaceContainerHigh = GalleryColors.lightSurfaceContainerHigh,
    surfaceContainerHighest = GalleryColors.lightSurfaceContainerHighest,
    outline = GalleryColors.lightOutline,
    outlineVariant = GalleryColors.lightOutlineVariant,
    error = GalleryColors.lightError,
    onError = GalleryColors.lightOnError,
    scrim = GalleryColors.scrim,
)

@Composable
fun GalleryTheme(
    themeMode: ThemeMode = ThemeMode.System,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark, ThemeMode.Amoled -> true
    }

    val dynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme: ColorScheme = when {
        // AMOLED'i dynamic color override etmez — saf siyah bilinçli bir tercih.
        themeMode == ThemeMode.Amoled -> AmoledColorScheme
        dynamicColor && dynamicAvailable -> {
            val context = LocalContext.current
            if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
