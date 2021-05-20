package com.example.runlah.dashboard

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import java.time.LocalDateTime

data class Record(
    val formattedDate: String = "",
    val distance: String = "0",
    val timeTaken: String = "0",
    val speed: String = "0",
    val steps: String = "0",
    val latLngArray: ArrayList<LatLng> = arrayListOf(),
    val uuid: String = "null",
    val rawDate: LocalDateTime,
    val rawDistance: Double
): Serializable