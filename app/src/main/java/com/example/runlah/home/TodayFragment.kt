package com.example.runlah.home

import android.content.Context
import android.content.Intent
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.example.runlah.R
import com.example.runlah.dashboard.Record
import com.example.runlah.dashboard.TodayData
import com.example.runlah.databinding.FragmentTodayBinding
import com.example.runlah.login.LoginActivity
import com.example.runlah.util.DateUtil
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime

class TodayFragment : Fragment(), SensorEventListener {
    private lateinit var binding: FragmentTodayBinding
    private lateinit var currentDateTime: LocalDateTime
    private val viewModel: SharedViewModel by activityViewModels()
    private val todayDataMap = hashMapOf("today" to TodayData())
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
        getTodayData()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, surroundingTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    fun getTodayData() {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        firestore
            .collection("users")
            .document(auth.currentUser!!.uid)
            .collection("records")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                // group by day, get today's date only
                val todayRef = todayDataMap["today"]!!
                documentSnapshot.forEach {  document ->
                    val time = document["timestamp"] as Timestamp
                    val recordDateTime = DateUtil.getDateInLocalDateTime(time)
                    if (recordDateTime.dayOfMonth == currentDateTime.dayOfMonth) {
                        val distance = (document["distanceTravelled"] as Double).toFloat()
                        val stepCount = document["stepCount"]
                        var steps = 0
                        if (stepCount is Long)
                            steps = (stepCount as Long).toDouble().toInt()
                        else
                            steps = (stepCount as Double).toInt()
                        todayRef.distance+= distance
                        todayRef.steps+= steps
                    }
                }
                displayData()
            }
    }

    private fun displayData() {
        val todayRef = todayDataMap["today"]!!
        val stringDistance = "${String.format("%.2f", todayRef.distance/1000)} km"
        val stringSteps = "${todayRef.steps} steps"
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