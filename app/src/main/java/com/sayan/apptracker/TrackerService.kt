package com.sayan.apptracker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Tracks which app is in the foreground and for how long, as one "session" row per
 * app visit (start time -> end time). Does NOT read screen content, text, or messages -
 * only the package name of the app currently in front, which is standard Android
 * window-focus information.
 */
class TrackerService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile private var currentPkg: String? = null
    @Volatile private var currentStart: Long? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        scope.launch {
            AppDatabase.get(applicationContext).dao().openSession()?.let { open ->
                currentPkg = open.packageName
                currentStart = open.startTime
            }
        }
    }

    override fun onAccessibilityEvent(ev: AccessibilityEvent?) {
        if (ev == null || ev.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = ev.packageName?.toString() ?: return
        if (pkg == packageName || pkg == currentPkg) return // self, or same app (different screen)

        val now = System.currentTimeMillis()
        val closingPkg = currentPkg
        val closingStart = currentStart
        val idle = isIdle(pkg)

        // Update in-memory state synchronously (accessibility callbacks are sequential),
        // so a fast app switch right after this one still sees the correct "current" app.
        currentPkg = if (idle) null else pkg
        currentStart = if (idle) null else now

        val dao = AppDatabase.get(applicationContext).dao()
        scope.launch {
            if (closingPkg != null && closingStart != null) {
                dao.close(closingPkg, closingStart, now)
            }
            if (!idle) {
                dao.insert(AppSession(packageName = pkg, startTime = now))
            }
        }
    }

    private fun isIdle(pkg: String) =
        pkg.contains("systemui") || pkg.contains("launcher", ignoreCase = true) ||
            pkg.contains("inputmethod", ignoreCase = true)

    override fun onInterrupt() {}

    override fun onDestroy() {
        val p = currentPkg
        val s = currentStart
        if (p != null && s != null) {
            val dao = AppDatabase.get(applicationContext).dao()
            CoroutineScope(Dispatchers.IO).launch { dao.close(p, s, System.currentTimeMillis()) }
        }
        scope.cancel()
        super.onDestroy()
    }
}
