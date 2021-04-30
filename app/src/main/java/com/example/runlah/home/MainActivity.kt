package com.example.runlah.home

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.OrientationEventListener
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.runlah.R
import com.example.runlah.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), SensorEventListener {
    lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var light: Sensor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navigateToFragment()
        val orientationEventListener = object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation in 141..229) {
                    binding.apply {
                        blackOverlay.visibility = View.VISIBLE
                        supportActionBar!!.hide()
                    }
                } else {
                    binding.apply {
                        blackOverlay.visibility = View.GONE
                        supportActionBar!!.show()
//                        Log.i("hello","show bar")
                    }
                }
            }
        }
        orientationEventListener.enable()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun navigateToFragment() {
        val navController = findNavController(R.id.host_fragment)
        val bottomNav = binding.bottomNav
        NavigationUI.setupWithNavController(bottomNav, navController)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        binding.bottomNav.visibility = View.VISIBLE
        findNavController(R.id.host_fragment).navigateUp()
        return super.onOptionsItemSelected(item)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_LIGHT) {
            val lightValue = event.values[0]
            if (lightValue < 30)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

}