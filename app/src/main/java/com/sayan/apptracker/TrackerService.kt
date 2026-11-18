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
    private var lastKey: String? = null

    override fun onAccessibilityEvent(ev: AccessibilityEvent?) {
        if (ev == null || ev.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = ev.packageName?.toString() ?: return
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
}
