package com.example.runlah.dashboard

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.time.LocalDateTime

@Parcelize
data class Record(
    val documentId: String = "",
    val formattedDate: String = "",
    val distance: String = "0",
    val timeTaken: String = "0",
    val speed: String = "0",
    val steps: String = "0",
    val latLngArray: ArrayList<LatLng> = arrayListOf(),
    val uuid: String = "null",
    val rawDate: LocalDateTime,
    val rawDistance: Double,
    val rawSteps: Int
): Parcelable