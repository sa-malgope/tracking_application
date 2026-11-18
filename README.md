# App Tracker

Personal Android app: logs which app/screen you open and when, plus today's opens and foreground time per app.

- Usage access (UsageStatsManager) -> opens + time per app (Today tab)
- Accessibility service (window changes only, no content read) -> timeline of app + screen (Timeline tab)
- Data stored locally in Room. No network.

## Build APK
Push to GitHub -> Actions tab -> "Build APK" -> download artifact `app-debug-apk`.

Local: `gradle assembleDebug` (Gradle 8.9, JDK 17) or open in Android Studio.

## First run
Setup tab: grant Usage access and enable Accessibility service. On Android 13+ sideloaded apps need "Allow restricted settings" in App info first.
"# tracking_application" 
