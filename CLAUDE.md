# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Gallery is a performance-focused, ad-free, open-source Android gallery app built with Kotlin and Jetpack Compose. `minSdk = 31` (Android 12+), `compileSdk = 37`, Java 21.

**Current status:** Building **v0.1 MVP** — see `PLAN.md` for the active step-by-step roadmap. Workflow is incremental: each numbered step in `PLAN.md` ends at a manually-testable state, the user tests on a real device, then commits. Do not start a step before the user approves it.

**Hard constraint:** No third-party ad, analytics, or crash-reporter SDKs. All deps must come from AndroidX / JetBrains / Google Media3 / Coil. A clean `./gradlew app:dependencies` is part of v0.1 acceptance.

## Build & Run Commands

```bash
./gradlew assembleDebug
./gradlew installDebug                                            # install on connected device/emulator
./gradlew test
./gradlew connectedAndroidTest                                    # device required
./gradlew test --tests "com.barackilic.gallery.ExampleUnitTest"   # single test class
./gradlew lint
```

## Architecture

**Pattern:** MVVM + Unidirectional Data Flow. Compose collects `StateFlow<UiState>` from a ViewModel; UI sends back a single `onEvent(UiEvent)` entry point. No MVI ceremony in v0.1 — adopt per-screen later only if a screen genuinely needs it.

**Layering** (single module, package-enforced boundaries):

```
ui/        Compose + ViewModel + UiState (sealed interface)
domain/    Pure Kotlin: models + repository interfaces  (NO Android imports)
data/      MediaStore source, Room DAOs, repository implementations
worker/    WorkManager jobs
core/      Constants, permission helpers, shared util
```

`domain/` is Android-free so it can be lifted into a `:domain` Gradle module later without rewrites. Stay single-module until build time or team boundaries demand otherwise — not preemptively.

### Tech stack

| Concern | Choice |
|---|---|
| DI | **Deferred — see `PLAN.md` "Ertelenmiş Kararlar"** (must be decided before Step 2 wires the first ViewModel) |
| UI | Jetpack Compose + Material3 (Material You, dynamic color), type-safe Navigation Compose (`@Serializable` routes) |
| Async | Coroutines + Flow; `Dispatchers.IO` for MediaStore & Room |
| Paging | Paging 3 over MediaStore cursor |
| Images | Coil 3 + `MediaStore.loadThumbnail` (API 29+) |
| Video | Media3 ExoPlayer |
| DB | Room (KSP) — trash metadata only in v0.1 |
| Background | WorkManager |
| Permissions | Accompanist Permissions |

### Key design decisions

- **Media scanning:** MediaStore + Paging 3. Never scan the filesystem manually — that's the "first-launch takes hours" antipattern this project explicitly rejects.
- **Live updates:** `ContentObserver` on `MediaStore.Files` URI → `PagingSource.invalidate()`. Plus a foreground-resume fallback for OEMs that delay observer notifications.
- **Trash (30-day):** System `MediaStore.createTrashRequest()` (`IS_TRASHED` flag) + a Room row tracking `trashedAt` + original bucket. A periodic `WorkManager` job calls `createDeleteRequest()` for entries older than 30 days. Restore flips `IS_TRASHED=0`. Scoped-storage-safe and keeps files in the user's folders.
- **Repository boundary:** Return `Result<T>` or domain sealed types — do not throw across the layer. UI never sees `IOException`.
- **State holder split:** Screen state → ViewModel. UI-only state (scroll position, dialog visibility) → `remember` / `rememberSaveable`.
- **One ViewModel per screen.** `UiState` lives next to its screen as a `sealed interface` (typical states: `Loading`, `Empty`, `Success`, `NoPermission`, `Error`).

## Conventions

- All dependencies declared in `gradle/libs.versions.toml` and referenced via `libs.*` — never inline coordinates in `build.gradle.kts`.
- Read `PLAN.md` before starting work — it is the source of truth for which step is active and what "done" means for that step. **Always check the "Ertelenmiş Kararlar" section before adding/removing foundational deps** (DI framework, KSP, plugin flags) — there are intentional gaps with revisit triggers.

## Build constraints (AGP 9 transitional setup)

The project uses AGP 9.2.1 with the **standalone** Kotlin Android plugin (not AGP 9's built-in Kotlin) via:

```
android.builtInKotlin=false   # in gradle.properties
android.newDsl=false          # in gradle.properties
```

Reason: KSP (needed for Room codegen at Step 10) is not yet compatible with AGP 9's built-in Kotlin. Removing either flag breaks KSP. The Kotlin version itself is free to track latest — Step 1 verified Kotlin 2.4.0 + KSP 2.3.9 build cleanly. Re-verify when Room `@Entity` classes are introduced (Step 10). Full rationale lives in `PLAN.md` → Ertelenmiş Kararlar.
