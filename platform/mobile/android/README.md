# UPrep — Native Android App

A native Android app for UPrep. It hosts the deployed UPrep web app
(`https://65.2.108.70.sslip.io`) in a full-screen `WebView`, so it stays in sync
with the live site and covers all features (login, self-signup, learn/LMS, and
CMDS) — including file uploads and downloads.

- **Language:** Kotlin
- **Build:** Gradle (Kotlin DSL), Android Gradle Plugin 8.5.2, Gradle 8.7
- **minSdk:** 24 (Android 7) · **targetSdk/compileSdk:** 34
- **Package:** `in.uprep.app`

## What it does
- Loads the live UPrep site in a WebView (JS + DOM storage enabled).
- **Back button** navigates web history, then exits.
- **Pull-to-refresh** reloads the page.
- **File uploads** (`<input type="file">`) work — used by CMDS document/video/image uploads.
- **Downloads** (e.g. exported CSVs) open via the system.
- External links (non-UPrep hosts) open in the phone's browser.

## Build & run (Android Studio — recommended)
1. Open **Android Studio** → **Open** → select this `mobile/android/` folder.
2. Let it **Gradle sync** (it downloads the SDK/build-tools and generates the
   Gradle wrapper automatically).
3. Pick an emulator (or a plugged-in device with USB debugging) and press **Run** ▶.
4. To share an APK: **Build → Build Bundle(s)/APK(s) → Build APK(s)** — the APK
   is written to `app/build/outputs/apk/`.

## Build from the command line (optional)
Requires the Android SDK installed and `ANDROID_HOME`/`local.properties` set.
The Gradle wrapper jar is not committed; generate it once, then build:

```bash
cd mobile/android
gradle wrapper --gradle-version 8.7   # only needed the first time
./gradlew assembleDebug                # APK -> app/build/outputs/apk/debug/
```

## Point the app at a different backend
Change `baseUrl` in
[`app/src/main/java/in/uprep/app/MainActivity.kt`](app/src/main/java/in/uprep/app/MainActivity.kt).
If you use a plain-HTTP host (not HTTPS), also set
`android:usesCleartextTraffic="true"` in `AndroidManifest.xml`.

## Notes / next steps
- This is a WebView shell — a real, installable native app that reuses the live
  web UI. For the best experience the web UI should be mobile-responsive.
- If you later want fully-native screens (native login form, offline content,
  push notifications), those can be added incrementally on top of this project
  against the same `/api/*` endpoints.
- The launcher icon is a simple vector placeholder (`res/drawable/ic_launcher.xml`);
  replace it with a real icon set via Android Studio's **Image Asset** wizard.
