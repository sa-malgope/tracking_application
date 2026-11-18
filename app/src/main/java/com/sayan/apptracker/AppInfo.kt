package com.sayan.apptracker

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.provider.Settings

/** Looks up app labels and icons for packages seen in sessions, with a small cache. */
object AppInfo {

    private val labelCache = HashMap<String, String>()
    private val iconCache = HashMap<String, Bitmap?>()

    fun label(ctx: Context, pkg: String): String = labelCache.getOrPut(pkg) {
        try {
            val pm = ctx.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            pkg
        }
    }

    fun icon(ctx: Context, pkg: String): Bitmap? = iconCache.getOrPut(pkg) {
        try {
            toBitmap(ctx.packageManager.getApplicationIcon(pkg))
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun toBitmap(d: Drawable): Bitmap {
        if (d is BitmapDrawable) return d.bitmap
        val w = d.intrinsicWidth.coerceAtLeast(1)
        val h = d.intrinsicHeight.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        d.setBounds(0, 0, canvas.width, canvas.height)
        d.draw(canvas)
        return bmp
    }

    fun isAccessibilityOn(ctx: Context): Boolean {
        val enabled = Settings.Secure.getString(
            ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val cn = ComponentName(ctx, TrackerService::class.java)
        return enabled.split(':').any {
            it.equals(cn.flattenToString(), ignoreCase = true) ||
                it.equals(cn.flattenToShortString(), ignoreCase = true)
        }
    }
}
