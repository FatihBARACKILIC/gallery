---
version: alpha
name: Gallery
description: İçerik öncelikli, yüksek performanslı Android galeri uygulaması için Material 3 (Material You) tasarım kimliği. Koyu-öncelikli; Açık / Koyu / AMOLED varyantları destekler.
colors:
  primary: "#9FCAFF"
  on-primary: "#00315C"
  primary-container: "#194A7A"
  on-primary-container: "#D2E4FF"
  secondary: "#BBC7DB"
  on-secondary: "#253140"
  secondary-container: "#3B4858"
  on-secondary-container: "#D7E3F8"
  tertiary: "#D7BDE2"
  on-tertiary: "#3B2948"
  background: "#0E0F11"
  on-background: "#E4E2E6"
  surface: "#0E0F11"
  surface-container: "#1A1B1E"
  surface-container-high: "#242528"
  surface-container-highest: "#2E3033"
  on-surface: "#E4E2E6"
  on-surface-variant: "#C5C6D0"
  outline: "#8F9099"
  outline-variant: "#44474E"
  error: "#FFB4AB"
  on-error: "#690005"
  scrim: "#000000"
typography:
  display-small:
    fontFamily: Roboto
    fontSize: 36px
    fontWeight: 400
    lineHeight: 44px
    letterSpacing: 0px
  headline-small:
    fontFamily: Roboto
    fontSize: 24px
    fontWeight: 400
    lineHeight: 32px
    letterSpacing: 0px
  title-large:
    fontFamily: Roboto
    fontSize: 22px
    fontWeight: 400
    lineHeight: 28px
    letterSpacing: 0px
  title-medium:
    fontFamily: Roboto
    fontSize: 16px
    fontWeight: 500
    lineHeight: 24px
    letterSpacing: 0.15px
  body-large:
    fontFamily: Roboto
    fontSize: 16px
    fontWeight: 400
    lineHeight: 24px
    letterSpacing: 0.5px
  body-medium:
    fontFamily: Roboto
    fontSize: 14px
    fontWeight: 400
    lineHeight: 20px
    letterSpacing: 0.25px
  label-large:
    fontFamily: Roboto
    fontSize: 14px
    fontWeight: 500
    lineHeight: 20px
    letterSpacing: 0.1px
  label-medium:
    fontFamily: Roboto
    fontSize: 12px
    fontWeight: 500
    lineHeight: 16px
    letterSpacing: 0.5px
rounded:
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 28px
  full: 999px
spacing:
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 24px
  xxl: 32px
components:
  top-app-bar:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    typography: "{typography.title-large}"
    height: 64px
    padding: 16px
  nav-bar:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.label-medium}"
    height: 80px
  nav-bar-active:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    rounded: "{rounded.full}"
  fab:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"
    rounded: "{rounded.lg}"
    size: 56px
  button-filled:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: 12px
    height: 40px
  button-tonal:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: 12px
    height: 40px
  button-text:
    backgroundColor: "transparent"
    textColor: "{colors.primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: 12px
  icon-button:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface-variant}"
    size: 48px
  card:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.md}"
    padding: 16px
  thumbnail:
    backgroundColor: "{colors.surface-container-high}"
    rounded: "{rounded.xs}"
  chip-filter:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.label-large}"
    rounded: "{rounded.sm}"
    height: 32px
    padding: 8px
  chip-filter-selected:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    rounded: "{rounded.sm}"
  search-bar:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.body-large}"
    rounded: "{rounded.full}"
    height: 56px
    padding: 16px
  list-item:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-large}"
    height: 56px
    padding: 16px
  bottom-sheet:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.xl}"
    padding: 24px
  dialog:
    backgroundColor: "{colors.surface-container-highest}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.xl}"
    padding: 24px
  selection-overlay:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"
---

## Overview

Gallery, 1.000.000 fotoğrafa kadar akıcı çalışacak şekilde tasarlanan, içerik öncelikli bir Android galeri uygulamasıdır. Tasarım dili **Material 3 / Material You**'dur ve tek bir kurala hizmet eder: *arayüz geri çekilir, fotoğraflar öne çıkar.*

Chrome (üst çubuk, alt navigasyon, ikonlar) nötr ve sessizdir; rengi ve dikkati içeriğe bırakır. Renk paleti büyük ölçüde renksizdir — tek bir mavi vurgu yalnızca etkileşim ve seçim durumlarını taşır. Yüksek yoğunluklu grid'ler, hızlı kaydırma ve büyük thumbnail alanları, performans hissini görsel olarak da pekiştirir: ekran her zaman dolu, akıcı ve hızlı görünmelidir.

Kimlik **koyu-öncelikli** kurgulanmıştır çünkü fotoğraflar siyaha yakın zeminlerde daha canlı durur. Üç tema varyantı desteklenir: Açık, Koyu (varsayılan) ve AMOLED (saf siyah).

## Colors

Palet, nötr yüzeyler üzerine kurulu yüksek kontrastlı bir sistemdir. Material 3 renk rolleri kullanılır; vurgu rengi tek ve disiplinlidir.

- **primary (#9FCAFF):** Açık mavi vurgu. FAB ikonları, seçili durumlar, bağlantılar ve birincil eylemler. Fotoğrafların rengiyle yarışmaması için kasıtlı olarak desatüredir.
- **secondary / tertiary:** Seçim çipleri, ikincil vurgular ve hafıza/anı banner'ları gibi destekleyici aksanlar.
- **surface ve surface-container kademeleri (#0E0F11 → #2E3033):** Material 3 tonal elevation. Yükseklik gölgeyle değil, daha açık yüzey tonuyla ifade edilir. Üst çubuk surface'te, kartlar surface-container'da, sheet'ler surface-container-high'da oturur.
- **on-surface / on-surface-variant:** Sırasıyla birincil metin ve ikincil metin/ikon. Metadata, tarih başlıkları ve ikon etiketleri variant tonunu kullanır.
- **outline / outline-variant:** İnce ayraçlar ve giriş alanı kenarlıkları.
- **scrim (#000000):** Tam ekran görüntüleyici ve modal arkası karartma.

**Tema varyantları**

- **Koyu (varsayılan):** Yukarıdaki token değerleri.
- **AMOLED:** `background` ve `surface` saf siyaha (#000000) çekilir; `surface-container` kademeleri bir tık koyulaşır. Pil tasarrufu ve tam ekran fotoğraf izleme için.
- **Açık:** `surface` #FAF8FC, `on-surface` #1A1C1E, `primary` #415E91, `surface-container` #EEEDF1. Aynı rol yapısı, ters parlaklık.

Material You etkinse, sistem dinamik renkleri (wallpaper tabanlı) bu rollerin yerini alabilir; token değerleri fallback olarak korunur.

## Typography

Tek tip aile: **Roboto** (Material 3 varsayılanı). Tip ölçeği M3 ölçeğinin alt kümesidir.

- **display-small / headline-small:** Boş durumlar, onboarding ve büyük başlıklar.
- **title-large:** Üst çubuk başlıkları (ekran adı, albüm adı).
- **title-medium:** Bölüm başlıkları, sheet başlıkları, liste öğesi başlıkları.
- **body-large / body-medium:** Gövde metni, ayar açıklamaları, EXIF değerleri.
- **label-large:** Buton ve çip metinleri.
- **label-medium:** Tarih başlıkları, video süre etiketleri, sayaçlar ("1.204 fotoğraf"), thumbnail rozetleri. Yoğun ve küçük; her zaman variant renginde.

Sayısal yoğunluk (dosya sayıları, boyutlar, çözünürlük) label ölçeğiyle taşınır ve asla içeriği gölgelemez.

## Layout

- **4dp grid.** Tüm boşluk ve hizalama `spacing` token'larının katlarıdır.
- **Edge-to-edge.** İçerik sistem çubuklarının altına uzanır; thumbnail grid'i ekran kenarına dayanır (yatay padding yalnızca liste/ayar ekranlarında).
- **Foto grid'i:** Ayarlanabilir 2–5 sütun, sütunlar arası 2dp gibi minimal aralık (içerik yoğunluğu için). Sticky tarih başlıkları ve sağ kenarda tarih bazlı hızlı kaydırıcı.
- **Üst çubuk:** Küçük (small) top app bar; kaydırınca yüzey tonu hafifçe yükselir.
- **Alt navigasyon:** 3–4 sekme (Fotoğraflar, Albümler, Arama, ve opsiyonel Kişiler). Aktif sekme pill (full radius) secondary-container vurgusu alır.
- **FAB:** Bağlama duyarlı; örn. albüm ekranında "Yeni albüm". Sağ-alt, 16dp kenar boşluğu.
- **Güvenli alanlar:** Tam ekran görüntüleyici dışında içerik insets'e saygı gösterir.

## Elevation & Depth

Material 3 tonal yükseklik kullanılır — gölge minimumda tutulur.

- **Level 0:** surface (arka plan, grid).
- **Level 1:** surface-container (kartlar, alt navigasyon).
- **Level 2:** surface-container-high (sheet'ler, arama çubuğu, basılı çipler).
- **Level 3:** surface-container-highest (dialog'lar, menüler).
- Gölge yalnızca FAB ve sürüklenen öğelerde, düşük yoğunlukta belirir.
- Tam ekran görüntüleyicide içerik scrim üzerine oturur; kontroller scrim gradyanıyla okunur kalır.

## Shapes

Material 3 şekil ölçeği:

- **xs (4px):** Thumbnail köşeleri — neredeyse kare, içeriği maksimize eder.
- **sm (8px):** Çipler, küçük yüzeyler.
- **md (12px):** Kartlar, albüm kapakları.
- **lg (16px):** FAB, büyük kartlar.
- **xl (28px):** Alt sheet üst köşeleri, dialog'lar.
- **full (999px):** Butonlar, arama çubuğu, seçili nav pill'i, biyometrik avatar.

## Components

- **top-app-bar:** Ekran adı + sondaki ikon eylemleri (arama, çoklu seçim, taşma menüsü).
- **nav-bar / nav-bar-active:** Alt navigasyon; aktif sekme pill vurgulu.
- **fab:** Birincil oluşturma eylemi.
- **button-filled / button-tonal / button-text:** Sırasıyla birincil, ikincil ve düşük vurgu eylemleri; tümü full radius.
- **card / thumbnail:** Albüm kapakları kart, medya öğeleri thumbnail (xs köşe).
- **chip-filter / -selected:** Arama ve filtre durumları.
- **search-bar:** Full radius, surface-container-high zemin.
- **list-item:** Ayar ve seçenek satırları.
- **bottom-sheet / dialog:** Modal aksiyonlar (paylaşım, sıralama, EXIF, onaylar).
- **selection-overlay:** Çoklu seçimde thumbnail üzerine primary-container tikli katman.

## Do's and Don'ts

- **Do:** Yüzeyleri nötr tut; rengi fotoğraflara bırak. Vurgu rengini yalnızca etkileşim/seçim için kullan.
- **Do:** Yüksek yoğunluklu grid, minimal aralık, edge-to-edge yerleşim kullan.
- **Do:** Yüksekliği gölgeyle değil, tonal yüzey kademeleriyle ifade et.
- **Do:** Tam ekran görüntüleyicide kontrolleri scrim gradyanıyla okunur kıl, sonra otomatik gizle.
- **Don't:** Birden fazla parlak vurgu rengi kullanma; palet renksiz kalmalı.
- **Don't:** Thumbnail'lara kalın kenarlık veya büyük köşe yuvarlaması ekleme — içerik alanını yer.
- **Don't:** Ağır gölge, gradyan dolgu veya dekoratif efekt kullanma.
- **Don't:** Chrome'u içerikten daha dikkat çekici yapma; üst çubuk ve ikonlar sessiz kalmalı.
