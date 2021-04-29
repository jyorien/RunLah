package com.example.runlah.home

import android.content.Context
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.OrientationEventListener
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.runlah.R
import com.example.runlah.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navigateToFragment()
        val orientationEventListener = object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation in 151..199)
                    Log.i("hello","upside down")
            }
        }
        orientationEventListener.enable()
    }

    fun navigateToFragment() {
        val navController = findNavController(R.id.host_fragment)
        val bottomNav = binding.bottomNav
        NavigationUI.setupWithNavController(bottomNav, navController)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        binding.bottomNav.visibility = View.VISIBLE
        findNavController(R.id.host_fragment).navigateUp()
        return super.onOptionsItemSelected(item)
    }

}