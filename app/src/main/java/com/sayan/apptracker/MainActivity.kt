package com.sayan.apptracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@Composable
fun TimelineTab(tick: Int) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.get(ctx).dao() }
    val events by dao.recent().collectAsState(initial = emptyList())
    val fmt = remember { SimpleDateFormat("dd MMM HH:mm:ss", Locale.getDefault()) }
    val on = remember(tick) { Usage.isAccessibilityOn(ctx) }

    Column(Modifier.fillMaxSize()) {
        if (!on) {
            Text(
                "Accessibility service is off. Enable it in Setup tab to log app and screen changes.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${events.size} events (latest 500)")
            OutlinedButton(onClick = { scope.launch(Dispatchers.IO) { dao.clear() } }) {
                Text("Clear")
            }
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(events, key = { it.id }) { ev ->
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(Usage.label(ctx, ev.packageName), style = MaterialTheme.typography.titleMedium)
                    Text(
                        ev.screen?.substringAfterLast('.') ?: "-",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(fmt.format(Date(ev.timestamp)), style = MaterialTheme.typography.bodySmall)
                }
                Divider()
            }
        }
    }
}

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
            }
        }
    }
}

@Composable
fun SetupTab(tick: Int) {
    val ctx = LocalContext.current
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
        Button(onClick = { open(ctx, Settings.ACTION_ACCESSIBILITY_SETTINGS) }) {
            Text("Open accessibility settings")
        }
        Text(
            "Android 13+: if the accessibility toggle is greyed out, open App info for " +
                "App Tracker > top-right menu > Allow restricted settings, then retry.",
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedButton(onClick = { open(ctx, Settings.ACTION_APPLICATION_DETAILS_SETTINGS, true) }) {
            Text("Open App info")
        }
    }
}

private fun open(ctx: Context, action: String, withPkg: Boolean = false) {
    val i = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (withPkg) i.data = android.net.Uri.parse("package:${ctx.packageName}")
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
