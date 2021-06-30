package com.example.runlah.home

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.runlah.R
import com.example.runlah.databinding.FragmentTodayBinding
import com.example.runlah.util.Tips
import java.time.LocalDateTime

class TodayFragment : Fragment(), SensorEventListener {
    private lateinit var binding: FragmentTodayBinding
    private lateinit var currentDateTime: LocalDateTime
    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var sensorManager: SensorManager
    private var surroundingTemperatureSensor: Sensor? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        currentDateTime = LocalDateTime.now()
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        surroundingTemperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_today, container, false)
        (activity as MainActivity).supportActionBar!!.title = "Today"
        (activity as MainActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        if (surroundingTemperatureSensor != null) {
            binding.apply {
                ambientTemperature.visibility = View.VISIBLE
                temperatureText.visibility = View.VISIBLE
            }
        }
        binding.sharedViewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.todayMap.observe(viewLifecycleOwner, {
            if (it[stepKey] != null && it[distanceKey] != null)
                displayData(steps = it[stepKey] as Int, distance = it[distanceKey] as Double)

        })
        binding.btnGoRun.setOnClickListener { goToRun() }

        return binding.root
    }

    private fun goToRun() {
        findNavController().navigate(TodayFragmentDirections.actionTodayFragmentToRecordFragment())
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, surroundingTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun displayData(distance: Double, steps: Int) {
        val stringDistance = "${String.format("%.2f", distance/1000)} km"
        val stringSteps = "$steps steps"
        binding.todayDistance.text = stringDistance
        binding.todaySteps.text = stringSteps
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            val temperature = event.values[0].toString()
            binding.ambientTemperature.text = "$temperature \u2103"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

}