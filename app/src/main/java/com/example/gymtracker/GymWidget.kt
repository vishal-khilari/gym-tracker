package com.example.gymtracker
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider

class GymWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val l = Store.load(context)
        val last = l.lastOrNull()
        val diff = if (l.size > 1) last!!.kg - l[l.size - 2].kg else null
        val goal = Store.goal(context)
        provideContent {
            Column(
                GlanceModifier.fillMaxSize().background(ColorProvider(Color(0xFF1B1B2F))).padding(12.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Body weight", style = TextStyle(color = ColorProvider(Color.LightGray), fontSize = 12.sp))
                Text(last?.let { "%.1f kg".format(it.kg) } ?: "-- kg",
                    style = TextStyle(color = ColorProvider(Color.White), fontSize = 28.sp, fontWeight = FontWeight.Bold))
                if (last != null) Text("${last.slot} · ${last.date}", style = TextStyle(color = ColorProvider(Color.LightGray), fontSize = 11.sp))
                if (diff != null) Text("%+.1f kg vs previous".format(diff),
                    style = TextStyle(color = ColorProvider(if (diff <= 0) Color(0xFF4ADE80) else Color(0xFFF87171)), fontSize = 12.sp))
                if (goal > 0 && last != null) Text("%.1f kg to goal".format(last.kg - goal),
                    style = TextStyle(color = ColorProvider(Color(0xFFFBBF24)), fontSize = 12.sp))
                if (last == null) Text("Tap to log", style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp))
            }
        }
    }
}

class GymWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GymWidget()
}
