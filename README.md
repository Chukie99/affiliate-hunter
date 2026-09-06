# Affiliate Hunter Shopee — v2 OP Android

**Cari yang pasti laku — bukan asal viral**

Aplikasi Android untuk hunter affiliate Shopee — FLAT Modern Minimal **#3368A0** solid no gradient, universal **Android 7-14 (API 24-34)**.

## v2 OP (5 upgrade)
1. **Link affiliate auto** — Copy 1-tap jadi `?aff=ID` (set di Pengaturan)
2. **Velocity Trend** — terjual/hari + badge `🔥 +32% 7d`
3. **Komisi real per kategori** — parfum 8%, skincare 10%, hijab 5% ...
4. **Anti-toko abal** — flag umur toko <30 hari & rating 4.9 tapi ulasan <10 + Hide abal
5. **Scheduler 12 jam** — auto scan parfum/hijab + notif Top baru

## 9 layar FLAT
Splash • Cari • Filter hunter • Hasil ranking OP • Detail chart • Favorit (Room) • Link aff • Scheduler • Export CSV + share

## Stack
Kotlin + Compose 1.6.8 + Material3 1.2.1 + Room + Hilt + WorkManager + DataStore + OkHttp + Shopee API v4 (legal, rate-limit 1 req) + Apache Commons CSV

## Build
- Signed APK via GitHub Actions `v*` tags + QC gate `testDebugUnitTest` + `lintDebug`
- Local: `./gradlew assembleRelease`
