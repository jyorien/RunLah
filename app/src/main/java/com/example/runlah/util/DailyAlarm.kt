package com.example.runlah.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

object DailyAlarm {
    fun setAlarm(context: Context, alarmId: Int) {
        val scheduledCalendar = Calendar.getInstance()
        scheduledCalendar.set(Calendar.HOUR_OF_DAY, 0)
        scheduledCalendar.set(Calendar.MINUTE, 0)
        scheduledCalendar.set(Calendar.SECOND, 0)
        scheduledCalendar.set(Calendar.MILLISECOND, 0)

        val currentCalendar = Calendar.getInstance()

        if (currentCalendar.after(scheduledCalendar))
            scheduledCalendar.add(Calendar.DATE, 1)

        Intent(context, DailyReceiver::class.java).also {

            val pendingIntent = PendingIntent.getBroadcast(context, alarmId, it, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledCalendar.timeInMillis, pendingIntent)
            Log.i("hello", "alarm set")
            Log.i("hello", currentCalendar.time.toString())
            Log.i("hello", scheduledCalendar.time.toString())
        }
    }
}