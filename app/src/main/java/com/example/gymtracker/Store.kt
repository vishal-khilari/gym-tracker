package com.example.gymtracker
import android.content.Context
import java.time.LocalDate

data class Entry(val date: LocalDate, val slot: String, val kg: Float)

object Store {
    private fun sp(c: Context) = c.getSharedPreferences("gym", Context.MODE_PRIVATE)
    fun load(c: Context): List<Entry> =
        sp(c).getString("e", "")!!.split(";").filter { it.isNotBlank() }.mapNotNull {
            runCatching { val p = it.split("|"); Entry(LocalDate.parse(p[0]), p[1], p[2].toFloat()) }.getOrNull()
        }.sortedWith(compareBy({ it.date }, { it.slot != "Morning" }))
    private fun save(c: Context, l: List<Entry>) =
        sp(c).edit().putString("e", l.joinToString(";") { "${it.date}|${it.slot}|${it.kg}" }).apply()
    fun add(c: Context, e: Entry) = save(c, load(c).filterNot { it.date == e.date && it.slot == e.slot } + e)
    fun remove(c: Context, e: Entry) = save(c, load(c).filterNot { it == e })
    fun goal(c: Context) = sp(c).getFloat("goal", 0f)
    fun setGoal(c: Context, g: Float) = sp(c).edit().putFloat("goal", g).apply()
}
