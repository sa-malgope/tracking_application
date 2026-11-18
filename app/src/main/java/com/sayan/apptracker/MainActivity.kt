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
import androidx.compose.ui.Alignment
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

    private var resumeTick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppRoot(resumeTick)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumeTick += 1
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(resumeTick: Int) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val titles = listOf("Timeline", "Today", "Setup")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("App Tracker") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> TimelineTab(resumeTick)
                1 -> TodayTab(resumeTick)
                else -> SetupTab(resumeTick)
            }
        }
    }
}

private fun startOfToday(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

@Composable
fun rememberNowTicker(): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            now = System.currentTimeMillis()
        }
    }
    return now
}

@Composable
fun AppIconView(pkg: String) {
    val context = LocalContext.current
    val bitmap = remember(pkg) { AppInfo.icon(context, pkg) }
    Surface(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.size(40.dp)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null
            )
        }
    }
}

private fun dayLabel(timestamp: Long, dateFormat: SimpleDateFormat): String {
    val today = startOfToday()
    val yesterday = today - 24L * 60L * 60L * 1000L
    return when {
        timestamp >= today -> "Today"
        timestamp >= yesterday -> "Yesterday"
        else -> dateFormat.format(Date(timestamp))
    }
}

@Composable
fun TimelineTab(resumeTick: Int) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dao = remember { AppDatabase.get(context).dao() }
    val sessions by dao.recent().collectAsState(initial = emptyList())
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val accessibilityOn = remember(resumeTick) { AppInfo.isAccessibilityOn(context) }
    val now = rememberNowTicker()

    Column(modifier = Modifier.fillMaxSize()) {

        if (!accessibilityOn) {
            Text(
                text = "Accessibility service is off. Enable it in Setup to start logging.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${sessions.size} app visits")
            OutlinedButton(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        dao.clear()
                    }
                }
            ) {
                Text("Clear")
            }
        }

        var lastDay: String? = null

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = sessions, key = { it.id }) { session ->
                val day = dayLabel(session.startTime, dateFormat)
                if (day != lastDay) {
                    lastDay = day
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                    )
                }

                val endTime = session.endTime

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconView(session.packageName)
                    Column(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = AppInfo.label(context, session.packageName),
                            style = MaterialTheme.typography.titleMedium
                        )
                        val range = if (endTime != null) {
                            timeFormat.format(Date(session.startTime)) + " - " + timeFormat.format(Date(endTime))
                        } else {
                            timeFormat.format(Date(session.startTime)) + " - now"
                        }
                        Text(text = range, style = MaterialTheme.typography.bodySmall)
                    }
                    val durationMillis = (endTime ?: now) - session.startTime
                    Text(text = formatDuration(durationMillis), style = MaterialTheme.typography.bodyMedium)
                }
                Divider()
            }
        }
    }
}

private data class AppTotal(
    val pkg: String,
    val totalMillis: Long,
    val visitCount: Int
)

@Composable
fun TodayTab(resumeTick: Int) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).dao() }
    val since = remember { startOfToday() }
    val sessions by dao.since(since).collectAsState(initial = emptyList())
    val now = rememberNowTicker()

    val totals = remember(sessions, now) {
        val grouped = sessions.groupBy { it.packageName }
        val result = ArrayList<AppTotal>()
        for ((pkg, list) in grouped) {
            var sum = 0L
            for (item in list) {
                val end = item.endTime ?: now
                sum += (end - item.startTime)
            }
            result.add(AppTotal(pkg = pkg, totalMillis = sum, visitCount = list.size))
        }
        result.sortedByDescending { it.totalMillis }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Since midnight",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = totals, key = { it.pkg }) { total ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconView(total.pkg)
                    Column(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = AppInfo.label(context, total.pkg),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(text = "${total.visitCount} opens", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(text = formatDuration(total.totalMillis))
                }
                Divider()
            }
        }
    }
}

@Composable
fun SetupTab(resumeTick: Int) {
    val context = LocalContext.current
    val accessibilityOn = remember(resumeTick) { AppInfo.isAccessibilityOn(context) }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val statusText = if (accessibilityOn) "Accessibility service: ON" else "Accessibility service: OFF"
        Text(text = statusText, style = MaterialTheme.typography.titleMedium)

        Text(
            text = "This is the only permission needed. It tells the app which app is in front, " +
                "so visits and durations can be logged. It does not read on-screen text or messages."
        )

        Button(
            onClick = {
                openSettings(context, Settings.ACTION_ACCESSIBILITY_SETTINGS, false)
            }
        ) {
            Text("Open accessibility settings")
        }

        Text(
            text = "Android 13+: if the toggle is greyed out, open App info for App Tracker, " +
                "use the top-right menu to Allow restricted settings, then retry.",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedButton(
            onClick = {
                openSettings(context, Settings.ACTION_APPLICATION_DETAILS_SETTINGS, true)
            }
        ) {
            Text("Open App info")
        }
    }
}

private fun openSettings(context: Context, action: String, withPackage: Boolean) {
    val intent = Intent(action)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (withPackage) {
        intent.data = Uri.parse("package:" + context.packageName)
    }
    context.startActivity(intent)
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
