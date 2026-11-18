package com.sayan.apptracker

import android.content.Context
import android.content.Intent
<<<<<<< HEAD
import android.net.Uri
=======
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
<<<<<<< HEAD
import androidx.compose.foundation.Image
=======
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
<<<<<<< HEAD
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
=======
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
<<<<<<< HEAD
import androidx.compose.material3.Surface
=======
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
<<<<<<< HEAD
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
=======
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var tick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { App(tick) } }
    }

    override fun onResume() {
        super.onResume()
        tick++
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(tick: Int) {
    var tab by remember { mutableIntStateOf(0) }
    val titles = listOf("Timeline", "Today", "Setup")
    Scaffold(topBar = { TopAppBar(title = { Text("App Tracker") }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            TabRow(selectedTabIndex = tab) {
                titles.forEachIndexed { i, t ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) })
                }
            }
            when (tab) {
                0 -> TimelineTab(tick)
                1 -> TodayTab(tick)
                else -> SetupTab(tick)
            }
        }
    }
}

<<<<<<< HEAD
private fun startOfToday(): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

@Composable
fun rememberNowTicker(): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) { delay(5000); now = System.currentTimeMillis() }
    }
    return now
}

@Composable
fun AppIconView(pkg: String) {
    val ctx = LocalContext.current
    val bmp = remember(pkg) { AppInfo.icon(ctx, pkg) }
    Surface(shape = RoundedCornerShape(10.dp), modifier = Modifier.size(40.dp)) {
        if (bmp != null) {
            Image(bitmap = bmp.asImageBitmap(), contentDescription = null)
        }
    }
}

private fun dayLabel(ts: Long, fmt: SimpleDateFormat): String {
    val today = startOfToday()
    val yesterday = today - 24L * 60 * 60 * 1000
    return when {
        ts >= today -> "Today"
        ts >= yesterday -> "Yesterday"
        else -> fmt.format(Date(ts))
    }
}

=======
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
@Composable
fun TimelineTab(tick: Int) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.get(ctx).dao() }
<<<<<<< HEAD
    val sessions by dao.recent().collectAsState(initial = emptyList())
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val on = remember(tick) { AppInfo.isAccessibilityOn(ctx) }
    val now = rememberNowTicker()
=======
    val events by dao.recent().collectAsState(initial = emptyList())
    val fmt = remember { SimpleDateFormat("dd MMM HH:mm:ss", Locale.getDefault()) }
    val on = remember(tick) { Usage.isAccessibilityOn(ctx) }
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e

    Column(Modifier.fillMaxSize()) {
        if (!on) {
            Text(
<<<<<<< HEAD
                "Accessibility service is off. Enable it in Setup to start logging.",
=======
                "Accessibility service is off. Enable it in Setup tab to log app and screen changes.",
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
<<<<<<< HEAD
            Text("${sessions.size} app visits")
=======
            Text("${events.size} events (latest 500)")
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
            OutlinedButton(onClick = { scope.launch(Dispatchers.IO) { dao.clear() } }) {
                Text("Clear")
            }
        }
        LazyColumn(Modifier.fillMaxSize()) {
<<<<<<< HEAD
            var lastDay: String? = null
            items(sessions, key = { it.id }) { s ->
                val day = dayLabel(s.startTime, dateFmt)
                if (day != lastDay) {
                    lastDay = day
                    Text(
                        day,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                    )
                }
                val end = s.endTime
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    AppIconView(s.packageName)
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(AppInfo.label(ctx, s.packageName), style = MaterialTheme.typography.titleMedium)
                        val range = if (end != null) {
                            "${timeFmt.format(Date(s.startTime))} - ${timeFmt.format(Date(end))}"
                        } else {
                            "${timeFmt.format(Date(s.startTime))} - now"
                        }
                        Text(range, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(fmtDuration((end ?: now) - s.startTime), style = MaterialTheme.typography.bodyMedium)
=======
            items(events, key = { it.id }) { ev ->
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(Usage.label(ctx, ev.packageName), style = MaterialTheme.typography.titleMedium)
                    Text(
                        ev.screen?.substringAfterLast('.') ?: "-",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(fmt.format(Date(ev.timestamp)), style = MaterialTheme.typography.bodySmall)
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
                }
                Divider()
            }
        }
    }
}

<<<<<<< HEAD
private data class AppTotal(val pkg: String, val totalMs: Long, val visits: Int)

@Composable
fun TodayTab(tick: Int) {
    val ctx = LocalContext.current
    val dao = remember { AppDatabase.get(ctx).dao() }
    val since = remember { startOfToday() }
    val sessions by dao.since(since).collectAsState(initial = emptyList())
    val now = rememberNowTicker()

    val totals = remember(sessions, now) {
        sessions.groupBy { it.packageName }
            .map { (pkg, list) ->
                AppTotal(
                    pkg = pkg,
                    totalMs = list.sumOf { (it.endTime ?: now) - it.startTime },
                    visits = list.size
                )
            }
            .sortedByDescending { it.totalMs }
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Since midnight",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(Modifier.fillMaxSize()) {
            items(totals, key = { it.pkg }) { t ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    AppIconView(t.pkg)
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(AppInfo.label(ctx, t.pkg), style = MaterialTheme.typography.titleMedium)
                        Text("${t.visits} opens", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(fmtDuration(t.totalMs))
                }
                Divider()
=======
@Composable
fun TodayTab(tick: Int) {
    val ctx = LocalContext.current
    val has = remember(tick) { Usage.hasUsageAccess(ctx) }
    var data by remember { mutableStateOf<List<AppUsage>>(emptyList()) }
    var refresh by remember { mutableIntStateOf(0) }

    LaunchedEffect(tick, refresh, has) {
        data = if (has) withContext(Dispatchers.IO) { Usage.today(ctx) } else emptyList()
    }

    Column(Modifier.fillMaxSize()) {
        if (!has) {
            Text(
                "Usage access not granted. Enable it in Setup tab.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Since midnight")
                OutlinedButton(onClick = { refresh++ }) { Text("Refresh") }
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(data, key = { it.pkg }) { u ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(u.label, style = MaterialTheme.typography.titleMedium)
                            Text("${u.opens} opens", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(fmtDuration(u.millis))
                    }
                    Divider()
                }
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
            }
        }
    }
}

@Composable
fun SetupTab(tick: Int) {
    val ctx = LocalContext.current
<<<<<<< HEAD
    val acc = remember(tick) { AppInfo.isAccessibilityOn(ctx) }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Accessibility service: ${if (acc) "ON" else "OFF"}", style = MaterialTheme.typography.titleMedium)
        Text(
            "This is the only permission needed. It tells the app which app is in front, " +
                "so visits and durations can be logged. It does not read on-screen text or messages."
        )
=======
    val usage = remember(tick) { Usage.hasUsageAccess(ctx) }
    val acc = remember(tick) { Usage.isAccessibilityOn(ctx) }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("1. Usage access: ${if (usage) "ON" else "OFF"}", style = MaterialTheme.typography.titleMedium)
        Text("Gives app open counts and foreground time.")
        Button(onClick = { open(ctx, Settings.ACTION_USAGE_ACCESS_SETTINGS) }) {
            Text("Open usage access settings")
        }
        Text("2. Accessibility service: ${if (acc) "ON" else "OFF"}", style = MaterialTheme.typography.titleMedium)
        Text("Logs which screen is opened inside each app. Screen content is not read.")
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
        Button(onClick = { open(ctx, Settings.ACTION_ACCESSIBILITY_SETTINGS) }) {
            Text("Open accessibility settings")
        }
        Text(
<<<<<<< HEAD
            "Android 13+: if the toggle is greyed out, open App info for App Tracker, " +
                "use the top-right menu to Allow restricted settings, then retry.",
=======
            "Android 13+: if the accessibility toggle is greyed out, open App info for " +
                "App Tracker > top-right menu > Allow restricted settings, then retry.",
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedButton(onClick = { open(ctx, Settings.ACTION_APPLICATION_DETAILS_SETTINGS, true) }) {
            Text("Open App info")
        }
    }
}

private fun open(ctx: Context, action: String, withPkg: Boolean = false) {
    val i = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
<<<<<<< HEAD
    if (withPkg) i.data = Uri.parse("package:${ctx.packageName}")
=======
    if (withPkg) i.data = android.net.Uri.parse("package:${ctx.packageName}")
>>>>>>> 8e46d2f75018afec3bec4fa83d5f7a9bb190b99e
    ctx.startActivity(i)
}

private fun fmtDuration(ms: Long): String {
    val s = ms / 1000
    val h = s / 3600
    val m = (s % 3600) / 60
    return when {
        h > 0 -> "${h}h ${m}m"
        m > 0 -> "${m}m ${s % 60}s"
        else -> "${s}s"
    }
}
