package com.example.runlah

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.runlah.util.Tips
import java.time.LocalDateTime


class App : Application() {


    override fun onCreate() {
        super.onCreate()
        val sharedPref = getSharedPreferences(getString(R.string.dark_mode_string), MODE_PRIVATE)
        val isDark = sharedPref.getBoolean(getString(R.string.dark_mode_string), false)
        if (isDark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setNewTip()
    }

    private fun setNewTip() {
        // temporary solution for daily tips
        val lastDate = LocalDateTime.now()
        val todayDay = lastDate.dayOfMonth
        val todayMonth = lastDate.monthValue
        val todayYear = lastDate.year
        val todaySharedPref = getSharedPreferences(getString(R.string.today_date), MODE_PRIVATE)

        val tipSharedPref = getSharedPreferences(getString(R.string.tip), MODE_PRIVATE)
        if (!todaySharedPref.contains(getString(R.string.today_day))) {
            todaySharedPref.edit().putInt(getString(R.string.today_day),todayDay).apply()
            todaySharedPref.edit().putInt(getString(R.string.today_month),todayMonth).apply()
            todaySharedPref.edit().putInt(getString(R.string.today_year),todayYear).apply()
            tipSharedPref.edit().putString(getString(R.string.tip), Tips.getTip()).apply()
            return
        }
        val storedDay = todaySharedPref.getInt(getString(R.string.today_day), 0)
        val storedMonth = todaySharedPref.getInt(getString(R.string.today_month), 0)
        val storedYear = todaySharedPref.getInt(getString(R.string.today_year), 0)
        val storedTip = tipSharedPref.getString(getString(R.string.tip), "")
        if (storedDay != todayDay || storedMonth != todayMonth || storedYear != todayYear) {
            var newTip = Tips.getTip()
            if (newTip == storedTip)
                newTip = Tips.getTip()
            tipSharedPref.edit().putString(getString(R.string.tip), newTip).apply()
        }
    }

}