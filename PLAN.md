# PLAN.md — v0.1 MVP

## Görev
Android cihazdaki tüm medyayı (fotoğraf + video) yüksek performansla listeleyen, tam ekran görüntüleyici, basit video oynatıcı, paylaşma ve 30 günlük çöp kutusu içeren bir galeri uygulamasının v0.1 MVP sürümünü inşa etmek. Üçüncü parti reklam / takip kütüphanesi kullanılmayacak; tüm bağımlılıklar AndroidX / JetBrains / Google Media3 ekosisteminden seçilecek.

## Etkilenen Dosyalar

### Değişecek / Yeni oluşturulacak
- `gradle/libs.versions.toml` — yeni bağımlılıklar (Hilt, Room, Paging 3, Media3, Coil, Navigation Compose, WorkManager, Accompanist Permissions, KSP)
- `build.gradle.kts` (root) — KSP ve Hilt plugin alias'ları
- `app/build.gradle.kts` — yeni plugin'ler, dependency'ler, KSP konfigürasyonu
- `app/src/main/AndroidManifest.xml` — `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `POST_NOTIFICATIONS`, `FileProvider`, `Application` sınıfı, `MainActivity` `singleTop`
- `app/src/main/res/xml/file_paths.xml` — FileProvider için (yeni)
- `app/src/main/java/com/barackilic/gallery/` altında yeni paket yapısı:
  - `GalleryApp.kt` — `@HiltAndroidApp`
  - `MainActivity.kt` — Hilt entry + NavHost'a geçiş
  - `core/` — sabitler, ortak yardımcılar, permission yardımcıları
  - `data/mediastore/` — `MediaStoreSource`, `MediaPagingSource`, `ContentObserver`
  - `data/db/` — Room: `TrashedItemDao`, `TrashedItemEntity`, `GalleryDatabase`
  - `data/repository/` — `MediaRepository`, `AlbumRepository`, `TrashRepository`
  - `domain/model/` — `MediaItem`, `Album`, `MediaType`
  - `ui/navigation/` — `NavGraph`, rota sabitleri
  - `ui/photos/` — fotoğraf+video grid ekranı, ViewModel
  - `ui/albums/` — albüm listesi + albüm içi ekran, ViewModel
  - `ui/viewer/` — tam ekran pager, foto görüntüleyici, video player, ViewModel
  - `ui/trash/` — çöp kutusu ekranı, ViewModel
  - `ui/common/` — ortak composable'lar (MediaThumb, DurationBadge, SectionHeader, PermissionGate)
  - `worker/TrashCleanupWorker.kt`

### Değişmeyecek
- Gradle wrapper dosyaları, `settings.gradle.kts`
- `ui/theme/` (renk/typography v0.1 için yeterli, ileride özelleştirilebilir)
- Launcher ikon ve kaynak resimleri
- `LICENSE`, `README.md`, `CLAUDE.md` (gerekirse v0.1 sonunda güncellenir)

## Mimari Özet

- **Sunum:** Jetpack Compose + Material3, tek `MainActivity` + Navigation Compose
- **DI:** Hilt
- **Async:** Kotlin Coroutines + Flow
- **Veri kaynakları:**
  - `MediaStore` (sistemin SQLite indeksi) — milyonlarca öğeyi cursor + Paging 3 ile tarayacak; **kendi tarayıcımızı yazmayacağız.** Bu, "ilk açılışta saatlerce bekleme" sorununun çözümüdür: sistem zaten indekslemiş.
  - `ContentObserver` — yeni medya eklendiğinde paging kaynağını invalidate eder (canlı güncelleme)
  - Room — sadece çöp kutusu meta verisi (silinme zamanı, orijinal `BUCKET_ID`, MediaStore URI)
- **Görsel yükleme:** Coil 3 — `MediaStore.loadThumbnail` (API 29+) ile küçük thumb'lar, bellek + disk cache
- **Video:** Media3 (ExoPlayer) — viewer'da tek `ExoPlayer` instance, lifecycle-aware
- **Sayfalama:** Paging 3 — `MediaStore` cursor üzerinden `PagingSource`; LazyVerticalGrid içinde
- **Çöp kutusu stratejisi:** `MediaStore.createTrashRequest()` (sistem `IS_TRASHED` flag'ı) + Room'da timestamp; 30 gün sonra `WorkManager` periyodik işi `createDeleteRequest()` ile kalıcı siler. Bu sayede dosya kullanıcı klasöründe kalır, scoped storage uyumlu ve geri yükleme `IS_TRASHED=0` ile tek satırdır.

## Uygulama Adımları

Her adım sonunda manuel test edilebilir bir durum hedefleniyor. Her adım kendi commit'i olacak.

### Adım 1 — Bağımlılıklar ve temel iskelet
- `libs.versions.toml`'a Room, Paging 3, Media3, Coil, Navigation Compose, WorkManager, Accompanist Permissions, KSP plugin'i ekle
- `GalleryApp` (Application), Manifest'e ekle
- `MainActivity` içine `NavHost` koy (3 rota: Photos / Albums / Trash)
- 3 sekmeli `NavigationBar` ile ekran iskeletlerini bağla
- **Test:** Uygulama derlenir ve açılır, sekmeler arası geçiş çalışır
- ⚠ **Hilt bu adımda atlandı.** Sebebi ve revize koşulu için aşağıda "Ertelenmiş Kararlar" bölümüne bak. Step 2'ye geçmeden önce DI stratejisini netleştir.

### Adım 2 — İzin akışı ve MediaStore taraması
- `PermissionGate` composable: API 33+ için `READ_MEDIA_IMAGES`+`READ_MEDIA_VIDEO`, API 31-32 için `READ_EXTERNAL_STORAGE`
- İzin verilmediğinde rationale + "Ayarlar'a git" akışı
- `MediaStoreSource`: `MediaStore.Files` üzerinden tek query (image + video birlikte), projection optimize, `DATE_TAKEN` / `DATE_MODIFIED` desc
- `MediaPagingSource` (Paging 3) — sayfa başına ~300 öğe
- **Test:** İzin ekranı + grant sonrası loglarda sayım görünür (UI henüz yok)

### Adım 3 — Foto/video grid (tarih sırasıyla, thumbnail)
- `PhotosScreen`: `LazyVerticalGrid` (3-4 kolon, cihaza göre adaptive), `collectAsLazyPagingItems`
- `MediaThumb` composable: Coil + `MediaStore.loadThumbnail`
- Video öğelerinde sağ-alt köşede süre badge'i (`DurationBadge`)
- **Test:** Cihazdaki tüm medya akıcı şekilde grid'de görünür; 1k+ öğe ile scroll testi

### Adım 4 — Tarih başlıkları (Gün / Ay / Yıl gruplama)
- Pinch-to-zoom yerine v0.1'de basit: üstte 3 düğme (Gün / Ay / Yıl), seçime göre header granülaritesi değişir
- `LazyVerticalGrid` içinde `item(span = maxLineSpan)` ile header satırları
- Gruplama paging veri akışı üzerinde `insertSeparators` ile
- **Test:** Üç moda da geçildiğinde başlıklar doğru ve kayma akıcı

### Adım 5 — Canlı güncelleme (ContentObserver)
- Repository'de `MediaStore.Files.getContentUri("external")` için `ContentObserver` flow'u
- Değişiklik geldiğinde `PagingSource.invalidate()`
- **Test:** Cihazla yeni foto çek → uygulamaya dönünce listede otomatik en üstte

### Adım 6 — Albümler ekranı
- `AlbumRepository`: `BUCKET_ID` + `BUCKET_DISPLAY_NAME` grupla, her bucket için sayım ve kapak thumb (en yeni öğe)
- `AlbumsScreen`: grid (2 kolon, büyük kare), Camera / Screenshots / Downloads vs. otomatik gelir
- Albüme tıklayınca `PhotosScreen`'in filtreli versiyonuna git (`bucketId` query param)
- **Test:** Albümler doğru gruplanır, içine girince sadece o klasörün öğeleri görünür

### Adım 7 — Tam ekran görüntüleyici (foto)
- `ViewerScreen`: `HorizontalPager`, başlangıç indeksi gridden gelir
- Coil ile tam çözünürlük; zoom için v0.1'de basit `Modifier.pointerInput` (pinch + double-tap) — hızlı bir özel impl ya da küçük bir kütüphane (örn. `net.engawapg.lib:zoomable`) — kararı bu adımda netleştireceğim
- Tek dokunuş: sistem barlarını gizle/göster (`WindowInsetsController`)
- **Test:** Gridden bir fotoğrafa girilir, kaydırarak sonraki/önceki, sistem barları toggle

### Adım 8 — Video oynatma
- Viewer'da öğe video ise Media3 `PlayerView` (Compose AndroidView wrapper)
- Play/pause merkez butonu, alt çubukta seekbar
- 10 sn ileri / geri butonları (`player.seekTo(player.currentPosition ± 10_000)`)
- Tek `ExoPlayer` viewer scope'unda; sayfa değişiminde `setMediaItem`
- **Test:** Video açılır, oynat/duraklat ve ±10sn çalışır, çıkışta player release edilir

### Adım 9 — Paylaşım
- Viewer üst bar'da "Share" butonu → `ACTION_SEND` intent, `MediaStore` content URI'si ile (FileProvider gerekmiyor; MediaStore URI'leri zaten paylaşılabilir)
- **Test:** Tek foto/video paylaşımı diğer uygulamalara açılır

### Adım 10 — Çöp kutusu (soft delete + restore + 30 gün otomatik)
- `TrashRepository`:
  - Sil: `MediaStore.createTrashRequest([uri], true)` → `IntentSender` UI'a → kullanıcı onayı → Room'a `(uri, trashedAt = now)` insert
  - Liste: Room'dan oku, her satır için MediaStore'dan thumbnail
  - Geri yükle: `createTrashRequest([uri], false)` + Room'dan sil
- `TrashScreen`: grid + uzun bas → çoklu seçim → "Restore" / "Delete forever"
- `TrashCleanupWorker` (WorkManager periyodik, 24 saat): `trashedAt < now - 30d` olanlar için `createDeleteRequest`
- Viewer'da çöp kutusu ikonu
- **Test:** Sil → Trash sekmesinde görünür → Restore çalışır; sistem ayarlarından manuel saat ileri al ve worker'ı tetikleyerek 30 gün senaryosunu test et

## Bağımlılık Seçimleri (gerekçeli)

| Amaç | Tercih | Neden |
|---|---|---|
| DI | Hilt | AndroidX resmi, KSP ile hızlı, Compose entegrasyonu pürüzsüz |
| Görsel | Coil 3 | Compose-first, Kotlin, küçük, reklam/tracker yok |
| Video | Media3 ExoPlayer | Google'ın aktif video player'ı, ExoPlayer 2 deprecated |
| DB | Room + KSP | Standart, KSP ile hızlı derleme |
| Sayfalama | Paging 3 | Cursor-tabanlı, milyonlarca öğe için tasarlanmış |
| Async iş | WorkManager | Çöp kutusu temizliği için pil dostu, AndroidX |
| İzin UI | Accompanist Permissions | İzin akışı boilerplate'ini azaltır |

## Ertelenmiş Kararlar (Tech Debt)

> Burası "şimdi yapmamayı seçtiğimiz, ama unutursak kod çorba olacak" şeylerin kaydı. Her madde net bir tetikleyici nokta ile yazılmalı.

### Hilt (DI) — Step 2'de tekrar değerlendirilecek
- **Şu anki durum (2026-06-13):** Hilt 2.59.2 sadece Kotlin metadata 2.3.0'a kadar destek veriyor; AGP 9.2.1 + Compose BOM 2026.05 Kotlin 2.4 metadata üretiyor. Hilt'i eklemek için ya AGP/Compose'u downgrade etmek ya da Hilt'in metadata 2.4 desteklemesini beklemek lazım.
- **Step 1'de neden zararsız:** Bu adımda enjekte edilecek hiçbir şey yok (placeholder ekranlar). Hilt = 0 kazanç.
- **Karar tetikleyicisi: Step 2 başlamadan ÖNCE.** İlk ViewModel + Repository yazılmadan önce şu üçünden birini seç ve commit'le:
  1. Hilt ekosistemi düzelmiş → Hilt'i ekle (asıl plan), `@HiltAndroidApp` + `@HiltViewModel` + `@AndroidEntryPoint`'i geri koy.
  2. Hâlâ kırık ve hızlı çözüm istiyorsak → **Koin** (saf Kotlin, codegen yok, metadata sorunu yok, ~30 dk kurulum).
  3. Tek modül + az graph için → **manuel DI** (`GalleryApp` içinde lazy singleton + `ViewModelProvider.Factory`).
- **Kırmızı çizgi:** Step 2 boyunca DI kararı verilmeden ad-hoc singleton/ServiceLocator dağıtmayın. Tek bir strateji seçildikten sonra ViewModel yazımı başlar. Aksi takdirde Step 5-10'a kadar her ekranda farklı bir wiring stili çıkar.

### KSP / Build flags — Step 10'a kadar dokunma
- `gradle.properties`'ta `android.builtInKotlin=false` + `android.newDsl=false`, `libs.versions.toml`'da standalone Kotlin + KSP 2.3.9 var. Sebep: KSP henüz AGP 9'un built-in Kotlin'iyle uyumlu değil ama Step 10'da Room codegen için KSP şart. Bunları temizlemeden önce KSP'nin AGP 9 built-in Kotlin desteğini doğrula.
- Kotlin sürümü 2.4.x'e bağımsız olarak çıkabilir (Step 1 testinde KSP 2.3.9 + Kotlin 2.4.0 Step 1 için sorunsuz). Step 10'da @Entity eklendiğinde Room/KSP'nin Kotlin 2.4 metadata'sını sindirebildiğini tekrar test et; sindiremezse ya KSP 2.4.x'i bekle ya da Kotlin'i 2.3.21'e geri pinle.

## Riskler / Belirsizlikler

- **Scoped storage silme/onay akışı:** `createTrashRequest` / `createDeleteRequest` her seferinde sistem onay dialog'u açar. Toplu işlemler tek dialog'da onaylanabilir (URI listesi), bunu UX'te göz önünde bulunduracağım. v0.1'de tek-öğe akışına odaklanıp toplu seçimi minimal tutacağım.
- **HEIC / RAW formatları:** Cihaz Coil'a düzgün decoder verirse görünür; vermezse placeholder. v0.1'de RAW desteği hedef değil, ama görüntüde patlama olmamalı.
- **1.000.000 öğe gerçek dünya testi:** Paging 3 + MediaStore cursor mimarisinde teorik olarak ölçeklenir, ama bunu sizin cihazınızda doğrulamadan "çözüldü" demeyeceğim. Stres testi için yapay bir senaryo gerekirse not edeceğim.
- **Pinch-to-zoom kütüphanesi:** Viewer'da zoom için kendi impl mi yoksa küçük bir kütüphane (örn. `net.engawapg.lib:zoomable`) mu, Adım 7'de bir-iki cümlelik kararla netleştireceğim — sizin tercihinizi de soracağım.
- **ContentObserver gecikmesi:** Bazı OEM'lerde MediaStore güncellemesi anında gelmiyor. v0.1'de "uygulama foreground'a dönünce yenile" fallback'i eklenecek.
- **API 31-37 farkları:** `minSdk=31` olduğu için Photo Picker (API 34+) zorunlu değil; klasik izin akışı tüm sürümlerde çalışacak. API 34 cihazda "Selected photos only" senaryosu v0.2'ye bırakılabilir — şu an "tüm medya" izni isteyeceğiz.

## Başarı Kriterleri

- [ ] İlk açılışta izin alındıktan sonra **5 saniye içinde** ilk thumbnail'lar grid'de görünür (sistem MediaStore indekslidir; kendi taramamızı yapmayacağız)
- [ ] 10.000+ öğeli bir cihazda LazyVerticalGrid'de gözle görünür frame drop olmadan akıcı scroll
- [ ] Yeni foto çekildiğinde uygulama foreground'da iken liste 2 saniye içinde güncellenir
- [ ] Albümler ekranında en az `DCIM/Camera` ve `Pictures/Screenshots` otomatik görünür
- [ ] Viewer'da kaydırma, sistem barı toggle, video play/pause, ±10sn ve paylaşım çalışır
- [ ] Sil → 30 gün boyunca Trash'te → Restore geri getirir; worker manuel tetiklemede 30 günden eski öğeleri kalıcı siler
- [ ] APK içinde reklam / analytics / crash-reporter SDK'sı **yok** (`./gradlew app:dependencies` çıktısı temiz)
- [ ] Her adım sonunda derleyici uyarısız (`./gradlew assembleDebug`) ve lint clean (`./gradlew lint`)
