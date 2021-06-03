package com.example.runlah

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.runlah.util.DailyAlarm
import com.example.runlah.util.DailyReceiver
import java.util.*

class App: Application() {
    val ALARM_ID = 9999

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
            DailyAlarm.setAlarm(applicationContext, ALARM_ID)
            sharedPref2.edit().putBoolean("isNotFirstRun", true).apply()
        }
        Log.i("hello" ,"isNotFirstRun ${sharedPref2.getBoolean("isNotFirstRun", false)}" )
//        cancelAlarm()
//        sharedPref2.edit().putBoolean("isNotFirstRun", false).apply()

    }

    private fun cancelAlarm() {
        Intent(applicationContext, DailyReceiver::class.java).also {
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext, ALARM_ID, it, PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            Log.i("hello", "alarm cancelled")
        }

    }
}