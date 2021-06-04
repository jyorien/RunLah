package com.example.runlah.home

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MenuItem
import android.view.OrientationEventListener
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.runlah.R
import com.example.runlah.databinding.ActivityMainBinding
import com.example.runlah.settings.SettingsActivity


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
        val orientationEventListener = object : OrientationEventListener(
            this,
            SensorManager.SENSOR_DELAY_NORMAL
        ) {
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
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.bottom_nav -> binding.bottomNav.visibility = View.GONE
            }
        }
        val bottomNav = binding.bottomNav
        NavigationUI.setupWithNavController(bottomNav, navController)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                Intent(this, SettingsActivity::class.java).also {
                    startActivity(it)
                }
            }

            android.R.id.home -> {
                binding.bottomNav.visibility = View.VISIBLE

                val navController = findNavController(R.id.host_fragment)
                val destinationId = navController.currentDestination?.id
                // different action ids for different fragments with back button
                if (destinationId == R.id.history_fragment) findNavController(R.id.host_fragment).navigate(R.id.action_history_fragment_to_dashboard_fragment)
                else findNavController(R.id.host_fragment).navigate(R.id.action_record_fragment_to_today_fragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_LIGHT) {
            val lightValue = event.values[0]
            if (lightValue < 30) {
                val layout = window.attributes
                layout.screenBrightness = 0f
                window.attributes = layout
            }
            else {
                val layout = window.attributes
                layout.screenBrightness = 0.3f
                window.attributes = layout
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

}