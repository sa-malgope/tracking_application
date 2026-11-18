package com.sayan.apptracker

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.provider.Settings

object AppInfo {

    private val labelCache = HashMap<String, String>()
    private val iconCache = HashMap<String, Bitmap?>()

    fun label(context: Context, pkg: String): String {
        val cached = labelCache[pkg]
        if (cached != null) {
            return cached
        }
        val resolved = try {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            pkg
        }
        labelCache[pkg] = resolved
        return resolved
    }

    fun icon(context: Context, pkg: String): Bitmap? {
        if (iconCache.containsKey(pkg)) {
            return iconCache[pkg]
        }
        val bitmap = try {
            toBitmap(context.packageManager.getApplicationIcon(pkg))
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        iconCache[pkg] = bitmap
        return bitmap
    }

    private fun toBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun isAccessibilityOn(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val target = ComponentName(context, TrackerService::class.java)
        val parts = enabled.split(':')
        for (part in parts) {
            if (part.equals(target.flattenToString(), ignoreCase = true)) {
                return true
            }
            if (part.equals(target.flattenToShortString(), ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
