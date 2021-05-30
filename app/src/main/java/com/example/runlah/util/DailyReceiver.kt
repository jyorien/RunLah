package com.example.runlah.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log

class DailyReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        var newTip = Tips.getTip()
        val sharedPref = context?.getSharedPreferences("tip",MODE_PRIVATE)
        if (sharedPref?.getString("tip","") == newTip) newTip = Tips.getTip()
        sharedPref?.edit()?.putString("tip", newTip)?.apply()
        Log.i("hello", sharedPref?.getString("tip","?").toString())
    }
}