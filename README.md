# App Tracker

<<<<<<< HEAD
Personal Android app: logs app visits (app name, start time, end time, duration) and
shows today's total time + opens per app. Nothing else - it does not read on-screen
text, messages, or who you're talking to.

- Accessibility service (window-focus only) -> one "session" row per app visit, in the Timeline tab
- Today tab aggregates those sessions since midnight: total time + opens per app
- Data stored locally in Room. No network. Only permission needed: Accessibility service.
=======
Personal Android app: logs which app/screen you open and when, plus today's opens and foreground time per app.

- Usage access (UsageStatsManager) -> opens + time per app (Today tab)
- Accessibility service (window changes only, no content read) -> timeline of app + screen (Timeline tab)
- Data stored locally in Room. No network.
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e

## Build APK
Push to GitHub -> Actions tab -> "Build APK" -> download artifact `app-debug-apk`.

Local: `gradle assembleDebug` (Gradle 8.9, JDK 17) or open in Android Studio.

## First run
<<<<<<< HEAD
Setup tab: enable the Accessibility service. On Android 13+ sideloaded apps need
"Allow restricted settings" in App info first (menu button, top right).
=======
Setup tab: grant Usage access and enable Accessibility service. On Android 13+ sideloaded apps need "Allow restricted settings" in App info first.
"# tracking_application" 
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
