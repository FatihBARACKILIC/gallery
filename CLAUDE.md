# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Gallery is a performance-focused, ad-free, open-source Android gallery app built with Kotlin and Jetpack Compose. `minSdk = 30` (Android 11+), `compileSdk = 37`, Java 21. Material You dynamic color is only enabled on API 31+ (Android 12); API 30 falls back to a static color scheme.

**Current status:** **v0.2 Redesign aktif** — `docs/plans/PLAN.md` v0.2 yol haritasını tutar (8 ekran redesign + çoklu seçim + EXIF + Arama; toplam 12 adım). Tamamlanan: Adım 1 (Foundation: ortak component'lar + 4-tab nav), Adım 2 (Permission redesign — Adım 1'den önce yapıldı), Adım 3a (Fotoğraflar: zoom seviyeleri + Türkçe doğal header'lar), Adım 3b (Justified mode L5). Sıradaki: Adım 4 (Albümler redesign). v0.1 PLAN'ı `docs/plans/PLAN-v0.1.md`'ye arşivlendi (mimari kararlar referansı için hâlâ değerli). Tasarım kaynağı: `design.md` (Stitch AI çıktısı) — Material 3, koyu-öncelikli, "arayüz geri çekilir, fotoğraflar öne çıkar" disiplini. Workflow incremental: her adım manuel test edilebilir bir durumda biter, kullanıcı gerçek cihazda test eder, sonra commit. Kullanıcı onaylamadan adıma başlama; her ekranın detay SS'i adım başında gelir.

**Dil notu:** `docs/plans/PLAN.md` Türkçe yazılır (görev, etkilenen dosyalar, ertelenmiş kararlar). Kullanıcı Türkçe konuşur; commit mesajları ve PLAN.md aynı dilde tutulur. Kod, identifier'lar ve teknik yorumlar İngilizce.

**Hard constraint:** No third-party ad, analytics, or crash-reporter SDKs — ever.

Other 3rd-party deps are evaluated case-by-case on trust signals (maintainer reputation, production usage, network footprint, transitive surface) because the app reads user photos. AndroidX / JetBrains / Google / Coil are the preferred ecosystem but not a hard requirement — Koin (Step 3) and Telephoto (Step 7) are accepted examples outside the list. A clean `./gradlew app:dependencies` (no ad/analytics/crash SDK in the transitive graph) is part of every release acceptance bar.

## Build & Run Commands

```bash
./gradlew assembleDebug
./gradlew installDebug                                            # install on connected device/emulator
./gradlew test
./gradlew connectedAndroidTest                                    # device required
./gradlew test --tests "com.barackilic.gallery.ExampleUnitTest"   # single test class
./gradlew lint
./gradlew app:dependencies                                        # verify no ad/analytics/crash SDKs in graph
```

## Architecture

**Package root:** `com.barackilic.gallery` (single Gradle module `:app`, namespace + applicationId aynı).

**Pattern:** MVVM + Unidirectional Data Flow. Compose collects `StateFlow<UiState>` from a ViewModel; UI sends back a single `onEvent(UiEvent)` entry point. No MVI ceremony in v0.1 — adopt per-screen later only if a screen genuinely needs it.

**Layering** (single module, package-enforced boundaries):

```
ui/        Compose + ViewModel + UiState (sealed interface)
domain/    Pure Kotlin: models + repository interfaces  (NO Android imports)
data/      MediaStore source + repository implementations
core/      Constants, permission helpers, shared util
```

`domain/` is Android-free so it can be lifted into a `:domain` Gradle module later without rewrites. Stay single-module until build time or team boundaries demand otherwise — not preemptively.

### Tech stack

| Concern | Choice |
|---|---|
| DI | **Koin** — `koin-android` + `koin-androidx-compose`. Module DSL only (`single`, `viewModel`); no `KoinComponent`, no `by inject()`, no dynamic `get<T>()` in screens. Constructor injection everywhere. This "Hilt-shaped" usage keeps a future Hilt migration mechanical — see `docs/plans/PLAN.md` "Ertelenmiş Kararlar". |
| UI | Jetpack Compose + Material3 (Material You, dynamic color), type-safe Navigation Compose (`@Serializable` routes) |
| Async | Coroutines + Flow; `Dispatchers.IO` for MediaStore cursor work |
| Paging | Paging 3 over MediaStore cursor |
| Images | Coil 3 + `MediaStore.loadThumbnail` (API 29+) |
| Video | Media3 ExoPlayer |
| Permissions | Accompanist Permissions |

### Key design decisions

- **Media scanning:** MediaStore + Paging 3. Never scan the filesystem manually — that's the "first-launch takes hours" antipattern this project explicitly rejects.
- **Live updates:** `ContentObserver` on `MediaStore.Files` URI → `PagingSource.invalidate()`. Plus a foreground-resume fallback for OEMs that delay observer notifications.
- **Trash (30-day):** MediaStore is the source of truth — no app-side database. Soft delete via `createTrashRequest()` (sets `IS_TRASHED=1`); list via `QUERY_ARG_MATCH_TRASHED = MATCH_ONLY`; restore via `createTrashRequest(value=false)`; permanent delete via `createDeleteRequest()`. The system auto-deletes 30 days after trashing (Android 11+ guarantee, exposed as `DATE_EXPIRES`), so no app-side worker is needed. All mutating operations require the user-confirmation system dialog via `IntentSender`. Trash list shows items trashed by *any* app, not just ours.
- **Permissions:** MVP UX "tüm medya" istiyor (`READ_MEDIA_IMAGES` + `READ_MEDIA_VIDEO`). `READ_MEDIA_VISUAL_USER_SELECTED` manifest'te **bilerek** deklare edildi (Step 7 cleanup) — sistem partial-access verirse lint susar ama akış hâlâ tam-erişim. **Manifest'ten silme.** "limited access banner + re-pick" ileride eklenecek; o iş yapılana kadar bu deklarasyon kullanılmıyor gibi görünebilir.
- **Permission akışı:** `Destination.Permission` ayrı bir nav destination (redesign edilmiş ekran: launcher icon + "Galeri" + pill button + "Daha sonra"). Splash sonrası `MainActivity` izin var/yok kontrolüyle başlangıç destination'ı seçer (yok → `Permission`, var → `Photos`). "Daha sonra" → Photos'a popUpTo(Permission, inclusive). Photos/Albums/BucketPhotos hâlâ `PermissionGate`'i defansif inline fallback olarak kullanır (izin runtime'da iptal edilirse). Helper'lar `ui/permission/MediaPermissions.kt`'de (`mediaPermissions()`, `hasMediaPermissions(context)`).
- **Navigation tabs:** 4 tab — Fotoğraflar / Albümler / Arama / Ayarlar. Trash artık tab değil; Settings altında tek `ListItem`'dan erişiliyor. Search ve Settings ekranları v0.2'de placeholder; Search Adım 11/12'de, Settings v0.3'te tam tasarlanacak. Tab kompozisyonu `TopLevelTab` enum'unda, ek tab eklemeden önce `GalleryNavBar`'daki sub-route highlight mantığını kontrol et (Albums→AlbumPhotos, Settings→Trash gibi).
- **Photos zoom system:** 4 uniform + 1 justified zoom seviyesi (`ZoomLevel`: L24/L12/L6/L3/L5). L3 default; L5 = justified row layout (her foto kendi aspect ratio'sunda). Geçiş hem pinch (Box wrap + `pointerInput.awaitEachGesture`, threshold 1.25) hem 3-nokta menüden ("Daha küçük"/"Daha büyük"). Header granularity zoom'a bağlı: L3 → Relative (Bugün/Dün/Bu Hafta/Bu Ay + Ay/Yıl), L6/L12/L5 → Monthly ("Haziran 2026"), L24 → Yearly ("2026"). TR ay isimleri hardcoded (Locale-bağımsız). `PhotosViewModel.gridCells` `flatMapLatest` ile zoom değişince header'ları yeniden hesaplar. L5'te `JustifiedEntry` + `buildJustifiedEntries` ile satır packing (target aspect-units = 2.5).
- **Splash screen:** Android 12+ resmi yol (`androidx.core:core-splashscreen`). `Theme.Gallery.Splash` (values + values-night) sistem splash penceresinin tek kontrol noktasıdır; Compose'un buna erişimi yok çünkü splash Compose başlamadan önce çizilir. `MainActivity.onCreate`'in ilk satırı `installSplashScreen()` çağrısıdır, `super.onCreate`'den önce. **`themes.xml` veya `installSplashScreen()` "Compose kullanıyoruz, gerek yok" diye silinmez** — splash → MainActivity geçişi, exit animasyonu ve API 30 backport davranışı için zorunlu.
- **Tema sistemi:** `ThemeMode` enum (System/Light/Dark/**Amoled**). 3 ColorScheme (`design.md` token'larına göre); **AMOLED dynamic color'ı bilinçli override eder** — saf siyahı koruması için. `GalleryTheme(themeMode = System, dynamicColor = true)` default'ları `MainActivity`'yi bozmaz. Token kaynağı tek: `design.md` → `GalleryColors` / `GalleryShapes` / `GallerySpacing` objelerine eşlenir. Inline hex veya magic number yazma.
- **Tipografi:** `FontFamily.Default` — sistem fontu (Pixel'de Roboto, Samsung'da One UI Sans, vb.). **Bilerek bundle etmiyoruz.** Dynamic color'la aynı felsefe: arayüz sistemle harmanlanır, fotoğraflar öne çıkar; kullanıcının accessibility/özel font tercihine saygı gösterilir. `design.md` "Roboto" diyor ama bu Stitch AI'ın M3 varsayılanı, brand kararı değil — fidelity için Roboto Flex bundle etme.
- **Repository boundary:** Return `Result<T>` or domain sealed types — do not throw across the layer. UI never sees `IOException`.
- **State holder split:** Screen state → ViewModel. UI-only state (scroll position, dialog visibility) → `remember` / `rememberSaveable`.
- **One ViewModel per screen.** `UiState` lives next to its screen as a `sealed interface` (typical states: `Loading`, `Empty`, `Success`, `NoPermission`, `Error`).

## Conventions

- All dependencies declared in `gradle/libs.versions.toml` and referenced via `libs.*` — never inline coordinates in `build.gradle.kts`.
- Read `docs/plans/PLAN.md` before starting work — it is the source of truth for which step is active and what "done" means for that step. **Always check the "Ertelenmiş Kararlar" section before adding/removing foundational deps** (DI framework, KSP, plugin flags) — there are intentional gaps with revisit triggers.
- **Commit mesajları: Türkçe, küçük harf, ASCII-only (aksansız).** Mevcut tarz: `step N: kisa aciklama (parantezli detay)` veya `kategori: kisa aciklama`. Örnekler: `lint temizligi`, `v0.2 prep: tema sistemi`, `step 10: cop kutusu`. Türkçe karakter (ş/ç/ğ/ü/ö/ı) kullanma — sadece commit mesajlarında; kod ve yorumlarda zaten İngilizce.
- **Lint clean disiplini.** Commit öncesi `./gradlew lint` sıfır warning vermeli. Geçici suppression eklemek yerine kök nedeni çöz; mecbursan `@Suppress` yanına kısa "neden" yorumu + kaldırma tetikleyicisi yaz. Proje boyunca iki kez "lint temizligi" commit'i yapıldı — bu standardın ihlal edildiğinde toplandığının kanıtı.
- **Branding assets.** Launcher icon kaynak dosyaları (PNG/SVG orijinalleri) proje kökünde `branding/` altında git'te tutulur. APK'ya gitmez; sadece yeniden export için referans. Image Asset Studio bunlardan `res/mipmap-*/` ve `res/values/ic_launcher_background.xml` üretir. `branding/`'i silme veya `.gitignore`'a alma.

## Workflow

- Kullanıcı SS atar veya istek yapar; Claude doğrudan dosyaya atlamadan önce 2–4 net seçenek `AskUserQuestion`'la sunar (özellikle scope/dep/UX kararları için). Onay sonrası uygular.
- `docs/plans/PLAN.md`'deki bir adıma kullanıcı **açıkça onay vermeden başlama**. Her adımın sonunda manuel test edilebilir bir durum hedeflenir; kullanıcı cihazda test eder, sonra commit atılır.
- Commit'i Claude atmadan önce kullanıcı isteyene kadar bekler. `git push` yalnız kullanıcı açıkça söylerse yapılır.

## Build constraints (AGP 9 transitional setup)

The project uses AGP 9.2.1 with the **standalone** Kotlin Android plugin (not AGP 9's built-in Kotlin) via:

```
android.builtInKotlin=false   # in gradle.properties
android.newDsl=false          # in gradle.properties
```

Reason: KSP requires the standalone Kotlin plugin under AGP 9. KSP was originally added for Room codegen at Step 10, but Step 10 ultimately used MediaStore as the source of truth and Room/KSP-processor were dropped. The KSP plugin alias is still applied (no-op without processors); the flags above remain so we can re-introduce a KSP processor (Hilt migration, future codegen) without re-litigating the build setup. Re-evaluate removing the flags + standalone Kotlin plugin once AGP 9's built-in Kotlin supports KSP, or sooner if we commit to staying KSP-free. Full rationale lives in `docs/plans/PLAN.md` → Ertelenmiş Kararlar.
