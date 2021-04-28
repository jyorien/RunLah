package com.example.runlah.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object DateUtil {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getDateInLocalDateTime(time: Timestamp): LocalDateTime {
        // converts firebase Timestamp to LocalDateTime object
        val formatter =
            DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss O uuuu", Locale.ENGLISH)
        // parse string to LocalDateTime object
        return LocalDateTime.parse(time.toDate().toString(), formatter)
    }
}