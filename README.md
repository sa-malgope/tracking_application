# App Tracker

Personal Android app: logs app visits (app name, start time, end time, duration) and
shows today's total time + opens per app. Nothing else - it does not read on-screen
text, messages, or who you're talking to.

- Accessibility service (window-focus only) -> one "session" row per app visit, in the Timeline tab
- Today tab aggregates those sessions since midnight: total time + opens per app
- Data stored locally in Room. No network. Only permission needed: Accessibility service.

## Build APK
Push to GitHub -> Actions tab -> "Build APK" -> download artifact `app-debug-apk`.

Local: `gradle assembleDebug` (Gradle 8.9, JDK 17) or open in Android Studio.

## First run
Setup tab: enable the Accessibility service. On Android 13+ sideloaded apps need
"Allow restricted settings" in App info first (menu button, top right).
