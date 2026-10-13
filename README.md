# My Calculator (native Android)

A simple, offline calculator written in Kotlin. No WebView, internet permission, accounts, or ads.

## Build in Android Studio
1. Extract this zip.
2. Open the `MyCalculator` folder in Android Studio.
3. Let Gradle sync, installing Android SDK 35 if prompted.
4. Select **Build > Build APK(s)**.
5. Find `app/build/outputs/apk/debug/app-debug.apk`, copy it to your Android phone, and install it. Android may request permission to install from that source.

## Build with GitHub Actions (no computer required)
1. Create a new GitHub repository.
2. Upload the **contents** of this folder, including the `.github` directory, to the repository root.
3. Open **Actions > Build Android APK > Run workflow**.
4. Open the completed run, download the `MyCalculator-debug-apk` artifact, unzip it, and install `app-debug.apk` on your phone.

This source package is not an APK. It requires building with Android's SDK. The APK generated here is a debug build suitable for personal testing, not a signed Play Store release.
