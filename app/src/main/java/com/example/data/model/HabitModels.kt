package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoodHabit(
    val id: String,
    val label: String,
    val days: List<Int> = emptyList() // Day of week: 0=Dom, 1=Seg, 2=Ter, 3=Qua, 4=Qui, 5=Sex, 6=Sab
)

@JsonClass(generateAdapter = true)
data class IfThenPlan(
    val se: String = "",
    val entao: String = ""
)

@JsonClass(generateAdapter = true)
data class BadHabit(
    val id: String,
    val label: String,
    val since: String, // YYYY-MM-DD
    val best: Int = 0,
    val plan: IfThenPlan? = null
)

@JsonClass(generateAdapter = true)
data class UrgeLog(
    val habitId: String,
    val outcome: String, // "resisti" or "cedi"
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class TriggerLog(
    val habitId: String,
    val trigger: String, // "Tédio", "Cansaço", etc.
    val kind: String, // "slip" or "urge"
    val timestamp: Long = System.currentTimeMillis()
)
