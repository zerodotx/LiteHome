# LiteHome — minimal Android launcher

LiteHome is a small native Kotlin launcher designed for OPPO A3x / ColorOS and other compatible Android devices.

## Features
- Minimal dark home screen with clock/date and four favorite app shortcuts
- Swipe up to open the searchable alphabetical app drawer
- Android app icons and labels
- No third-party runtime libraries, ads, analytics, accounts, background services, or Internet permission
- Release minification and resource shrinking enabled

## Build requirements
- Android Studio with Android SDK Platform 35
- JDK 17
- Gradle 8.9 (the included GitHub Actions workflow installs it)

## Build locally
Open this repository folder in Android Studio, let Gradle sync, then choose **Build → Build APK(s)**. The debug APK is normally generated at `app/build/outputs/apk/debug/app-debug.apk`.

Or run:

```bash
gradle --no-daemon assembleDebug
```

## Automated APK build
Every push to `main`, and manual workflow dispatches, run the **Build LiteHome APK** GitHub Actions workflow. When the run succeeds:
1. Open the repository's **Actions** tab.
2. Select the successful **Build LiteHome APK** run.
3. Download the `LiteHome-debug-apk` artifact.
4. Extract it and install `app-debug.apk` on your Android device.

The artifact is retained for 14 days. It is a debug build, not a Play Store release.

## Install
Transfer the APK to your phone and install it. If Android blocks the installation, temporarily allow your file manager to install unknown apps. Press Home and select LiteHome as the default launcher. To switch back, open **Settings → Apps → Default apps → Home app** (menu names vary by device).

## Privacy
The manifest declares no runtime permissions and no Internet permission. The `<queries>` declaration allows Android to discover apps that expose a launcher activity; it is not a runtime permission. No network code, analytics SDK, or ads SDK is included.

## Compatibility and limitations
The project targets Android API 34 and requires Android 8.0 (API 26) or later. ColorOS gesture behavior may vary. APK size, memory usage, and compatibility have not yet been measured on a physical OPPO A3x. The 1–3 MB size goal is aspirational, not a verified result.

## Contributing
Issues and pull requests are welcome. Please describe the device and Android/ColorOS version when reporting bugs.

## License
No license has been selected yet. Until a license is added, normal copyright restrictions apply; public visibility alone does not grant permission to reuse or redistribute the code.
