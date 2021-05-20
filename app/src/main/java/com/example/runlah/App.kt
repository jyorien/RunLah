package com.example.runlah

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        val sharedPref = getSharedPreferences(getString(R.string.dark_mode_string), MODE_PRIVATE)
        val isDark = sharedPref.getBoolean(getString(R.string.dark_mode_string), false)
        if (isDark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}