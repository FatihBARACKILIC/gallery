# PLAN.md — v0.2 Redesign

> v0.1 MVP tamamlandı (Step 10 / çöp kutusu dahil). Bu plan, Stitch AI ile çıkarılan yeni tasarım kimliği (`design.md`) etrafında mevcut ekranların yeniden inşası + ilk grup yeni özellikleri kapsar. v0.1 referans olarak `PLAN-v0.1.md`'ye arşivlendi.

## Görev

Mevcut 8 ekranı `design.md` kimliğine taşı (Material 3, koyu-öncelikli, içerik öncelikli) ve mevcudu zenginleştiren 4 özelliği ekle: çoklu seçim modu, EXIF bottom sheet, Arama sekmesi (başlangıç + sonuçlar). Detaylı ekran tasarımları her adımın başında ayrı SS olarak gelecek; bu plan iskeleti ve sıralamayı tutar.

## Kapsam — v0.2 içinde

| # | Ekran / Özellik | Tip |
|---|---|---|
| 1 | Permission | Redesign |
| 2 | Fotoğraflar (grid) | Redesign |
| 3 | Albümler | Redesign |
| 4 | Albüm Detay | Redesign |
| 5 | Viewer — foto | Redesign |
| 6 | Viewer — video | Redesign |
| 7 | Çöp Kutusu | Redesign |
| 8 | Çöp Kutusu — boş | Redesign |
| 9 | Çoklu seçim modu (Photos + AlbumDetail + Trash) | Yeni mod |
| 10 | EXIF bottom sheet (Viewer'dan) | Yeni |
| 11 | Arama — başlangıç (sekme, recent, type chips) | Yeni |
| 12 | Arama — sonuçlar (filter + grid) | Yeni |

## Kapsam dışı — sonraki release'lere ertelendi

- **v0.3:** Ayarlar, Kilit Ekranı (biyometri + PIN), Kilitli Klasör (içerik + boş)
- **v0.4:** Fotoğraf Düzenleyici, Kolaj Oluşturucu, Video Kırpma
- **v0.5:** Kişiler (ML Kit Face Detection — on-device), Yedekleme (servis seçimi açık)

Heavy dep kararları (ML Kit, yedekleme servisi) ilgili release'in PLAN'ında "Ertelenmiş Kararlar" altında tartışılacak. Şimdiden bağlamayalım.

## Etkilenen Dosyalar

### Değişecek
- `ui/photos/PhotosScreen.kt`, `PhotoGridCell.kt`, `PhotosViewModel.kt` — yeni grid stili + sticky tarih başlıkları + selection mode entegrasyonu
- `ui/albums/AlbumsScreen.kt`, `AlbumsViewModel.kt` — 2-col card grid
- `ui/viewer/ViewerScreen.kt`, `ViewerViewModel.kt` — minimal top + alt action bar, scrim gradient, EXIF tetikleyici
- `ui/trash/TrashScreen.kt`, `TrashViewModel.kt` — grid + kalan gün rozeti + empty state
- `ui/common/PermissionGate.kt` — design'a göre yeniden
- `ui/navigation/Destinations.kt`, `GalleryNavHost.kt` — Search rotası
- `MainActivity.kt` — nav bar yeni component'a değişir (pill highlight'lı 4-tab); tab kompozisyonu kullanıcı SS'leri geldikçe netleşir
- `ui/theme/*` — ✅ tamamlandı (Color/Type/Shapes/Spacing/Theme)

### Yeni
- `ui/common/GalleryTopBar.kt` — küçük (small) top app bar, scroll'da surface tonu yükselir
- `ui/common/GalleryNavBar.kt` — 4-tab nav, aktif sekme `secondary-container` full-radius pill
- `ui/common/GalleryFab.kt` — `primary-container` zemin, `lg` radius
- `ui/common/SearchField.kt` — full-radius, `surface-container-high`
- `ui/common/FilterChipRow.kt` — `chip-filter` + `chip-filter-selected`
- `ui/common/EmptyState.kt` — büyük ikon + başlık + açıklama (Çöp Kutusu / Arama / Albümler için ortak)
- `ui/common/SelectionTopBar.kt` — "N seçildi" + actions; BackHandler'lı
- `ui/common/SelectionState.kt` — Composable-scoped state holder (paging'le uyumlu, ID set'i)
- `ui/viewer/ExifSheet.kt` — `ModalBottomSheet`, dosya bilgileri
- `ui/viewer/ViewerBottomBar.kt` — share / fav / edit / trash / info
- `ui/search/SearchScreen.kt`, `SearchViewModel.kt`, `SearchUiState.kt`
- `data/search/SearchHistoryStore.kt` — DataStore Preferences ile recent searches
- `domain/model/MediaExif.kt` — Android-bağımsız EXIF DTO
- `data/mediastore/ExifReader.kt` — `androidx.exifinterface` ile okuma

### Değişmeyecek
- `data/mediastore/MediaPagingSource.kt`, `MediaStoreSource.kt`, `TrashMediaActions.kt` (cursor query yapısı)
- `data/repository/*` — interface'ler aynı; yeni search repo eklenir
- `domain/repository/*` — yeni `SearchRepository` eklenir, diğerleri aynı
- `LICENSE`, `README.md`

## Mimari Özet

- **Tema:** `ThemeMode` enum (System/Light/Dark/Amoled) + dynamic color (✅). Ayarlardan değiştirme v0.3'te.
- **Navigation:** 4 tab — Fotoğraflar / Albümler / Arama / Ayarlar (Adım 1'de kuruldu). Trash, Ayarlar altında bir alt-route. Settings ekranı v0.2'de minimal placeholder, v0.3'te tam tasarım gelecek.
- **Selection mode:** Tek `SelectionState` holder pattern; Photos, AlbumDetail ve Trash'te ortak. PagingData'nın diff'lemesine müdahale etmiyoruz — seçili `Set<Long>` UI tarafında tutulur.
- **Search:** v0.2'de basit filtre — MediaStore cursor query'sine filename + bucket name `LIKE %q%` koşulu. Type filter (foto/video) zaten projection'da var. Tam-metin / metadata index'i v0.3+.
- **EXIF:** `androidx.exifinterface` ile lazım anda oku (viewer "info" tıklanınca); cache etme, küçük overhead.

## Uygulama Adımları

Her adım sonunda manuel test edilebilir. Her adım kendi commit'i. Kullanıcı SS göndermeden adıma başlama.

### Adım 1 — Foundation: ortak component'lar + yeni nav bar ✅ (2026-06-21)
- `GalleryTopBar`, `GalleryNavBar` (pill), `GalleryFab`, `SearchField`, `FilterChipRow`, `EmptyState` iskeletleri
- `MainActivity` nav bar'ı `GalleryNavBar`'a geçti; 4 tab: **Fotoğraflar / Albümler / Arama / Ayarlar**
- Arama tab: `ui/search/SearchScreen.kt` placeholder ("Arama yakında" EmptyState ile)
- Ayarlar tab: `ui/settings/SettingsScreen.kt` placeholder + tek `ListItem` "Çöp Kutusu" → TrashScreen
- TrashScreen artık tab değil, Settings altı sub-route; `onBack` callback eklendi (back button)
- Tüm UI string'leri Türkçe'ye çevrildi (Photos/Albums/Search/Settings + Trash içeriği)

### Adım 2 — Permission ekranı redesign ✅ (Adım 1'den önce yapıldı — 2026-06-21)
- Logo + başlık + açıklama + birincil pill button + text "Daha sonra"
- **Akış değişikliği:** Permission artık ayrı `Destination.Permission` — splash sonrası izin yoksa burası açılır, izin varsa direkt Photos. "Daha sonra" → Photos'a geçer, içerik yoksa empty state.
- Photos/Albums/BucketPhotos'taki defansif `PermissionGate` korunur (izin runtime'da iptal edilirse veya kullanıcı "Daha sonra" demişse fallback)
- **Test:** İlk açılış akışı bozulmaz

### Adım 3a — Fotoğraflar: foundation (top bar + zoom + doğal TR header)
- Yeni `GalleryTopBar` PhotosScreen'e bağlanır ("Fotoğraflar" başlık + 3-nokta menü)
- 3-nokta menü: "Daha küçük" / "Daha büyük" (zoom out/in)
- 4 uniform zoom seviyesi: L24 / L12 / L6 / L3 (kare hücreler, default L3)
- Pinch-to-zoom (transformable state + threshold)
- `GroupingMode` (Day/Month/Year SegmentedButton) silinir
- Yeni tarih header sistemi (Bugün / Dün / Bu Hafta / Bu Ay / Mayıs 2026 / 2025 ...)
- Ay isimleri hardcoded TR (Ocak, Şubat, ...)
- **Test:** Pinch + butonlar L24↔L3 arası geçiş yapar; header'lar doğru gruplama gösterir

### Adım 3b — Fotoğraflar: justified mode (L5)
- Justified row layout — her foto kendi aspect ratio'sunda; satır packing algoritması
- MediaStore projection'a `WIDTH`/`HEIGHT` eklenir, `MediaItem`'a aspect ratio alanı
- L5 header'ları farklı granülerlik: Haziran 2026 / Mayıs 2026 / ... / 2025 (Bugün/Dün/Bu Hafta yok)
- Pinch out L3 → L5 geçişi
- **Test:** Karışık portrait/landscape akıcı, layout shift yok

### Adım 4 — Albümler grid redesign
- 2-col card grid, kapak + isim + sayı
- **Test:** Camera/Screenshots/Downloads doğru görünür

### Adım 5 — Albüm Detay redesign
- Top bar (back + isim + sayı), 3-col grid, FAB davranışı SS'e göre
- **Test:** Albüm içine giriş

### Adım 6 — Viewer (foto) redesign
- Minimal top bar + alt action bar (share/fav/edit/trash/info), scrim gradient, otomatik gizleme
- **Test:** Pinch zoom + pan + bar toggle akıcı

### Adım 7 — Viewer (video) redesign
- Alt seekbar + play/pause + ±10sn, yeni stil
- **Test:** Video oynatma + seek + sayfa geçişi

### Adım 8 — Trash redesign + empty state
- 3-col grid, kalan gün rozeti (`DATE_EXPIRES`'tan), "Tümünü temizle"
- Boş state: ikon + başlık + açıklama
- **Test:** Dolu/boş senaryolar

### Adım 9 — Çoklu seçim modu
- Long-press → `SelectionTopBar` ("N seçildi") + actions (share, trash, restore [trash'te])
- Selection overlay: thumbnail üzerine `primary-container` tikli katman
- BackHandler temizler; sistem onay dialog'u toplu URI listesiyle açılır
- **Test:** Photos / AlbumDetail / Trash üçünde de long-press → multi-select → action

### Adım 10 — EXIF bottom sheet
- Viewer "info" → `ModalBottomSheet`: dosya adı, tarih, boyut, çözünürlük, kamera (make/model/iso/aperture/exposure), GPS coords (string)
- `androidx.exifinterface` eklenir
- **Test:** Farklı kamera/screenshot/WhatsApp foto için sheet doğru veriler

### Adım 11 — Arama — başlangıç ekranı (yeni sekme)
- Nav'a Search tab; SearchField + recent searches list + file type chip'leri (Fotoğraflar / Videolar / GIF / Belgeler — design'a göre)
- Recent searches DataStore Preferences'ta (yeni dep)
- **Test:** Arama sekmesine geçiş, recent listesi (boş başlar)

### Adım 12 — Arama — sonuçlar
- Query'de FilterChipRow (foto/video) + sort menüsü
- Sonuç grid (Adım 3'ün PhotoGridCell'ini paylaşır)
- Boş sonuç state: `EmptyState`
- **Test:** Birkaç sorgu — filename match, bucket match, filter chip değişimi

## Bağımlılık Seçimleri

| Amaç | Tercih | Neden |
|---|---|---|
| EXIF okuma | `androidx.exifinterface` | Resmi AndroidX, ek dep değil sayılır, network footprint yok |
| Search history | `androidx.datastore.preferences` | Resmi AndroidX, hafif, async-safe |
| Harita preview (EXIF) | (yok) | v0.2'de coordinates string; map embed büyük dep, v0.3+ |

`./gradlew app:dependencies` çıktısı v0.2 sonunda hâlâ ad/analytics/crash SDK içermemeli.

## Ertelenmiş Kararlar

> v0.1 PLAN'ının "Ertelenmiş Kararlar"ı (DI / Koin, KSP flags) hâlâ geçerli — `PLAN-v0.1.md`'ye bakın.

### Nav bar 4. tab → Settings ✅ (uygulandı: 2026-06-21, Adım 1)
- 4 tab: Fotoğraflar / Albümler / Arama / Ayarlar. Trash, Ayarlar ekranı altında tek `ListItem`'dan erişiliyor.
- Settings ekranı v0.3'te tam tasarlanacak (tema seçimi, hakkında, vb. eklenir). v0.2 boyunca minimal: sadece Çöp Kutusu satırı.

### Dynamic color + tasarım disiplini (2026-06-20)
- design.md "tek mavi vurgu" diyor; Material You dynamic açıkken seçim/FAB rengi kullanıcı wallpaper'ından gelir → disiplin kaybolur.
- Karar: v0.2'de dynamic color **on** tutuluyor (kullanıcı tercih sinyali). Ayarlardan kapatma (v0.3) gelince static palet tercih edenler için seçenek olur.

### EXIF harita preview
- v0.2: GPS coordinates sadece string. Map embed (Compose Maps, osmdroid) önemli dep + tracker yüzeyi riski.
- Trigger: Kullanıcı geri bildirimi map istiyorsa v0.3'te osmdroid (tiles self-hosted veya OSM public) değerlendirilir.

### Search performansı
- v0.2: cursor `LIKE %q%` filename + bucket'ta. ~50k+ öğeli cihazda kabul edilebilir; 500k+ için yavaşlar.
- Trigger: Stres testte fark edilirse, v0.3'te MediaStore üzerine FTS index (Room) düşünülür. Bu Room/KSP'yi geri getirir — `PLAN-v0.1.md` Ertelenmiş Kararlar'a paralel olarak değerlendirilmeli.

## Riskler / Belirsizlikler

- **Tasarım detay eksikliği:** Her ekran SS'i adım başında gelecek; spacing/icon/animasyon detayları geldikçe iterate.
- **Selection + Paging:** Paging 3 page'ları geri geldikçe seçim state'i ID set'inde korunmalı. Standart pattern ama dikkat.
- **ExifInterface URI desteği:** `InputStream` overload'u kullanılır (`contentResolver.openInputStream`). Bazı HEIC/RAW dosyalarda alan eksik dönebilir — UI "Bilinmiyor" göstermeli, crash değil.
- **DataStore migration:** Hâlâ yok ama eklendiğinde tek kullanım (recent searches). Future-proof key isimlendirmesi.

## Başarı Kriterleri

- [ ] 8 mevcut ekran `design.md` kimliğine geçti — koyu yüzeyler, pill nav, label-medium metadata, edge-to-edge grid
- [ ] Çoklu seçim 3 ekranda da çalışıyor; toplu trash/restore tek sistem onay dialog'unda
- [ ] EXIF sheet kamera/screenshot/3rd-party app foto için doğru veri
- [ ] Arama: recent + chip filter + sonuç grid; boş sonuç state'i var
- [ ] `./gradlew app:dependencies` temiz (ekstra sadece `androidx.exifinterface` + `androidx.datastore.preferences`)
- [ ] Lint clean (`./gradlew lint`), debug build başarılı, her adım kendi commit'i
