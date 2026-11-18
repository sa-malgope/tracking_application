package com.sayan.apptracker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@Composable
fun TimelineTab(tick: Int) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.get(ctx).dao() }
    val sessions by dao.recent().collectAsState(initial = emptyList())
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFmt = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val on = remember(tick) { AppInfo.isAccessibilityOn(ctx) }
    val now = rememberNowTicker()

    Column(Modifier.fillMaxSize()) {
        if (!on) {
            Text(
                "Accessibility service is off. Enable it in Setup to start logging.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${sessions.size} app visits")
            OutlinedButton(onClick = { scope.launch(Dispatchers.IO) { dao.clear() } }) {
                Text("Clear")
            }
        }
        LazyColumn(Modifier.fillMaxSize()) {
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
                }
                Divider()
            }
        }
    }
}

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
            }
        }
    }
}

@Composable
fun SetupTab(tick: Int) {
    val ctx = LocalContext.current
    val acc = remember(tick) { AppInfo.isAccessibilityOn(ctx) }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Accessibility service: ${if (acc) "ON" else "OFF"}", style = MaterialTheme.typography.titleMedium)
        Text(
            "This is the only permission needed. It tells the app which app is in front, " +
                "so visits and durations can be logged. It does not read on-screen text or messages."
        )
        Button(onClick = { open(ctx, Settings.ACTION_ACCESSIBILITY_SETTINGS) }) {
            Text("Open accessibility settings")
        }
        Text(
            "Android 13+: if the toggle is greyed out, open App info for App Tracker, " +
                "use the top-right menu to Allow restricted settings, then retry.",
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedButton(onClick = { open(ctx, Settings.ACTION_APPLICATION_DETAILS_SETTINGS, true) }) {
            Text("Open App info")
        }
    }
}

private fun open(ctx: Context, action: String, withPkg: Boolean = false) {
    val i = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (withPkg) i.data = Uri.parse("package:${ctx.packageName}")
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
