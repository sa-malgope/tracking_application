package com.sayan.apptracker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TrackerService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var currentPkg: String? = null

    @Volatile
    private var currentStart: Long? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        scope.launch {
            val open = AppDatabase.get(applicationContext).dao().openSession()
            if (open != null) {
                currentPkg = open.packageName
                currentStart = open.startTime
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) {
            return
        }
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val pkg = event.packageName?.toString() ?: return

        if (pkg == packageName) {
            return
        }
        if (pkg == currentPkg) {
            return
        }

        val now = System.currentTimeMillis()
        val closingPkg = currentPkg
        val closingStart = currentStart
        val idle = isIdle(pkg)

        if (idle) {
            currentPkg = null
            currentStart = null
        } else {
            currentPkg = pkg
            currentStart = now
        }

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

    private fun isIdle(pkg: String): Boolean {
        return pkg.contains("systemui") ||
            pkg.contains("launcher", ignoreCase = true) ||
            pkg.contains("inputmethod", ignoreCase = true)
    }

    override fun onInterrupt() {
        // No-op.
    }

    override fun onDestroy() {
        val pkg = currentPkg
        val start = currentStart
        if (pkg != null && start != null) {
            val dao = AppDatabase.get(applicationContext).dao()
            val end = System.currentTimeMillis()
            CoroutineScope(Dispatchers.IO).launch {
                dao.close(pkg, start, end)
            }
        }
        scope.cancel()
        super.onDestroy()
    }
}
