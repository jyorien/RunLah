package com.example.runlah.dashboard

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class Record(
    val date: String,
    val distance: String = "0",
    val timeTaken: String = "0",
    val speed: String = "0",
    val steps: String = "0",
    val latLngArray: ArrayList<LatLng> = arrayListOf()
): Serializable