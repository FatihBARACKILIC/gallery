# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Gallery is an Android application built with Kotlin and Jetpack Compose. It targets API 31+ (Android 12+) with compileSdk 37. The project is in early initialization — `MainActivity` currently shows a placeholder greeting.

## Build & Run Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install and run on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single unit test class
./gradlew test --tests "com.barackilic.gallery.ExampleUnitTest"

# Lint check
./gradlew lint

# Clean build
./gradlew clean
```

## Architecture

Single-module Android app (`app/`) using:
- **Jetpack Compose** (via `kotlin.plugin.compose`) for all UI — no XML layouts
- **Material3** for theming and components
- **Edge-to-edge** display enabled in `MainActivity`

### Package structure
- `com.barackilic.gallery` — app root, contains `MainActivity`
- `com.barackilic.gallery.ui.theme` — `GalleryTheme`, colors, typography

### Key conventions
- All dependencies are declared in `gradle/libs.versions.toml` (version catalog) and referenced via `libs.*` aliases — add new dependencies there, not directly in `build.gradle.kts`
- Kotlin code style is set to `official`
- `minSdk = 31`: no need to handle pre-Android-12 cases
- ProGuard/R8 optimization is disabled in release builds (`optimization { enable = false }`)
