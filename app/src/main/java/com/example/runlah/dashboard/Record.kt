package com.example.runlah.dashboard

import java.io.Serializable

data class Record(
    val date: String,
    val distance: String = "0",
    val timeTaken: String = "0",
    val speed: String = "0",
    val steps: String = "0",
    val startLat: String = "",
    val startLng: String = "",
    val endLat: String = "",
    val endLng: String = ""
): Serializable