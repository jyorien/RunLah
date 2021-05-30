package com.example.runlah

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.runlah.util.DailyReceiver
import java.util.*

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        val sharedPref = getSharedPreferences(getString(R.string.dark_mode_string), MODE_PRIVATE)
        val isDark = sharedPref.getBoolean(getString(R.string.dark_mode_string), false)
        if (isDark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val sharedPref2 = getSharedPreferences("isNotFirstRun", MODE_PRIVATE)
        val isNotFirstRun = sharedPref2.getBoolean("isNotFirstRun", false)
        if (!isNotFirstRun) {
            setAlarm()
            sharedPref2.edit().putBoolean("isNotFirstRun", true).apply()
        }
    }

    private fun setAlarm() {
        val scheduledCalendar = Calendar.getInstance()
        scheduledCalendar.set(Calendar.HOUR_OF_DAY, 0)
        scheduledCalendar.set(Calendar.MINUTE, 0)
        scheduledCalendar.set(Calendar.SECOND, 0)
        scheduledCalendar.set(Calendar.MILLISECOND, 0)

        val currentCalendar = Calendar.getInstance()

        if (currentCalendar.after(scheduledCalendar))
            scheduledCalendar.add(Calendar.DATE, 1)

        Intent(applicationContext, DailyReceiver::class.java).also {
            val ALARM_ID = 9999
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext, ALARM_ID, it, PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, scheduledCalendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
            Log.i("hello", "alarm set")
        }
    }
}