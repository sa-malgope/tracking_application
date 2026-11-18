package com.sayan.apptracker

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import java.time.LocalDate
import java.time.ZoneId

data class AppUsage(val pkg: String, val label: String, val opens: Int, val millis: Long)

object Usage {

    private val labels = HashMap<String, String>()

    fun label(ctx: Context, pkg: String): String = labels.getOrPut(pkg) {
        try {
            val pm = ctx.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            pkg
        }
    }

    @Suppress("DEPRECATION")
    fun hasUsageAccess(ctx: Context): Boolean {
        val ops = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = ops.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), ctx.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isAccessibilityOn(ctx: Context): Boolean {
        val enabled = Settings.Secure.getString(
            ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val cn = ComponentName(ctx, TrackerService::class.java)
        return enabled.split(':').any {
            it.equals(cn.flattenToString(), true) || it.equals(cn.flattenToShortString(), true)
        }
    }

    /** Opens count and foreground time per app since local midnight. */
    @Suppress("DEPRECATION")
    fun today(ctx: Context): List<AppUsage> {
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(start, now)
        val e = UsageEvents.Event()

        val fgSince = HashMap<String, Long>()
        val lastBg = HashMap<String, Long>()
        val total = HashMap<String, Long>()
        val opens = HashMap<String, Int>()

        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            val p = e.packageName ?: continue
            when (e.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    if (fgSince.containsKey(p)) continue
                    val lb = lastBg[p]
                    if (lb != null && e.timeStamp - lb < 2000) {
                        // in-app screen change, not a new open
                        total[p] = (total[p] ?: 0L) + (e.timeStamp - lb)
                    } else {
                        opens[p] = (opens[p] ?: 0) + 1
                    }
                    fgSince[p] = e.timeStamp
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val s = fgSince.remove(p) ?: continue
                    total[p] = (total[p] ?: 0L) + (e.timeStamp - s)
                    lastBg[p] = e.timeStamp
                }
            }
        }
        fgSince.forEach { (p, s) -> total[p] = (total[p] ?: 0L) + (now - s) }

        return opens.keys.union(total.keys)
            .filter { it != ctx.packageName }
            .map { AppUsage(it, label(ctx, it), opens[it] ?: 0, total[it] ?: 0L) }
            .filter { it.opens > 0 || it.millis > 0 }
            .sortedByDescending { it.millis }
    }
}
