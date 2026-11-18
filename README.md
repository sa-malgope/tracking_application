# App Tracker

Personal Android app: logs app visits (app name, start time, end time, duration) and
shows today's total time and opens per app. It does not read on-screen text, messages,
or who you are talking to.

- Accessibility service (window-focus only) produces one "session" row per app visit, shown in the Timeline tab.
- Today tab aggregates those sessions since midnight: total time and opens per app.
- Data stored locally in Room. No network. Only permission needed: Accessibility service.

## Build APK
Push to GitHub, open the Actions tab, run "Build APK", then download the `app-debug-apk` artifact from that run.

Local: `gradle assembleDebug` (Gradle 8.9, JDK 17) or open in Android Studio.

## First run
Open the Setup tab and enable the Accessibility service. On Android 13+, sideloaded apps
need "Allow restricted settings" in App info first (top-right menu button).
