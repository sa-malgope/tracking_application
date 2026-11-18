package com.sayan.apptracker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

<<<<<<< HEAD
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
=======
class TrackerService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastKey: String? = null
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e

    override fun onAccessibilityEvent(ev: AccessibilityEvent?) {
        if (ev == null || ev.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = ev.packageName?.toString() ?: return
<<<<<<< HEAD
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
=======
        if (pkg == packageName || pkg in IGNORED || pkg.contains("inputmethod")) return
        val cls = ev.className?.toString()
        val key = "$pkg/$cls"
        if (key == lastKey) return
        lastKey = key
        val now = System.currentTimeMillis()
        scope.launch {
            AppDatabase.get(applicationContext).dao()
                .insert(AppEvent(packageName = pkg, screen = cls, timestamp = now))
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private val IGNORED = setOf("com.android.systemui")
    }
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
}
