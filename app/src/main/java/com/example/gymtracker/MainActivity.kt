package com.example.gymtracker
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContent { MaterialTheme(colorScheme = darkColorScheme()) { Surface(Modifier.fillMaxSize()) { Screen() } } }
    }
}

@Composable
fun Screen() {
    val ctx: Context = LocalContext.current
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf(Store.load(ctx)) }
    var goal by remember { mutableStateOf(Store.goal(ctx)) }
    var input by remember { mutableStateOf("") }
    var goalInput by remember { mutableStateOf(if (goal > 0) goal.toString() else "") }
    var slot by remember { mutableStateOf(if (LocalTime.now().hour < 12) "Morning" else "Evening") }
    fun refresh() { entries = Store.load(ctx); scope.launch { GymWidget().updateAll(ctx) } }

    LazyColumn(Modifier.statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Gym Tracker", style = MaterialTheme.typography.headlineMedium) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(input, { input = it }, Modifier.weight(1f), label = { Text("Weight (kg)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Button({
                    input.toFloatOrNull()?.let { Store.add(ctx, Entry(LocalDate.now(), slot, it)); input = ""; refresh() }
                }) { Text("Save") }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Morning", "Evening").forEach { FilterChip(slot == it, { _ -> slot = it }, { Text(if (it == "Morning") "☀ Morning" else "🌙 Evening") }) }
            }
        }
        item {
            val last = entries.lastOrNull()
            val avg7 = entries.takeLast(7).map { it.kg }.average()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Stat("Latest", last?.let { "%.1f".format(it.kg) } ?: "--")
                Stat("7-entry avg", if (entries.isEmpty()) "--" else "%.1f".format(avg7))
                Stat("Since start", if (entries.size < 2) "--" else "%+.1f".format(last!!.kg - entries.first().kg))
                Stat("To goal", if (goal > 0 && last != null) "%.1f".format(last.kg - goal) else "--")
            }
        }
        item {
            val pts = entries.takeLast(30).map { it.kg }
            val line = MaterialTheme.colorScheme.primary
            Canvas(Modifier.fillMaxWidth().height(140.dp)) {
                if (pts.size < 2) return@Canvas
                val mn = pts.min() - 0.5f; val mx = pts.max() + 0.5f
                val path = Path()
                pts.forEachIndexed { i, v ->
                    val x = size.width * i / (pts.size - 1); val y = size.height * (1 - (v - mn) / (mx - mn))
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, line, style = Stroke(width = 6f))
                pts.forEachIndexed { i, v -> drawCircle(line, 7f, Offset(size.width * i / (pts.size - 1), size.height * (1 - (v - mn) / (mx - mn)))) }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(goalInput, { goalInput = it }, Modifier.weight(1f), label = { Text("Goal weight (kg)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedButton({ goal = goalInput.toFloatOrNull() ?: 0f; Store.setGoal(ctx, goal); refresh() }) { Text("Set") }
            }
        }
        item { Text("History", style = MaterialTheme.typography.titleMedium) }
        items(entries.reversed()) { e ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${e.date}  ·  ${if (e.slot == "Morning") "☀" else "🌙"} ${e.slot}")
                Text("%.1f kg".format(e.kg), style = MaterialTheme.typography.titleMedium)
                TextButton({ Store.remove(ctx, e); refresh() }) { Text("✕") }
            }
        }
    }
}

@Composable
fun Stat(label: String, value: String) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value, style = MaterialTheme.typography.titleLarge)
    Text(label, style = MaterialTheme.typography.labelSmall)
}
