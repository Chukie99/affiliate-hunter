# Affiliate Hunter Shopee — v2.1 Fullstack (Web + API + Android) Android

**Cari yang pasti laku — bukan asal viral**

Aplikasi Android untuk hunter affiliate Shopee — FLAT Modern Minimal **#3368A0** solid no gradient, universal **Android 7-14 (API 24-34)**.

## v2.1 Fullstack (Web + API + Android) (5 upgrade)
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


## v2.1 Fullstack — Web + Backend (baru)

**Orang awam tinggal buka link, tidak usah build APK**

- **Web Next.js** `web/` — FLAT #3368A0 sama, deploy 1-klik Vercel: `web/` → `npm install && npm run build` → deploy. Buka `https://affiliate-hunter.vercel.app` langsung cari.
- **API `GET /api/search?keyword=parfum&limit=20`** — proxy Shopee + retry + cache 10 menit, aman dari blokir HP. Android bisa set Backend URL di Pengaturan → pakai web sebagai proxy.
- **API `POST /api/affiliate {link, affId}`** — kalau env `SHOPEE_AFF_APP_ID/SECRET` diset, generate link komisi real via Shopee Affiliate Open API. Kalau belum daftar, fallback `?aff=ID` (tidak error, cuma note).
- **Android fix v2.0.1**: image CDN, trustFlag real (shop ctime), Export CSV affId real, price Min/Max input, affId sync DataStore, onboarding, load-more 30, Detail image+share, Settings Backend URL, keystore alias fix.

### Deploy Web (Vercel)
1. Push repo ke GitHub
2. Import di vercel.com → root `web` → env kosong (jalan fallback) atau set `SHOPEE_AFF_APP_ID/SECRET` untuk komisi real
3. Android: Pengaturan → Backend URL = `https://xxx.vercel.app`

